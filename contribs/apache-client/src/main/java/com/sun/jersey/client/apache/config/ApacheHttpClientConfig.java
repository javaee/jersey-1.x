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
package com.sun.jersey.client.apache.config;

import com.sun.jersey.api.client.config.ClientConfig;


/**
   Contains configuration options specific to clients that root with
   the {@link com.sun.jersey.impl.client.httpclient.HttpClientHandler}.

   @author jorgew
**/

public interface ApacheHttpClientConfig extends ClientConfig
{
    /**
       A value of "true" indicates that the client should
       interactively prompt for credentials should it receive a 401
       response.

       The value MUST be an instance of {@link java.lang.Boolean}.
       If the property is absent the default value is "true"
     **/
    public static final String PROPERTY_INTERACTIVE =
        "com.sun.jersey.impl.client.httpclient.interactive";

    /**
       A value of "true" indicates the client should handle cookies
       automatically using HttpClient's default cookie policy. A value
       of "false" will cause the client to ignore all cookies.

       The value MUST be an instance of {@link java.lang.Boolean}.
       If the property is absent the default value is "false"
    **/
    public static final String PROPERTY_HANDLE_COOKIES =
        "com.sun.jersey.impl.client.httpclient.handleCookies";

    /**
       The credential provider that should be used to retrive
       credentials from a user. The provider will be used only if
       PROPERTY_INTERACTIVE is set to true.

       The value MUST be an instance of {@link
       org.apache.commons.httpclient.auth.CredentialsProvider}.  If
       the property is absent a default provider will be used.
    **/
    public static final String PROPERTY_CREDENTIALS_PROVIDER =
        "com.sun.jersey.impl.client.httpclient.credentialsProvider";

    /**
       A value of "true" indicates that a client should send an
       authentication request even before the server gives a 401
       response.

       If the value of this property is set to "true" default
       credientials must be set for the target or proxy.

       The value MUST be an instance of {@link java.lang.Boolean}.
       If the property is absent the default value is "false"
     **/
    public static final String PROPERTY_PREEMPTIVE_AUTHENTICATION =
        "com.sun.jersey.impl.client.httpclient.preemptiveAuthentication";

    /**
       A value of "true" indicates that a client should use a proxy
       when connecting to a host. {@link #PROPERTY_PROXY_HOST} and
       {@link #PROPERTY_PROXY_PORT} MUST be set.

       The value MUST be an instance of {@link java.lang.Boolean}. If
       the property is absent the default value is "false".
     **/
    public static final String PROPERTY_PROXY_SET =
        "com.sun.jersey.impl.client.httpclient.proxySet";

    /**
       A value indicating the proxy host to use.  The proxy host will
       only be set if {@link #PROPERTY_PROXY_SET} is set to "true".

       The value MUST be an instance of {@link java.lang.String}.  If
       the property is absent the default value is "localhost".
     **/
    public static final String PROPERTY_PROXY_HOST =
        "com.sun.jersey.impl.client.httpclient.proxyHost";

    /**
       A value indicating the proxy port to use.  The proxy port will
       only be set if {@link #PROPERTY_PROXY_SET} is set to "true".

       The value MUST be an instance of {@link java.lang.Integer}. If
       the property is absent the default value is 8080.
     **/
    public static final String PROPERTY_PROXY_PORT =
        "com.sun.jersey.impl.client.httpclient.proxyPort";

    /**
       Sets the credentials for the given authentication scope.  Any
       previous credentials for the given scope will be overwritten.

       @param realm The authentication realm.  The null realm
       signifies default credentials for the given host, which should
       be used when no credentials have been explicitly supplied for
       the challenging realm.
       @param host The host the realm belongs to. The null host
       signifies default credentials which should be used when no
       credentials have been explicitly supplied for the challenging
       host.
       @param port The port the realm belongs to. A negitive port
       signifies the credentials are applicaple to any port when no
       credentials have been explicitly supplied for the challenging
       port.
       @param usernamepassword The username:password formed string.
    **/
    public void setCredentials(String realm, String host, int port,
                               String usernamepassword);

