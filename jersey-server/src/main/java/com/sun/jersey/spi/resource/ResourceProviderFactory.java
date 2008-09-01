/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.spi.resource;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.impl.resource.PerRequestProvider;
import com.sun.jersey.spi.service.ComponentProvider;
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
     * "com.sun.jersey.config.property.DefaultResourceProviderClass".
     * 
     * If there is no such property then the per-request resource provider
     * will be chosen.
     * 
     * @param provider the component provider
     * @param resourceProvider the component provider for resource classes
     * @param resource the abstract resource for the provider.
     * @param resourceFeatures the resource features
     * @param resourceProperties the resource properties
     * @return the resource provider.
     * @throws IllegalArgumentException if the Java type of resource provider
     *         property is not Class<? extends ResourceProvider>.
     */
    public ResourceProvider createProvider(
            ComponentProvider provider,
            ComponentProvider resourceProvider,
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
                            + " of type " + v.getClass() + " not of a subclass of com.sun.jersey.spi.resource.ResourceProvider");
                }
            } else {
                throw new IllegalArgumentException("Property value for " 
                        + ResourceConfig.PROPERTY_DEFAULT_RESOURCE_PROVIDER_CLASS
                        + " of type " + v.getClass() + " not of type Class<? extends com.sun.jersey.spi.resource.ResourceProvider>");
            }
        }
        
        try {
            ResourceProvider r = (ResourceProvider)provider.
                    getInstance(ComponentProvider.Scope.PerRequest, providerClass);
            r.init(provider, resourceProvider, resource);
            return r;
        } catch (IllegalAccessException ex) {
            throw new ContainerException("Unable to create resource provider", ex);
        } catch (InstantiationException ex) {
            throw new ContainerException("Unable to create resource provider", ex);
        }
    }
}
