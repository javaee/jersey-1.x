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

import java.util.Map;

import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
   A default client configuration for clients that root with the
   {@link
   com.sun.jersey.impl.client.httpclient.HttpClientHandler}. This
   class may be extended for specific configuration purposes.

   @author jorgew
**/

public class DefaultApacheHttpClientConfig extends DefaultClientConfig
    implements ApacheHttpClientConfig
{
    /**
       The HttpState of the current client.  This is used to maintain
       authentication credentials.

       The value MUST be an instance of {@link
       org.apache.commons.httpclient.HttpState}.

       This shouldn't be used directly, instead use {@link
       #setCredentials}, {@link #setProxyCredentials}, {@link
       #clearCredentials}, or {@link #clearProxyCredentials}.
    **/
    public static final String PROPERTY_HTTP_STATE =
        "com.sun.jersey.impl.client.httpclient.httpState";

    private HttpState getHttpState()
    {
        Map<String, Object> props = getProperties();
        HttpState state = (HttpState) props.get(PROPERTY_HTTP_STATE);

        if (state == null) {
            state = new HttpState();
            props.put (PROPERTY_HTTP_STATE, state);
        }

        return state;
    }

    public void setCredentials(String realm, String host, int port,
                               String usernamepassword)
    {
        AuthScope authScope = new AuthScope (host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials (usernamepassword);
        HttpState state = getHttpState();

        state.setCredentials (authScope, creds);
    }

    public void setCredentials(String realm, String host, int port,
                               String username, String password)
    {
        AuthScope authScope = new AuthScope (host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials (username,password);
        HttpState state = getHttpState();

        state.setCredentials (authScope, creds);
    }

    public void setCredentials(String realm, String host, int port,
                               String username, String password,
                               String domain, String thisHost)
    {
        AuthScope authScope = new AuthScope (host, port, realm);
        NTCredentials creds = new NTCredentials (username,password, thisHost, domain);
        HttpState state = getHttpState();

        state.setCredentials (authScope, creds);
    }

    public void setProxyCredentials(String realm, String host, int port,
                                    String usernamepassword)
    {
        AuthScope authScope = new AuthScope (host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials (usernamepassword);
        HttpState state = getHttpState();

        state.setProxyCredentials (authScope, creds);
    }

    public void setProxyCredentials(String realm, String host, int port,
                                    String username, String password)
    {
        AuthScope authScope = new AuthScope (host, port, realm);
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials (username,password);
        HttpState state = getHttpState();

        state.setProxyCredentials (authScope, creds);
    }

    public void setProxyCredentials(String realm, String host, int port,
                                    String username, String password,
                                    String domain, String thisHost)
    {
        AuthScope authScope = new AuthScope (host, port, realm);
        NTCredentials creds = new NTCredentials (username,password, thisHost, domain);
        HttpState state = getHttpState();

        state.setProxyCredentials (authScope, creds);
    }

    public void clearCredentials()
    {
        HttpState state = getHttpState();
        state.clearCredentials();
    }

    public void clearProxyCredentials()
    {
        HttpState state = getHttpState();
        state.clearProxyCredentials();
    }
}

