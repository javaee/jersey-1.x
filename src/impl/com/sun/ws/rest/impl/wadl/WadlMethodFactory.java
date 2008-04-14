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

package com.sun.ws.rest.impl.wadl;

import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Resource;
import com.sun.ws.rest.api.MediaTypes;
import com.sun.ws.rest.api.core.HttpContext;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.uri.UriTemplate;
import com.sun.ws.rest.impl.model.MediaTypeHelper;
import com.sun.ws.rest.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
/* package */ final class WadlMethodFactory {

    public static final class WadlOptionsMethod extends ResourceMethod {
        public WadlOptionsMethod(Map<String, List<ResourceMethod>> methods, 
                AbstractResource resource, String path) {
            super("OPTIONS",
                    UriTemplate.EMPTY,
                    MediaTypeHelper.GENERAL_MEDIA_TYPE_LIST, 
                    MediaTypeHelper.GENERAL_MEDIA_TYPE_LIST,
                    new WadlOptionsMethodDispatcher(methods, resource, path));        
        }

        @Override
        public String toString() {
            return "WADL OPTIONS method" ;
        }
    }
    
    private static final class WadlOptionsMethodDispatcher extends 
            ResourceHttpOptionsMethod.OptionsRequestDispatcher {
        private final AbstractResource resource;
        private final String path;
        
        WadlOptionsMethodDispatcher(Map<String, List<ResourceMethod>> methods,
                AbstractResource resource, String path) {
            super(methods);
            this.resource = resource;
            this.path = path;
        }
            
        @Override
        public void dispatch(final Object o, final HttpContext context) {            
            final Application a = genatateApplication(context.getUriInfo(), 
                    resource, path);
            
            context.getResponse().setResponse(
                    Response.ok(a, MediaTypes.WADL).header("Allow", allow).build());
        }
        
        private Application generate() {
            return path == null ? WadlGenerator.generate(resource) : 
                WadlGenerator.generate(resource, path);
        }
    }
    
    private static Application genatateApplication(UriInfo info, 
            AbstractResource resource, String path) {   
        Application a = path == null ? WadlGenerator.generate(resource) : 
                WadlGenerator.generate(resource, path);
        
        a.getResources().setBase(info.getBaseUri().toString());
                
        final Resource r = a.getResources().getResource().get(0);
        r.setPath(info.getBaseUri().relativize(
                info.getAbsolutePath()).toString());
        
        // remove path params since path is fixed at this point
        r.getParam().clear();
        
        return a;
    }
}
