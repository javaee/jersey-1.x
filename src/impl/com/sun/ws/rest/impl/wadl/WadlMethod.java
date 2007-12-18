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
import com.sun.ws.rest.impl.model.method.*;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.impl.model.MediaTypeHelper;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import java.util.Arrays;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class WadlMethod extends ResourceMethod {
    
    private static final class WadlMethodDispatcher implements RequestDispatcher {
        private final Application a;
        
        WadlMethodDispatcher(AbstractResource resource) {
            this.a = WadlGenerator.generate(resource);
        }
        
        public void dispatch(final Object resource, 
                final HttpRequestContext requestContext, 
                final HttpResponseContext responseContext) {
            // TODO is it correct to use the base URI here?
            // should it be the absolute URI
            if (a.getResources().getBase()==null) {
                a.getResources().setBase(requestContext.getBaseUri().toString());
                
                Resource r = a.getResources().getResource().get(0);
                String p = requestContext.getBaseUri().relativize(
                        requestContext.getAbsolutePath()).toString();
                r.setPath(p);
            }
            
            responseContext.setResponse(
                    Response.ok(a, MediaTypes.WADL).build());
        }
    }
    
    public WadlMethod(AbstractResource resource) {
        super("GET",
                null,
                MediaTypeHelper.GENERAL_MEDIA_TYPE_LIST,
                Arrays.asList(MediaTypes.WADL),
                new WadlMethodDispatcher(resource));
    }
    
    @Override
    public String toString() {
        return "WADL method" ;
    }
}