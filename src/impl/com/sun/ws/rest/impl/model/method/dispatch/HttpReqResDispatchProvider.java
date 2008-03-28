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

package com.sun.ws.rest.impl.model.method.dispatch;

import com.sun.ws.rest.api.core.HttpContext;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpReqResDispatchProvider implements ResourceMethodDispatchProvider {
    
    static final class HttpReqResDispatcher extends ResourceJavaMethodDispatcher {
        HttpReqResDispatcher(AbstractResourceMethod abstractResourceMethod) {
            super(abstractResourceMethod);
        }

        public void _dispatch(Object resource, HttpContext context) 
        throws IllegalAccessException, InvocationTargetException {
            method.invoke(resource, context.getRequest(), context.getResponse());
            MediaType m = getAcceptableMediaType(context.getRequest());
            if (m != null) 
                context.getResponse().
                        getHttpHeaders().putSingle("Content-Type", m);
        }
    }

    
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        // TODO: add return type to ARM
        if (abstractResourceMethod.getMethod().getReturnType() != void.class) return null;
        
        // TODO: use ARM getParams instead
        Class<?>[] parameters = abstractResourceMethod.getMethod().getParameterTypes();
        if (parameters.length != 2) return null;
        if (parameters[0] != HttpRequestContext.class) return null;
        if (parameters[1] != HttpResponseContext.class) return null;
                
        return new HttpReqResDispatcher(abstractResourceMethod);
    }
}