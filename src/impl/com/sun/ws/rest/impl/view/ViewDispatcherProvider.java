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

package com.sun.ws.rest.impl.view;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.view.Views;
import com.sun.ws.rest.impl.dispatch.URITemplateDispatcher;
import com.sun.ws.rest.impl.response.Responses;
import com.sun.ws.rest.spi.dispatch.DispatchContext;
import com.sun.ws.rest.spi.dispatch.Dispatcher;
import com.sun.ws.rest.impl.dispatch.DispatcherProvider;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ViewDispatcherProvider implements DispatcherProvider {
    
    private final class ViewDispatcher extends URITemplateDispatcher {
        private String view;
        
        ViewDispatcher(URITemplateType t, String view) {
            super(t);
            
            this.view = view;
        }
        
        public boolean dispatch(DispatchContext context, Object node, String path) {
            final HttpRequestContext request = context.getHttpRequestContext();
            final HttpResponseContext response = context.getHttpResponseContext();
            final String httpMethod = request.getHttpMethod();
            
            if (!httpMethod.equals("GET")) {
                response.setResponse(Responses.METHOD_NOT_ALLOWED);
                return true;
            }
            
            Response r = context.getHttpContext().createLocalForward(view);
            response.setResponse(r);
            return true;
        }
    }
    
    private URITemplateDispatcher createViewDispatcher(String path, Class<?> resource) {
        if (path.startsWith("/")) {
            // TODO get the name of the leaf node of the path
            // and use that for the URI template
            return null;
        } if (path.matches("index\\.[^/]*")) {
            String absolutePath = "/" + resource.getName().replace('.','/').replace('$','/') + '/' + path;
            return new ViewDispatcher(URITemplateType.NULL, absolutePath);
        } else {
            if (!path.startsWith("/"))
                path = "/" + path;            
            String absolutePath = "/" + resource.getName().replace('.','/').replace('$','/') + path;
            int i = path.lastIndexOf('.');
            if (i > 0)
                path = path.substring(0, i);
            
            return new ViewDispatcher(new URITemplateType(path), absolutePath);            
        }
    }
    
    public URITemplateDispatcher[] createDispatchers(Class<?> resource, ResourceConfig config) throws ContainerException {
        if (resource == null)
            return null;
        
        Map<String, Class<?>> viewMap = new HashMap<String, Class<?>>();
        Set<String> views = new HashSet<String>();
        for (;resource != null; resource = resource.getSuperclass()) {
            Views vAnnotation = resource.getAnnotation(Views.class);
            if (vAnnotation == null)
                continue;
            
            for (String name : vAnnotation.value()) {
                if (views.contains(name))
                    continue;

                if (!name.startsWith("/") && name.contains("/"))
                    continue;

                views.add(name);
                viewMap.put(name, resource);
            }
        }
                
        if (viewMap.isEmpty())
            return null;
        
        List<URITemplateDispatcher> ds = new ArrayList<URITemplateDispatcher>();
        for (Map.Entry<String, Class<?>> view : viewMap.entrySet()) {
            URITemplateDispatcher d = createViewDispatcher(view.getKey(), view.getValue());
            if (d != null)
                ds.add(d);
        }

        return ds.toArray(new URITemplateDispatcher[0]);
    }
    
}
