/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.template;

import com.sun.ws.rest.spi.template.TemplateProcessor;
import com.sun.ws.rest.spi.template.TemplateContext;
import com.sun.ws.rest.api.view.Viewable;
import com.sun.ws.rest.api.core.ResourceConfig;
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
    
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations) {
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
            String absolutePath = getAbsolutePath(ui.getAncestorResources().get(0).getClass(), 
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

    public long getSize(Viewable t) {
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