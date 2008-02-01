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

package com.sun.ws.rest.spi.resource;

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.impl.resource.PerRequestProvider;
import com.sun.ws.rest.spi.service.ComponentProvider;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * A singleton that manages access to resource provider instances.
 */
public class ResourceProviderFactory {
    
    private static ResourceProviderFactory instance;
    
    public static synchronized ResourceProviderFactory getInstance() {
        if (instance == null)
            instance = new ResourceProviderFactory();
        return instance;
    }
    
    /**
     * Obtain a ResourceProvider instance for the supplied
     * resourceClass.
     * <p>
     * This method will first search for a class that implements 
     * {@link ResourceProvider} that is declared as an annotation on 
     * resourceClass.
     * 
     * If not found the the {@link ResourceProvider} class will be looked up 
     * in the resourceProperties using the property name 
     * "com.sun.ws.rest.config.property.DefaultResourceProviderClass".
     * 
     * If there is no such property then the per-request resource provider
     * will be chosen.
     * 
     * @param provider the component provider
     * @param resource the abstract resource for the provider.
     * @param resourceFeatures the resource features
     * @param resourceProperties the resource properties
     * @return the resource provider.
     * @throws IllegalArgumentException if the Java type of resource provider
     *         property is not Class<? extends ResourceProvider>.
     */
    public ResourceProvider createProvider(
            ComponentProvider provider,
            AbstractResource resource,
            Map<String, Boolean> resourceFeatures,
            Map<String, Object> resourceProperties) {
        Class<? extends ResourceProvider> providerClass = null;
        
        // Use annotations to identify the correct provider, note that
        // @ResourceFactory is a meta-annotation so we look for annotations
        // on the annotations of the resource class
        Annotation annotations[] = resource.getResourceClass().getAnnotations();
        for (Annotation a: annotations) {
            Class<?> annotationClass = a.annotationType();
            ResourceFactory rf = annotationClass.getAnnotation(ResourceFactory.class);
            if (rf != null && providerClass==null)
                providerClass = rf.value();
            else if (rf != null && providerClass!=null)
                throw new ContainerException(resource.getResourceClass().toString()+
                        " has multiple ResourceFactory annotations");
        }
        
        if (providerClass == null) {
            Object v = resourceProperties.
                    get(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS);
            if (v == null) {
                // Use default provider if none specified
                providerClass = PerRequestProvider.class;
            } else if (v instanceof Class) {
                Class<?> c = (Class<?>)v;
                if (ResourceProvider.class.isAssignableFrom(c)) {
                    providerClass = c.asSubclass(ResourceProvider.class);
                } else {
                    throw new IllegalArgumentException("Property value for " 
                            + ResourceConfig.PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS
                            + " of type " + v.getClass() + " not of a subclass of com.sun.ws.rest.spi.resource.ResourceProvider");
                }
            } else {
                throw new IllegalArgumentException("Property value for " 
                        + ResourceConfig.PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS
                        + " of type " + v.getClass() + " not of type Class<? extends com.sun.ws.rest.spi.resource.ResourceProvider>");
            }
        }
        
        try {
            ResourceProvider r = (ResourceProvider)provider.
                    getInstance(ComponentProvider.Scope.ApplicationDefined, providerClass);
            r.init(provider, resource);
            return r;
        } catch (IllegalAccessException ex) {
            throw new ContainerException("Unable to create resource provider", ex);
        } catch (InstantiationException ex) {
            throw new ContainerException("Unable to create resource provider", ex);
        }
    }
}
