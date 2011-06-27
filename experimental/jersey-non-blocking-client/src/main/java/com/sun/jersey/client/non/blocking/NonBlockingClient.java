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
package com.sun.jersey.client.non.blocking;

import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.sun.jersey.api.client.AsyncViewResource;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.URI;
import java.util.concurrent.ExecutorService;

/**
 * A {@link Client} that utilizes the Ning Async Http client to send and receive
 * HTTP request and responses.
 * <p>
 * The following properties are only supported at construction of this class:
 * {@link com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig#PROPERTY_EXECUTOR_SERVICE}
 * {@link com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig#PROPERTY_DISABLE_COOKIES}
 * {@link com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig#PROPERTY_AUTH_USERNAME}
 * {@link com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig#PROPERTY_AUTH_PASSWORD}
 * {@link com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig#PROPERTY_AUTH_PREEMPTIVE}
 * {@link com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig#PROPERTY_AUTH_SCHEME}
 * {@link com.sun.jersey.api.client.config.ClientConfig#PROPERTY_FOLLOW_REDIRECTS}
 * {@link com.sun.jersey.api.client.config.ClientConfig#PROPERTY_CONNECT_TIMEOUT}
 * <p>
 * By default a request entity is buffered and repeatable such that
 * authorization may be performed automatically in response to a 401 response.
 * <p>
 * If a {@link com.sun.jersey.api.client.ClientResponse} is obtained and an
 * entity is not read from the response then
 * {@link com.sun.jersey.api.client.ClientResponse#close() } MUST be called 
 * after processing the response to release connection-based resources.
 *
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public class NonBlockingClient extends Client {

    private NonBlockingClientHandler clientHandlerNing;

    /**
     * Create a new client instance.
     *
     */
    public NonBlockingClient() {
        this(createDefaultClientHandler(null), new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     */
    public NonBlockingClient(final NonBlockingClientHandler root) {
        this(root, new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance with a client configuration.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     */
    public NonBlockingClient(final NonBlockingClientHandler root, final ClientConfig config) {
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
    public NonBlockingClient(final NonBlockingClientHandler root, final ClientConfig config,
                             final IoCComponentProviderFactory provider) {
        super(root, config, provider);

        this.clientHandlerNing = root;
    }

    /**
     * Get the Apache HTTP client handler.
     *
     * @return the Apache HTTP client handler.
     */
    public NonBlockingClientHandler getClientHandlerNing() {
        return clientHandlerNing;
    }

    /**
     * Create a default client.
     *
     * @return a default client.
     */
    public static NonBlockingClient create() {
        return new NonBlockingClient(createDefaultClientHandler(null));
    }

    /**
     * Create a default client with client configuration.
     *
     * @param cc the client configuration.
     * @return a default client.
     */
    public static NonBlockingClient create(final ClientConfig cc) {
        return new NonBlockingClient(createDefaultClientHandler(cc), cc);
    }

    /**
     * Create a default client with client configuration and component provider.
     *
     * @param cc the client configuration.
     * @param provider the IoC component provider factory.
     * @return a default client.
     */
    public static NonBlockingClient create(final ClientConfig cc, final IoCComponentProviderFactory provider) {
        return new NonBlockingClient(createDefaultClientHandler(cc), cc, provider);
    }

    /**
     * Create a default Apache HTTP client handler.
     *
     * @param cc ClientConfig instance. Might be null.
     *
     * @return a default Apache HTTP client handler.
     */
    private static NonBlockingClientHandler createDefaultClientHandler(final ClientConfig cc) {
        return new NonBlockingClientHandler(cc);
    }

    /**
     * Not supported for Ning client integration. See
     * {@link com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig#PROPERTY_EXECUTOR_SERVICE}.
     *
     * Always throws NotImplementedException.
     *
     * @param es not used.
     */
    @Override
    public void setExecutorService(ExecutorService es) {
        throw new NotImplementedException();
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.getClientHandlerNing().getExecutorService();
    }

//    private static URI getProxyUri(final Object proxy) {
//        if (proxy instanceof URI) {
//            return (URI) proxy;
//        } else if (proxy instanceof String) {
//            return URI.create((String) proxy);
//        } else {
//            throw new ClientHandlerException("The proxy URI (" + NonBlockingClientConfig.PROPERTY_PROXY_URI +
//                    ") property MUST be an instance of String or URI");
//        }
//    }

    public Request getRequest(ClientRequest request) {
        return getClientHandlerNing().getRequest(request);
    }

    public ClientResponse getClientResponse(Response response) {
        return getClientHandlerNing().getClientResponse(response);
    }

    @Override
    public AsyncWebResource asyncResource(URI u) {
        return new NonBlockingAsyncWebResource(this, (CopyOnWriteHashMap<String, Object>)this.getProperties(), u);
    }

    @Override
    public AsyncViewResource asyncViewResource(URI u) {
        return new NonBlockingAsyncViewResource(this, u);
    }

    /**
     * Close the underlying connections.
     */
    public void close() {
        clientHandlerNing.getHttpClient().close();
    }
}