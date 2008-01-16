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

import com.sun.ws.rest.api.MediaTypes;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.api.model.AbstractSubResourceLocator;
import com.sun.ws.rest.api.model.AbstractSubResourceMethod;
import com.sun.ws.rest.api.view.Views;
import com.sun.ws.rest.impl.model.method.ResourceHeadWrapperMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.model.method.ResourceViewMethod;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractorFactory;
import com.sun.ws.rest.impl.uri.rules.HttpMethodRule;
import com.sun.ws.rest.impl.uri.rules.SubLocatorRule;
import com.sun.ws.rest.impl.view.ViewFactory;
import com.sun.ws.rest.impl.uri.PathPattern;
import com.sun.ws.rest.impl.uri.PathTemplate;
import com.sun.ws.rest.api.uri.UriTemplate;
import com.sun.ws.rest.impl.uri.rules.RightHandPathRule;
import com.sun.ws.rest.impl.uri.rules.UriRulesFactory;
import com.sun.ws.rest.impl.view.ViewFactory;
import com.sun.ws.rest.impl.wadl.WadlFactory;
import com.sun.ws.rest.spi.resource.ResourceProvider;
import com.sun.ws.rest.spi.resource.ResourceProviderFactory;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import com.sun.ws.rest.spi.uri.rules.UriRules;
import com.sun.ws.rest.spi.view.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceClass {
    private static final Logger LOGGER = Logger.getLogger(ResourceClass.class.getName());
    
    private final UriRules<UriRule> rules;
    private final ResourceConfig config;
    public final AbstractResource resource;
    public final ResourceProvider resolver;
    public final boolean hasSubResources;

    public ResourceClass(Object containerMemento, ResourceConfig config,
            ResourceProviderFactory resolverFactory, AbstractResource resource) {
        this.resource = resource;

        this.config = config;

        this.resolver = resolverFactory.createProvider(resource, config.getFeatures(), config.getProperties());

        boolean hasSubResourcesAux = false;

        RulesMap<UriRule> rulesMap = new RulesMap<UriRule>();

        hasSubResourcesAux = processSubResourceLocators(rulesMap);

        final Map<PathPattern, ResourceMethodMap> patternMethodMap =
                processSubResourceMethods();

        final ResourceMethodMap methodMap = processMethods();

        processViews(containerMemento, methodMap, patternMethodMap);

        // Create the rules for the sub-resource HTTP methods
        for (Map.Entry<PathPattern, ResourceMethodMap> e : patternMethodMap.entrySet()) {
            hasSubResourcesAux = true;

            final PathPattern p = e.getKey();
            final ResourceMethodMap rmm = e.getValue();
            
            rmm.sort();
            processWadl(resource, p, rmm);        
            rulesMap.put(p,
                    new RightHandPathRule(p.getTemplate().endsWithSlash(),
                    new HttpMethodRule(rmm)));
        }

        // Create the rules for the HTTP methods
        methodMap.sort();
        /**
         * Add WADL GET method after method map has been sorted.
         * This ensures that application specific methods get priority
         * but it means if a method produces a wild card like "*\/*"
         * then that method will consume the request even if the client
         * explicitly accepts the WADL media type.
         * 
         * WADL will not be created if there are no sub-resource methods,
         * sub-resource locators present and no resource methods present
         * or only the HTTP OPTIONS resource method is present.
         */
        if (!rulesMap.isEmpty() || methodMap.size() > 1)
            processWadl(resource, methodMap);        
        if (!methodMap.isEmpty()) {
            // No need to adapt with the RightHandPathRule as the URI path
            // will be consumed when such a rule is accepted
            rulesMap.put(PathPattern.EMPTY_PATH, new HttpMethodRule(methodMap));
        }

        this.hasSubResources = hasSubResourcesAux;

        this.rules = UriRulesFactory.create(rulesMap);
    }

    public UriRules<UriRule> getRules() {
        return rules;
    }

    private void addToPatternMethodMap(
            Map<PathPattern, ResourceMethodMap> tmm,
            PathPattern p,
            ResourceMethod rm) {
        ResourceMethodMap rmm = tmm.get(p);
        if (rmm == null) {
            rmm = new ResourceMethodMap();
            tmm.put(p, rmm);
        }
        rmm.put(rm);
    }

    private boolean processSubResourceLocators(RulesMap<UriRule> rulesMap) {
        boolean hasSubResources = false;
        for (final AbstractSubResourceLocator locator : resource.getSubResourceLocators()) {
            hasSubResources = true;

            UriTemplate t = new PathTemplate(
                    locator.getUriPath().getValue(),
                    locator.getUriPath().isEncode());

            PathPattern p = new PathPattern(
                    t,
                    locator.getUriPath().isLimited());

            UriRule r = new SubLocatorRule(
                    t.getTemplateVariables(),
                    locator.getMethod(),
                    ParameterExtractorFactory.createExtractorsForSublocator(locator));

            rulesMap.put(p, new RightHandPathRule(t.endsWithSlash(), r));
        }
        return hasSubResources;
    }

    private Map<PathPattern, ResourceMethodMap> processSubResourceMethods() {
        final Map<PathPattern, ResourceMethodMap> patternMethodMap =
                new HashMap<PathPattern, ResourceMethodMap>();
        for (final AbstractSubResourceMethod method : this.resource.getSubResourceMethods()) {

            UriTemplate t = new PathTemplate(
                    method.getUriPath().getValue(),
                    method.getUriPath().isEncode());

            // TODO what does it mean to support limited=false
            PathPattern p = new PathPattern(t, false);

            ResourceMethod rm = new ResourceHttpMethod(t.getTemplateVariables(), method);
            addToPatternMethodMap(patternMethodMap, p, rm);
        }

        for (Map.Entry<PathPattern, ResourceMethodMap> e : patternMethodMap.entrySet()) {
            processHead(e.getValue());
            processOptions(e.getValue(), this.resource, e.getKey());            
        }

        return patternMethodMap;
    }

    private ResourceMethodMap processMethods() {
        final ResourceMethodMap methodMap = new ResourceMethodMap();
        for (final AbstractResourceMethod resourceMethod : this.resource.getResourceMethods()) {
            ResourceMethod rm = new ResourceHttpMethod(resourceMethod);
            methodMap.put(rm);
        }

        processHead(methodMap);
        processOptions(methodMap, this.resource, null);

        return methodMap;
    }

    private void processHead(ResourceMethodMap methodMap) {
        List<ResourceMethod> getList = methodMap.get(HttpMethod.GET);
        if (getList == null || getList.isEmpty()) {
            return;
        }

        List<ResourceMethod> headList = methodMap.get(HttpMethod.HEAD);
        if (headList == null) {
            headList = new ArrayList<ResourceMethod>();
        }

        for (ResourceMethod getMethod : getList) {
            if (!containsMediaOfMethod(headList, getMethod)) {
                ResourceMethod headMethod = new ResourceHeadWrapperMethod(getMethod);
                methodMap.put(headMethod);
                headList = methodMap.get(HttpMethod.HEAD);
            }
        }
    }

    /**
     * Determin if a the resource method list contains a method that 
     * has the same consume/produce media as another resource method.
     * 
     * @param methods the resource methods
     * @param method the resource method to check
     * @return true if the list contains a method with the same media as method.
     */
    private boolean containsMediaOfMethod(List<ResourceMethod> methods,
            ResourceMethod method) {
        for (ResourceMethod m : methods) {
            if (method.mediaEquals(m)) {
                return true;
            }
        }

        return false;
    }

    private void processOptions(ResourceMethodMap methodMap, 
            AbstractResource resource, PathPattern p) {
        List<ResourceMethod> l = methodMap.get("OPTIONS");
        if (l != null) {
            return;
        }

        ResourceMethod optionsMethod = WadlFactory.createWadlOptionsMethod(methodMap, resource, p);
        if (optionsMethod == null)
            optionsMethod = new ResourceHttpOptionsMethod(methodMap);
        methodMap.put(optionsMethod);
    }

    private void processViews(Object containerMemento,
            ResourceMethodMap methodMap,
            Map<PathPattern, ResourceMethodMap> patternMethodMap) {

        // Get all the view names
        Map<String, Class<?>> viewMap = getViews();

        // Create the views
        for (Map.Entry<String, Class<?>> view : viewMap.entrySet()) {
            final Class<?> resourceClass = view.getValue();
            final String viewName = view.getKey();

            String path = getAbsolutePathOfView(resourceClass, viewName);

            String pathPattern = getPathPatternOfView(resourceClass, viewName);

            View v = ViewFactory.createView(containerMemento, path);
            if (v == null) {
                continue;
            }

            ResourceMethod rm = new ResourceViewMethod(v);
            if (pathPattern.length() == 0) {
                methodMap.put(rm);
            } else {
                UriTemplate t = new PathTemplate(pathPattern, true);
                PathPattern p = new PathPattern(t, false);
                addToPatternMethodMap(patternMethodMap, p, rm);
            }
        }
    }

    private Map<String, Class<?>> getViews() {
        // Find all the view names
        Class<?> resourceClass = this.resource.getResourceClass();
        Map<String, Class<?>> viewMap = new HashMap<String, Class<?>>();
        Set<String> views = new HashSet<String>();
        for (; resourceClass != null; resourceClass = resourceClass.getSuperclass()) {
            Views vAnnotation = resourceClass.getAnnotation(Views.class);
            if (vAnnotation == null) {
                continue;
            }

            for (String name : vAnnotation.value()) {
                if (views.contains(name)) {
                    continue;
                }

                if (!name.startsWith("/") && name.contains("/")) {
                    continue;
                }

                views.add(name);
                viewMap.put(name, resourceClass);
            }
        }

        return viewMap;
    }

    private String getPathPatternOfView(Class<?> resourceClass, String path) {
        if (path.startsWith("/")) {
            throw new IllegalArgumentException();
        // TODO get the name of the leaf node of the path
        // and use that for the URI template
        }

        if (path.matches("index\\.[^/]*")) {
            return "";
        } else {
            // Remove the type of view from the URI template
            int i = path.lastIndexOf('.');
            if (i > 0) {
                path = path.substring(0, i);
            }

            return path;
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
        return "/" + resourceClass.getName().replace('.', '/').replace('$', '/');
    }
    
    private void processWadl(AbstractResource resource, ResourceMethodMap methodMap) {
        processWadl(resource, null, methodMap);
    }
    
    private void processWadl(AbstractResource resource, PathPattern p, ResourceMethodMap methodMap) {
        // Check if there is already a method that explicitly produces WADL
        // If so do not override that method
        if (methodMap.get("GET") != null) {
            for (ResourceMethod getMethod : methodMap.get("GET")) {
                for (MediaType m : getMethod.getProduceMime())
                    if (m.equals(MediaTypes.WADL))
                        return;
            }
        }
        
        ResourceMethod wadlMethod = WadlFactory.createWadlGetMethod(resource, p);
        if (wadlMethod != null) methodMap.put(wadlMethod);
    }
}