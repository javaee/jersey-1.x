/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.server.impl.template;

import com.sun.jersey.spi.template.ResolvedViewable;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.spi.template.TemplateProcessor;
import com.sun.jersey.spi.template.TemplateContext;
import com.sun.jersey.spi.template.TemplateContextException;
import com.sun.jersey.spi.template.ViewProcessor;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class TemplateFactory implements TemplateContext {
    
    private final Set<ViewProcessor> viewProcessors;

    public TemplateFactory(ProviderServices providerServices) {
        viewProcessors = providerServices.getProvidersAndServices(
                ViewProcessor.class);

        Set<TemplateProcessor> templateProcessors = providerServices.getProvidersAndServices(
                TemplateProcessor.class);

        for (TemplateProcessor tp : templateProcessors) {
            viewProcessors.add(new TemplateViewProcessor(tp));
        }
    }

    /**
     * Get the set of template processors.
     *
     * @return the set of template processors.
     */
    private Set<ViewProcessor> getViewProcessors() {
        return viewProcessors;
    }

    // TemplateContext
    
    public ResolvedViewable resolveViewable(Viewable v) {
        if (v.isTemplateNameAbsolute()) {
            return resolveAbsoluteViewable(v);
        } else if (v.getResolvingClass() != null) {
            return resolveRelativeViewable(v, v.getResolvingClass());
        } else {
            if (v.getModel() == null)
                throw new TemplateContextException("The model of the view MUST not be null");

            return resolveRelativeViewable(v, v.getModel().getClass());
        }
    }

    public ResolvedViewable resolveViewable(Viewable v, UriInfo ui) {
        if (v.isTemplateNameAbsolute()) {
            return resolveAbsoluteViewable(v);
        } else if (v.getResolvingClass() != null) {
            return resolveRelativeViewable(v, v.getResolvingClass());
        } else {
            final List<Object> mrs = ui.getMatchedResources();
            if (mrs == null || mrs.size() == 0)
                throw new TemplateContextException("There is no last matching resource available");

            return resolveRelativeViewable(v, mrs.get(0).getClass());
        }
    }

    public ResolvedViewable resolveViewable(Viewable v, Class<?> resolvingClass) {
        if (v.isTemplateNameAbsolute()) {
            return resolveAbsoluteViewable(v);
        } else if (v.getResolvingClass() != null) {
            return resolveRelativeViewable(v, v.getResolvingClass());
        } else {
            if (resolvingClass == null)
                throw new TemplateContextException("Resolving class MUST not be null");
            
            return resolveRelativeViewable(v, resolvingClass);
        }
    }

    
    private ResolvedViewable resolveAbsoluteViewable(Viewable v) {
        for (ViewProcessor vp : getViewProcessors()) {
            Object resolvedTemplateObject = vp.resolve(v.getTemplateName());
            if (resolvedTemplateObject != null) {
                return new ResolvedViewable(vp, resolvedTemplateObject, v);
            }
        }

        return null;
    }

    private ResolvedViewable resolveRelativeViewable(Viewable v, Class<?> resolvingClass) {
        String path = v.getTemplateName();
        if (path == null || path.length() == 0)
            path = "index";

        // Find in directories
        for (Class c = resolvingClass; c != Object.class; c = c.getSuperclass()) {
            String absolutePath = getAbsolutePath(c, path, '/');

            for (ViewProcessor vp : getViewProcessors()) {
                Object resolvedTemplateObject = vp.resolve(absolutePath);
                if (resolvedTemplateObject != null) {
                    return new ResolvedViewable(vp, resolvedTemplateObject, v, c);
                }
            }
        }

        // Find in flat files
        for (Class c = resolvingClass; c != Object.class; c = c.getSuperclass()) {
            String absolutePath = getAbsolutePath(c, path, '.');

            for (ViewProcessor vp : getViewProcessors()) {
                Object resolvedTemplateObject = vp.resolve(absolutePath);
                if (resolvedTemplateObject != null) {
                    return new ResolvedViewable(vp, resolvedTemplateObject, v, c);
                }
            }
        }

        return null;
    }

    private String getAbsolutePath(Class<?> resourceClass, String path, char delim) {
        return '/' + resourceClass.getName().replace('.', '/').replace('$', delim) + delim + path;
    }
}