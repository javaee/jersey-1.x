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

import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.impl.model.MediaTypeList;
import com.sun.ws.rest.impl.model.MimeHelper;
import com.sun.ws.rest.impl.model.ResourceClass;
import java.lang.reflect.Method;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceJavaMethod extends ResourceMethod {
    protected final Method method;

    protected RequestDispatcher dispatcher;
    
    protected ResourceJavaMethod(ResourceClass resourceClass, Method method) throws ContainerException {
        super(resourceClass);
        this.consumeMime = getConsumeMimeList(resourceClass, method);
        this.produceMime = getProduceMimeList(resourceClass, method);

        this.method = method;
    }
    
    public Method getMethod() {
        return method;
    }
    
    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }
    
    private MediaTypeList getConsumeMimeList(ResourceClass mc, Method method) {
        if (method.isAnnotationPresent(ConsumeMime.class))
            return MimeHelper.createMediaTypes(method.getAnnotation(ConsumeMime.class));
        else
            return mc.consumeMime;
    }
        
    private MediaTypeList getProduceMimeList(ResourceClass mc, Method method) {
        if (method.isAnnotationPresent(ProduceMime.class))
            return MimeHelper.createMediaTypes(method.getAnnotation(ProduceMime.class));
        else
            return mc.produceMime;
    }
}