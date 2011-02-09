/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * An encapsulation of the Apache {@link HttpState} that provides easier
 * functionality for setting up credentials and proxy credentials.
 * 
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 */
public class ApacheHttpClientState {
    private final HttpState state;

    /**
     * Create a new instance with a default instance of an Apache HTTP state.
     */
    public ApacheHttpClientState() {
        this(new HttpState());
    }

    /**
     * Create a new state instance with an Apacahe HTTP state instance.
     * 
     * @param state the Apache HTTP state.
     */
    public ApacheHttpClientState(HttpState state) {
        this.state = state;
    }

    /**
     *
     * Sets the credentials for the given authentication scope.  Any
     * previous credentials for the given scope will be overwritten.
     *
     * @param realm The authentication realm.  The null realm
     * signifies default credentials for the given host, which should
     * be used when no credentials have been explicitly supplied for
     * the challenging realm.
     * @param host The host the realm belongs to. The null host
     * signifies default credentials which should be used when no
     * credentials have been explicitly supplied for the challenging
     * host.
     * @param port The port the realm belongs to. A negitive port
     * signifies the credentials are applicaple to any port when no
     * credentials have been explicitly supplied for the challenging
     * port.
     * @param usernamepassword The username:password formed string.
     */
    public void setCredentials(String realm, String host, int port,
            String usernamepassword) {
        AuthScope authScope = new AuthScope(host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(usernamepassword);

        getHttpState().setCredentials(authScope, creds);
    }

    /**
     * Sets the credentials for the given authentication scope.  Any
     * previous credentials for the given scope will be overwritten.
     *
     * @param realm The authentication realm.  The null realm
     * signifies default credentials for the given host, which should
     * be used when no credentials have been explicitly supplied for
     * the challenging realm.
     * @param host The host the realm belongs to. The null host
     * signifies default credentials which should be used when no
     * credentials have been explicitly supplied for the challenging
     * host.
     * @param port The port the realm belongs to. A negitive port
     * signifies the credentials are applicaple to any port when no
     * credentials have been explicitly supplied for the challenging
     * port.
     * @param username The username
     * @param password The password
     */
    public void setCredentials(String realm, String host, int port,
            String username, String password) {
        AuthScope authScope = new AuthScope(host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);

        getHttpState().setCredentials(authScope, creds);
    }

    /**
     * Sets the credentials for the given authentication scope.  Any
     * previous credentials for the given scope will be overwritten.
     *
     * This method should be used when setting credentials for the
     * NTLM authentication scheme.
     *
     * @param realm The authentication realm.  The null realm
     * signifies default credentials for the given host, which should
     * be used when no credentials have been explicitly supplied for
     * the challenging realm.
     * @param host The host the realm belongs to. The null host
     * signifies default credentials which should be used when no
     * credentials have been explicitly supplied for the challenging
     * host.
     * @param port The port the realm belongs to. A negitive port
     * signifies the credentials are applicaple to any port when no
     * credentials have been explicitly supplied for the challenging
     * port.
     * @param username The username, this should not include the
     * domain to authenticate with. For example: "user" is correct
     * wheareas "DOMAIN\\user" is not.
     * @param password The password
     * @param thisHost The host the authentication requiest is originating
     * from. Essentially, the computer name for this machine.
     * @param domain The domain to authentice with.
     */
    public void setCredentials(String realm, String host, int port,
            String username, String password,
            String domain, String thisHost) {
        AuthScope authScope = new AuthScope(host, port, realm);
        NTCredentials creds = new NTCredentials(username, password, thisHost, domain);

        getHttpState().setCredentials(authScope, creds);
    }

    /**
     * Sets the proxy credentials for the given authentication scope.
     * Any previous credentials for the given scope will be
     * overwritten.
     *
     * @param realm The authentication realm.  The null realm
     * signifies default credentials for the given host, which should
     * be used when no credentials have been explicitly supplied for
     * the challenging realm.
     * @param host The host the realm belongs to. The null host
     * signifies default credentials which should be used when no
     * credentials have been explicitly supplied for the challenging
     * host.
     * @param port The port the realm belongs to. A negitive port
     * signifies the credentials are applicaple to any port when no
     * credentials have been explicitly supplied for the challenging
     * port.
     * @param usernamepassword The username:password formed string.
     */
    public void setProxyCredentials(String realm, String host, int port,
            String usernamepassword) {
        AuthScope authScope = new AuthScope(host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(usernamepassword);

        getHttpState().setProxyCredentials(authScope, creds);
    }

    /**
     * Sets the proxy credentials for the given authentication scope.
     * Any previous credentials for the given scope will be
     * overwritten.
     *
     * @param realm The authentication realm.  The null realm
     * signifies default credentials for the given host, which should
     * be used when no credentials have been explicitly supplied for
     * the challenging realm.
     * @param host The host the realm belongs to. The null host
     * signifies default credentials which should be used when no
     * credentials have been explicitly supplied for the challenging
     * host.
     * @param port The port the realm belongs to. A negitive port
     * signifies the credentials are applicaple to any port when no
     * credentials have been explicitly supplied for the challenging
     * port.
     * @param username The username
     * @param password The password
     */
    public void setProxyCredentials(String realm, String host, int port,
            String username, String password) {
        AuthScope authScope = new AuthScope(host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);

        getHttpState().setProxyCredentials(authScope, creds);
    }

    /**
     * Sets the proxy credentials for the given authentication scope.
     * Any previous credentials for the given scope will be
     * overwritten.
     *
     * This method should be used when setting credentials for the
     * NTLM authentication scheme.
     *
     * @param realm The authentication realm.  The null realm
     * signifies default credentials for the given host, which should
     * be used when no credentials have been explicitly supplied for
     * the challenging realm.
     * @param host The host the realm belongs to. The null host
     * signifies default credentials which should be used when no
     * credentials have been explicitly supplied for the challenging
     * host.
     * @param port The port the realm belongs to. A negitive port
     * signifies the credentials are applicaple to any port when no
     * credentials have been explicitly supplied for the challenging
     * port.
     * @param username The username, this should not include the
     * domain to authenticate with. For example: "user" is correct
     * wheareas "DOMAIN\\user" is not.
     * @param password The password
     * @param thisHost The host the authentication requiest is originating
     * from. Essentially, the computer name for this machine.
     * @param domain The domain to authentice with.
     */
    public void setProxyCredentials(String realm, String host, int port,
            String username, String password,
            String domain, String thisHost) {
        AuthScope authScope = new AuthScope(host, port, realm);
        NTCredentials creds = new NTCredentials(username, password, thisHost, domain);

        getHttpState().setProxyCredentials(authScope, creds);
    }

    /**
     * Clears all credentials.
     */
    public void clearCredentials() {
        getHttpState().clearCredentials();
    }

    /**
     * Clears all proxy credentials.
     */
    public void clearProxyCredentials() {
        getHttpState().clearProxyCredentials();
    }

    /**
     * Get the HTTP state.
     *
     * @return the HTTP state.
     */
    public HttpState getHttpState() {
        return state;
    }
}