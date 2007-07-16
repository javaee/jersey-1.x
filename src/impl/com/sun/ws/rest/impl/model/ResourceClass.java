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

import com.sun.ws.rest.impl.view.ViewFactory;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.core.WebResource;
import com.sun.ws.rest.api.view.Views;
import com.sun.ws.rest.impl.dispatch.URITemplateDispatcher;
import com.sun.ws.rest.impl.model.method.ResourceGenericMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethodMap;
import com.sun.ws.rest.impl.model.method.ResourceMethodMapDispatcher;
import com.sun.ws.rest.impl.model.method.ResourceViewMethod;
import com.sun.ws.rest.impl.model.node.NodeDispatcherFactory;
import com.sun.ws.rest.impl.resolver.WebResourceResolverFactoryFacade;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import com.sun.ws.rest.spi.resolver.WebResourceResolver;
import com.sun.ws.rest.spi.resolver.WebResourceResolverFactory;
import com.sun.ws.rest.spi.view.View;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceClass extends BaseResourceClass {
    private static final Logger LOGGER = Logger.getLogger(ResourceClass.class.getName());

    public final ResourceConfig config;
    
    public final Class<?> c;
    
    public final WebResourceResolver resolver;
            
    public final MediaTypeList consumeMime;
    
    public final MediaTypeList produceMime;
    
    public final boolean hasSubResources;
    
    public ResourceClass(Object containerMemento, Class<?> c, ResourceConfig config) {
        this(containerMemento, c, config, null);
    }
    
    public ResourceClass(Object containerMemento, Class<?> c, ResourceConfig config, 
            WebResourceResolverFactory resolverFactory) {
        this.c = c;
        this.config = config;
        this.resolver = WebResourceResolverFactoryFacade.
                createWebResourceResolver(resolverFactory, c);
        
        this.consumeMime = getConsumeMimeList();
        this.produceMime = getProduceMimeList();

        MethodList methods = new MethodList(c);

        boolean hasSubResources = false;
                
        
        // Resolver methods
        for (final Method m : methods.hasNotAnnotation(HttpMethod.class).hasAnnotation(UriTemplate.class)) {
            hasSubResources = true;
            
            UriTemplate at = m.getAnnotation(UriTemplate.class);
            String tValue = at.value();
            if (!tValue.startsWith("/"))
                tValue = "/" + tValue;
            URITemplateType t = (at.limited()) ? new URITemplateType(
                    tValue, URITemplateType.RIGHT_HANDED_REGEX) : 
                new URITemplateType(
                    tValue, URITemplateType.RIGHT_SLASHED_REGEX);
            
            final URITemplateDispatcher d = NodeDispatcherFactory.create(t, m);            
            dispatchers.add(d);
        }
        
        
        // Templated HTTP methods
        Map<URITemplateType, ResourceMethodMap> templatedMethodMap = new HashMap<URITemplateType, ResourceMethodMap>();
        for (Method m : methods.hasAnnotation(HttpMethod.class).hasAnnotation(UriTemplate.class)) {            
            String tValue = m.getAnnotation(UriTemplate.class).value();
            if (!tValue.startsWith("/"))
                tValue = "/" + tValue;            
            URITemplateType t = new URITemplateType(tValue, 
                    URITemplateType.RIGHT_SLASHED_REGEX);
            
            ResourceMethod rm = new ResourceHttpMethod(this, m);
            addToTemplatedMethodMap(templatedMethodMap, t, rm);
        }        
        
        
        // HTTP methods
        final ResourceMethodMap methodMap = new ResourceMethodMap();
        for (Method m : methods.hasAnnotation(HttpMethod.class).hasNotAnnotation(UriTemplate.class)) {
            ResourceMethod rm = new ResourceHttpMethod(this, m);
            methodMap.put(rm);
        }
        
        // WebResource interface method for HTTP methods
        if (WebResource.class.isAssignableFrom(c)) {
            try {
                Method m = c.getMethod("handleRequest", HttpRequestContext.class, HttpResponseContext.class);
                
                ResourceMethod genericMethod = new ResourceGenericMethod(this, m);
                
                // TODO check if the method has a URI template
                
                // Add the generic method to the list of all existing methods
                for (String methodName : methodMap.keySet()) {
                    methodMap.get(methodName).add(genericMethod);
                }
                // Add to the null method to support any methods not declared
                methodMap.put(genericMethod);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }
        
        // Process the views
        processViews(containerMemento, methodMap, templatedMethodMap);

        
        // Process templated HTTP methods
        for (Map.Entry<URITemplateType, ResourceMethodMap> e : templatedMethodMap.entrySet()) {
            hasSubResources = true;
            
            e.getValue().sort();
            dispatchers.add(new ResourceMethodMapDispatcher(e.getKey(), e.getValue()));
        }
        
        // Process HTTP methods
        if (!methodMap.isEmpty()) {
            methodMap.sort();
            dispatchers.add(new ResourceMethodMapDispatcher(URITemplateType.NULL, methodMap));
        }
        
        
        if (dispatchers.isEmpty()) {
            String message = "The class " 
                    + c + 
                    " does not contain any static sub-resources, HTTP methods or resolving methods";
            LOGGER.severe(message);
            throw new ContainerException(message);
        }
        
        // Sort the dispatchers using the URI template as the primiary sort key
        Collections.sort(dispatchers, URITemplateDispatcher.COMPARATOR);
        this.hasSubResources = hasSubResources;
    }
    
    private void addToTemplatedMethodMap(
            Map<URITemplateType, ResourceMethodMap> tmm,
            URITemplateType t,
            ResourceMethod rm) {
        ResourceMethodMap rmm = tmm.get(t);
        if (rmm == null) {
            rmm = new ResourceMethodMap();
            tmm.put(t, rmm);
        }
        rmm.put(rm);
    }
    
    private MediaTypeList getConsumeMimeList() {
        return MimeHelper.createMediaTypes(c.getAnnotation(ConsumeMime.class));
    }
        
    private MediaTypeList getProduceMimeList() {
        return MimeHelper.createMediaTypes(c.getAnnotation(ProduceMime.class));
    }
    
    
    private void processViews(Object containerMemento, 
            ResourceMethodMap methodMap, 
            Map<URITemplateType, ResourceMethodMap> templatedMethodMap) {        

        // Get all the view names
        Map<String, Class<?>> viewMap = getViews();

        // Create the views
        for (Map.Entry<String, Class<?>> view : viewMap.entrySet()) {
            final Class<?> resourceClass = view.getValue();
            final String viewName = view.getKey();
            
            String path = getAbsolutePathOfView(resourceClass, viewName);
            URITemplateType t = getURITemplateOfView(resourceClass, viewName);
            
            View v = ViewFactory.createView(containerMemento, path);
            if (v == null)
                continue;
            
            ResourceMethod rm = new ResourceViewMethod(this, v);
            if (t.equals(URITemplateType.NULL)) {
                methodMap.put(rm);
            } else {
                addToTemplatedMethodMap(templatedMethodMap, t, rm);
            }
        }        
    }
    
    private Map<String, Class<?>> getViews() {
        // Find all the view names
        Class<?> resourceClass = c;
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
    
    private URITemplateType getURITemplateOfView(Class<?> resourceClass, String path) {
        if (path.startsWith("/")) {
            // TODO get the name of the leaf node of the path
            // and use that for the URI template
            return null;
        } 
        
        if (path.matches("index\\.[^/]*")) {
            return URITemplateType.NULL;
        } else {
            path = "/" + path;            
            int i = path.lastIndexOf('.');
            if (i > 0)
                path = path.substring(0, i);
            
            return new URITemplateType(path);            
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
