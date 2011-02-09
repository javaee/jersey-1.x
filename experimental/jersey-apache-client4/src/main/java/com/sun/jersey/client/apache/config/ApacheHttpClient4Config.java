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
package com.sun.jersey.client.apache.config;

import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Configuration options specific to the Client API that utilizes
 * {@link com.sun.jersey.client.apache.ApacheHttpClient4} or {@link com.sun.jersey.client.apache.ApacheHttpClient4Handler}.
 *
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public interface ApacheHttpClient4Config extends ClientConfig {

//    /**
//     * A value of "true" indicates that the client should
//     * interactively prompt for credentials should it receive a 401
//     * response.
//     *
//     * The value MUST be an instance of {@link java.lang.Boolean}.
//     * If the property is absent the default value is "false"
//     */
//    public static final String PROPERTY_INTERACTIVE =
//            "com.sun.jersey.impl.client.httpclient.interactive";

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

//    /**
//     * The credential provider that should be used to retrieve
//     * credentials from a user. The provider will be used only if
//     * {@link #PROPERTY_INTERACTIVE} is set to true.
//     *
//     * The value MUST be an instance of {@link
//     * org.apache.commons.httpclient.auth.CredentialsProvider}.  If
//     * the property is absent a default provider will be used.
//     */
//    public static final String PROPERTY_CREDENTIALS_PROVIDER =
//            "com.sun.jersey.impl.client.httpclient.credentialsProvider";

//    /**
//     * A value of "true" indicates that a client should send an
//     * authentication request even before the server gives a 401
//     * response.
//     *
//     * This property may only be set when constructing a {@link com.sun.jersey.client.apache.ApacheHttpClient4}
//     * instance.
//     *
//     * If the value of this property is set to "true" default
//     * credientials must be set for the target or proxy.
//     *
//     * The value MUST be an instance of {@link java.lang.Boolean}.
//     * If the property is absent the default value is "false"
//     */
//    public static final String PROPERTY_PREEMPTIVE_AUTHENTICATION =
//            "com.sun.jersey.impl.client.httpclient.preemptiveAuthentication";
//
//    /**
//     * A value of a URI to configure the proxy host and proxy port to proxy
//     * HTTP requests and responses. If the port component of the URI is absent
//     * then a default port of 8080 be selected.
//     *
//     * The value MUST be an instance of {@link String} or {@link java.net.URI}.
//     * If the property absent then no proxy will be utilized.
//     */
//    public static final String PROPERTY_PROXY_URI =
//            "com.sun.jersey.impl.client.httpclient.proxyURI";

    /**
     * The {@link ApacheHttpClient4State} of the current client.
     * This is used to maintain authentication credentials.
     *
     * The value MUST be an instance of {@link ApacheHttpClient4State}.
     */
    public static final String PROPERTY_HTTP_STATE =
            "com.sun.jersey.impl.client.httpclient.httpState";

    /**
     * Get the HTTP state. Credentials may be set on the HTTP state.
     * <p>
     * If no state exists then an instance is created and added
     * as the property {@link #PROPERTY_HTTP_STATE}.
     *
     * @return the HTTP state.
     */
    public ApacheHttpClient4State getState();
}
