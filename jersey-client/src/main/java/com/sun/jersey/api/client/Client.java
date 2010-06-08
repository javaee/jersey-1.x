/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.api.client;

import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.proxy.ViewProxy;
import com.sun.jersey.client.proxy.ViewProxyProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCProviderFactory;
import com.sun.jersey.core.spi.component.ProviderFactory;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentInjector;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.core.spi.factory.ContextResolverFactory;
import com.sun.jersey.core.spi.factory.InjectableProviderFactory;
import com.sun.jersey.core.spi.factory.MessageBodyFactory;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.spi.inject.ClientSide;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import com.sun.jersey.spi.service.ServiceFinder;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

/**
 * The main class for creating {@link WebResource} instances and configuring
 * the properties of connections and requests.
 * <p>
 * {@link ClientFilter} instances may be added to the client for filtering
 * requests and responses, including those of {@link WebResource} instances
 * created from the client.
 * <p>
 * A client may be configured by passing a {@link ClientConfig} instance to
 * the appropriate construtor.
 * <p>
 * Methods to create instances of {@link WebResource} are thread-safe. Methods
 * that modify configuration and or filters are not guaranteed to be
 * thread-safe.
 * <p>
 * The creation of a <code>Client</code> instance is an expensive operation and
 * the instance may make use of and retain many resources. It is therefore
 * recommended that a <code>Client</code> instance is reused for the creation of
 * {@link WebResource} instances that require the same configuration settings.
 * <p>
 * A client may integrate with an IoC framework by passing a
 * {@link IoCComponentProviderFactory} instance to the appropriate constructor.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class Client extends Filterable implements ClientHandler {

    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private ProviderFactory componentProviderFactory;

    private Providers providers;

    private boolean destroyed = false;

    private Map<String, Object> properties;

    private Set<ViewProxyProvider> vpps;

    private static class ContextInjectableProvider<T> extends
            SingletonTypeInjectableProvider<Context, T> {

        ContextInjectableProvider(Type type, T instance) {
            super(type, instance);
        }
    }

    /**
     * Create a new client instance.
     *
     */
    public Client() {
        this(createDefaultClientHander(), new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     */
    public Client(ClientHandler root) {
        this(root, new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance with a client configuration.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     */
    public Client(ClientHandler root, ClientConfig config) {
        this(root, config, null);
    }

    /**
     * Create a new instance with a client configuration and a
     * component provider.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     * @param provider the IoC component provider factory.
     */
    public Client(final ClientHandler root, final ClientConfig config,
            final IoCComponentProviderFactory provider) {
        // Defer instantiation of root to component provider
        super(root);

        Errors.processWithErrors(new Errors.Closure<Void>() {
            @Override
            public Void f() {
                Errors.setReportMissingDependentFieldOrMethod(false);
                init(root, config, provider);
                return null;
            }
        });
    }

    private void init(ClientHandler root, ClientConfig config,
            IoCComponentProviderFactory provider) {

        Class<?>[] components = ServiceFinder.find("jersey-client-components").toClassArray();
        if (components.length > 0) {
            if (LOGGER.isLoggable(Level.INFO)) {
                StringBuilder b = new StringBuilder();
                b.append("Adding the following classes declared in META-INF/services/jersey-client-components to the client configuration:");
                for (Class c : components)
                        b.append('\n').append("  ").append(c);
                LOGGER.log(Level.INFO, b.toString());
            }

            config = new ComponentsClientConfig(config, components);
        }

        final InjectableProviderFactory injectableFactory = new InjectableProviderFactory();

        getProperties().putAll(config.getProperties());

        if (provider != null) {
            if (provider instanceof IoCComponentProcessorFactoryInitializer) {
                IoCComponentProcessorFactoryInitializer i = (IoCComponentProcessorFactoryInitializer)provider;
                i.init(new ComponentProcessorFactoryImpl(injectableFactory));
            }
        }

        // Set up the component provider factory
        this.componentProviderFactory = (provider == null)
                ? new ProviderFactory(injectableFactory)
                : new IoCProviderFactory(injectableFactory, provider);

        ProviderServices providerServices = new ProviderServices(
                ClientSide.class,
                this.componentProviderFactory,
                config.getClasses(),
                config.getSingletons());

        // Get the set of WebResourceProxy
        vpps = providerServices.getServices(ViewProxyProvider.class);

        // Allow injection of features and properties
        injectableFactory.add(new ContextInjectableProvider<FeaturesAndProperties>(
                FeaturesAndProperties.class, config));

        // Allow injection of client config
        injectableFactory.add(new ContextInjectableProvider<ClientConfig>(
                ClientConfig.class, config));

        // Allow injection of client
        injectableFactory.add(new ContextInjectableProvider<Client>(
                Client.class, this));

        injectableFactory.configure(providerServices);

        // Obtain all context resolvers
        final ContextResolverFactory crf = new ContextResolverFactory();

        // Obtain all message body readers/writers
        final MessageBodyFactory bodyContext = new MessageBodyFactory(providerServices);
        // Allow injection of message body context
        injectableFactory.add(new ContextInjectableProvider<MessageBodyWorkers>(
                MessageBodyWorkers.class, bodyContext));

        // Injection of Providers
        this.providers = new Providers() {
            public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t,
                    Annotation[] as, MediaType m) {
                return bodyContext.getMessageBodyReader(c, t, as, m);
            }

            public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c, Type t,
                    Annotation[] as, MediaType m) {
                return bodyContext.getMessageBodyWriter(c, t, as, m);
            }

            public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> c) {
                throw new IllegalArgumentException("This method is not supported on the client side");
            }

            public <T> ContextResolver<T> getContextResolver(Class<T> ct, MediaType m) {
                return crf.resolve(ct, m);
            }
        };
        injectableFactory.add(
                new ContextInjectableProvider<Providers>(
                Providers.class, this.providers));

        injectableFactory.add(new InjectableProvider<Context, Type>() {
            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }

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

        // Initiate context resolvers
        crf.init(providerServices, injectableFactory);

        // Initiate message body readers/writers
        bodyContext.init();


        // Inject on all components
        Errors.setReportMissingDependentFieldOrMethod(true);
        componentProviderFactory.injectOnAllComponents();
        componentProviderFactory.injectOnProviderInstances(config.getSingletons());
        componentProviderFactory.injectOnProviderInstance(root);
    }

    private class ComponentProcessorFactoryImpl implements IoCComponentProcessorFactory {
        private final InjectableProviderFactory injectableFactory;

        ComponentProcessorFactoryImpl(InjectableProviderFactory injectableFactory) {
            this.injectableFactory = injectableFactory;
        }

        public ComponentScope getScope(Class c) {
            return ComponentScope.Singleton;
        }

        public IoCComponentProcessor get(Class c, ComponentScope scope) {
            final ComponentInjector ci = new ComponentInjector(injectableFactory, c);
            return new IoCComponentProcessor() {

                public void preConstruct() {
                }

                public void postConstruct(Object o) {
                    ci.inject(o);
                }
            };
        }
    }

    /**
     * Destroy the client. Any system resources associated with the client
     * will be cleaned up.
     * <p>
     * This method must be called when there are not responses pending otherwise
     * undefined behaviour will occur.
     * <p>
     * The client must not be reused after this method is called otherwise
     * undefined behaviour will occur.
     */
    public void destroy() {
        if (!destroyed) {
            componentProviderFactory.destroy();
            destroyed = true;
        }
    }

    /**
     * Defer to {@link #destroy() }
     */
    @Override
    protected void finalize() {
        destroy();
    }

    /**
     * Get the {@link Providers} utilized by the client.
     *
     * @return the {@link Providers} utilized by the client.
     */
    public Providers getProviders() {
        return providers;
    }

    /**
     * Create a Web resource from the client.
     *
     * @param u the URI of the resource.
     * @return the Web resource.
     */
    public WebResource resource(String u) {
        return resource(URI.create(u));
    }

    /**
     * Create a Web resource from the client.
     *
     * @param u the URI of the resource.
     * @return the Web resource.
     */
    public WebResource resource(URI u) {
        return new WebResource(this, u);
    }

    /**
     * Create an asynchronous Web resource from the client.
     *
     * @param u the URI of the resource.
     * @return the Web resource.
     */
    public AsyncWebResource asyncResource(String u) {
        return asyncResource(URI.create(u));
    }

    /**
     * Create an asynchronous Web resource from the client.
     *
     * @param u the URI of the resource.
     * @return the Web resource.
     */
    public AsyncWebResource asyncResource(URI u) {
        return new AsyncWebResource(this, u);
    }

    public ViewResource viewResource(String u) {
        return viewResource(URI.create(u));
    }

    public ViewResource viewResource(URI u) {
        return new ViewResource(this, u);
    }

    public AsyncViewResource asyncViewResource(String u) {
        return asyncViewResource(URI.create(u));
    }

    public AsyncViewResource asyncViewResource(URI u) {
        return new AsyncViewResource(this, u);
    }

    public <T> T view(String u, Class<T> type) {
        ViewResource vr = viewResource(u);
        return vr.get(type);
    }

    public <T> T view(URI uri, Class<T> type) {
        ViewResource vr = viewResource(uri);
        return vr.get(type);
    }

    public <T> T view(String u, T t) {
        ViewResource vr = viewResource(u);
        return vr.get(t);
    }
    
    public <T> T view(URI uri, T t) {
        ViewResource vr = viewResource(uri);
        return vr.get(t);
    }

    public <T> Future<T> asyncView(String u, Class<T> type) {
        AsyncViewResource vr = asyncViewResource(u);
        return vr.get(type);
    }

    public <T> Future<T> asyncView(URI uri, Class<T> type) {
        AsyncViewResource vr = asyncViewResource(uri);
        return vr.get(type);
    }

    public <T> Future<T> asyncView(String u, T t) {
        AsyncViewResource vr = asyncViewResource(u);
        return vr.get(t);
    }
    
    public <T> Future<T> asyncView(URI uri, T t) {
        AsyncViewResource vr = asyncViewResource(uri);
        return vr.get(t);
    }
    
    public <T> T view(Class<T> c, ClientResponse response) {
        return getViewProxy(c).view(c, response);
    }

    public <T> T view(T t, ClientResponse response) {
        return getViewProxy((Class<T>)t.getClass()).view(t, response);
    }

    public <T> ViewProxy<T> getViewProxy(Class<T> c) {
        for (ViewProxyProvider vpp : vpps) {
            ViewProxy<T> vp = vpp.proxy(this, c);
            if (vp != null) {
                return vp;
            }
        }
        throw new IllegalArgumentException("A view proxy is not " +
                "available for the class '" + c.getName() + "'");
    }
  
    /**
     * Get the mutable property bag.
     *
     * @return the property bag.
     */
    public Map<String, Object> getProperties() {
        if (properties == null)
            properties = new HashMap<String, Object>();

        return properties;
    }

    /**
     * Set if redirection should be performed or not.
     *
     * This method is the functional equivalent to setting the property
     * {@link ClientConfig#PROPERTY_FOLLOW_REDIRECTS} on the property bag
     * returned from {@link #getProperties}
     *
     * @param redirect if true then the client will automatically redirect
     *        to the URI declared in 3xx responses.
     */
    public void setFollowRedirects(Boolean redirect) {
        getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, redirect);
    }

    /**
     * Set the read timeout interval, in milliseconds.
     *
     * This method is the functional equivalent to setting the property
     * {@link ClientConfig#PROPERTY_READ_TIMEOUT} on the property bag
     * returned from {@link #getProperties}
     *
     * @param interval the read timeout interval. If null or 0 then
     * an interval of infinity is declared.
     */
    public void setReadTimeout(Integer interval) {
        getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, interval);
    }

    /**
     * Set the connect timeout interval, in milliseconds.
     *
     * This method is the functional equivalent to setting the property
     * {@link ClientConfig#PROPERTY_CONNECT_TIMEOUT} on the property bag
     * returned from {@link #getProperties}
     *
     * @param interval the connect timeout interval. If null or 0 then
     * an interval of infinity is declared.
     */
    public void setConnectTimeout(Integer interval) {
        getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, interval);
    }

    /**
     * Set the client to send request entities using chunked encoding
     * with a particular chunk size.
     *
     * This method is the functional equivalent to setting the property
     * {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE} on the property bag
     * returned from {@link #getProperties}
     *
     * @param chunkSize the chunked encoding size. If &lt= 0 then the default
     *        size will be used. If null then chunked encoding will not be
     *        utilized.
     */
    public void setChunkedEncodingSize(Integer chunkSize) {
        getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, chunkSize);
    }

    // ClientHandler

    public ClientResponse handle(final ClientRequest request) throws ClientHandlerException {
        request.getProperties().putAll(properties);
        request.getProperties().put(Client.class.getName(), this);

        final ClientResponse response = getHeadHandler().handle(request);
        
        response.getProperties().put(Client.class.getName(), this);
        return response;
    }

    /**
     * Create a default client.
     *
     * @return a default client.
     */
    public static Client create() {
        return new Client(createDefaultClientHander());
    }

    /**
     * Create a default client with client configuration.
     *
     * @param cc the client configuration.
     * @return a default client.
     */
    public static Client create(ClientConfig cc) {
        return new Client(createDefaultClientHander(), cc);
    }

    /**
     * Create a default client with client configuration and component provider.
     *
     * @param cc the client configuration.
     * @param provider the IoC component provider factory.
     * @return a default client.
     */
    public static Client create(ClientConfig cc, IoCComponentProviderFactory provider) {
        return new Client(createDefaultClientHander(), cc, provider);
    }

    /**
     * Create a default client handler.
     * <p>
     * This implementation returns a client handler implementation that
     * utilizes {@link HttpURLConnection}.
     *
     * @return a default client handler.
     */
    private static ClientHandler createDefaultClientHander() {
        return new URLConnectionClientHandler();
    }
}