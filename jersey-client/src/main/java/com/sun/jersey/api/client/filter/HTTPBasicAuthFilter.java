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
package com.sun.jersey.api.client.filter;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.core.HttpHeaders;

/**
 * Client filter adding HTTP Basic Authentication header to the HTTP request, if no such header is already present
 *
 * @author Jakub.Podlesak@Sun.COM, Craig.McClanahan@Sun.COM
 */
public final class HTTPBasicAuthFilter extends ClientFilter {

    private final String authentication;

    /**
     * Creates a new HTTP Basic Authentication filter using provided username and password credentials
     *
     * @param username
     * @param password
     */
    public HTTPBasicAuthFilter(final String username, final String password) {
        authentication = "Basic " + encodeCredentialsBasic(username, password);
    }

    @Override
    public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {

        if (!cr.getMetadata().containsKey(HttpHeaders.AUTHORIZATION)) {
            cr.getMetadata().add(HttpHeaders.AUTHORIZATION, authentication);
        }
        return getNext().handle(cr);
    }
    /**
     * <p>Convenience string for Base 64 encoding.</p>
     */
    private static final String BASE64_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789+/";

    /**
     * <p>Encode the specified credentials into a String as required by
     * HTTP Basic Authentication (<a href= "http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>).</p>
     *
     * @param username Username to be encoded
     * @param password Password to be encoded
     */
    private String encodeCredentialsBasic(final String username, final String password) {

        String encode = username + ":" + password;
        int paddingCount = (3 - (encode.length() % 3)) % 3;
        encode += "\0\0".substring(0, paddingCount);

        StringBuilder encoded = new StringBuilder();

        for (int i = 0; i < encode.length(); i += 3) {
            int j = (encode.charAt(i) << 16) + (encode.charAt(i + 1) << 8) + encode.charAt(i + 2);
            encoded.append(BASE64_CHARS.charAt((j >> 18) & 0x3f));
            encoded.append(BASE64_CHARS.charAt((j >> 12) & 0x3f));
            encoded.append(BASE64_CHARS.charAt((j >> 6) & 0x3f));
            encoded.append(BASE64_CHARS.charAt(j & 0x3f));
        }

        return encoded.toString();
    }
}
