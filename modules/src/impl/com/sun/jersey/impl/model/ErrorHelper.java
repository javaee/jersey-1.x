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

package com.sun.jersey.impl.model;

import com.sun.jersey.impl.ImplMessages;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.HttpMethod;
import com.sun.jersey.api.container.ContainerException;
import java.lang.reflect.Method;

/**
 * Error helper class for reporting errors related to processing a Web resource.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ErrorHelper {
    
    public static ContainerException objectNotAWebResource(Class resourceClass) {
        return new ContainerException(ImplMessages.OBJECT_NOT_A_WEB_RESOURCE(resourceClass.getName()));
    }
    
    public static ContainerException badClassConsumeMime(Exception e, Class resourceClass, ConsumeMime c) {
        return new ContainerException(ImplMessages.BAD_CLASS_CONSUMEMIME(resourceClass,
                                                                                           c.value()), e);
    }
    
    public static ContainerException badClassProduceMime(Exception e, Class resourceClass, ProduceMime p) {
        return new ContainerException(ImplMessages.BAD_CLASS_PRODUCEMIME(resourceClass,
                                                                                           p.value()), e);
    }
    
    public static ContainerException badMethodHttpMethod(Class resourceClass, Method m, HttpMethod hm) {
        return new ContainerException(ImplMessages.BAD_METHOD_HTTPMETHOD(resourceClass,
                                                                                           hm.value(),
                                                                                           m.toString()));
    }
    
    public static ContainerException badMethodConsumeMime(Exception e, Class resourceClass, Method m, ConsumeMime c) {
        return new ContainerException(ImplMessages.BAD_METHOD_CONSUMEMIME(resourceClass,
                                                                                   c.value(),
                                                                                   m.toString()), e);
    }
    
    public static ContainerException badMethodProduceMime(Exception e, Class resourceClass, Method m, ProduceMime p) {
        return new ContainerException(ImplMessages.BAD_METHOD_PRODUCEMIME(resourceClass,
                                                                                   p.value(),
                                                                                   m.toString()), e);
    }
}
