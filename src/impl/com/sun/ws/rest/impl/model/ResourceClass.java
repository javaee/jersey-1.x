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

import com.sun.ws.rest.spi.dispatch.DispatchContext;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.SubResources;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.core.WebResource;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.dispatch.DispatcherFactory;
import com.sun.ws.rest.impl.dispatch.URITemplateDispatcher;
import com.sun.ws.rest.impl.model.method.HttpRequestDispatcher;
import com.sun.ws.rest.impl.model.method.ResourceGenericMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethodMap;
import com.sun.ws.rest.impl.model.method.ResourceMethodMapDispatcher;
import com.sun.ws.rest.impl.model.node.NodeDispatcherFactory;
import com.sun.ws.rest.impl.resolver.WebResourceResolverFactoryFacade;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import com.sun.ws.rest.spi.resolver.WebResourceResolver;
import com.sun.ws.rest.spi.resolver.WebResourceResolverFactory;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
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
    
    public ResourceClass(Class<?> c, ResourceConfig config) {
        this(c, config, null);
    }
    
    public ResourceClass(Class<?> c, ResourceConfig config, WebResourceResolverFactory resolverFactory) {
        this.c = c;
        this.config = config;
        this.resolver = WebResourceResolverFactoryFacade.
                createWebResourceResolver(resolverFactory, c);
        
        this.consumeMime = getConsumeMimeList();
        this.produceMime = getProduceMimeList();

        MethodList methods = new MethodList(c);

        boolean hasSubResources = false;
        
        // Sub-resources as static classes
        SubResources subResources = c.getAnnotation(SubResources.class);
        if (subResources != null) {
            // Remove duplicates
            Set<Class> subResourceSet = new HashSet<Class>(Arrays.asList(subResources.value()));
            for (final Class<?> subResource : subResourceSet) {
                hasSubResources = true;

                UriTemplate tAnnotation = subResource.getAnnotation(UriTemplate.class);
                if (tAnnotation == null) {
                    throw new ContainerException("Sub-resource " 
                            + subResource + 
                            ", references by resource " 
                            + c + 
                            ", is not annotated with a UriTemplate");
                }

                String tValue = tAnnotation.value();
                if (!tValue.startsWith("/"))
                    tValue = "/" + tValue;
                URITemplateType t = new URITemplateType(tValue, URITemplateType.RIGHT_HANDED_REGEX);

                URITemplateDispatcher d = ClassDispatcherFactory.create(t, subResource);
                dispatchers.add(d);
            }
        }
        
        
        // Resolver methods
        for (final Method m : methods.hasNotAnnotation(HttpMethod.class).hasAnnotation(UriTemplate.class)) {
            hasSubResources = true;
            
            String tValue = m.getAnnotation(UriTemplate.class).value();
            if (!tValue.startsWith("/"))
                tValue = "/" + tValue;
            URITemplateType t = new URITemplateType(
                    tValue, URITemplateType.RIGHT_HANDED_REGEX);
            
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
            
            ResourceMethodMap rmm = templatedMethodMap.get(t);
            if (rmm == null) {
                rmm = new ResourceMethodMap();
                templatedMethodMap.put(t, rmm);
            }
            ResourceMethod rm = new ResourceHttpMethod(this, m);
            rmm.put(rm);
        }        
        for (Map.Entry<URITemplateType, ResourceMethodMap> e : templatedMethodMap.entrySet()) {
            hasSubResources = true;
            
            e.getValue().sort();
            dispatchers.add(new ResourceMethodMapDispatcher(e.getKey(), e.getValue()));
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
                
                HttpRequestDispatcher hrd = new HttpRequestDispatcher() {
                    public void dispatch(Object resource, HttpRequestContext request, HttpResponseContext response) {
                        ((WebResource)resource).handleRequest(request, response);                        
                    }
                };
                ResourceMethod genericMethod = new ResourceGenericMethod(this, m, hrd);
                
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
        
        // Process HTTP methods
        if (!methodMap.isEmpty()) {
            methodMap.sort();
            dispatchers.add(new ResourceMethodMapDispatcher(URITemplateType.NULL, methodMap));
        }
        
        
        // Add all dispatchers created from dispatch providers
        
        URITemplateDispatcher[] ds = DispatcherFactory.createDispatchers(c, config);
        if (ds != null && ds.length > 0) {
            hasSubResources = true;
            for (URITemplateDispatcher d : ds)
                if (!d.getTemplate().equals(URITemplateType.NULL))
                    hasSubResources = true;
            Collections.addAll(dispatchers, ds);
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
    
    private MediaTypeList getConsumeMimeList() {
        return MimeHelper.createMediaTypes(c.getAnnotation(ConsumeMime.class));
    }
        
    private MediaTypeList getProduceMimeList() {
        return MimeHelper.createMediaTypes(c.getAnnotation(ProduceMime.class));
    }

    // Dispatcher 
    
    public boolean dispatch(DispatchContext context, Object node, String path) {
        for (final URITemplateDispatcher d : dispatchers) {
            if (context.matchLeftHandPath(d.getTemplate(), path)) {
                // Get the right hand side of the path
                path = context.getRightHandPath();
                if (path == null) {
                    // Redirect to path ending with a '/' if template
                    // ends in '/'
                    if (d.getTemplate().endsWithSlash())
                        return redirect(context);
                } else if (path.length() == 1) {
                    // No matchLeftHandPath if path ends in '/' but template does not
                    if (!d.getTemplate().endsWithSlash())
                        return false;
                    
                    // Consume the '/'
                    path = null;
                }
                
                return d.dispatch(context, node, path);
            }
        }
        
        return false;
    }
    
    private boolean redirect(DispatchContext context) {
        HttpRequestContext request = context.getHttpRequestContext();
        HttpResponseContext response = context.getHttpResponseContext();
        
        response.setResponse(ResponseBuilderImpl.
                temporaryRedirect(URI.create(request.getURIPath() + "/")).build());        
        return true;
    }
}
