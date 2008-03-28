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
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class VoidVoidDispatchProvider implements ResourceMethodDispatchProvider {
    
    public static final class VoidVoidMethodInvoker extends ResourceJavaMethodDispatcher {
        public VoidVoidMethodInvoker(AbstractResourceMethod abstractResourceMethod) {
            super(abstractResourceMethod);
        }

        public void _dispatch(Object resource, HttpContext context) 
        throws IllegalAccessException, InvocationTargetException {
            method.invoke(resource);
        }
    }
    

    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        // TODO: use ARM.getParams instead
        if (abstractResourceMethod.getMethod().getParameterTypes().length != 0) return null;
        // TODO: use ARM.retType? instead
        if (abstractResourceMethod.getMethod().getReturnType() != void.class) return null;
        
        return new VoidVoidMethodInvoker(abstractResourceMethod);
    }
}
