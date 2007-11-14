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
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractSubResourceLocator;
import com.sun.ws.rest.api.model.AbstractSubResourceMethod;
import com.sun.ws.rest.api.view.Views;
import com.sun.ws.rest.impl.dispatch.UriTemplateDispatcher;
import com.sun.ws.rest.impl.model.method.ResourceHeadWrapperMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethodList;
import com.sun.ws.rest.impl.model.method.ResourceMethodMap;
import com.sun.ws.rest.impl.model.method.ResourceMethodMapDispatcher;
import com.sun.ws.rest.impl.model.method.ResourceViewMethod;
import com.sun.ws.rest.impl.model.node.NodeDispatcherFactory;
import com.sun.ws.rest.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.ws.rest.impl.view.ViewFactory;
import com.sun.ws.rest.spi.dispatch.UriPathTemplate;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import com.sun.ws.rest.spi.resource.ResourceProvider;
import com.sun.ws.rest.spi.resource.ResourceProviderFactory;
import com.sun.ws.rest.spi.view.View;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;


/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceClass extends BaseResourceClass {
    private static final Logger LOGGER = Logger.getLogger(ResourceClass.class.getName());

    public final ResourceConfig config;
    
    public final AbstractResource resource;
    
    public final ResourceProvider resolver;
            
    public final boolean hasSubResources;
    
    public ResourceClass(Object containerMemento, Class<?> c, ResourceConfig config, 
            ResourceProviderFactory resolverFactory) {
        this.resource = IntrospectionModeller.createResource(c);
        this.config = config;
        this.resolver = resolverFactory.createProvider(c, 
                config.getFeatures(), config.getProperties());

        boolean hasSubResources = false;
                
        hasSubResources = processSubResourceLocators();
                
        final Map<UriTemplateType, ResourceMethodMap> templatedMethodMap = processSubResourceMethods();

        final ResourceMethodMap methodMap = processMethods();

        processViews(containerMemento, methodMap, templatedMethodMap);

        
        // Create the dispatchers for the sub-resource HTTP methods
        for (Map.Entry<UriTemplateType, ResourceMethodMap> e : templatedMethodMap.entrySet()) {
            hasSubResources = true;
            
            e.getValue().sort();
            uriResolver.add(e.getKey(), 
                    new ResourceMethodMapDispatcher(e.getKey(), e.getValue()));
        }
        
        // Create the dispatcher for the HTTP methods
        if (!methodMap.isEmpty()) {
            methodMap.sort();
            uriResolver.add(UriTemplateType.NULL, 
                    new ResourceMethodMapDispatcher(UriTemplateType.NULL, methodMap));
        }
        
        
        if (uriResolver.getUriTemplates().isEmpty()) {
            String message = "The class " 
                    + c + 
                    " does not contain any sub-resource locators, sub-resource HTTP methods or HTTP methods";
            LOGGER.severe(message);
            throw new ContainerException(message);
        }
        
        this.hasSubResources = hasSubResources;
    }
    
    private void addToTemplatedMethodMap(
            Map<UriTemplateType, ResourceMethodMap> tmm,
            UriTemplateType t,
            ResourceMethod rm) {
        ResourceMethodMap rmm = tmm.get(t);
        if (rmm == null) {
            rmm = new ResourceMethodMap();
            tmm.put(t, rmm);
        }
        rmm.put(rm);
    }
    
    private boolean processSubResourceLocators() {
        boolean hasSubResources = false;
        for (final AbstractSubResourceLocator subResourceLocator : this.resource.getSubResourceLocators()) {
            hasSubResources = true;
            
            UriTemplateType t = new UriPathTemplate(
                    subResourceLocator.getUriTemplate().getRawTemplate(), 
                    subResourceLocator.getUriTemplate().isLimited(), 
                    subResourceLocator.getUriTemplate().isEncode());
                        
            final UriTemplateDispatcher d = NodeDispatcherFactory.create(t, subResourceLocator.getMethod());            
            uriResolver.add(d.getTemplate(), d);
        }
        
        return hasSubResources;
    }
    
    private Map<UriTemplateType, ResourceMethodMap> processSubResourceMethods() {
        final Map<UriTemplateType, ResourceMethodMap> templatedMethodMap = 
                new HashMap<UriTemplateType, ResourceMethodMap>();
        for (final AbstractSubResourceMethod subResourceMethod : this.resource.getSubResourceMethods()) {
            
            // TODO what does it mean to support limited=false
            UriTemplateType t = new UriPathTemplate(
                    subResourceMethod.getUriTemplate().getRawTemplate(), 
                    false,
                    subResourceMethod.getUriTemplate().isEncode());
                        
            ResourceMethod rm = new ResourceHttpMethod(this, subResourceMethod);
            addToTemplatedMethodMap(templatedMethodMap, t, rm);
        }
        
        for (ResourceMethodMap methodMap : templatedMethodMap.values()) {
            processHead(methodMap);
            processOptions(methodMap);
        }
        
        return templatedMethodMap;
    }

    private ResourceMethodMap processMethods() {
        final ResourceMethodMap methodMap = new ResourceMethodMap();
        for (final com.sun.ws.rest.api.model.AbstractResourceMethod resourceMethod : this.resource.getResourceMethods()) {
            ResourceMethod rm = new ResourceHttpMethod(this, resourceMethod);
            methodMap.put(rm);
        }
        
        processHead(methodMap);
        processOptions(methodMap);
        
        return methodMap;
    }
    
    private void processHead(ResourceMethodMap methodMap) {
        ResourceMethodList getList = methodMap.get(HttpMethod.GET);
        if (getList == null || getList.isEmpty())
            return;
        
        ResourceMethodList headList = methodMap.get(HttpMethod.HEAD);        
        if (headList == null) headList = new ResourceMethodList();
        
        for (ResourceMethod getMethod : getList) {
            if (!headList.containsMediaOfMethod(getMethod)) {
                ResourceMethod headMethod = new ResourceHeadWrapperMethod(getMethod);
                methodMap.put(headMethod);
                headList = methodMap.get(HttpMethod.HEAD);
            }
        }
    }
    
    private void processOptions(ResourceMethodMap methodMap) {
        ResourceMethodList l = methodMap.get("OPTIONS");
        if (l != null)
            return;
        
        ResourceMethod optionsMethod = new ResourceHttpOptionsMethod(this, methodMap.getAllow());
        methodMap.put(optionsMethod);
    }
    
    private void processViews(Object containerMemento, 
            ResourceMethodMap methodMap, 
            Map<UriTemplateType, ResourceMethodMap> templatedMethodMap) {        

        // Get all the view names
        Map<String, Class<?>> viewMap = getViews();

        // Create the views
        for (Map.Entry<String, Class<?>> view : viewMap.entrySet()) {
            final Class<?> resourceClass = view.getValue();
            final String viewName = view.getKey();
            
            String path = getAbsolutePathOfView(resourceClass, viewName);
            UriTemplateType t = getURITemplateOfView(resourceClass, viewName);
            
            View v = ViewFactory.createView(containerMemento, path);
            if (v == null)
                continue;
            
            ResourceMethod rm = new ResourceViewMethod(this, v);
            if (t.equals(UriTemplateType.NULL)) {
                methodMap.put(rm);
            } else {
                addToTemplatedMethodMap(templatedMethodMap, t, rm);
            }
        }        
    }
    
    private Map<String, Class<?>> getViews() {
        // Find all the view names
        Class<?> resourceClass = this.resource.getResourceClass();
        Map<String, Class<?>> viewMap = new HashMap<String, Class<?>>();
        Set<String> views = new HashSet<String>();
        for (;resourceClass != null; resourceClass = resourceClass.getSuperclass()) {
            Views vAnnotation = resourceClass.getAnnotation(Views.class);
            if (vAnnotation == null)
                continue;
            
            for (String name : vAnnotation.value()) {
                if (views.contains(name))
                    continue;

                if (!name.startsWith("/") && name.contains("/"))
                    continue;

                views.add(name);
                viewMap.put(name, resourceClass);
            }
        }
        
        return viewMap;
    }
    
    private UriTemplateType getURITemplateOfView(Class<?> resourceClass, String path) {
        if (path.startsWith("/")) {
            // TODO get the name of the leaf node of the path
            // and use that for the URI template
            return null;
        } 
        
        if (path.matches("index\\.[^/]*")) {
            return UriTemplateType.NULL;
        } else {
            // Remove the type of view from the URI template
            int i = path.lastIndexOf('.');
            if (i > 0)
                path = path.substring(0, i);
            
            return new UriPathTemplate(path, false, true);
        }
    }
    
    private String getAbsolutePathOfView(Class<?> resourceClass, String path) {
        if (path.startsWith("/")) {
            // TODO get the name of the leaf node of the path
            // and use that for the URI template
            return null;
        } 
        
        return getAbsolutePathOfClass(resourceClass) + '/' + path;
    }
    
    private String getAbsolutePathOfClass(Class<?> resourceClass) {
        return "/" + resourceClass.getName().replace('.','/').replace('$','/');
    }
}
