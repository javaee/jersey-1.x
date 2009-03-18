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
package com.sun.jersey.server.impl.application;

import com.sun.jersey.server.impl.container.filter.FilterFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.ws.rs.ext.ExceptionMapper;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.core.spi.component.ComponentInjector;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCProviderFactory;
import com.sun.jersey.core.spi.component.ProviderFactory;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.server.impl.ThreadLocalHttpContext;
import com.sun.jersey.server.impl.model.ResourceClass;
import com.sun.jersey.server.impl.model.RulesMap;
import com.sun.jersey.server.impl.model.parameter.CookieParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.HeaderParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.HttpContextInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.MatrixParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.PathParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.QueryParamInjectableProvider;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.server.impl.modelapi.validation.BasicValidator;
import com.sun.jersey.server.impl.template.TemplateFactory;
import com.sun.jersey.server.impl.uri.PathPattern;
import com.sun.jersey.server.impl.uri.PathTemplate;
import com.sun.jersey.server.impl.uri.rules.ResourceClassRule;
import com.sun.jersey.server.impl.uri.rules.ResourceObjectRule;
import com.sun.jersey.server.impl.uri.rules.RightHandPathRule;
import com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule;
import com.sun.jersey.server.impl.wadl.WadlFactory;
import com.sun.jersey.server.impl.wadl.WadlResource;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderFactory;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.core.spi.factory.ContextResolverFactory;
import com.sun.jersey.core.spi.factory.MessageBodyFactory;
import com.sun.jersey.server.impl.component.IoCResourceFactory;
import com.sun.jersey.server.impl.component.ResourceFactory;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import com.sun.jersey.spi.inject.InjectableProviderContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorFactory;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.StringReaderFactory;
import com.sun.jersey.server.impl.resource.PerRequestFactory;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.spi.StringReaderWorkers;
import com.sun.jersey.spi.template.TemplateContext;
import com.sun.jersey.spi.uri.rules.UriRule;
import java.lang.annotation.Annotation;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

