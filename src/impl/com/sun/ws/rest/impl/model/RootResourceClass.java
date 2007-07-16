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
import com.sun.ws.rest.impl.dispatch.URITemplateDispatcher;
import com.sun.ws.rest.spi.dispatch.ResourceDispatchContext;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import com.sun.ws.rest.spi.resolver.WebResourceResolverFactory;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.UriTemplate;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class RootResourceClass extends BaseResourceClass {
    private final ResourceConfig resourceConfig;
            
    private final WebResourceResolverFactory resolverFactory;
    
    private final ConcurrentMap<Class, ResourceClass> metaClassMap = new ConcurrentHashMap<Class, ResourceClass>();
        
    private final Object containerMemento;
    
    public RootResourceClass(Object containerMemento, ResourceConfig resourceConfig, 
            WebResourceResolverFactory resolverFactory) {
        this.containerMemento = containerMemento;
        this.resourceConfig = resourceConfig;
        this.resolverFactory = resolverFactory;
        
        add(resourceConfig.getResourceClasses());
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
    
    public void add(Set<Class> resourceClasses) throws ContainerException {
        for (Class resourceClass : resourceClasses)
            addResource(resourceClass);
        
        Collections.sort(dispatchers, URITemplateDispatcher.COMPARATOR);
    }
    
    public void add(Class<?>... resourceClasses) throws ContainerException {
        for (Class resourceClass : resourceClasses)
            addResource(resourceClass);
        
        Collections.sort(dispatchers, URITemplateDispatcher.COMPARATOR);
    }
    
    private void addResource(final Class<?> c) throws ContainerException {
        final UriTemplate tAnnotation = c.getAnnotation(UriTemplate.class);
        if (tAnnotation == null)
            return;

        String tValue = tAnnotation.value();
        
        if (!tValue.startsWith("/")) {
            throw new ContainerException(
                    "The URI template " 
                    + tAnnotation.value() + 
                    ", of class "
                    + c +
                    ", is not an absolute path (it does not start with a '/' character)");
        }
        
        ResourceClass resourceClass = getResourceClass(c);

        String rightHandPattern = (resourceClass.hasSubResources) ? 
                URITemplateType.RIGHT_HANDED_REGEX : URITemplateType.RIGHT_SLASHED_REGEX;
        URITemplateType t = new URITemplateType(tValue, rightHandPattern);
        
        URITemplateDispatcher d = ClassDispatcherFactory.create(t, c);
        dispatchers.add(d);
    }
    
    public boolean dispatch(ResourceDispatchContext context, String path) {
        return dispatch(context, null, path);
    }
}
