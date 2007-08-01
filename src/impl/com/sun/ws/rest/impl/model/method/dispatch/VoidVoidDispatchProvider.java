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

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import com.sun.ws.rest.impl.model.method.ResourceMethodData;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class VoidVoidDispatchProvider implements ResourceMethodDispatchProvider {
    
    public static final class VoidVoidMethodInvoker extends ResourceJavaMethodDispatcher {
        public VoidVoidMethodInvoker(ResourceMethodData method) {
            super(method);
        }

        public void _dispatch(Object resource, HttpRequestContext request, HttpResponseContext response) 
        throws IllegalAccessException, InvocationTargetException {
            method.invoke(resource);
        }
    }
    

    public RequestDispatcher create(ResourceMethodData method) {
        if (method.method.getParameterTypes().length != 0) return null;
        if (method.method.getReturnType() != void.class) return null;
        
        return new VoidVoidMethodInvoker(method);
    }
}