    /**
       Sets the credentials for the given authentication scope.  Any
       previous credentials for the given scope will be overwritten.

       @param realm The authentication realm.  The null realm
       signifies default credentials for the given host, which should
       be used when no credentials have been explicitly supplied for
       the challenging realm.
       @param host The host the realm belongs to. The null host
       signifies default credentials which should be used when no
       credentials have been explicitly supplied for the challenging
       host.
       @param port The port the realm belongs to. A negitive port
       signifies the credentials are applicaple to any port when no
       credentials have been explicitly supplied for the challenging
       port.
       @param username The username
       @param password The password
    **/
    public void setCredentials(String realm, String host, int port,
                               String username, String password);

    /**
       Sets the credentials for the given authentication scope.  Any
       previous credentials for the given scope will be overwritten.

       This method should be used when setting credentials for the
       NTLM authentication scheme.

       @param realm The authentication realm.  The null realm
       signifies default credentials for the given host, which should
       be used when no credentials have been explicitly supplied for
       the challenging realm.
       @param host The host the realm belongs to. The null host
       signifies default credentials which should be used when no
       credentials have been explicitly supplied for the challenging
       host.
       @param port The port the realm belongs to. A negitive port
       signifies the credentials are applicaple to any port when no
       credentials have been explicitly supplied for the challenging
       port.
       @param username The username, this should not include the
       domain to authenticate with. For example: "user" is correct
       wheareas "DOMAIN\\user" is not.
       @param password The password
       @param thisHost The host the authentication requiest is originating
       from. Essentially, the computer name for this machine.
       @param domain The domain to authentice with.
    **/
    public void setCredentials(String realm, String host, int port,
                               String username, String password,
                               String thisHost, String domain);

    /**
       Sets the proxy credentials for the given authentication scope.
       Any previous credentials for the given scope will be
       overwritten.

       @param realm The authentication realm.  The null realm
       signifies default credentials for the given host, which should
       be used when no credentials have been explicitly supplied for
       the challenging realm.
       @param host The host the realm belongs to. The null host
       signifies default credentials which should be used when no
       credentials have been explicitly supplied for the challenging
       host.
       @param port The port the realm belongs to. A negitive port
       signifies the credentials are applicaple to any port when no
       credentials have been explicitly supplied for the challenging
       port.
       @param usernamepassword The username:password formed string.
    **/
    public void setProxyCredentials(String realm, String host, int port,
                                    String usernamepassword);

    /**
       Sets the proxy credentials for the given authentication scope.
       Any previous credentials for the given scope will be
       overwritten.

       @param realm The authentication realm.  The null realm
       signifies default credentials for the given host, which should
       be used when no credentials have been explicitly supplied for
       the challenging realm.
       @param host The host the realm belongs to. The null host
       signifies default credentials which should be used when no
       credentials have been explicitly supplied for the challenging
       host.
       @param port The port the realm belongs to. A negitive port
       signifies the credentials are applicaple to any port when no
       credentials have been explicitly supplied for the challenging
       port.
       @param username The username
       @param password The password
    **/
    public void setProxyCredentials(String realm, String host, int port,
                                    String username, String password);

    /**
       Sets the proxy credentials for the given authentication scope.
       Any previous credentials for the given scope will be
       overwritten.

       This method should be used when setting credentials for the
       NTLM authentication scheme.

       @param realm The authentication realm.  The null realm
       signifies default credentials for the given host, which should
       be used when no credentials have been explicitly supplied for
       the challenging realm.
       @param host The host the realm belongs to. The null host
       signifies default credentials which should be used when no
       credentials have been explicitly supplied for the challenging
       host.
       @param port The port the realm belongs to. A negitive port
       signifies the credentials are applicaple to any port when no
       credentials have been explicitly supplied for the challenging
       port.
       @param username The username, this should not include the
       domain to authenticate with. For example: "user" is correct
       wheareas "DOMAIN\\user" is not.
       @param password The password
       @param thisHost The host the authentication requiest is originating
       from. Essentially, the computer name for this machine.
       @param domain The domain to authentice with.
    **/
    public void setProxyCredentials(String realm, String host, int port,
                                    String username, String password,
                                    String thisHost, String domain);

    /**
       Clears all credentials.
    **/
    public void clearCredentials();

    /**
       Clears all proxy credentials.
    **/
    public void clearProxyCredentials();
}
