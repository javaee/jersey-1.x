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

package com.sun.ws.rest.impl.model;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.dispatch.UriTemplateDispatcher;
import com.sun.ws.rest.spi.dispatch.ResourceDispatchContext;
import com.sun.ws.rest.spi.dispatch.UriPathTemplate;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import com.sun.ws.rest.spi.resource.ResourceProviderFactory;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.ws.rs.UriTemplate;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class RootResourceClass extends BaseResourceClass {
    private static final Logger LOGGER = Logger.getLogger(RootResourceClass.class.getName());

    private final ResourceConfig resourceConfig;
            
    private final ResourceProviderFactory resolverFactory;
    
    private final ConcurrentMap<Class, ResourceClass> metaClassMap = new ConcurrentHashMap<Class, ResourceClass>();
        
    private final Object containerMemento;
    
    public RootResourceClass(Object containerMemento, ResourceConfig resourceConfig) {
        this.containerMemento = containerMemento;
        this.resourceConfig = resourceConfig;
        this.resolverFactory = ResourceProviderFactory.getInstance();
        
        add(resourceConfig.getResourceClasses());

        if (uriResolver.getUriTemplates().isEmpty()) {
            String message = "The ResourceConfig instance does not contain any root resource classes";
            LOGGER.severe(message);
            throw new ContainerException(message);
        }
    }
    
    public ResourceClass getResourceClass(Class c) {
        assert c != null;
        
        // Try the non-blocking read, the most common opertaion
        ResourceClass rmc = metaClassMap.get(c);
        if (rmc != null) return rmc;
                
        // ResourceClass is not present use a synchronized block
        // to ensure that only one ResourceClass instance is created
        // and put to the map
        synchronized(metaClassMap) {
            // One or more threads may have been blocking on the synchronized
            // block, re-check the map
            rmc = metaClassMap.get(c);
            if (rmc != null) return rmc;
            
            rmc = new ResourceClass(containerMemento, c, resourceConfig, resolverFactory);
            metaClassMap.put(c, rmc);
            return rmc;
        }
    }
    
    private void add(Set<Class> resourceClasses) {
        for (Class resourceClass : resourceClasses)
            addResource(resourceClass);
    }
    
    private void add(Class<?>... resourceClasses) {
        for (Class resourceClass : resourceClasses)
            addResource(resourceClass);
    }
    
    private void addResource(final Class<?> c) {
        final UriTemplate tAnnotation = c.getAnnotation(UriTemplate.class);
        if (tAnnotation == null) {
            // TODO log warning
            return;   
        }

        ResourceClass resourceClass = getResourceClass(c);

        // TODO what does it mean to support limited=false
        // when there are sub-resources present?
        UriTemplateType t = new UriPathTemplate(
                tAnnotation, resourceClass.hasSubResources);
                
        UriTemplateDispatcher d = ClassDispatcherFactory.create(t, c);
        uriResolver.add(d.getTemplate(), d);
    }
    
    public boolean dispatch(ResourceDispatchContext context, StringBuilder path) {
        return dispatch(context, null, path);
    }
}
