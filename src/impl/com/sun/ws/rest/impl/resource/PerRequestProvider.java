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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpContext;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceConstructor;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractor;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractorFactory;
import com.sun.ws.rest.spi.resource.ResourceProvider;
import com.sun.ws.rest.spi.service.ComponentProvider;
import com.sun.ws.rest.spi.service.ComponentProvider.Scope;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author mh124079
 */
public final class PerRequestProvider implements ResourceProvider {

    private Class<?> c;
    
    private Constructor<?> constructor;
    
    private ParameterExtractor[] extractors;
    
    public void init(ComponentProvider provider,
            AbstractResource abstractResource) {
        this.c = abstractResource.getResourceClass();
        
        // TODO select the most appropriate constructor 
        // instead of just picking up the first one
        if (abstractResource.getConstructors().isEmpty()) {
            this.constructor = null;
            this.extractors = null;
        } else {
            AbstractResourceConstructor abstractConstructor = 
                    abstractResource.getConstructors().get(0);
            
            this.constructor = abstractConstructor.getCtor();
            if (this.constructor.getParameterTypes().length > 0) {
                this.extractors = ParameterExtractorFactory.
                        createExtractorsForConstructor(abstractConstructor);
            } else {
                this.constructor = null;
                this.extractors = null;                
            }
        }
    }

    public Object getInstance(ComponentProvider provider, HttpContext context) {
        try {
            if (constructor == null) {
                return provider.getInstance(Scope.ApplicationDefined, c);
            } else {
                Object[] values = new Object[extractors.length];
                for (int i = 0; i < extractors.length; i++) {
                    if (extractors[i] == null)
                        values[i] = null;
                    else
                        values[i] = extractors[i].extract(context);
                }
                return provider.getInstance(Scope.ApplicationDefined, 
                    constructor, values);
            }
        } catch (InstantiationException ex) {
            throw new ContainerException("Unable to create resource", ex);
        } catch (IllegalAccessException ex) {
            throw new ContainerException("Unable to create resource", ex);
        } catch (InvocationTargetException ex) {
            Throwable t = ex.getTargetException();
            if (t instanceof RuntimeException) {
                // Rethrow the runtime exception
                throw (RuntimeException)t;
            } else {
                // TODO should a checked exception be wrapped in 
                // WebApplicationException ?
                throw new ContainerException("Unable to create resource", t);
            }
        }        
    }
}