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

package com.sun.jersey.impl.model.method;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.impl.application.ResourceMethodDispatcherFactory;
import java.lang.reflect.Method;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceHttpMethod extends ResourceMethod {
    private final Method m;

    public ResourceHttpMethod(ResourceMethodDispatcherFactory df, 
            AbstractResourceMethod method) {
        this(df, UriTemplate.EMPTY, method);
    }
    
    public ResourceHttpMethod(
            ResourceMethodDispatcherFactory df,
            UriTemplate template,
            AbstractResourceMethod method) {
        super(method.getHttpMethod(),
                template,
                method.getSupportedInputTypes(), 
                method.getSupportedOutputTypes(),
                df.getDispatcher(method));

        this.m = method.getMethod();
        
        if (getDispatcher() == null) {
            String msg = ImplMessages.NOT_VALID_HTTPMETHOD(m,
                    method.getHttpMethod(), m.getDeclaringClass());
            throw new ContainerException(msg);
        }
    }
    
    @Override
    public String toString() {
        return ImplMessages.RESOURCE_METHOD(m.getDeclaringClass(), m.getName());
    }
}