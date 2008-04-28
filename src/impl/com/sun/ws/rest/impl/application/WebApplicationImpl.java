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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWorkers;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.api.Responses;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.ThreadLocalHttpContext;
import com.sun.ws.rest.impl.model.ResourceClass;
import com.sun.ws.rest.impl.model.RulesMap;
import com.sun.ws.rest.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.ws.rest.impl.modelapi.validation.BasicValidator;
import com.sun.ws.rest.impl.template.TemplateFactory;
import com.sun.ws.rest.impl.uri.PathPattern;
import com.sun.ws.rest.impl.uri.PathTemplate;
import com.sun.ws.rest.impl.uri.UriHelper;
import com.sun.ws.rest.impl.uri.rules.ResourceClassRule;
import com.sun.ws.rest.impl.uri.rules.ResourceObjectRule;
import com.sun.ws.rest.impl.uri.rules.RightHandPathRule;
import com.sun.ws.rest.impl.uri.rules.RootResourceClassesRule;
import com.sun.ws.rest.impl.wadl.WadlFactory;
import com.sun.ws.rest.impl.wadl.WadlResource;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.MessageBodyContext;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.resource.Injectable;
import com.sun.jersey.spi.resource.Inject;
import com.sun.jersey.spi.resource.ResourceProviderFactory;
import com.sun.jersey.spi.resource.TypeInjectable;
import com.sun.jersey.spi.service.ComponentProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import com.sun.jersey.spi.template.TemplateContext;
import com.sun.jersey.spi.uri.rules.UriRule;

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
    
    private final SecurityContext securityContextProxy; 

    private final Map<Class<? extends Annotation>, Injectable<? extends Annotation, ?>> 
            annotationInjectables;
    
    private final Map<Type, TypeInjectable> typeInjectables;
            
    private boolean initiated;
    
    private ResourceConfig resourceConfig;
    
    private RootResourceClassesRule rootsRule;
            
    private MessageBodyFactory bodyFactory;
    
    private ComponentProvider provider;
    
    private TemplateContext templateContext;

    private ResourceContext resourceContext;
    
    public WebApplicationImpl() {
        this.resolverFactory = ResourceProviderFactory.getInstance();

        this.context = new ThreadLocalHttpContext();
        
        InvocationHandler requestHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(context.getRequest(), args);
            }
        };
        this.httpHeadersProxy = createProxy(HttpHeaders.class, requestHandler);
        this.requestProxy = createProxy(Request.class, requestHandler);
        this.securityContextProxy = createProxy(SecurityContext.class, requestHandler);        
        InvocationHandler uriInfoHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(context.getUriInfo(), args);
            }
        };
        this.uriInfoProxy = createProxy(UriInfo.class, uriInfoHandler);
        
        this.annotationInjectables = createAnnotationInjectables();
        this.typeInjectables = createTypeInjectables();
    }
    
    @Override
    public WebApplication clone() {
        WebApplicationImpl wa = new WebApplicationImpl();
        if (provider instanceof DefaultComponentProvider) {
            wa.initiate(resourceConfig, null);
        } else {
            AdaptingComponentProvider acp = (AdaptingComponentProvider)provider;
            wa.initiate(resourceConfig, acp.getAdaptedComponentProvider());            
        }
        
        return wa;
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
    
    private void injectResources(Class oClass, final Object o) {
        while (oClass != null) {
            for (final Field f : oClass.getDeclaredFields()) {    
                /* first try to inject using type injectables
                 */
                final TypeInjectable i = typeInjectables.get(f.getGenericType());
                if (i != null) {
                    i.inject(o, f);
                }
                else {
                    /* if there was no injectable registered for the field type try
                     * annotation based injection
                     */
                    final Annotation[] annotations = f.getAnnotations();
                    if ( annotations != null && annotations.length > 0 ) {
                        for ( Annotation annotation : annotations ) {
                            final Injectable injectable = annotationInjectables.get(
                                    annotation.annotationType());
                            if (injectable != null) {
                                injectable.inject(o, f);
                            }
                        }
                    }
                }
            }
            oClass = oClass.getSuperclass();
        }
    }

    
    private final class AdaptingComponentProvider implements ComponentProvider {
        private final ComponentProvider cp;
        
        AdaptingComponentProvider(ComponentProvider cp) {
            this.cp = cp;
        }

        public ComponentProvider getAdaptedComponentProvider() {
            return cp;
        }
        
        //
        
        public <T> T getInstance(Scope scope, Class<T> c) 
                throws InstantiationException, IllegalAccessException {
            T o = cp.getInstance(scope,c);
            if (o == null) {
                o = c.newInstance();
                injectResources(o);
            } else {
                injectResources(cp.getInjectableInstance(o));
            }
            return o;
        }

        public <T> T getInstance(Scope scope, Constructor<T> contructor, Object[] parameters) 
                throws InstantiationException, IllegalArgumentException, 
                IllegalAccessException, InvocationTargetException {
            T o = cp.getInstance(scope, contructor, parameters);
            if (o == null) {
                o = contructor.newInstance(parameters);
                injectResources(o);
            } else {
                injectResources(cp.getInjectableInstance(o));
            }
            return o;
        }

        public <T> T getInjectableInstance(T instance) {
            return cp.getInjectableInstance(instance);
        }
        
        public void inject(Object instance) {
            cp.inject(instance);
            injectResources(cp.getInjectableInstance(instance));
        }
    }
    
    private final class DefaultComponentProvider implements ComponentProvider {
        public <T> T getInstance(Scope scope, Class<T> c) 
                throws InstantiationException, IllegalAccessException {
            final T o = c.newInstance();
            injectResources(o);
            return o;
        }

        public <T> T getInstance(Scope scope, Constructor<T> contructor, Object[] parameters) 
                throws InstantiationException, IllegalArgumentException, 
                IllegalAccessException, InvocationTargetException {
            final T o = contructor.newInstance(parameters);
            injectResources(o);
            return o;
        }

        public <T> T getInjectableInstance(T instance) {
            return instance;
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
                new ContextInjectable<ResourceConfig>() {
                    public ResourceConfig getInjectableValue(Context c) {
                        return WebApplicationImpl.this.resourceConfig;
                    }
                }
            );

        this.resourceContext = new ResourceContext() {
            public <T> T getResource(Class<T> c) {
                final ResourceClass rc = getResourceClass(c);
                if (rc == null) {
                    LOGGER.severe("No resource class found for class " + c.getName());
                    throw new ContainerException("No resource class found for class " + c.getName());
                }
                final Object instance = rc.resolver.getInstance(
                        WebApplicationImpl.this.provider, context);
                return instance != null ? c.cast(instance) : null;
            }            
        };
        
        // Allow injection of resource context
        addInjectable(ResourceContext.class, new ContextInjectable<ResourceContext>() {
            @Override
            public ResourceContext getInjectableValue(Context a) {
                return WebApplicationImpl.this.resourceContext;
            }            
        });

        // Set up the component provider
        this.provider = (provider == null)
            ? new DefaultComponentProvider()
            : new AdaptingComponentProvider(provider);
            
        // Create the component provider cache
        ComponentProviderCache cpc = new ComponentProviderCache(this.provider, 
                resourceConfig.getProviderClasses());

        // Obtain all context resolvers
        ContextResolverFactory crf = new ContextResolverFactory(cpc);
        this.typeInjectables.putAll(crf.getInjectables());

        // Obtain all the templates
        this.templateContext = new TemplateFactory(cpc);
        // Allow injection of template context
        addInjectable(TemplateContext.class,
                new ContextInjectable<TemplateContext>() {
                    public TemplateContext getInjectableValue(Context c) {
                        return templateContext;
                    }
                }
            );
                    
        // Obtain all message body readers/writers
        this.bodyFactory = new MessageBodyFactory(cpc);
        
        // Obtain all root resources
        this.rootsRule = new RootResourceClassesRule(
            processRootResources(resourceConfig.getResourceClasses()));
    }

    
    public MessageBodyContext getMessageBodyContext() {
        return bodyFactory;
    }

    public ComponentProvider getComponentProvider() {
        return provider;
    }
    
    public void handleRequest(ContainerRequest request, ContainerResponse response) {
        final WebApplicationContext localContext = new WebApplicationContext(this, request, response);        
        context.set(localContext);
        
        if (resourceConfig.getFeature(ResourceConfig.FEATURE_NORMALIZE_URI)) {
            final URI uri = request.getRequestUri();
            final URI normalizedUri = UriHelper.normalize(uri, 
                    !resourceConfig.getFeature(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH));

            if (uri != normalizedUri && 
                    resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT)) {
                response.setResponse(Response.temporaryRedirect(normalizedUri).build());
                return;
            }
        }

        /**
         * The matching algorithm currently works from an absolute path.
         * The path is required to be in encoded form.
         */
        StringBuilder path = new StringBuilder();
        path.append("/").append(localContext.getPath(false));

        if (!resourceConfig.getFeature(ResourceConfig.FEATURE_MATCH_MATRIX_PARAMS))
            path = stripMatrixParams(path);

        // If there are URI conneg extensions for media and language
        if (!resourceConfig.getMediaTypeMappings().isEmpty() || 
                !resourceConfig.getLanguageMappings().isEmpty()) {
            uriConneg(path, request);
        }
        
        try {
            if (!rootsRule.accept(path, null, localContext)) {
                // Resource was not found
                response.setResponse(Responses.notFound().build());                
            }
        } catch (WebApplicationException e) {
            onExceptionWithWebApplication(e, response);
        }
    }
    
    public void addInjectable(Type fieldType, TypeInjectable injectable) {
        typeInjectables.put(fieldType, injectable);
    }

    public <T extends Annotation, V> void addInjectable(Injectable<T, V> injectable) {
        if (injectable.getAnnotationClass() == null) {
            throw new IllegalArgumentException("The annotation class must not be null.");
        }
        annotationInjectables.put(injectable.getAnnotationClass(), injectable);
    }
    
    public HttpContext getThreadLocalHttpContext() {
        return context;
    }
    
    // 

    private void verifyResourceConfig() {
        Iterator<Class<?>> i = resourceConfig.getProviderClasses().iterator();
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
    
    private RulesMap<UriRule> processRootResources(Set<Class<?>> classes) {
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
            if (Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
                LOGGER.warning("The " + c + ", registered as a root resource class " + 
                        "of the ResourceConfig cannot be instantiated" +  
                        ". This class will be ignored");
                continue;                   
            } else if (Modifier.isInterface(modifiers)) {
                LOGGER.warning("The " + c + ", registered as a root resource class " + 
                        "of the ResourceConfig cannot be instantiated" +  
                        ". This interface will be ignored");
                continue;                                   
            }
            
            ResourceClass r = getResourceClass(ar);
            rootResources.add(r.resource);
            
            UriTemplate t = new PathTemplate(
                    r.resource.getUriPath().getValue(),
                    r.resource.getUriPath().isEncode());
            
            PathPattern p = new PathPattern(t, r.resource.getUriPath().isLimited());
                    
            rulesMap.put(p, new RightHandPathRule(
                    resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                    t.endsWithSlash(),
                    new ResourceClassRule(t, c)));
        }
        
        createWadlResource(rootResources, rulesMap);
        
        return rulesMap;
    }

    private void createWadlResource(Set<AbstractResource> rootResources, 
            RulesMap<UriRule> rulesMap) {
        // TODO get ResourceConfig to check the WADL generation feature
        
        Object wr = WadlFactory.createWadlResource(rootResources);
        if (wr == null) return;
        
        // Preload wadl resource runtime meta data
        getResourceClass(WadlResource.class);
        UriTemplate t = new PathTemplate(
                "application.wadl",
                false);
        PathPattern p = new PathPattern(t, false);
        
        rulesMap.put(p, new RightHandPathRule(
                resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),                
                false,
                new ResourceObjectRule(t, wr)));        
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
        
    /**
     * 
     */
    private void uriConneg(StringBuilder path, ContainerRequest request) {
        int si = path.lastIndexOf("/");
        // Path ends in slash
        if (si == path.length() - 1) {
            // Find the next slash
            si = path.lastIndexOf("/", si - 1);
        }
        // If no slash that set to start of path
        if (si == -1) si = 0;
        
        MediaType accept = null;
        for (Map.Entry<String, MediaType> e : resourceConfig.getMediaTypeMappings().entrySet()) {
            int i = path.indexOf(e.getKey(), si);
            if (i > 0 && path.charAt(i - 1) == '.') {
                accept = e.getValue();
                path.delete(i - 1, i + e.getKey().length());
            }
        }
        if (accept != null) {
            // TODO do not modify request headers
            MultivaluedMap<String, String> h = request.getRequestHeaders();            
            h.putSingle("Accept", accept.toString());
        }
        
        String acceptLanguage = null;
        for (Map.Entry<String, String> e : resourceConfig.getLanguageMappings().entrySet()) {
            int i = path.indexOf(e.getKey(), si);
            if (i > 0 && path.charAt(i - 1) == '.') {
                acceptLanguage = e.getValue();
                path.delete(i - 1, i + e.getKey().length());
            }
        }
        if (acceptLanguage != null) {
            // TODO do not modify request headers
            MultivaluedMap<String, String> h = request.getRequestHeaders();            
            h.putSingle("Accept-Language", acceptLanguage);
        }        
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> c, InvocationHandler i) {
        return (T)Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            new Class[] { c },
            i);
    }
    
    private Map<Class<? extends Annotation>, Injectable<? extends Annotation, ?>>
            createAnnotationInjectables() {
        final Map<Class<? extends Annotation>, Injectable<? extends Annotation, ?>> result = 
                new HashMap<Class<? extends Annotation>, Injectable<? extends Annotation, ?>>();
        
        /* create an injectable for @Inject, that injects instances
         * pulled from the component provider
         */
        result.put(Inject.class, new Injectable<Inject, Object>() {
            @Override
            public Object getInjectableValue(Object o, Field f, Inject a) {
                try {
                    return WebApplicationImpl.this.provider.getInstance(
                            Scope.Undefined, f.getType());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not get instance from component provider for type " + 
                            f.getType() +
                            " when trying to inject this on " + o.getClass().getName(), e);
                    throw new ContainerException("Could not get instance from component provider for type " + 
                            f.getType(), e );
                }
            }

            @Override
            public Class<Inject> getAnnotationClass() {
                return Inject.class;
            }
        });
        
        return result;
    }
    
    private abstract class ContextInjectable<V> extends TypeInjectable<Context, V> {
        public Class<Context> getAnnotationClass() {
            return Context.class;
        }
    }
    
    private Map<Type, TypeInjectable> createTypeInjectables() {
        Map<Type, TypeInjectable> is = new HashMap<Type, TypeInjectable>();
                
        is.put(MessageBodyContext.class,
                new ContextInjectable<MessageBodyContext>() {
                    public MessageBodyContext getInjectableValue(Context c) {
                        return bodyFactory;
                    }
                }
            );
            
        is.put(MessageBodyWorkers.class,
                new ContextInjectable<MessageBodyWorkers>() {
                    public MessageBodyWorkers getInjectableValue(Context c) {
                        return bodyFactory;
                    }
                }
            );
            
        is.put(HttpContext.class,
                new ContextInjectable<HttpContext>() {
                    public HttpContext getInjectableValue(Context c) {
                        return context;
                    }
                }
            );
        
        is.put(HttpHeaders.class,
                new ContextInjectable<HttpHeaders>() {
                    public HttpHeaders getInjectableValue(Context c) {
                        return httpHeadersProxy;
                    }
                }
            );
            
        is.put(UriInfo.class,
                new ContextInjectable<UriInfo>() {
                    public UriInfo getInjectableValue(Context c) {
                        return uriInfoProxy;
                    }
                }
            );
            
        is.put(Request.class,
                new ContextInjectable<Request>() {
                    public Request getInjectableValue(Context c) {
                        return requestProxy;
                    }
                }
            );

        is.put(SecurityContext.class,
                new ContextInjectable<SecurityContext>() {
                    public SecurityContext getInjectableValue(Context c) {
                        return securityContextProxy;
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

            r = Response.status(r.getStatus()).entity(sw.toString()).
                    type("text/plain").build();
        }
        response.setResponse(r);
    }
}
