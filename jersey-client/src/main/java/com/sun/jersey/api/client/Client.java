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
package com.sun.jersey.api.client;

import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.impl.application.ComponentProviderCache;
import com.sun.jersey.impl.application.ContextResolverFactory;
import com.sun.jersey.impl.application.InjectableProviderFactory;
import com.sun.jersey.impl.application.MessageBodyFactory;
import com.sun.jersey.impl.client.urlconnection.URLConnectionClientHandler;
import com.sun.jersey.spi.container.MessageBodyWorkers;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import com.sun.jersey.spi.service.ComponentContext;
import com.sun.jersey.spi.service.ComponentProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

/**
 * The HTTP client class for handling requests and responses specified by 
 * {@link ClientHandler} or for creating {@link WebResource} instances.
 * <p>
 * {@link ClientFilter} instances may be added to the client for filtering
 * requests and responses (including those of {@link WebResource} instances
 * created from the client).
 * <p>
 * A client may be configured by passing a {@link ClientConfig} instance to
 * the appropriate construtor.
 * <p>
 * A client may integrate with an IoC framework by passing a 
 * {@link ComponentProvider} instance to the appropriate constructor.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class Client extends Filterable implements ClientHandler {
    private InjectableProviderFactory injectableFactory;
    
    private final ClientConfig config;
    
    private final ComponentProvider provider;
    
    private final MessageBodyFactory bodyContext;
    
    private Map<String, Object> properties;
    
    private final class AdaptingComponentProvider implements ComponentProvider {
        private final ComponentProvider cp;
        
        AdaptingComponentProvider(ComponentProvider cp) {
            this.cp = cp;
        }

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

        public <T> T getInstance(ComponentContext cc, Scope scope, Class<T> c) 
                throws InstantiationException, IllegalAccessException {
            T o = cp.getInstance(cc, scope,c);
            if (o == null) {
                o = c.newInstance();
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

        public <T> T getInstance(ComponentContext cc, Scope scope, Class<T> c) 
                throws InstantiationException, IllegalAccessException {
            return getInstance(scope, c);
        }
        
        public <T> T getInjectableInstance(T instance) {
            return instance;
        }
        
        public void inject(Object instance) {
            injectResources(instance);
        }
    }
    
    private static class ContextInjectableProvider<T> extends
            SingletonTypeInjectableProvider<Context, T> {

        ContextInjectableProvider(Type type, T instance) {
            super(type, instance);
        }
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
     * compoenent provider.
     * 
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     * @param provider the component provider.
     */
    public Client(ClientHandler root, ClientConfig config, 
            ComponentProvider provider) {
        // Defer instantiation of root to component provider
        super(root);
    
        this.injectableFactory = new InjectableProviderFactory();

        this.config = config;
        
        getProperties().putAll(config.getProperties());
        
        // Allow injection of resource config
        injectableFactory.add(new ContextInjectableProvider<ClientConfig>(
                ClientConfig.class, config));
        
        // Set up the component provider
        this.provider = (provider == null)
            ? new DefaultComponentProvider()
            : new AdaptingComponentProvider(provider);
            
        // Create the component provider cache
        ComponentProviderCache cpc = new ComponentProviderCache(
                    this.injectableFactory,
                    this.provider,
                    config.getClasses(),
                    config.getSingletons());


        // Obtain all context resolvers
        final ContextResolverFactory crf = new ContextResolverFactory(cpc, 
                injectableFactory);
        
        // Obtain all message body readers/writers
        this.bodyContext = new MessageBodyFactory(cpc);
        // Allow injection of message body context
        injectableFactory.add(new ContextInjectableProvider<MessageBodyWorkers>(
                MessageBodyWorkers.class, bodyContext));
        
        // Injection of Providers
        Providers p = new Providers() {
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
                Providers.class, p));

        // Initiate message body readers/writers
        bodyContext.init();
        
        // Inject on root client handler
        injectResources(root);
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
     * {@link ClientConfig.PROPERTY_FOLLOW_REDIRECTS} on the property bag 
     * returned from {@link #getProperties}
     * 
     * @param redirect if true then the client will automatically redirect
     *        to the URI declared in 3xx responses.
     */
    public void setFollowRedirects(Boolean redirect) {
        getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, redirect);
    }

    /**
     * Set the read timeout interval. 
     * 
     * This method is the functional equivalent to setting the property 
     * {@link ClientConfig.PROPERTY_READ_TIMEOUT} on the property bag 
     * returned from {@link #getProperties}
     * 
     * @param interval the read timeout interval. If null or 0 then
     * an interval of infinity is declared.
     */
    public void setReadTimeout(Integer interval) {
        getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, interval);  
    }
    
    /**
     * Set the connect timeout interval.
     * 
     * This method is the functional equivalent to setting the property 
     * {@link ClientConfig.PROPERTY_CONNECT_TIMEOUT} on the property bag 
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
     * {@link ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE} on the property bag 
     * returned from {@link #getProperties}
     * 
     * @param chunkSize the chunked encoding size. If &lt= 0 then the default
     *        size will be used. If null then chunked encoding will not be
     *        utilized.
     */
    public void setChunkedEncodingSize(Integer chunkSize) {
        getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, chunkSize);        
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
    
    // ClientHandler
    
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        cr.getProperties().putAll(properties);
        return getHeadHandler().handle(cr);
    }

    //
    
    private void injectResources(Object o) {
        injectableFactory.injectResources(o);
    }
        
    /**
     * Create a default client.
     * 
     * @return a default client.
     */
    public static Client create() {
        return new Client(new URLConnectionClientHandler());
    }
    
    /**
     * Create a default client with client configuration.
     * 
     * @param cc the client configuration.
     * @return a default client.
     */
    public static Client create(ClientConfig cc) {
        return new Client(new URLConnectionClientHandler(), cc);
    }
    
    /**
     * Create a default client with client configuration and component provider.
     * 
     * @param cc the client configuration.
     * @param cp the component provider.
     * @return a default client.
     */
    public static Client create(ClientConfig cc, ComponentProvider cp) {
        return new Client(new URLConnectionClientHandler(), cc, cp);
    }
}