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

package com.sun.ws.rest.impl.model.method;

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.model.MimeHelper;
import com.sun.ws.rest.impl.model.ResourceClass;
import java.lang.reflect.Method;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceHttpOptionsMethod extends ResourceMethod {
    private final RequestDispatcher dispatcher;
    
    private static class OptionsRequestDispatcher implements RequestDispatcher {
        private final String allow;
        
        OptionsRequestDispatcher(String allow) {
            this.allow = allow;
        }
        
        public void dispatch(Object resource, 
                HttpRequestContext requestContext, 
                HttpResponseContext responseContext) {
            Response r = new ResponseBuilderImpl().ok().
                    header("Allow", allow).build();
            responseContext.setResponse(r, null);
        }
    }
    
    public ResourceHttpOptionsMethod(ResourceClass resourceClass, String allow) throws ContainerException {
        super(resourceClass);
        
        this.httpMethod = "OPTIONS";
        this.consumeMime = MimeHelper.GENERAL_MEDIA_TYPE_LIST;
        this.produceMime = MimeHelper.GENERAL_MEDIA_TYPE_LIST;
        this.dispatcher = new OptionsRequestDispatcher(allow);
    }
    
    public Method getMethod() {
        return null;
    }
    
    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }    
}