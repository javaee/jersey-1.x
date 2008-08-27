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

package com.sun.jersey.impl.template;

import com.sun.jersey.spi.template.TemplateProcessor;
import com.sun.jersey.spi.template.TemplateContext;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.api.core.ResourceConfig;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ViewableMessageBodyWriter implements MessageBodyWriter<Viewable> {
    
    @Context UriInfo ui;
    
    @Context TemplateContext tc;
    
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Viewable.class.isAssignableFrom(type);
    }

    public void writeTo(Viewable v, 
            Class<?> type, Type genericType, Annotation[] annotations, 
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, 
            OutputStream entityStream) throws IOException {
        if (v instanceof ResolvedViewable) {
            ResolvedViewable rv = (ResolvedViewable)v;
            rv.getTemplate().writeTo(v.getTemplateName(), v.getModel(), entityStream);
        } else {
            String absolutePath = getAbsolutePath(ui.getMatchedResources().get(0).getClass(), 
                    v.getTemplateName());

            boolean resolved = false;
            for (TemplateProcessor t : tc.getTemplateProcessors()) {
                String resolvedPath = t.resolve(absolutePath);
                if (resolvedPath != null) {
                    resolved = true;
                    t.writeTo(resolvedPath, v.getModel(), entityStream);
                }
            }
            
            if (resolved == false) {
                throw new IOException("The template name, " + 
                        v.getTemplateName() + 
                        ", could not be resolved to the path of a template");
            }
        }
    }

    public long getSize(Viewable t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }
    
    
    private String getAbsolutePath(Class<?> resourceClass, String path) {
        if (path == null || path.length() == 0) {
            path = "index";
        } else if (path.startsWith("/")) {
            return path;
        }

        return getAbsolutePath(resourceClass) + '/' + path;
    }

    private String getAbsolutePath(Class<?> resourceClass) {
        return "/" + resourceClass.getName().replace('.', '/').replace('$', '/');
    }

}