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

import static javax.ws.rs.HttpMethod.*;
import javax.ws.rs.HttpMethod;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.model.method.dispatch.ResourceMethodDispatcherFactory;
import java.lang.reflect.Method;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceHttpMethod extends ResourceJavaMethod {
    /**
     * Common HTTP methods. Other HTTP methods are also supported, e.g. those
     * specified by WebDAV but are not listed here for reasons of conciseness.
     */
    public static final String COMMON_METHODS[] = {GET, POST, PUT, DELETE, HEAD};
    
    public ResourceHttpMethod(ResourceClass resourceClass, Method method) throws ContainerException {
        super(resourceClass, method);
        this.httpMethod = getHttpMethod(method);
        
        ResourceMethodData rmd = new ResourceMethodData(method, httpMethod, consumeMime, produceMime);
        this.dispatcher = ResourceMethodDispatcherFactory.create(rmd);
        if (dispatcher == null) {
            String msg = ImplMessages.NOT_VALID_HTTPMETHOD(method, 
                                                                httpMethod, 
                                                                resourceClass);
            throw new ContainerException(msg);
        }
    }
    
    private String getHttpMethod(Method method) throws ContainerException {
        HttpMethod httpMethod = method.getAnnotation(HttpMethod.class);
        if (httpMethod == null) {
            throw new ContainerException("Java method is not annotated with HttpMethod");
        }

        if (httpMethod.value().length() > 0)
            return httpMethod.value();
        
        String methodName = method.getName().toUpperCase();
        for (String m : COMMON_METHODS) {
            if (methodName.startsWith(m)) {
                return m;
            }
        }
        
        throw new ContainerException("The HTTP method cannot be determined from the name of the Java method " +
                method + " of the class " + method.getDeclaringClass());
    }    
}