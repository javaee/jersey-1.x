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
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.model.method.dispatch.ResourceMethodDispatcherFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceHttpMethod extends ResourceJavaMethod {
    
    public ResourceHttpMethod(ResourceClass resourceClass, AbstractResourceMethod abstractResourceMethod) throws ContainerException {
        super(resourceClass, abstractResourceMethod);
        this.httpMethod = abstractResourceMethod.getHttpMethod();
        
        this.dispatcher = ResourceMethodDispatcherFactory.create(abstractResourceMethod);
        if (dispatcher == null) {
            String msg = ImplMessages.NOT_VALID_HTTPMETHOD(abstractResourceMethod.getMethod(), 
                                                                httpMethod, 
                                                                resourceClass);
            throw new ContainerException(msg);
        }
    }
    
    
}