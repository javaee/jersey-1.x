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

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.spi.dispatch.RequestDispatcher;

/**
 * Service-provider interface for creating {@link RequestDispatcher} instances.
 * <p>
 * An implementation (a service-provider) identifies itself by placing a 
 * provider-configuration file (if not already present), 
 * "com.sun.research.ws.rest.spi.invoker.ResourceMethodDispatchProvider" in the 
 * resource directory <tt>META-INF/services</tt>, and including the fully qualified
 * service-provider-class of the implementation in the file.
 * <p>
 * A provider will examine the model of the Web resource method and 
 * determine if an invoker can be created for that Web resource method.
 * <p>
 * Multiple providers can specify the support for different Web resource method
 * patterns, ranging from simple patterns (such as void return and intput 
 * parameters) to complex patterns that take type URI and query arguments 
 * and HTTP reuqest headers as typed parameters.
 * 
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface ResourceMethodDispatchProvider {
    
    /**
     * Create a {@link RequestDispatcher} according the {@link ResourceMethod} 
     * of a Web resource.
     * <p>
     * 
     * 
     * @param method the model of a method of a Web resource.
     * @return the dispatcher, otherwise null if it could not be created.
     */
    RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) throws ContainerException;    
}
