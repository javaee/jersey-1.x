/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.client.apache4;

import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;

/**
 * A {@link Client} that utilizes the Apache HTTP Client to send and receive
 * HTTP request and responses.
 * <p>
 * The following properties are only supported at construction of this class:
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_CONNECTION_MANAGER}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_HTTP_PARAMS}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_CREDENTIALS_PROVIDER}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_DISABLE_COOKIES}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PROXY_URI}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PROXY_USERNAME}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PROXY_PASSWORD}}<br>
 * {@link com.sun.jersey.client.apache4.config.ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION}}<br>
 * <p>
 * The default behaviour of the client is different than what it described in the
 * the property {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE}. By default, the apache client
 * uses chunked encoding to write the entity. In order to buffer the entity the property
 * {@link ApacheHttpClient4Config#PROPERTY_ENABLE_BUFFERING} must be set to true.
 * <p>
 * Using of authorization is dependent on the chunk encoding setting. If the entity
 * buffering is enabled, the entity is buffered and authorization can be performed
 * automatically in response to a 401 by sending the request again. When entity buffering
 * is disabled (chunked encoding is used) then the property
 * {@link ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION} must
 * be set to {@code true}.
 * <p/>
 * If a {@link com.sun.jersey.api.client.ClientResponse} is obtained and an
 * entity is not read from the response then
 * {@link com.sun.jersey.api.client.ClientResponse#close() } MUST be called
 * after processing the response to release connection-based resources.
 *
 * @see ApacheHttpClient4Config#PROPERTY_CONNECTION_MANAGER
 *
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public class ApacheHttpClient4 extends Client {

    private final ApacheHttpClient4Handler client4Handler;

    /**
     * Create a new client instance.
     *
     */
    public ApacheHttpClient4() {
        this(createDefaultClientHandler(null), new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     */
    public ApacheHttpClient4(final ApacheHttpClient4Handler root) {
        this(root, new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance with a client configuration.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     */
    public ApacheHttpClient4(final ApacheHttpClient4Handler root, final ClientConfig config) {
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
    public ApacheHttpClient4(final ApacheHttpClient4Handler root, final ClientConfig config,
                             final IoCComponentProviderFactory provider) {
        super(root, config, provider);

        this.client4Handler = root;
    }

    /**
     * Get the Apache HTTP client handler.
     *
     * @return the Apache HTTP client handler.
     */
    public ApacheHttpClient4Handler getClientHandler() {
        return client4Handler;
    }

    /**
     * Create a default client.
     *
     * @return a default client.
     */
    public static ApacheHttpClient4 create() {
        return new ApacheHttpClient4(createDefaultClientHandler(null));
    }

    /**
     * Create a default client with client configuration.
     *
     * @param cc the client configuration.
     * @return a default client.
     */
    public static ApacheHttpClient4 create(final ClientConfig cc) {
        return new ApacheHttpClient4(createDefaultClientHandler(cc), cc);
    }

    /**
     * Create a default client with client configuration and component provider.
     *
     * @param cc the client configuration.
     * @param provider the IoC component provider factory.
     * @return a default client.
     */
    public static ApacheHttpClient4 create(final ClientConfig cc, final IoCComponentProviderFactory provider) {
        return new ApacheHttpClient4(createDefaultClientHandler(cc), cc, provider);
    }

    /**
     * Create a default Apache HTTP client handler.
     *
     * @param cc ClientConfig instance. Might be null.
     *
     * @return a default Apache HTTP client handler.
     */
    private static ApacheHttpClient4Handler createDefaultClientHandler(final ClientConfig cc) {

        Object connectionManager = null;
        Object httpParams = null;

        if(cc != null) {
            connectionManager = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER);
            if(connectionManager != null) {
                if(!(connectionManager instanceof ClientConnectionManager)) {
                    Logger.getLogger(ApacheHttpClient4.class.getName()).log(
                            Level.WARNING,
                            "Ignoring value of property " + ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER +
                                    " (" + connectionManager.getClass().getName() +
                                    ") - not instance of org.apache.http.conn.ClientConnectionManager."
                    );
                    connectionManager = null;
                }
            }

            httpParams = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS);
            if(httpParams != null) {
                if(!(httpParams instanceof HttpParams)) {
                    Logger.getLogger(ApacheHttpClient4.class.getName()).log(
                            Level.WARNING,
                            "Ignoring value of property " + ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS +
                                    " (" + httpParams.getClass().getName() +
                                    ") - not instance of org.apache.http.params.HttpParams."
                    );
                    httpParams = null;
                }
            }
        }


        final DefaultHttpClient client = new DefaultHttpClient(
                (ClientConnectionManager)connectionManager,
                (HttpParams)httpParams
        );

        CookieStore cookieStore = null;
        boolean preemptiveBasicAuth = false;

        if(cc != null) {
            for(Map.Entry<String, Object> entry : cc.getProperties().entrySet())
                client.getParams().setParameter(entry.getKey(), entry.getValue());

            if (cc.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_DISABLE_COOKIES))
                client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);

            Object credentialsProvider = cc.getProperty(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER);
            if(credentialsProvider != null && (credentialsProvider instanceof CredentialsProvider)) {
                client.setCredentialsProvider((CredentialsProvider)credentialsProvider);
            }

            final Object proxyUri = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_PROXY_URI);
            if(proxyUri != null) {
                final URI u = getProxyUri(proxyUri);
                final HttpHost proxy = new HttpHost(u.getHost(), u.getPort(), u.getScheme());

                if(cc.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME) &&
                        cc.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD)) {

                    client.getCredentialsProvider().setCredentials(
                            new AuthScope(u.getHost(), u.getPort()),
                            new UsernamePasswordCredentials(
                                    cc.getProperty(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME).toString(),
                                    cc.getProperty(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD).toString())
                    );

                }
                client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }

            preemptiveBasicAuth = cc.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION);
        }

        if(client.getParams().getParameter(ClientPNames.COOKIE_POLICY) == null || !client.getParams().getParameter(ClientPNames.COOKIE_POLICY).equals(CookiePolicy.IGNORE_COOKIES)) {
            cookieStore = new BasicCookieStore();
            client.setCookieStore(cookieStore);
        }

        return new ApacheHttpClient4Handler(client, cookieStore, preemptiveBasicAuth);
    }

    private static URI getProxyUri(final Object proxy) {
        if (proxy instanceof URI) {
            return (URI) proxy;
        } else if (proxy instanceof String) {
            return URI.create((String) proxy);
        } else {
            throw new ClientHandlerException("The proxy URI (" + ApacheHttpClient4Config.PROPERTY_PROXY_URI +
                    ") property MUST be an instance of String or URI");
        }
    }
}
