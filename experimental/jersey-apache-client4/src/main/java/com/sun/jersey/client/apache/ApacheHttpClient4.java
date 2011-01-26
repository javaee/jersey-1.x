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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.client.apache;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.apache.config.ApacheHttpClient4Config;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.Map;

/**
 * A {@link Client} that utilizes the Apache HTTP client to send and receive
 * HTTP request and responses.
 * <p>
 * If an {@link ApacheHttpClient4Handler} is not explicitly passed as a
 * constructor or method parameter then by default an instance is created with
 * an {@link HttpClient} constructed with a {@link MultiThreadedHttpConnectionManager}
 * instance.
 * <p>
 * The following properties are only supported at construction of this class:
 * {@link com.sun.jersey.client.apache.config.ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_AUTHENTICATION} and
 * {@link ClientConfig#PROPERTY_CONNECT_TIMEOUT}.
 * <p>
 * By default a request entity is buffered and repeatable such that
 * authorization may be performed automatically in response to a 401 response.
 * <p>
 * If the property {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE} size
 * is set to a value greater than 0 then chunked encoding will be enabled
 * and the request entity (if present) will not be buffered and is not
 * repeatable. For authorization to work in such scenarios the property
 * {@link com.sun.jersey.client.apache.config.ApacheHttpClient4Config#PROPERTY_PREEMPTIVE_AUTHENTICATION} must
 * be set to true.
 * <p>
 * If a response entity is obtained that is an instance of
 * {@link java.io.Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
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
public class ApacheHttpClient4 extends Client {

    private ApacheHttpClient4Handler client4Handler;
    
    /**
     * Create a new client instance.
     *
     */
    public ApacheHttpClient4() {
        this(createDefaultClientHander(null), new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     */
    public ApacheHttpClient4(ApacheHttpClient4Handler root) {
        this(root, new DefaultClientConfig(), null);
    }

    /**
     * Create a new client instance with a client configuration.
     *
     * @param root the root client handler for dispatching a request and
     *        returning a response.
     * @param config the client configuration.
     */
    public ApacheHttpClient4(ApacheHttpClient4Handler root, ClientConfig config) {
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
    public ApacheHttpClient4(ApacheHttpClient4Handler root, ClientConfig config,
            IoCComponentProviderFactory provider) {
        super(root, config, provider);

        this.client4Handler = root;

//        HttpClient client = root.getHttpClient();
        
//        client.getParams().setAuthenticationPreemptive(
//                config.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_AUTHENTICATION));

//        final Integer connectTimeout = (Integer)config.getProperty(ApacheHttpClient4Config.PROPERTY_CONNECT_TIMEOUT);
//        if (connectTimeout != null) {
//            client.getConnectionManager(). .getParams().setConnectionTimeout(connectTimeout);
//        }
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
        return new ApacheHttpClient4(createDefaultClientHander(null));
    }

    /**
     * Create a default client with client configuration.
     *
     * @param cc the client configuration.
     * @return a default client.
     */
    public static ApacheHttpClient4 create(ClientConfig cc) {
        return new ApacheHttpClient4(createDefaultClientHander(cc), cc);
    }

    /**
     * Create a default client with client configuration and component provider.
     *
     * @param cc the client configuration.
     * @param provider the IoC component provider factory.
     * @return a default client.
     */
    public static ApacheHttpClient4 create(ClientConfig cc, IoCComponentProviderFactory provider) {
        return new ApacheHttpClient4(createDefaultClientHander(cc), cc, provider);
    }

    /**
     * Create a default Apache HTTP client handler.
     *
     * @param cc ClientConfig instance. Might be null.
     *
     * @return a default Apache HTTP client handler.
     */
    private static ApacheHttpClient4Handler createDefaultClientHander(ClientConfig cc) {
        final HttpClient client = new DefaultHttpClient();
        CookieStore cookieStore = null;

        if(cc != null) {
            for(Map.Entry<String, Object> entry : cc.getProperties().entrySet())
                client.getParams().setParameter(entry.getKey(), entry.getValue());

            if (cc.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_DISABLE_COOKIES))
                client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        }

        if(client.getParams().getParameter(ClientPNames.COOKIE_POLICY) == null || !client.getParams().getParameter(ClientPNames.COOKIE_POLICY).equals(CookiePolicy.IGNORE_COOKIES)) {
            cookieStore = new BasicCookieStore();
            ((DefaultHttpClient)client).setCookieStore(cookieStore);
        }

        return new ApacheHttpClient4Handler(client, cookieStore);
    }
}