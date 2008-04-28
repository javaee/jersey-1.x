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

package com.sun.jersey.api.client;

import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.ws.rest.impl.application.ComponentProviderCache;
import com.sun.ws.rest.impl.application.ContextResolverFactory;
import com.sun.ws.rest.impl.application.MessageBodyFactory;
import com.sun.ws.rest.impl.client.urlconnection.URLConnectionClientHandler;
import com.sun.jersey.spi.container.MessageBodyContext;
import com.sun.jersey.spi.resource.TypeInjectable;
import com.sun.jersey.spi.service.ComponentProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Context;

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
public class Client extends Filterable implements ClientHandler {
    private final Map<Type, TypeInjectable> injectables;

    private final ClientConfig config;
    
    private final ComponentProvider provider;
    
    private final MessageBodyContext bodyContext;
    
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
    
    private abstract class HttpContextInjectable<V> extends TypeInjectable<Context, V> {
        public Class<Context> getAnnotationClass() {
            return Context.class;
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
    
        this.injectables = new HashMap<Type, TypeInjectable>();
        
        this.config = config;
        // Allow injection of client config
        addInjectable(ResourceConfig.class,
                new HttpContextInjectable<ClientConfig>() {
                    public ClientConfig getInjectableValue(Context c) {
                        return Client.this.config;
                    }
                }
            );        
                           
        // Set up the component provider
        this.provider = (provider == null)
            ? new DefaultComponentProvider()
            : new AdaptingComponentProvider(provider);
            
        // Create the component provider cache
        ComponentProviderCache cpc = new ComponentProviderCache(this.provider, 
                config.getProviderClasses());

        // Obtain all context resolvers
        ContextResolverFactory crf = new ContextResolverFactory(cpc);
        this.injectables.putAll(crf.getInjectables());

        // Obtain all message body readers/writers
        this.bodyContext = new MessageBodyFactory(cpc);
        // Allow injection of message body context
        addInjectable(MessageBodyContext.class,
                new HttpContextInjectable<MessageBodyContext>() {
                    public MessageBodyContext getInjectableValue(Context c) {
                        return bodyContext;
                    }
                }
            );
            
        // Inject resources on root client handler
        injectResources(root);
    }
        
    /**
     * Add an injectable resource to the set maintained by the client.
     * The fieldType is used as a unique key and therefore adding an injectable
     * for a type already supported will override the existing one.
     * 
     * @param fieldType the type of the field that will be injected.
     * @param injectable the injectable for the field.
     */
    public final void addInjectable(Type fieldType, TypeInjectable injectable) {
        injectables.put(fieldType, injectable);
    }
    
    /**
     * Create a Web resource from the client.
     * 
     * @param u the URI of the resource.
     * @return the Web resource.
     */
    public final WebResource resource(String u) {
        return resource(URI.create(u));
    }
    
    /**
     * Create a Web resource from the client.
     * 
     * @param u the URI of the resource.
     * @return the Web resource.
     */
    public final WebResource resource(URI u) {
        return new WebResource(this, u);
    }
    
    // ClientHandler
    
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
        return getHeadHandler().handle(cr);
    }

    //
    
    private void injectResources(Object o) {
        injectResources(o.getClass(), o);
    }
    
    private void injectResources(Class oClass, Object o) {
        while (oClass != null) {
            for (Field f : oClass.getDeclaredFields()) {            
                TypeInjectable i = injectables.get(f.getGenericType());
                if (i != null)
                    i.inject(o, f);
            }
            oClass = oClass.getSuperclass();
        }
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