/**
 * A Web application that contains a set of resources, each referenced by 
 * an absolute URI template.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class WebApplicationImpl implements WebApplication {

    private static final Logger LOGGER = Logger.getLogger(WebApplicationImpl.class.getName());
    
    private final Map<Class, AbstractResource> abstractResourceMap =
            new HashMap<Class, AbstractResource>();

    private final ConcurrentMap<Class, ResourceClass> metaClassMap =
            new ConcurrentHashMap<Class, ResourceClass>();
    
    private final ThreadLocalHttpContext context;
    
    private final CloseableServiceFactory closeableFactory;

    private boolean initiated;
    
    private ResourceConfig resourceConfig;
    
    private RootResourceClassesRule rootsRule;
    
    private ServerInjectableProviderFactory injectableFactory;

    private ProviderFactory cpFactory;

    private ResourceFactory rcpFactory;

    private IoCComponentProviderFactory provider;

    private MessageBodyFactory bodyFactory;

    private StringReaderFactory stringReaderFactory;
    
    private TemplateContext templateContext;
    
    private ExceptionMapperFactory exceptionFactory;
    
    private ResourceMethodDispatcherFactory dispatcherFactory;
    
    private ResourceContext resourceContext;

    private FilterFactory filterFactory;

    private WadlFactory wadlFactory;

    public WebApplicationImpl() {
        this.context = new ThreadLocalHttpContext();

        InvocationHandler requestHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {                
                return method.invoke(context.getRequest(), args);
            }
        };
        InvocationHandler uriInfoHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(context.getUriInfo(), args);
            }
        };
        
        // Create injectable provider factory
        this.injectableFactory = new ServerInjectableProviderFactory();
        injectableFactory.add(new ContextInjectableProvider<InjectableProviderContext>(
                InjectableProviderContext.class, injectableFactory));
        injectableFactory.add(new ContextInjectableProvider<ServerInjectableProviderContext>(
                ServerInjectableProviderContext.class, injectableFactory));
        
        // Add proxied injectables
        final Map<Type, Object> m = new HashMap<Type, Object>();
        m.put(HttpContext.class, context);
        m.put(HttpHeaders.class, createProxy(HttpHeaders.class, requestHandler));
        m.put(UriInfo.class, createProxy(UriInfo.class, uriInfoHandler));
        m.put(ExtendedUriInfo.class, createProxy(ExtendedUriInfo.class, uriInfoHandler));
        m.put(Request.class, createProxy(Request.class, requestHandler));
        m.put(SecurityContext.class, createProxy(SecurityContext.class, requestHandler));        
        injectableFactory.add(new InjectableProvider<Context, Type>() {
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }
            
            public Injectable getInjectable(ComponentContext ic, Context a, Type c) {
                final Object o = m.get(c);
                if (o != null) {
                    return new Injectable() {
                        public Object getValue() {
                            return o;
                        }
                    };
                } else
                    return null;
            }
        });

        closeableFactory = new CloseableServiceFactory(context);
        injectableFactory.add(closeableFactory);
    }

    private class ComponentProcessorImpl implements IoCComponentProcessor {
        private final ResourceComponentInjector rci;

        ComponentProcessorImpl(ComponentScope s, AbstractResource resource) {
            this.rci = new ResourceComponentInjector(injectableFactory, s, resource);
        }

        public void preConstruct() {
        }

        public void postConstruct(Object o) {
            rci.inject(context.get(), o);
        }
    }

    private class ComponentProcessorFactoryImpl implements IoCComponentProcessorFactory {
        private final ConcurrentMap<Class, IoCComponentProcessor> componentProcessorMap =
                new ConcurrentHashMap<Class, IoCComponentProcessor>();

        public IoCComponentProcessor get(Class c, ComponentScope scope) {
            IoCComponentProcessor cp = componentProcessorMap.get(c);
            if (cp != null) {
                return cp;
            }

            synchronized (metaClassMap) {
                cp = componentProcessorMap.get(c);
                if (cp != null) {
                    return cp;
                }

                cp = new ComponentProcessorImpl(scope, getAbstractResource(c));
                componentProcessorMap.put(c, cp);
            }
            return cp;
        }
    }
 
    @Override
    public WebApplication clone() {
        WebApplicationImpl wa = new WebApplicationImpl();
        wa.initiate(resourceConfig, provider);
        return wa;
    }

    public ResourceClass getResourceClass(Class c) {
        assert c != null;

        // Try the non-blocking read, the most common opertaion
        ResourceClass rc = metaClassMap.get(c);
        if (rc != null) {
            return rc;
        }

        // ResourceClass is not present use a synchronized block
        // to ensure that only one ResourceClass instance is created
        // and put to the map
        synchronized (metaClassMap) {
            // One or more threads may have been blocking on the synchronized
            // block, re-check the map
            rc = metaClassMap.get(c);
            if (rc != null) {
                return rc;
            }

            rc = newResourceClass(getAbstractResource(c));
            metaClassMap.put(c, rc);
            rc.init(rcpFactory);
        }
        return rc;
    }

    private ResourceClass getResourceClass(AbstractResource ar) {
        ResourceClass rc = newResourceClass(ar);
        metaClassMap.put(ar.getResourceClass(), rc);
        rc.init(rcpFactory);
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
        return new ResourceClass(
                resourceConfig,
                dispatcherFactory,
                injectableFactory,
                filterFactory,
                wadlFactory,
                ar);
    }

    private AbstractResource getAbstractResource(Object o) {
        return getAbstractResource(o.getClass());
    }
    
    private AbstractResource getAbstractResource(Class c) {
        AbstractResource ar = abstractResourceMap.get(c);
        if (ar == null) {
            ar = IntrospectionModeller.createResource(c);
            abstractResourceMap.put(c, ar);
        }

        return ar;
    }

    private static class ContextInjectableProvider<T> extends
            SingletonTypeInjectableProvider<Context, T> {
        ContextInjectableProvider(Type type, T instance) {
            super(type, instance);
        }
    }

    public boolean isInitiated () {
        return initiated;
    }

    public void initiate(ResourceConfig resourceConfig) {
        initiate(resourceConfig, null);
    }
    
    public void initiate(ResourceConfig resourceConfig, IoCComponentProviderFactory _provider) {
        if (resourceConfig == null) {
            throw new IllegalArgumentException("ResourceConfig instance MUST NOT be null");
        }

        if (initiated) {
            throw new ContainerException(ImplMessages.WEB_APP_ALREADY_INITIATED());
        }
        this.initiated = true;

        // Validate the resource config
        resourceConfig.validate();

        // Check the resource configuration
        this.resourceConfig = resourceConfig;

        this.provider = _provider;
        if (_provider != null) {
            if (_provider instanceof IoCComponentProcessorFactoryInitializer) {
                IoCComponentProcessorFactoryInitializer i = (IoCComponentProcessorFactoryInitializer)_provider;
                i.init(new ComponentProcessorFactoryImpl());
            }
        }
        
        // Set up the component provider factory to be
        // used with non-resource class components
        this.cpFactory = (_provider == null)
                ? new ProviderFactory(injectableFactory)
                : new IoCProviderFactory(injectableFactory, _provider);

        // Set up the resource component provider factory
        this.rcpFactory = (_provider == null)
                ? new ResourceFactory(this.resourceConfig, this.injectableFactory)
                : new IoCResourceFactory(this.resourceConfig, this.injectableFactory, _provider);
                
        this.resourceContext = new ResourceContext() {
            public <T> T getResource(Class<T> c) {
                final ResourceClass rc = getResourceClass(c);
                if (rc == null) {
                    LOGGER.severe("No resource class found for class " + c.getName());
                    throw new ContainerException("No resource class found for class " + c.getName());
                }
                final Object instance = rc.rcProvider.getInstance(context);
                return instance != null ? c.cast(instance) : null;
            }
        };

        ProviderServices providerServices = new ProviderServices(
                this.injectableFactory,
                this.cpFactory,
                resourceConfig.getProviderClasses(),
                resourceConfig.getProviderSingletons());

        // Add injectable provider for @Inject
        injectableFactory.add(
            new InjectableProvider<Inject, Type>() {
                    public ComponentScope getScope() {
                        return ComponentScope.Undefined;
                    }

                    @SuppressWarnings("unchecked")
                    public Injectable<Object> getInjectable(ComponentContext ic, Inject a, final Type c) {
                        if (!(c instanceof Class))
                            return null;

                        if (provider == null)
                            return null;
                        
                        final IoCComponentProvider p = provider.getComponentProvider(ic, (Class)c);
                        return new Injectable<Object>() {
                            public Object getValue() {
                                try {
                                    return p.getInstance();
                                } catch (Exception e) {
                                    LOGGER.log(Level.SEVERE, "Could not get instance from IoC component provider for type " +
                                            c, e);
                                    throw new ContainerException("Could not get instance from IoC component provider for type " +
                                            c, e);
                                }
                            }
                        };
                    }

                });
        
        // Allow injection of resource config
        injectableFactory.add(new ContextInjectableProvider<ResourceConfig>(
                ResourceConfig.class, resourceConfig));

        // Allow injection of resource context
        injectableFactory.add(new ContextInjectableProvider<ResourceContext>(
                ResourceContext.class, resourceContext));
        
        injectableFactory.configure(providerServices);
        
        // Obtain all context resolvers
        final ContextResolverFactory crf = new ContextResolverFactory(
                providerServices,
                injectableFactory);
        
        // Obtain all the templates
        this.templateContext = new TemplateFactory(providerServices);
        // Allow injection of template context
        injectableFactory.add(new ContextInjectableProvider<TemplateContext>(
                TemplateContext.class, templateContext));

        // Obtain all the exception mappers
        this.exceptionFactory = new ExceptionMapperFactory(providerServices);
        
        // Obtain all resource method dispatchers
        this.dispatcherFactory = new ResourceMethodDispatcherFactory(providerServices);
        
        // Obtain all message body readers/writers
        this.bodyFactory = new MessageBodyFactory(providerServices);
        injectableFactory.add(
                new ContextInjectableProvider<MessageBodyWorkers>(
                MessageBodyWorkers.class, bodyFactory));
        
        // Injection of Providers
        Providers p = new Providers() {
            public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t, 
                    Annotation[] as, MediaType m) {
                return bodyFactory.getMessageBodyReader(c, t, as, m);
            }

            public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c, Type t, 
                    Annotation[] as, MediaType m) {
                return bodyFactory.getMessageBodyWriter(c, t, as, m);
            }

            public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> c) {
                if (Throwable.class.isAssignableFrom(c)) 
                   return exceptionFactory.find((Class<Throwable>)c);
                else
                    return null;
            }

            public <T> ContextResolver<T> getContextResolver(Class<T> ct, MediaType m) {
                return crf.resolve(ct, m);
            }
        };
        injectableFactory.add(
                new ContextInjectableProvider<Providers>(
                Providers.class, p));
        
        // Initiate message body readers/writers
        bodyFactory.init();

        
        // Obtain all String readers
        this.stringReaderFactory = new StringReaderFactory();
        injectableFactory.add(
                new ContextInjectableProvider<StringReaderWorkers>(
                StringReaderWorkers.class, stringReaderFactory));
        stringReaderFactory.init(providerServices);

        MultivaluedParameterExtractorProvider mpep =
                new MultivaluedParameterExtractorFactory(stringReaderFactory);
        // Add the multi-valued parameter extractor provider
        injectableFactory.add(
                new ContextInjectableProvider<MultivaluedParameterExtractorProvider>(
                MultivaluedParameterExtractorProvider.class, mpep));


        // Add per-request-based injectable providers
        injectableFactory.add(new CookieParamInjectableProvider(mpep));
        injectableFactory.add(new HeaderParamInjectableProvider(mpep));
        injectableFactory.add(new HttpContextInjectableProvider());
        injectableFactory.add(new MatrixParamInjectableProvider(mpep));
        injectableFactory.add(new PathParamInjectableProvider(mpep));
        injectableFactory.add(new QueryParamInjectableProvider(mpep));

        // Intiate filters
        filterFactory = new FilterFactory(providerServices, resourceConfig);

        // Inject on all components
        cpFactory.injectOnAllComponents();
        cpFactory.injectOnProviderInstances(resourceConfig.getProviderSingletons());
        
        this.wadlFactory = new WadlFactory(resourceConfig);
        
        // Obtain all root resources
        this.rootsRule = new RootResourceClassesRule(processRootResources());
    }

    public MessageBodyWorkers getMessageBodyWorkers() {
        return bodyFactory;
    }

    public void handleRequest(ContainerRequest request, ContainerResponseWriter responseWriter) 
            throws IOException {
        final ContainerResponse response = new ContainerResponse(
                this,
                request,
                responseWriter);
        handleRequest(request, response);
    }
    
    public void handleRequest(ContainerRequest request, ContainerResponse response) throws IOException {
        final WebApplicationContext localContext = new
                WebApplicationContext(this, request, response);
        
        context.set(localContext);
        try {
            _handleRequest(localContext, request, response);
        } finally {
            PerRequestFactory.destroy(localContext);
            closeableFactory.close(localContext);
            context.set(null);
        }
    }

    public void destroy() {
        for (ResourceClass rc : metaClassMap.values()) {
            rc.destroy();
        }

        cpFactory.destroy();
    }

    private void _handleRequest(final WebApplicationContext localContext,
            ContainerRequest request, ContainerResponse response) throws IOException {
        try {
            for (ContainerRequestFilter f : filterFactory.getRequestFilters()) {
                request = f.filter(request);
                localContext.setContainerRequest(request);
            }
            
            /**
             * The matching algorithm currently works from an absolute path.
             * The path is required to be in encoded form.
             */
            StringBuilder path = new StringBuilder();
            path.append("/").append(request.getPath(false));

            if (!resourceConfig.getFeature(ResourceConfig.FEATURE_MATCH_MATRIX_PARAMS)) {
                path = stripMatrixParams(path);
            }

            // TODO convert to filter
            // If there are URI conneg extensions for media and language
            if (!resourceConfig.getMediaTypeMappings().isEmpty() ||
                    !resourceConfig.getLanguageMappings().isEmpty()) {
                uriConneg(path, request);
            }

            if (!rootsRule.accept(path, null, localContext)) {
                throw new NotFoundException(request.getRequestUri());
            }            
        } catch (WebApplicationException e) {
            mapWebApplicationException(e, response);
        } catch (MappableContainerException e) {
            mapMappableContainerException(e, response);
        } catch (RuntimeException e) {
            if (!mapException(e, response)) {
                throw e;
            }
        }

        try {
            // Process response filters from resources
            for (ContainerResponseFilter f : localContext.getResponseFilters()) {
                response = f.filter(request, response);
                localContext.setContainerResponse(response);
            }

            for (ContainerResponseFilter f : filterFactory.getResponseFilters()) {
                response = f.filter(request, response);
                localContext.setContainerResponse(response);
            }
        } catch (WebApplicationException e) {
            mapWebApplicationException(e, response);
        } catch (MappableContainerException e) {
            mapMappableContainerException(e, response);
        } catch (RuntimeException e) {
            if (!mapException(e, response)) {
                throw e;
            }
        }

        try {
            response.write();
        } catch (WebApplicationException e) {
            if (response.isCommitted()) {
                throw e;
            } else {
                mapWebApplicationException(e, response);
                response.write();
            }
        }
    }

    public HttpContext getThreadLocalHttpContext() {
        return context;
    }

    private void ensureTemplateUnused(UriTemplate t, AbstractResource ar, Set<UriTemplate> templates) {
        if (!templates.contains(t)) {
            templates.add(t);
        } else {
            LOGGER.severe(ImplMessages.AMBIGUOUS_RR_PATH(ar.getResourceClass(), t));
            throw new ContainerException(ImplMessages.AMBIGUOUS_RR_PATH(ar.getResourceClass(), t));
        }
    }

    private RulesMap<UriRule> processRootResources() {
        Set<Class<?>> classes = resourceConfig.getRootResourceClasses();
        Set<Object> singletons = resourceConfig.getRootResourceSingletons();
        if (classes.isEmpty() && singletons.isEmpty()) {
            LOGGER.severe(ImplMessages.NO_ROOT_RES_IN_RES_CFG());
            throw new ContainerException(ImplMessages.NO_ROOT_RES_IN_RES_CFG());
        }


        // Create the set of abstract root resources

        Map<Class<?>, AbstractResource> rootResourcesMap = new HashMap<Class<?>, AbstractResource>();
        for (Object o : singletons) {
            rootResourcesMap.put(o.getClass(), getAbstractResource(o));
        }

        for (Class<?> c : classes) {
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

            rootResourcesMap.put(c, getAbstractResource(c));
        }

        Set<AbstractResource> rootResources = new HashSet<AbstractResource>();
        for (Map.Entry<Class<?>, AbstractResource> e : rootResourcesMap.entrySet()) {
            rootResources.add(e.getValue());
        }

        initWadl(rootResources, wadlFactory);


        RulesMap<UriRule> rulesMap = new RulesMap<UriRule>();

        // need to validate possible conflicts in uri templates
        Set<UriTemplate> uriTemplatesUsed = new HashSet<UriTemplate>();

        for (Object o : singletons) {
            ComponentInjector ci = new ComponentInjector(injectableFactory, o.getClass());
            ci.inject(o);

            AbstractResource ar = rootResourcesMap.get(o.getClass());

            UriTemplate t = new PathTemplate(ar.getPath().getValue());

            ensureTemplateUnused(t, ar, uriTemplatesUsed);

            PathPattern p = new PathPattern(t);

            // Configure meta-data
            getResourceClass(ar);

            rulesMap.put(p, new RightHandPathRule(
                        resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                        t.endsWithSlash(),
                        new ResourceObjectRule(t, o)));
        }
        
        for (Class<?> c : classes) {
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

            AbstractResource ar = rootResourcesMap.get(c);

            UriTemplate t = new PathTemplate(ar.getPath().getValue());
            
            ensureTemplateUnused(t, ar, uriTemplatesUsed);

            PathPattern p = new PathPattern(t);

            // Configure meta-data
            getResourceClass(ar);

            rulesMap.put(p, new RightHandPathRule(
                    resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                    t.endsWithSlash(),
                    new ResourceClassRule(t, c)));
        }

        initWadlResource(rulesMap);
        
        return rulesMap;
    }

    private void initWadl(Set<AbstractResource> rootResources,
            WadlFactory wadlFactory) {
        // TODO get ResourceConfig to check the WADL generation feature
        
        if (!wadlFactory.isSupported())
            return;

        wadlFactory.init(injectableFactory, rootResources);
    }

    private void initWadlResource(RulesMap<UriRule> rulesMap) {
        if (!wadlFactory.isSupported())
            return;

        UriTemplate t = new PathTemplate("application.wadl");

        PathPattern p = new PathPattern(t);

        // If "application.wadl" is already defined to not add the
        // default WADL resource
        if (rulesMap.containsKey(p))
            return;
        
        // Configure meta-data
        getResourceClass(WadlResource.class);

        rulesMap.put(p, new RightHandPathRule(
                resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                t.endsWithSlash(),
                new ResourceClassRule(t, WadlResource.class)));
    }

    /**
     * Strip the matrix parameters from a path
     */
    private StringBuilder stripMatrixParams(StringBuilder path) {
        int e = path.indexOf(";");
        if (e == -1) {
            return path;
        }

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
        } while (e != -1);

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
        if (si == -1) {
            si = 0;
        }

        MediaType accept = null;
        for (Map.Entry<String, MediaType> e : resourceConfig.getMediaTypeMappings().entrySet()) {
            int i = path.indexOf(e.getKey(), si);
            if (i > 0 && (path.charAt(i - 1) == '.')) {
                int lengthWithExt = i + e.getKey().length();
                if (lengthWithExt == path.length()) {
                    accept = e.getValue();
                    path.delete(i - 1, lengthWithExt);
                } else {
                    char charAfterExt = path.charAt(lengthWithExt);
                    if (('/' == charAfterExt) || ('.' == charAfterExt)) { 
                        accept = e.getValue();
                        path.delete(i - 1, lengthWithExt);
                    }
                }
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
        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{c},
                i);
    }

    private void mapMappableContainerException(MappableContainerException e,
            HttpResponseContext response) {
        Throwable cause = e.getCause();

        if (cause instanceof WebApplicationException) {
            mapWebApplicationException((WebApplicationException)cause, response);
        } else if (!mapException(cause, response)) {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else {
                throw e;
            }
        }
    }

    private void mapWebApplicationException(WebApplicationException e, 
            HttpResponseContext response) {
        if (e.getResponse().getEntity() != null) {
            onException(e, e.getResponse(), response);
        } else {
            if (!mapException(e, response)) {
                onException(e, e.getResponse(), response);                    
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean mapException(Throwable e,
            HttpResponseContext response) {
        ExceptionMapper em = exceptionFactory.find(e.getClass());
        if (em == null) return false;

        try {
            Response r = em.toResponse(e);
            if (r == null)
                r = Response.noContent().build();
            onException(e, r, response);
        } catch (RuntimeException ex) {
            LOGGER.severe("Exception mapper " + em +
                    " for throwable " + e +
                    " threw a Runtime exception when " +
                    "attempting to obtain the response");
            Response r = Response.serverError().build();
            onException(ex, r, response);
        }
        return true;
    }
    
    private static void onException(Throwable e,
            Response r,
            HttpResponseContext response) {
        // Log the stack trace
        if (r.getStatus() >= 500) {
            LOGGER.log(Level.SEVERE, "Internal server error", e);
        }

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