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

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.WebResource;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.model.method.dispatch.ResourceJavaMethodDispatcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class WebResourceInterfaceMethod extends ResourceJavaMethod {
        
    private final static class WebResourceInterfaceRequestDispatcher extends ResourceJavaMethodDispatcher {
        WebResourceInterfaceRequestDispatcher(ResourceMethodData method) {
            super(method);
        }

        public void _dispatch(Object resource, 
                HttpRequestContext requestContext, HttpResponseContext responseContext) 
                throws IllegalAccessException, InvocationTargetException {
            ((WebResource)resource).handleRequest(requestContext, responseContext);
            MediaType m = getAcceptableMediaType(requestContext);
            if (m != null) responseContext.getHttpHeaders().putSingle("Content-Type", m);            
        }        
    }
    
    public WebResourceInterfaceMethod(ResourceClass metaClass, Method method)
            throws ContainerException {
        super(metaClass, method);
        
        ResourceMethodData rmd = new ResourceMethodData(method, httpMethod, consumeMime, produceMime);
        
        this.dispatcher = new WebResourceInterfaceRequestDispatcher(rmd);
    }    
}