/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.container.filter.UriConnegFilter;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.core.ParentRef;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ResourceConfigurator;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.api.model.AbstractResourceModelListener;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceModelContext;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderFactory;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCProviderFactory;
import com.sun.jersey.core.spi.factory.ContextResolverFactory;
import com.sun.jersey.core.spi.factory.MessageBodyFactory;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.server.impl.BuildId;
import com.sun.jersey.server.impl.ThreadLocalHttpContext;
import com.sun.jersey.server.impl.component.IoCResourceFactory;
import com.sun.jersey.server.impl.component.ResourceFactory;
import com.sun.jersey.server.impl.container.filter.FilterFactory;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderFactory;
import com.sun.jersey.server.impl.model.ResourceUriRules;
import com.sun.jersey.server.impl.model.RulesMap;
import com.sun.jersey.server.impl.model.parameter.CookieParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.FormParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.HeaderParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.HttpContextInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.MatrixParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.PathParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.QueryParamInjectableProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorFactory;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.StringReaderFactory;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.server.impl.modelapi.validation.BasicValidator;
import com.sun.jersey.server.impl.resource.PerRequestFactory;
import com.sun.jersey.server.impl.template.TemplateFactory;
import com.sun.jersey.server.impl.uri.rules.RootResourceClassesRule;
import com.sun.jersey.server.impl.wadl.WadlFactory;
import com.sun.jersey.server.spi.component.ResourceComponentInjector;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.StringReaderWorkers;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.ExceptionMapperContext;
import com.sun.jersey.spi.container.ResourceMethodCustomInvokerDispatchFactory;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationListener;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.InjectableProviderContext;
import com.sun.jersey.spi.inject.ServerSide;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import com.sun.jersey.spi.monitoring.DispatchingListener;
import com.sun.jersey.server.impl.monitoring.MonitoringProviderFactory;
import com.sun.jersey.spi.monitoring.RequestListener;
import com.sun.jersey.spi.monitoring.ResponseListener;
import com.sun.jersey.spi.service.ServiceFinder;
import com.sun.jersey.spi.template.TemplateContext;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRules;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private final ConcurrentMap<Class, UriRules<UriRule>> rulesMap =
            new ConcurrentHashMap<Class, UriRules<UriRule>>();

    private final ConcurrentMap<Class, ResourceComponentProvider> providerMap =
            new ConcurrentHashMap<Class, ResourceComponentProvider>();

    private static class ClassAnnotationKey {
        private final Class c;

        private final Set<Annotation> as;

        public ClassAnnotationKey(Class c, Annotation[] as) {
            this.c = c;
            this.as = new HashSet<Annotation>(Arrays.asList(as));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (this.c != null ? this.c.hashCode() : 0);
            hash = 67 * hash + (this.as != null ? this.as.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ClassAnnotationKey other = (ClassAnnotationKey) obj;
            if (this.c != other.c && (this.c == null || !this.c.equals(other.c))) {
                return false;
            }
            if (this.as != other.as && (this.as == null || !this.as.equals(other.as))) {
                return false;
            }
            return true;
        }
    }

    private final ConcurrentMap<ClassAnnotationKey, ResourceComponentProvider> providerWithAnnotationKeyMap =
            new ConcurrentHashMap<ClassAnnotationKey, ResourceComponentProvider>();

    private final ThreadLocalHttpContext context;

    private final CloseableServiceFactory closeableFactory;

    private boolean initiated;

    private ResourceConfig resourceConfig;

    private RootResourceClassesRule rootsRule;

    private ServerInjectableProviderFactory injectableFactory;

    private ProviderFactory cpFactory;

    private ResourceFactory rcpFactory;

    private IoCComponentProviderFactory provider;

    private List<IoCComponentProviderFactory> providerFactories;

    private Providers providers;

    private MessageBodyFactory bodyFactory;

    private StringReaderFactory stringReaderFactory;

    private TemplateContext templateContext;

    private ExceptionMapperFactory exceptionFactory;

    private ResourceMethodDispatchProvider dispatcherFactory;

    private ResourceContext resourceContext;

    private Set<AbstractResource> abstractRootResources;

    private Map<String, AbstractResource> explicitAbstractRootResources;

    private final AbstractResourceModelContext armContext = new AbstractResourceModelContext() {

            @Override
            public Set<AbstractResource> getAbstractRootResources() {
                return abstractRootResources;
            }
        };

    private FilterFactory filterFactory;

    private WadlFactory wadlFactory;

    private boolean isTraceEnabled;

    private RequestListener requestListener;

    private DispatchingListener dispatchingListener;

    private ResponseListener responseListener;

    public WebApplicationImpl() {
        this.context = new ThreadLocalHttpContext();

        InvocationHandler requestHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    return method.invoke(context.getRequest(), args);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            }
        };
        InvocationHandler uriInfoHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    return method.invoke(context.getUriInfo(), args);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
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
            @Override
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }

            @Override
            public Injectable getInjectable(ComponentContext ic, Context a, Type c) {
                final Object o = m.get(c);
                if (o != null) {
                    return new Injectable() {
                        @Override
                        public Object getValue() {
                            return o;
                        }
                    };
                } else
                    return null;
            }
        });

        injectableFactory.add(new InjectableProvider<Context, Type>() {
            @Override
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }

            @Override
            public Injectable<Injectable> getInjectable(ComponentContext ic, Context a, Type c) {
                if (c instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)c;
                    if (pt.getRawType() == Injectable.class) {
                        if (pt.getActualTypeArguments().length == 1) {
                            final Injectable<?> i = injectableFactory.getInjectable(
                                    a.annotationType(),
                                    ic,
                                    a,
                                    pt.getActualTypeArguments()[0],
                                    ComponentScope.PERREQUEST_UNDEFINED_SINGLETON);
                            if (i == null)
                                return null;
                            return new Injectable<Injectable>() {
                                @Override
                                public Injectable getValue() {
                                    return i;
                                }
                            };
                        }
                    }
                }

                return null;
            }
        });

        injectableFactory.add(new InjectableProvider<Inject, Type>() {
            @Override
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }

            @Override
            public Injectable<Injectable> getInjectable(ComponentContext ic, Inject a, Type c) {
                if (c instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)c;
                    if (pt.getRawType() == Injectable.class) {
                        if (pt.getActualTypeArguments().length == 1) {
                            final Injectable<?> i = injectableFactory.getInjectable(
                                    a.annotationType(),
                                    ic,
                                    a,
                                    pt.getActualTypeArguments()[0],
                                    ComponentScope.PERREQUEST_UNDEFINED_SINGLETON);
                            if (i == null)
                                return null;
                            return new Injectable<Injectable>() {
                                @Override
                                public Injectable getValue() {
                                    return i;
                                }
                            };
                        }
                    }
                }

                return null;
            }
        });

        injectableFactory.add(new InjectableProvider<InjectParam, Type>() {
            @Override
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }

            @Override
            public Injectable<Injectable> getInjectable(ComponentContext ic, InjectParam a, Type c) {
                if (c instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)c;
                    if (pt.getRawType() == Injectable.class) {
                        if (pt.getActualTypeArguments().length == 1) {
                            final Injectable<?> i = injectableFactory.getInjectable(
                                    a.annotationType(),
                                    ic,
                                    a,
                                    pt.getActualTypeArguments()[0],
                                    ComponentScope.PERREQUEST_UNDEFINED_SINGLETON);
                            if (i == null)
                                return null;
                            return new Injectable<Injectable>() {
                                @Override
                                public Injectable getValue() {
                                    return i;
                                }
                            };
                        }
                    }
                }

                return null;
            }
        });

        closeableFactory = new CloseableServiceFactory(context);
        injectableFactory.add(closeableFactory);
    }

    private class ComponentProcessorImpl implements IoCComponentProcessor {
        private final ResourceComponentInjector rci;

        ComponentProcessorImpl(ResourceComponentInjector rci) {
            this.rci = rci;
        }

        @Override
        public void preConstruct() {
        }

        @Override
        public void postConstruct(Object o) {
            rci.inject(context.get(), o);
        }
    }

    private static final IoCComponentProcessor NULL_COMPONENT_PROCESSOR = new IoCComponentProcessor() {
        @Override
        public void preConstruct() {
        }

        @Override
        public void postConstruct(Object o) {
        }
    };

    private class ComponentProcessorFactoryImpl implements IoCComponentProcessorFactory {
        private final ConcurrentMap<Class, IoCComponentProcessor> componentProcessorMap =
                new ConcurrentHashMap<Class, IoCComponentProcessor>();

        @Override
        public ComponentScope getScope(Class c) {
            return rcpFactory.getScope(c);
        }

        @Override
        public IoCComponentProcessor get(final Class c, final ComponentScope scope) {
            IoCComponentProcessor cp = componentProcessorMap.get(c);
            if (cp != null) {
                return (cp == NULL_COMPONENT_PROCESSOR) ? null : cp;
            }

            synchronized (abstractResourceMap) {
                cp = componentProcessorMap.get(c);
                if (cp != null) {
                    return (cp == NULL_COMPONENT_PROCESSOR) ? null : cp;
                }

                final ResourceComponentInjector rci = Errors.processWithErrors(new Errors.Closure<ResourceComponentInjector>() {
                    @Override
                    public ResourceComponentInjector f() {
                        return new ResourceComponentInjector(
                                injectableFactory, scope, getAbstractResource(c));
                    }
                });

                if (rci.hasInjectableArtifacts()) {
                    cp = new ComponentProcessorImpl(rci);
                    componentProcessorMap.put(c, cp);
                } else {
                    cp = null;
                    componentProcessorMap.put(c, NULL_COMPONENT_PROCESSOR);
                }
            }
            return cp;
        }
    }

    @Override
    public FeaturesAndProperties getFeaturesAndProperties() {
        return resourceConfig;
    }

    @Override
    public WebApplication clone() {
        WebApplicationImpl wa = new WebApplicationImpl();
        wa.initiate(resourceConfig, provider);
        return wa;
    }

    /* package */ UriRules<UriRule> getUriRules(final Class c) {
        assert c != null;

        // Try the non-blocking read, the most common operation
        UriRules<UriRule> r = rulesMap.get(c);
        if (r != null) {
            return r;
        }

        // Not present use a synchronized block to ensure that only one
        // instance is created and put to the map
        synchronized (abstractResourceMap) {
            // One or more threads may have been blocking on the synchronized
            // block, re-check the map
            r = rulesMap.get(c);
            if (r != null) {
                return r;
            }

            r = Errors.processWithErrors(new Errors.Closure<ResourceUriRules>() {
                @Override
                public ResourceUriRules f() {
                    return newResourceUriRules(getAbstractResource(c));
                }
            }).getRules();
            rulesMap.put(c, r);
        }
        return r;
    }

    /* package */ ResourceComponentProvider getResourceComponentProvider(final Class c) {
        assert c != null;

        // Try the non-blocking read, the most common operation
        ResourceComponentProvider rcp = providerMap.get(c);
        if (rcp != null) {
            return rcp;
        }

        // Not present use a synchronized block to ensure that only one
        // instance is created and put to the map
        synchronized (abstractResourceMap) {
            // One or more threads may have been blocking on the synchronized
            // block, re-check the map
            rcp = providerMap.get(c);
            if (rcp != null) {
                return rcp;
            }

            final ResourceComponentProvider _rcp = rcp = rcpFactory.getComponentProvider(null, c);
            Errors.processWithErrors(new Errors.Closure<Void>() {
                @Override
                public Void f() {
                    _rcp.init(getAbstractResource(c));
                    return null;
                }
            });

            providerMap.put(c, rcp);
        }
        return rcp;
    }

    /* package */ ResourceComponentProvider getResourceComponentProvider(final ComponentContext cc, final Class c) {
        assert c != null;

        if (cc == null || cc.getAnnotations().length == 0)
            return getResourceComponentProvider(c);

        if (cc.getAnnotations().length == 1) {
            final Annotation a = cc.getAnnotations()[0];
            if (a.annotationType() == Inject.class) {
                final Inject i = Inject.class.cast(a);
                final String value = (i.value() != null)
                        ? i.value().trim()
                        : "";
                if (value.isEmpty())
                    return getResourceComponentProvider(c);
            } else if (a.annotationType() == InjectParam.class) {
                final InjectParam i = InjectParam.class.cast(a);
                final String value = (i.value() != null)
                        ? i.value().trim()
                        : "";
                if (value.isEmpty())
                    return getResourceComponentProvider(c);
            }
        }

        final ClassAnnotationKey cak = new ClassAnnotationKey(c,
                cc.getAnnotations());

        // Try the non-blocking read, the most common operation
        ResourceComponentProvider rcp = providerWithAnnotationKeyMap.get(cak);
        if (rcp != null) {
            return rcp;
        }

        // Not present use a synchronized block to ensure that only one
        // instance is created and put to the map
        synchronized (abstractResourceMap) {
            // One or more threads may have been blocking on the synchronized
            // block, re-check the map
            rcp = providerWithAnnotationKeyMap.get(cak);
            if (rcp != null) {
                return rcp;
            }

            final ResourceComponentProvider _rcp = rcp = rcpFactory.getComponentProvider(cc, c);
            Errors.processWithErrors(new Errors.Closure<Void>() {
                @Override
                public Void f() {
                    _rcp.init(getAbstractResource(c));
                    return null;
                }
            });

            providerWithAnnotationKeyMap.put(cak, rcp);
        }
        return rcp;
    }

    /* package */ void initiateResource(AbstractResource ar) {
        initiateResource(ar.getResourceClass());
    }

    /* package */ void initiateResource(Class c) {
        getUriRules(c);
        getResourceComponentProvider(c);
    }

    /* package */ void initiateResource(AbstractResource ar, final Object resource) {
        final Class c = ar.getResourceClass();
        getUriRules(c);

        if (!providerMap.containsKey(c)) {
            providerMap.put(c, new ResourceComponentProvider() {
                @Override
                public void init(AbstractResource abstractResource) {
                }

                @Override
                public ComponentScope getScope() {
                    return ComponentScope.Singleton;
                }

                @Override
                public Object getInstance(HttpContext hc) {
                    return getInstance();
                }

                @Override
                public void destroy() {
                }

                @Override
                public Object getInstance() {
                    return resource;
                }
            });
        }
    }

    /* package */ Set<AbstractResource> getAbstractRootResources() {
        return abstractRootResources;
    }

    /* package */ Map<String, AbstractResource> getExplicitAbstractRootResources() {
        return explicitAbstractRootResources;
    }

    private ResourceUriRules newResourceUriRules(final AbstractResource ar) {
        assert null != ar;

        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        for (ResourceModelIssue issue : validator.getIssueList()) {
            Errors.error(issue.getMessage(), issue.isFatal());
        }
        return new ResourceUriRules(
                resourceConfig,
                getDispatchProvider(),
                injectableFactory,
                filterFactory,
                wadlFactory,
                dispatchingListener,
                ar);
    }

    protected ResourceMethodDispatchProvider getDispatchProvider() {
        return dispatcherFactory;
    }

    @Override
    public RequestListener getRequestListener() {
        return requestListener;
    }

    @Override
    public DispatchingListener getDispatchingListener() {
        return dispatchingListener;
    }

    @Override
    public ResponseListener getResponseListener() {
        return responseListener;
    }

    /* package */ AbstractResource getAbstractResource(Object o) {
        return getAbstractResource(o.getClass());
    }

    /* package */ AbstractResource getAbstractResource(Class c) {
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

    @Override
    public boolean isInitiated () {
        return initiated;
    }

    @Override
    public void initiate(ResourceConfig resourceConfig) {
        initiate(resourceConfig, null);
    }

    @Override
    public void initiate(final ResourceConfig rc, final IoCComponentProviderFactory _provider) {
        Errors.processWithErrors(new Errors.Closure<Void>() {
            @Override
            public Void f() {
                Errors.setReportMissingDependentFieldOrMethod(false);
                _initiate(rc, _provider);
                return null;
            }
        });
    }

    private void _initiate(final ResourceConfig rc, final IoCComponentProviderFactory _provider) {
        if (rc == null) {
            throw new IllegalArgumentException("ResourceConfig instance MUST NOT be null");
        }

        if (initiated) {
            throw new ContainerException(ImplMessages.WEB_APP_ALREADY_INITIATED());
        }
        this.initiated = true;

        LOGGER.info("Initiating Jersey application, version '" + BuildId.getBuildId() + "'");

        // If there are components defined in jaxrs-components then
        // wrap resource config with appended set of classes
        Class<?>[] components = ServiceFinder.find("jersey-server-components").toClassArray();
        if (components.length > 0) {
            if (LOGGER.isLoggable(Level.INFO)) {
                StringBuilder b = new StringBuilder();
                b.append("Adding the following classes declared in META-INF/services/jersey-server-components to the resource configuration:");
                for (Class c : components)
                    b.append('\n').append("  ").append(c);
                LOGGER.log(Level.INFO, b.toString());
            }

            this.resourceConfig = rc.clone();
            this.resourceConfig.getClasses().addAll(Arrays.asList(components));
        } else {
            this.resourceConfig = rc;
        }

        this.provider = _provider;

        this.providerFactories = new ArrayList<IoCComponentProviderFactory>(2);

        for (Object o : resourceConfig.getProviderSingletons()) {
            if (o instanceof IoCComponentProviderFactory) {
                providerFactories.add((IoCComponentProviderFactory)o);
            }
        }

        if (_provider != null)
            providerFactories.add(_provider);

        // Set up the component provider factory to be
        // used with non-resource class components
        this.cpFactory = (providerFactories.isEmpty())
                ? new ProviderFactory(injectableFactory)
                : new IoCProviderFactory(injectableFactory, providerFactories);

        // Set up the resource component provider factory
        this.rcpFactory = (providerFactories.isEmpty())
                ? new ResourceFactory(this.resourceConfig, this.injectableFactory)
                : new IoCResourceFactory(this.resourceConfig, this.injectableFactory, providerFactories);

        // Initiate IoCComponentProcessorFactoryInitializer
        for (IoCComponentProviderFactory f : providerFactories) {
            IoCComponentProcessorFactory cpf = null;
            if (f instanceof IoCComponentProcessorFactoryInitializer) {
                cpf = new ComponentProcessorFactoryImpl();
                IoCComponentProcessorFactoryInitializer i = (IoCComponentProcessorFactoryInitializer)f;
                i.init(cpf);
            }

        }

        this.resourceContext = new ResourceContext() {
            @Override
            public ExtendedUriInfo matchUriInfo(URI u) throws ContainerException {
                try {
                    return handleMatchResourceRequest(u);
                } catch (ContainerException ex) {
                    throw ex;
                } catch (WebApplicationException ex) {
                    if (ex.getResponse().getStatus() == 404) {
                        return null;
                    } else {
                        throw new ContainerException(ex);
                    }
                } catch (RuntimeException ex) {
                    throw new ContainerException(ex);
                }
            }

            @Override
            public Object matchResource(URI u) throws ContainerException {
                ExtendedUriInfo ui = matchUriInfo(u);
                return (ui != null) ? ui.getMatchedResources().get(0) : null;
            }

            @Override
            public <T> T matchResource(URI u, Class<T> c) throws ContainerException, ClassCastException {
                return c.cast(matchResource(u));
            }

            @Override
            public <T> T getResource(Class<T> c) {
                return c.cast(getResourceComponentProvider(c).getInstance(context));
            }
        };

        ProviderServices providerServices = new ProviderServices(
                ServerSide.class,
                this.cpFactory,
                resourceConfig.getProviderClasses(),
                resourceConfig.getProviderSingletons());

        injectableFactory.add(new ContextInjectableProvider<ProviderServices>(
                ProviderServices.class, providerServices));

        injectableFactory.add(new ContextInjectableProvider<ResourceMethodCustomInvokerDispatchFactory>(
                ResourceMethodCustomInvokerDispatchFactory.class, new ResourceMethodCustomInvokerDispatchFactory(providerServices)));

        // Add injectable provider for @ParentRef
        injectableFactory.add(
                new InjectableProvider<ParentRef, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.PerRequest;
                    }

                    @Override
                    public Injectable<Object> getInjectable(ComponentContext cc, ParentRef a, Type t) {
                        if (!(t instanceof Class))
                            return null;

                        final Class target = ReflectionHelper.getDeclaringClass(cc.getAccesibleObject());
                        final Class inject = (Class)t;
                        return new Injectable<Object>() {
                            @Override
                            public Object getValue() {
                                final UriInfo ui = context.getUriInfo();
                                final List l = ui.getMatchedResources();

                                final Object parent = getParent(l, target);
                                if (parent == null) return null;
                                try {
                                    return inject.cast(parent);
                                } catch (ClassCastException ex) {
                                    throw new ContainerException(
                                            "The parent resource is expected to be of class " + inject.getName() +
                                                    " but is of class " + parent.getClass().getName(),
                                            ex);
                                }
                            }

                            private Object getParent(List l, Class target) {
                                if (l.isEmpty()) {
                                    return null;
                                } else if (l.size() == 1) {
                                    return (l.get(0).getClass() == target) ? null : l.get(0);
                                } else {
                                    return (l.get(0).getClass() == target) ? l.get(1) : l.get(0);
                                }
                            }
                        };
                    }


                });

        // Add injectable provider for @Inject

        injectableFactory.add(
                new InjectableProvider<Inject, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.PerRequest;
                    }

                    @Override
                    public Injectable<Object> getInjectable(ComponentContext cc, Inject a, Type t) {
                        if (!(t instanceof Class))
                            return null;

                        final ResourceComponentProvider rcp = getResourceComponentProvider(cc, (Class)t);

                        return new Injectable<Object>() {
                            @Override
                            public Object getValue() {
                                return rcp.getInstance(context);
                            }
                        };
                    }

                });

        injectableFactory.add(
                new InjectableProvider<Inject, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.Undefined;
                    }

                    @Override
                    public Injectable<Object> getInjectable(ComponentContext cc, Inject a, Type t) {
                        if (!(t instanceof Class))
                            return null;

                        final ResourceComponentProvider rcp = getResourceComponentProvider(cc, (Class)t);
                        if (rcp.getScope() == ComponentScope.PerRequest)
                            return null;

                        return new Injectable<Object>() {
                            @Override
                            public Object getValue() {
                                return rcp.getInstance(context);
                            }
                        };
                    }

                });

        injectableFactory.add(
                new InjectableProvider<Inject, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.Singleton;
                    }

                    @Override
                    public Injectable<Object> getInjectable(ComponentContext cc, Inject a, Type t) {
                        if (!(t instanceof Class))
                            return null;

                        final ResourceComponentProvider rcp = getResourceComponentProvider(cc, (Class)t);
                        if (rcp.getScope() != ComponentScope.Singleton)
                            return null;

                        return new Injectable<Object>() {
                            @Override
                            public Object getValue() {
                                return rcp.getInstance(context);
                            }
                        };
                    }

                });

        // Add injectable provider for @ResourceRef

        injectableFactory.add(
                new InjectableProvider<InjectParam, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.PerRequest;
                    }

                    @Override
                    public Injectable<Object> getInjectable(ComponentContext cc, InjectParam a, Type t) {
                        if (!(t instanceof Class))
                            return null;

                        final ResourceComponentProvider rcp = getResourceComponentProvider(cc, (Class)t);

                        return new Injectable<Object>() {
                            @Override
                            public Object getValue() {
                                return rcp.getInstance(context);
                            }
                        };
                    }

                });

        injectableFactory.add(
                new InjectableProvider<InjectParam, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.Undefined;
                    }

                    @Override
                    public Injectable<Object> getInjectable(ComponentContext cc, InjectParam a, Type t) {
                        if (!(t instanceof Class))
                            return null;

                        final ResourceComponentProvider rcp = getResourceComponentProvider(cc, (Class)t);
                        if (rcp.getScope() == ComponentScope.PerRequest)
                            return null;

                        return new Injectable<Object>() {
                            @Override
                            public Object getValue() {
                                return rcp.getInstance(context);
                            }
                        };
                    }

                });

        injectableFactory.add(
                new InjectableProvider<InjectParam, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.Singleton;
                    }

                    @Override
                    public Injectable<Object> getInjectable(ComponentContext cc, InjectParam a, Type t) {
                        if (!(t instanceof Class))
                            return null;

                        final ResourceComponentProvider rcp = getResourceComponentProvider(cc, (Class)t);
                        if (rcp.getScope() != ComponentScope.Singleton)
                            return null;

                        return new Injectable<Object>() {
                            @Override
                            public Object getValue() {
                                return rcp.getInstance(context);
                            }
                        };
                    }

                });

        // Allow injection of features and properties
        injectableFactory.add(new ContextInjectableProvider<FeaturesAndProperties>(
                FeaturesAndProperties.class, resourceConfig));

        // Allow injection of resource config
        // Since the resourceConfig reference can change refer to the
        // reference directly.
        injectableFactory.add(
                new InjectableProvider<Context, Type>() {
                    @Override
                    public ComponentScope getScope() {
                        return ComponentScope.Singleton;
                    }

                    @Override
                    public Injectable<ResourceConfig> getInjectable(ComponentContext cc, Context a, Type t) {
                        if (t != ResourceConfig.class)
                            return null;
                        return new Injectable<ResourceConfig>() {
                            @Override
                            public ResourceConfig getValue() {
                                return resourceConfig;
                            }
                        };
                    }
                });

        // Allow injection of resource context
        injectableFactory.add(new ContextInjectableProvider<ResourceContext>(
                ResourceContext.class, resourceContext));

        // Configure the injectable factory with declared providers
        injectableFactory.configure(providerServices);

        boolean updateRequired = false;

        // Create application-declared Application instance as a component
        if (rc instanceof DeferredResourceConfig) {
            final DeferredResourceConfig drc = (DeferredResourceConfig)rc;
            // Check if resource config has already been cloned
            if (resourceConfig == drc)
                resourceConfig = drc.clone();

            final DeferredResourceConfig.ApplicationHolder da = drc.getApplication(cpFactory);
            resourceConfig.add(da.getApplication());
            updateRequired = true;

            injectableFactory.add(new ContextInjectableProvider<Application>(
                    Application.class, da.getOriginalApplication()));
        } else {
            injectableFactory.add(new ContextInjectableProvider<FeaturesAndProperties>(
                    Application.class, resourceConfig));
        }

        // Pipelined, decentralized configuration
        for(ResourceConfigurator configurator : providerServices.getProviders(ResourceConfigurator.class)) {
            configurator.configure(this.resourceConfig);
            updateRequired = true;
        }

        // Validate the resource config
        this.resourceConfig.validate();

        if (updateRequired) {
            // Check if application modified provider classes or singletons
            providerServices.update(resourceConfig.getProviderClasses(),
                    resourceConfig.getProviderSingletons(), injectableFactory);
        }

        // Obtain all the templates
        this.templateContext = new TemplateFactory(providerServices);
        // Allow injection of template context
        injectableFactory.add(new ContextInjectableProvider<TemplateContext>(
                TemplateContext.class, templateContext));

        // Obtain all context resolvers
        final ContextResolverFactory crf = new ContextResolverFactory();

        // Obtain all the exception mappers
        this.exceptionFactory = new ExceptionMapperFactory();

        // Obtain all message body readers/writers
        this.bodyFactory = new MessageBodyFactory(providerServices,
                getFeaturesAndProperties().getFeature(FeaturesAndProperties.FEATURE_PRE_1_4_PROVIDER_PRECEDENCE));
        injectableFactory.add(
                new ContextInjectableProvider<MessageBodyWorkers>(
                        MessageBodyWorkers.class, bodyFactory));

        // Injection of Providers
        this.providers = new Providers() {
            @Override
            public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t,
                                                                 Annotation[] as, MediaType m) {
                return bodyFactory.getMessageBodyReader(c, t, as, m);
            }

            @Override
            public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c, Type t,
                                                                 Annotation[] as, MediaType m) {
                return bodyFactory.getMessageBodyWriter(c, t, as, m);
            }

            @Override
            public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> c) {
                if (Throwable.class.isAssignableFrom(c))
                    return exceptionFactory.find(c);
                else
                    return null;
            }

            @Override
            public <T> ContextResolver<T> getContextResolver(Class<T> ct, MediaType m) {
                return crf.resolve(ct, m);
            }
        };
        injectableFactory.add(
                new ContextInjectableProvider<Providers>(
                        Providers.class, providers));

        // Obtain all String readers
        this.stringReaderFactory = new StringReaderFactory();
        injectableFactory.add(
                new ContextInjectableProvider<StringReaderWorkers>(
                        StringReaderWorkers.class, stringReaderFactory));

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
        injectableFactory.add(new FormParamInjectableProvider(mpep));

        // Create filter factory
        filterFactory = new FilterFactory(providerServices);

        // Initiate resource method dispatchers
        dispatcherFactory = ResourceMethodDispatcherFactory.create(providerServices);

        requestListener = MonitoringProviderFactory.createRequestListener(providerServices);
        dispatchingListener = MonitoringProviderFactory.createDispatchingListener(providerServices);
        responseListener = MonitoringProviderFactory.createResponseListener(providerServices);

        // Initiate the WADL factory
        this.wadlFactory = new WadlFactory(resourceConfig);

        // Initiate filter
        filterFactory.init(resourceConfig);
        if (!resourceConfig.getMediaTypeMappings().isEmpty() ||
                !resourceConfig.getLanguageMappings().isEmpty()) {
            boolean present = false;
            for (ContainerRequestFilter f : filterFactory.getRequestFilters()) {
                present |= f instanceof UriConnegFilter;
            }

            if (!present) {
                filterFactory.getRequestFilters().add(new UriConnegFilter(
                        resourceConfig.getMediaTypeMappings(),
                        resourceConfig.getLanguageMappings()));
            } else {
                LOGGER.warning("The media type and language mappings " +
                        "declared in the ResourceConfig are ignored because " +
                        "there is an instance of " + UriConnegFilter.class.getName() +
                        "present in the list of request filters.");
            }
        }

        // Initiate context resolvers
        crf.init(providerServices, injectableFactory);

        // Initiate the exception mappers
        exceptionFactory.init(providerServices);

        // Initiate message body readers/writers
        bodyFactory.init();

        // Initiate string readers
        stringReaderFactory.init(providerServices);

        // Inject on all components
        Errors.setReportMissingDependentFieldOrMethod(true);
        cpFactory.injectOnAllComponents();
        cpFactory.injectOnProviderInstances(resourceConfig.getProviderSingletons());

        // web application is ready
        for (IoCComponentProviderFactory providerFactory : providerFactories) {
            if (providerFactory instanceof WebApplicationListener) {
                WebApplicationListener listener = (WebApplicationListener) providerFactory;
                listener.onWebApplicationReady();
            }
        }

        createAbstractResourceModelStructures();
        callAbstractResourceModelListenersOnLoaded(providerServices);

        // Obtain all root resource rules
        RulesMap<UriRule> rootRules = new RootResourceUriRules(this,
                resourceConfig, wadlFactory, injectableFactory).getRules();
        this.rootsRule = new RootResourceClassesRule(rootRules);

        this.isTraceEnabled = resourceConfig.getFeature(ResourceConfig.FEATURE_TRACE) |
                resourceConfig.getFeature(ResourceConfig.FEATURE_TRACE_PER_REQUEST);
    }

    @Override
    public Providers getProviders() {
        return providers;
    }

    @Override
    public MessageBodyWorkers getMessageBodyWorkers() {
        return bodyFactory;
    }

    @Override
    public ExceptionMapperContext getExceptionMapperContext() {
        return exceptionFactory;
    }

    @Override
    public ServerInjectableProviderFactory getServerInjectableProviderFactory() {
        return injectableFactory;
    }

    @Override
    public void handleRequest(ContainerRequest request, ContainerResponseWriter responseWriter)
            throws IOException {
        final ContainerResponse response = new ContainerResponse(
                this,
                request,
                responseWriter);
        handleRequest(request, response);
    }

    @Override
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

    private WebApplicationContext handleMatchResourceRequest(URI u) {
        final WebApplicationContext oldContext = (WebApplicationContext)context.get();

        final WebApplicationContext newContext = oldContext.createMatchResourceContext(u);

        context.set(newContext);
        try {
            _handleRequest(newContext, newContext.getContainerRequest());
            return newContext;
        } finally {
            context.set(oldContext);
        }
    }

    @Override
    public void destroy() {
        for (ResourceComponentProvider rcp : providerMap.values()) {
            rcp.destroy();
        }

        for (ResourceComponentProvider rcp : providerWithAnnotationKeyMap.values()) {
            rcp.destroy();
        }

        cpFactory.destroy();
    }

    // Traceable

    @Override
    public boolean isTracingEnabled() {
        return isTraceEnabled;
    }

    @Override
    public void trace(String message) {
        context.get().trace(message);
    }

    private void _handleRequest(final WebApplicationContext localContext,
                                ContainerRequest request, ContainerResponse response) throws IOException {
        try {
             requestListener.onRequest(Thread.currentThread().getId(), request);
            _handleRequest(localContext, request);
        } catch (WebApplicationException e) {
            response.mapWebApplicationException(e);
        } catch (MappableContainerException e) {
            response.mapMappableContainerException(e);
        } catch (RuntimeException e) {
            if (!response.mapException(e)) {
                LOGGER.log(Level.SEVERE, "The RuntimeException could not be mapped to a response, " +
                        "re-throwing to the HTTP container", e);
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
            response.mapWebApplicationException(e);
        } catch (MappableContainerException e) {
            response.mapMappableContainerException(e);
        } catch (RuntimeException e) {
            if (!response.mapException(e)) {
                LOGGER.log(Level.SEVERE, "The RuntimeException could not be mapped to a response, " +
                        "re-throwing to the HTTP container", e);
                throw e;
            }
        }

        try {
            response.write();
            responseListener.onResponse(Thread.currentThread().getId(), response);
        } catch (WebApplicationException e) {
            if (response.isCommitted()) {
                LOGGER.log(Level.SEVERE, "The response of the WebApplicationException cannot be utilized " +
                        "as the response is already committed. Re-throwing to the HTTP container", e);
                throw e;
            } else {
                response.mapWebApplicationException(e);
                response.write();
            }
        }
    }

    private void _handleRequest(final WebApplicationContext localContext,
                                ContainerRequest request) {
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

        if (!rootsRule.accept(path, null, localContext)) {
            throw new NotFoundException(request.getRequestUri());
        }
    }

    @Override
    public HttpContext getThreadLocalHttpContext() {
        return context;
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


    private void createAbstractResourceModelStructures() {

        final Set<AbstractResource> rootARs = new HashSet<AbstractResource>();

        // Add declared singleton instances of root resource classes
        for (final Object o : resourceConfig.getRootResourceSingletons()) {
            rootARs.add(getAbstractResource(o));
        }

        // Add declared root resource classes
        for (final Class<?> c : resourceConfig.getRootResourceClasses()) {
            rootARs.add(getAbstractResource(c));
        }

        // Add explicit declared root resource classes
        final Map<String, AbstractResource> explicitRootARs =
                new HashMap<String, AbstractResource>();
        for (final Map.Entry<String, Object> e : resourceConfig.getExplicitRootResources().entrySet()) {
            final Object o = e.getValue();
            final Class c = (o instanceof Class) ? (Class)o : o.getClass();

            final AbstractResource ar = new AbstractResource(e.getKey(),
                    getAbstractResource(c));

            rootARs.add(ar);
            explicitRootARs.put(e.getKey(), ar);
        }

        abstractRootResources = Collections.unmodifiableSet(rootARs);
        explicitAbstractRootResources = Collections.unmodifiableMap(explicitRootARs);
    }

    private void callAbstractResourceModelListenersOnLoaded(ProviderServices providerServices) {
        for (AbstractResourceModelListener aml : providerServices.getProviders(AbstractResourceModelListener.class)) {
            aml.onLoaded(armContext);
        }
    }

    private <T> T createProxy(Class<T> c, InvocationHandler i) {
        return c.cast(Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{c},
                i));
    }
}
