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

package com.sun.jersey.api.client.filter;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.ws.rs.core.HttpHeaders;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.Base64;

/**
 * Client filter adding HTTP Basic Authentication header to the HTTP request,
 * if no such header is already present
 *
 * @author Jakub.Podlesak@Sun.COM, Craig.McClanahan@Sun.COM
 */
public final class HTTPBasicAuthFilter extends ClientFilter {

    private final String authentication;
    static private final Charset CHARACTER_SET = Charset.forName("iso-8859-1");

    /**
     * Creates a new HTTP Basic Authentication filter using provided username
     * and password credentials. This constructor allows you to avoid storing
     * plain password value in a String variable.
     *
     * @param username
     * @param password
     */
    public HTTPBasicAuthFilter(final String username, final byte[] password) {
        try {

            final byte[] prefix = (username + ":").getBytes(CHARACTER_SET);
            final byte[] usernamePassword = new byte[prefix.length + password.length];

            System.arraycopy(prefix, 0, usernamePassword, 0, prefix.length);
            System.arraycopy(password, 0, usernamePassword, prefix.length, password.length);

            authentication = "Basic " + new String(Base64.encode(usernamePassword), "ASCII");
        } catch (UnsupportedEncodingException ex) {
            // This should never occur
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates a new HTTP Basic Authentication filter using provided username
     * and password credentials.
     *
     * @param username
     * @param password
     */
    public HTTPBasicAuthFilter(final String username, final String password) {
        this(username, password.getBytes(CHARACTER_SET));
    }

    @Override
    public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {

        if (!cr.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            cr.getHeaders().add(HttpHeaders.AUTHORIZATION, authentication);
        }
        return getNext().handle(cr);
    }
}
