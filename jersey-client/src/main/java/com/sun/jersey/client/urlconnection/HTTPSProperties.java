/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.client.urlconnection;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

/**
 * HTTPS properties for SSL configuration of a {@link HttpsURLConnection}.
 * <p>
 * An instance of this class may be added as a property of the {@link Client}
 * or {@link ClientRequest} using the property name
 * {@link #PROPERTY_HTTPS_PROPERTIES}.
 * 
 * @author pavel.bucek@sun.com
 */
public class HTTPSProperties {

    /**
     * HTTPS properties property.
     * 
     * The value MUST be an instance of {@link com.sun.jersey.client.urlconnection.HTTPSProperties}.
     *
     * If the property is absent then HTTPS properties will not be used.
     */
    public static final String PROPERTY_HTTPS_PROPERTIES =
            "com.sun.jersey.client.impl.urlconnection.httpsProperties";

    private HostnameVerifier hostnameVerifier = null;
    
    private SSLContext sslContext = null;

    /**
     * Construct default properties with no {@link HostnameVerifier}
     * and a {@link SSLContext} constructed using <code>SSLContext.getInstance("SSL")</code>.
     *
     * @throws java.security.NoSuchAlgorithmException if the SSLContext could not
     *         be created.
     */
    public HTTPSProperties() throws NoSuchAlgorithmException {
        this(null, SSLContext.getInstance("SSL"));
    }

    /**
     * Construct with a {@link HostnameVerifier} and a {@link SSLContext}
     * constructed using <code>SSLContext.getInstance("SSL")</code>.
     *
     * @param hv the HostnameVerifier.
     * @throws java.security.NoSuchAlgorithmException if the SSLContext could not
     *         be created.
     */
    public HTTPSProperties(HostnameVerifier hv) throws NoSuchAlgorithmException {
        this (hv, SSLContext.getInstance("SSL"));
    }

    /**
     * Construct with a {@link HostnameVerifier} and a {@link SSLContext}.
     * 
     * @param hv the HostnameVerifier.
     * @param c the SSLContext. Must not be null.
     */
    public HTTPSProperties(HostnameVerifier hv, SSLContext c) {
        if(c == null)
            throw new IllegalArgumentException("SSLContext must not be null");

        this.hostnameVerifier = hv;
        this.sslContext = c;
    }


    /**
     * Get the P{@link HostnameVerifier}.
     *
     * @return the HostnameVerifier, is <code>null</code> if not set
     *         at construction.
     */
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Get the {@link SSLContext}.
     *
     * @return the SSLContext.
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }

    /**
     * Set the {@link HttpsURLConnection} with the HTTPS properties.
     *
     * @param connection the HttpsURLConnection.
     */
    public void setConnection(HttpsURLConnection connection) {
        if (hostnameVerifier != null)
            connection.setHostnameVerifier(hostnameVerifier);
        connection.setSSLSocketFactory(sslContext.getSocketFactory());
    }
}