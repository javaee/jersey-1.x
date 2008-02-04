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

package com.sun.ws.rest.impl.application;

import com.sun.ws.rest.spi.resource.Injectable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.ws.rs.WebApplicationException;
import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.impl.ThreadLocalHttpContext;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.model.RulesMap;
import com.sun.ws.rest.api.Responses;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.ResourceModelIssue;
import com.sun.ws.rest.impl.uri.PathPattern;
import com.sun.ws.rest.impl.uri.PathTemplate;
import com.sun.ws.rest.api.uri.UriTemplate;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.ws.rest.impl.modelapi.validation.BasicValidator;
import com.sun.ws.rest.impl.uri.rules.ResourceClassRule;
import com.sun.ws.rest.impl.uri.rules.RightHandPathRule;
import com.sun.ws.rest.impl.uri.rules.RootResourceClassesRule;
import com.sun.ws.rest.impl.uri.UriHelper;
import com.sun.ws.rest.impl.uri.rules.ResourceObjectRule;
import com.sun.ws.rest.impl.wadl.WadlFactory;
import com.sun.ws.rest.impl.wadl.WadlResource;
import com.sun.ws.rest.spi.container.ContainerRequest;
import com.sun.ws.rest.spi.container.ContainerResponse;
import com.sun.ws.rest.spi.container.MessageBodyContext;
import com.sun.ws.rest.spi.container.WebApplication;
import com.sun.ws.rest.spi.resource.ResourceProviderFactory;
import com.sun.ws.rest.spi.service.ComponentProvider;
import com.sun.ws.rest.spi.uri.rules.UriRule;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 * A Web application that contains a set of resources, each referenced by 
 * an absolute URI template.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class WebApplicationImpl implements WebApplication {
    private static final Logger LOGGER = Logger.getLogger(WebApplicationImpl.class.getName());

    private final ConcurrentMap<Class, ResourceClass> metaClassMap = 
            new ConcurrentHashMap<Class, ResourceClass>();

    private final ResourceProviderFactory resolverFactory;

    private final ThreadLocalHttpContext context;
    
    private final HttpHeaders httpHeadersProxy;
    
    private final UriInfo uriInfoProxy;
    
    private final Request requestProxy;
    
    private final Map<Type, Injectable> injectables;
            
    private boolean initiated;
    
    private ResourceConfig resourceConfig;
    
    private RootResourceClassesRule rootsRule;
            
    private MessageBodyContext bodyContext;
    
    private ComponentProvider provider;
    
    public WebApplicationImpl() {
        this.resolverFactory = ResourceProviderFactory.getInstance();

        this.context = new ThreadLocalHttpContext();
        
        InvocationHandler i = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(context.getHttpRequestContext(), args);
            }
        };
        this.httpHeadersProxy = createProxy(HttpHeaders.class, i);
        this.uriInfoProxy = createProxy(UriInfo.class, i);
        this.requestProxy = createProxy(Request.class, i);
        
        this.injectables = createInjectables();
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
            
            rmc = newResourceClass(getAbstractResource(c));
            metaClassMap.put(c, rmc);
            return rmc;
        }
    }
    
    public ResourceClass getResourceClass(AbstractResource ar) {
        ResourceClass rc = newResourceClass(ar);
        metaClassMap.put(ar.getResourceClass(), rc);
        return rc;
    }
        
    private ResourceClass newResourceClass(final AbstractResource ar) {
        assert null != ar;
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        boolean fatalIssueFound = false;
        for (ResourceModelIssue issue : validator.getIssueList()) {
            if (issue.isFatal()) {
                fatalIssueFound = true;
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.severe(issue.getMessage());
                }
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning(issue.getMessage());
                }
            }
        } // eof model validation
        if (fatalIssueFound) {
            LOGGER.severe(ImplMessages.FATAL_ISSUES_FOUND_AT_RES_CLASS(ar.getResourceClass().getName()));
            throw new ContainerException(ImplMessages.FATAL_ISSUES_FOUND_AT_RES_CLASS(ar.getResourceClass().getName()));
        }
        return new ResourceClass(resourceConfig, 
                getComponentProvider(), resolverFactory, ar);
    }
    
    private AbstractResource getAbstractResource(Class c) {
        return IntrospectionModeller.createResource(c);
    }
    
    /**
     * Inject resources onto fields of an object.
     * @param o the object
     */
    private void injectResources(Object o) {
        injectResources(o.getClass(), o);
    }
    
    private void injectResources(Class oClass, Object o) {
        while (oClass != null) {
            for (Field f : oClass.getDeclaredFields()) {            
                Injectable i = injectables.get(f.getGenericType());
                if (i != null)
                    i.inject(o, f);
            }
            oClass = oClass.getSuperclass();
        }
    }

    
    private final class AdaptingComponentProvider implements ComponentProvider {
        private final ComponentProvider cp;
        
        AdaptingComponentProvider(ComponentProvider cp) {
            this.cp = cp;
        }

        public Object getInstance(Scope scope, Class c) 
                throws InstantiationException, IllegalAccessException {
            Object o = cp.getInstance(scope,c);
            if (o == null)
                o = c.newInstance();
            injectResources(o);
            return o;
        }

        public Object getInstance(Scope scope, Constructor contructor, Object[] parameters) 
                throws InstantiationException, IllegalArgumentException, 
                IllegalAccessException, InvocationTargetException {
            Object o = cp.getInstance(scope, contructor, parameters);
            if (o == null)
                o = contructor.newInstance(parameters);
            injectResources(o);
            return o;
        }

        public void inject(Object instance) {
            cp.inject(instance);
            injectResources(instance);
        }
    }
    
    private final class DefaultComponentProvider implements ComponentProvider {
        public Object getInstance(Scope scope, Class c) 
                throws InstantiationException, IllegalAccessException {
            final Object o = c.newInstance();
            injectResources(o);
            return o;
        }

        public Object getInstance(Scope scope, Constructor contructor, Object[] parameters) 
                throws InstantiationException, IllegalArgumentException, 
                IllegalAccessException, InvocationTargetException {
            final Object o = contructor.newInstance(parameters);
            injectResources(o);
            return o;
        }

        public void inject(Object instance) {
            injectResources(instance);
        }
    }
    
    // WebApplication
            
    public void initiate(ResourceConfig resourceConfig) {
        initiate(resourceConfig, null);
    }
    
    public void initiate(ResourceConfig resourceConfig, ComponentProvider provider) {
        if (resourceConfig == null)
            throw new IllegalArgumentException("ResourceConfig instance MUST NOT be null");
        
        if (initiated)
            throw new ContainerException(ImplMessages.WEB_APP_ALREADY_INITIATED());
        this.initiated = true;
        
        this.resourceConfig = resourceConfig;
        verifyResourceConfig();

        
        // Allow injection of resource config
        addInjectable(ResourceConfig.class,
                new HttpContextInjectable<ResourceConfig>() {
                    public ResourceConfig getInjectableValue(HttpContext c) {
                        return WebApplicationImpl.this.resourceConfig;
                    }
                }
            );

        // Set up the component provider
        this.provider = (provider == null)
            ? new DefaultComponentProvider()
            : new AdaptingComponentProvider(provider);
            
        // Create the component provider cache
        ComponentProviderCache cpc = new ComponentProviderCache(this.provider, 
                resourceConfig.getProviderClasses());

        // Obtain all context resolvers
        ContextResolverFactory crf = new ContextResolverFactory(cpc);
        this.injectables.putAll(crf.getInjectables());

        // Obtain all message body readers/writers
        this.bodyContext = new MessageBodyFactory(cpc);
        
        // Obtain all root resources
        this.rootsRule = new RootResourceClassesRule(
            processRootResources(resourceConfig.getResourceClasses()));
    }

    
    public MessageBodyContext getMessageBodyContext() {
        return bodyContext;
    }

    public ComponentProvider getComponentProvider() {
        return provider;
    }
    
    public void handleRequest(ContainerRequest request, ContainerResponse response) {
        final WebApplicationContext localContext = new WebApplicationContext(this, request, response);        
        context.set(localContext);
        
        if (resourceConfig.getFeature(ResourceConfig.FEATURE_NORMALIZE_URI)) {
            final URI uri = request.getRequestUri();
            final URI normalizedUri = UriHelper.normalize(uri, !resourceConfig.getFeature(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH));

            if (uri != normalizedUri) {
                response.setResponse(Response.temporaryRedirect(normalizedUri).build());
                return;
            }
        }

        /**
         * The matching algorithm currently works from an absolute path.
         * The path is required to be in encoded form.
         */
        StringBuilder path = new StringBuilder();
        path.append("/").append(request.getPath(false));

        if (resourceConfig.getFeature(ResourceConfig.FEATURE_IGNORE_MATRIX_PARAMS))
            // TODO check for annotation on resource
            // Need to support overriding functionality on resource        
            path = stripMatrixParams(path);

        try {
            if (!rootsRule.accept(path, null, localContext)) {
                // Resource was not found
                response.setResponse(Responses.notFound());                
            }
        } catch (WebApplicationException e) {
            onExceptionWithWebApplication(e, response);
        }
    }
    
    public void addInjectable(Type fieldType, Injectable injectable) {
        injectables.put(fieldType, injectable);
    }
    
    
    // 

    private void verifyResourceConfig() {
        Iterator<Class> i = resourceConfig.getProviderClasses().iterator();
        while (i.hasNext()) {
            Class<?> pc = i.next();
            if (!pc.isAnnotationPresent(Provider.class)) {
                LOGGER.warning("The class, " + pc + ", registered as a provider class " + 
                        "of the ResourceConfig is not annotationed with " + Provider.class +  
                        ". This class will be ignored");
                i.remove();
            }
        }
    }
    
    private RulesMap<UriRule> processRootResources(Set<Class> classes) {
        if (classes.isEmpty()) {
            LOGGER.severe(ImplMessages.NO_ROOT_RES_IN_RES_CFG());
            throw new ContainerException(ImplMessages.NO_ROOT_RES_IN_RES_CFG());            
        }
        
        RulesMap<UriRule> rulesMap = new RulesMap<UriRule>();
        
        Set<AbstractResource> rootResources = new HashSet<AbstractResource>();
        for (Class<?> c : classes) {
            AbstractResource ar = getAbstractResource(c);
            if (!ar.isRootResource()) {
                LOGGER.warning("The class, " + c + ", registered as a root resource class " + 
                        "of the ResourceConfig is not a root resource class" +  
                        ". This class will be ignored");
                continue;   
            }            
            // TODO this should be moved to the validation
            // as such classes are not root resource classes
            int modifiers = c.getModifiers();
            if (Modifier.isAbstract(modifiers)) {
                LOGGER.warning("The abstract class, " + c + ", registered as a root resource class " + 
                        "of the ResourceConfig cannot be instantiated" +  
                        ". This class will be ignored");
                continue;                   
            } else if (Modifier.isInterface(modifiers)) {
                LOGGER.warning("The interface, " + c + ", registered as a root resource class " + 
                        "of the ResourceConfig cannot be instantiated" +  
                        ". This interface will be ignored");
                continue;                                   
            }
            
            ResourceClass r = getResourceClass(ar);
            rootResources.add(r.resource);
            
            UriTemplate t = new PathTemplate(
                    r.resource.getUriPath().getValue(),
                    r.resource.getUriPath().isEncode());
            
            PathPattern p = new PathPattern(t, r.hasSubResources);
                    
            rulesMap.put(p, new RightHandPathRule(t.endsWithSlash(),
                    new ResourceClassRule(t.getTemplateVariables(), c)));
        }
        
        createWadlResource(rootResources, rulesMap);
        
        return rulesMap;
    }

    private void createWadlResource(Set<AbstractResource> rootResources, 
            RulesMap<UriRule> rulesMap) {
        // TODO get ResourceConfig to check the WADL generation feature
        
        Object wr = WadlFactory.createWadlResource(rootResources);
        if (wr == null) return;
        
        ResourceClass r = getResourceClass(WadlResource.class);
        UriTemplate t = new PathTemplate(
                "application.wadl",
                false);
        PathPattern p = new PathPattern(t, r.hasSubResources);
        
        rulesMap.put(p, new RightHandPathRule(false,
                new ResourceObjectRule(t.getTemplateVariables(), wr)));        
    }
        
    /**
     * Strip the matrix parameters from a path
     */
    private StringBuilder stripMatrixParams(StringBuilder path) {        
        int e = path.indexOf(";");
        if (e == -1)
            return path;

        int s = 0;
        StringBuilder sb = new StringBuilder();
        do {
            // Append everything up to but not including the ';'
            sb.append(path, s, e);

            // Skip everything up to but not including the '/'
            s = path.indexOf("/", e + 1);
            if (s == -1) {
                break;
            }
            e = path.indexOf(";", s);
        } while(e != -1);
        
        if (s != -1) {
            // Append any remaining characters
            sb.append(path, s, path.length());
        }
        
        return sb;
    }  
        
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> c, InvocationHandler i) {
        return (T)Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            new Class[] { c },
            i);
    }
    
    private abstract class HttpContextInjectable<V> extends Injectable<HttpContext, V> {
        public Class<HttpContext> getAnnotationClass() {
            return HttpContext.class;
        }
    }
    
    private Map<Type, Injectable> createInjectables() {
        Map<Type, Injectable> is = new HashMap<Type, Injectable>();
                
        is.put(MessageBodyContext.class,
                new HttpContextInjectable<MessageBodyContext>() {
                    public MessageBodyContext getInjectableValue(HttpContext c) {
                        return bodyContext;
                    }
                }
            );
            
        is.put(HttpContextAccess.class,
                new HttpContextInjectable<HttpContextAccess>() {
                    public HttpContextAccess getInjectableValue(HttpContext c) {
                        return context;
                    }
                }
            );
        
        is.put(HttpHeaders.class,
                new HttpContextInjectable<HttpHeaders>() {
                    public HttpHeaders getInjectableValue(HttpContext c) {
                        return httpHeadersProxy;
                    }
                }
            );
            
        is.put(UriInfo.class,
                new HttpContextInjectable<UriInfo>() {
                    public UriInfo getInjectableValue(HttpContext c) {
                        return uriInfoProxy;
                    }
                }
            );
            
        is.put(Request.class,
                new HttpContextInjectable<Request>() {
                    public Request getInjectableValue(HttpContext c) {
                        return requestProxy;
                    }
                }
            );

        return is;
    }


    private static void onExceptionWithWebApplication(WebApplicationException e, 
        HttpResponseContext response) {
        Response r = e.getResponse();
        
        // Log the stack trace
        if (r.getStatus() >= 500)
            e.printStackTrace();
        
        if (r.getStatus() >= 500 && r.getEntity() == null) {
            // Write out the exception to a string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();

            r = Response.ok(sw.toString(), "text/plain").build();
        }
        response.setResponse(r);
    }
}
