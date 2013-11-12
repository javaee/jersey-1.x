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
package com.sun.jersey.client.apache4.config;

import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Configuration options specific to the Client API that utilizes
 * {@link com.sun.jersey.client.apache4.ApacheHttpClient4} or {@link com.sun.jersey.client.apache4.ApacheHttpClient4Handler}.
 *
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public interface ApacheHttpClient4Config extends ClientConfig {

    /**
     * A value of "false" indicates the client should handle cookies
     * automatically using HttpClient's default cookie policy. A value
     * of "true" will cause the client to ignore all cookies.
     *
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent the default value is "false"
     */
    public static final String PROPERTY_DISABLE_COOKIES =
            "com.sun.jersey.impl.client.httpclient.handleCookies";

    /**
     * The credential provider that should be used to retrieve
     * credentials from a user. Credentials needed for proxy authentication
     * are stored here as well.
     *
     * The value MUST be an instance of {@link
     * org.apache.http.client.CredentialsProvider}.  If
     * the property is absent a default provider will be used.
     */
    public static final String PROPERTY_CREDENTIALS_PROVIDER =
            "com.sun.jersey.impl.client.httpclient.credentialsProvider";

    /**
     * A value of "true" indicates that a client should send an
     * authentication request even before the server gives a 401
     * response.
     *
     * This property may only be set when constructing a {@link com.sun.jersey.client.apache4.ApacheHttpClient4}
     * instance.
     *
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent the default value is "false"
     */
    public static final String PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION =
            "com.sun.jersey.impl.client.httpclient.preemptiveBasicAuthentication";

    /**
     * Connection Manager which will be used to create {@link org.apache.http.client.HttpClient}.
     *
     * The value MUST be an instance of {@link org.apache.http.conn.ClientConnectionManager}.
     * If the property is absent a default Connection Manager will be used ({@link org.apache.http.impl.conn.SingleClientConnManager}).
     * If you want to use this client in multi-threaded environment, be sure you override
     * default value with {@link org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager} instance.
     */
    public static final String PROPERTY_CONNECTION_MANAGER =
            "com.sun.jersey.impl.client.httpclient.connectionManager";

    /**
     * Http parameters which will be used to create {@link org.apache.http.client.HttpClient}.
     *
     * The value MUST be an instance of {@link org.apache.http.params.HttpParams}.
     * If the property is absent default http parameters will be used.
     */
    public static final String PROPERTY_HTTP_PARAMS =
            "com.sun.jersey.impl.client.httpclient.httpParams";

    /**
     * A value of a URI to configure the proxy host and proxy port to proxy
     * HTTP requests and responses. If the port component of the URI is absent
     * then a default port of 8080 be selected.
     *
     * The value MUST be an instance of {@link String} or {@link java.net.URI}.
     * If the property absent then no proxy will be utilized.
     */
    public static final String PROPERTY_PROXY_URI =
            "com.sun.jersey.impl.client.httpclient.proxyURI";

    /**
     * User name which will be used for proxy authentication.
     *
     * The value MUST be an instance of {@link String}.
     * If the property absent then no proxy authentication will be utilized.
     */
    public static final String PROPERTY_PROXY_USERNAME =
            "com.sun.jersey.impl.client.httpclient.proxyUsername";

    /**
     * Password which will be used for proxy authentication.
     *
     * The value MUST be an instance of {@link String}.
     * If the property absent then no proxy authentication will be utilized.
     */
    public static final String PROPERTY_PROXY_PASSWORD =
            "com.sun.jersey.impl.client.httpclient.proxyPassword";

    /**
     * If {@code true} then chunk encoding will be disabled and entity will be buffered
     * in the client in order to calculate the size of the entity. When property
     * is {@code false} then chunk encoding will be enabled. In that case the
     * property {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE} can be
     * used to control the size of the chunk.
     * <p>
     * Note that the behaviour of the http client differs from the default client
     * configuration in the way that the chunk encoding is enabled by default and must
     * be disabled if needed. When entity buffering is enabled then the whole entity is
     * buffered and might cause out of memory errors if the entity is too large.
     * <p/>
     * <p>
     * Property must be of a {@link Boolean} type. Default value is {@code false}.
     * </p>
     *
     */
    public static final String PROPERTY_ENABLE_BUFFERING =
            "com.sun.jersey.impl.client.httpclient.enableBuffering";

}
