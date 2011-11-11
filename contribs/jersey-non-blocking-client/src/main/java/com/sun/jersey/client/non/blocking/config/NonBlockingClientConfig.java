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
package com.sun.jersey.client.non.blocking.config;

import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Configuration options specific to the Client API that utilizes
 * {@link com.sun.jersey.client.non.blocking.NonBlockingClient} or {@link com.sun.jersey.client.non.blocking.NonBlockingClientHandler}.
 *
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public interface NonBlockingClientConfig extends ClientConfig {

    /**
     * Executor Service
     *
     * The value MUST be an instance of {@link java.util.concurrent.ExecutorService}.
     */
    public static final String PROPERTY_EXECUTOR_SERVICE =
            "com.sun.jersey.impl.client.non.blocking.executorService";

    /**
     * A value of "false" indicates the client should handle cookies
     * automatically using HttpClient's default cookie policy. A value
     * of "true" will cause the client to ignore all cookies.
     *
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent the default value is "false"
     */
    public static final String PROPERTY_DISABLE_COOKIES =
            "com.sun.jersey.impl.client.non.blocking.handleCookies";

    /**
     * Username used for authentication.
     */
    public static final String PROPERTY_AUTH_USERNAME =
            "com.sun.jersey.impl.client.non.blocking.auth.userName";

    /**
     * Password used for authentication.
     */
    public static final String PROPERTY_AUTH_PASSWORD =
            "com.sun.jersey.impl.client.non.blocking.auth.password";

    /**
     * Use preemptive authentication.
     *
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent then the default value is "false".
     */
    public static final String PROPERTY_AUTH_PREEMPTIVE =
            "com.sun.jersey.impl.client.non.blocking.auth.preemptive";

    /**
     * Used authentication scheme.
     *
     * The value MUST be an instance of {@link java.lang.String}.
     * Default value is "BASIC" (applies only when username and
     * password are set).
     *
     * Valid values: "BASIC", "DIGEST", "NTLM", "KERBEROS", "SPNEGO".
     */
    public static final String PROPERTY_AUTH_SCHEME =
            "com.sun.jersey.impl.client.non.blocking.auth.scheme";

    /**
     * Proxy host.
     *
     * The value MUST be an instance of {@link java.lang.String}.
     */
    public static final String PROPERTY_PROXY_HOST =
            "com.sun.jersey.impl.client.non.blocking.proxy.host";

    /**
     * Proxy port.
     *
     * The value MUST be an instance of {@link java.lang.Integer}.
     */
    public static final String PROPERTY_PROXY_PORT =
            "com.sun.jersey.impl.client.non.blocking.proxy.port";

    /**
     * Username used for proxy authentication..
     *
     * The value MUST be an instance of {@link java.lang.String}.
     */
    public static final String PROPERTY_PROXY_USERNAME =
            "com.sun.jersey.impl.client.non.blocking.proxy.username";

    /**
     * Password used for proxy authentication..
     *
     * The value MUST be an instance of {@link java.lang.String}.
     */
    public static final String PROPERTY_PROXY_PASSWORD =
            "com.sun.jersey.impl.client.non.blocking.proxy.password";

    /**
     * Proxy protocol
     *
     * The value MUST be an instance of {@link java.lang.String}.
     *
     * Valid values: "HTTP", "HTTPS", "NTLM", "KERBEROS", "SPNEGO".
     */
    public static final String PROPERTY_PROXY_PROTOCOL =
            "com.sun.jersey.impl.client.non.blocking.proxy.protocol";

    /**
     * Request filter(s) which will be applied to modify request prior dispatching.
     * <p>
     * The instance may be a {@link com.ning.http.client.filter.RequestFilter} instance or
     * List of {@link com.ning.http.client.filter.RequestFilter} instances.
     *
     * @see com.ning.http.client.filter.RequestFilter
     */
    public static final String PROPERTY_REQUEST_FILTERS =
            "com.sun.jersey.impl.client.non.blocking.requestFilter";

    /**
     * Response filter(s) which will be applied after recieving the response.
     * <p>
     * The instance may be a {@link com.ning.http.client.filter.ResponseFilter} instance or
     * List of {@link com.ning.http.client.filter.ResponseFilter} instances.
     *
     * @see com.ning.http.client.filter.ResponseFilter
     */
    public static final String PROPERTY_RESPONSE_FILTERS =
            "com.sun.jersey.impl.client.non.blocking.responseFilter";
}
