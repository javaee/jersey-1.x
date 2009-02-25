/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.lang.Class;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class TemplateFactory implements TemplateContext {
    
    private final Set<TemplateProcessor> templates;
    
    public TemplateFactory(ProviderServices providerServices) {
        templates = providerServices.getProvidersAndServices(
                TemplateProcessor.class);
    }

    // TemplateContext
    
    public Set<TemplateProcessor> getTemplateProcessors() {
        return templates;
    }

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
        for (TemplateProcessor t : getTemplateProcessors()) {
            String resolvedPath = t.resolve(v.getTemplateName());
            if (resolvedPath != null) {
                return new ResolvedViewable(t, resolvedPath, v.getModel());
            }
        }

        return null;
    }

    private ResolvedViewable resolveRelativeViewable(Viewable v, Class<?> resolvingClass) {
        String path = v.getTemplateName();
        if (path == null || path.length() == 0)
            path = "index";

        for (Class c = resolvingClass; c != Object.class; c = c.getSuperclass()) {
            String absolutePath = getAbsolutePath(c, path);

            for (TemplateProcessor t : getTemplateProcessors()) {
                String resolvedPath = t.resolve(absolutePath);
                if (resolvedPath != null) {
                    return new ResolvedViewable(t, resolvedPath, v.getModel(), c);
                }
            }
        }

        return null;
    }

    private String getAbsolutePath(Class<?> resourceClass, String path) {
        return getAbsolutePath(resourceClass) + '/' + path;
    }

    private String getAbsolutePath(Class<?> resourceClass) {
        return "/" + resourceClass.getName().replace('.', '/').replace('$', '/');
    }

}