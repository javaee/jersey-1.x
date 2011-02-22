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
package com.sun.jersey.impl.client.filter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPDigestAuthFilter;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.impl.container.grizzly2.AbstractGrizzlyServerTester;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;


/**
 * @author pavel.bucek@sun.com
 */
public class HTTPDigestAuthFilterTest extends AbstractGrizzlyServerTester {

    @Path("/auth-digest")
    public static class Resource {
        @Context
        private HttpContext context;

        @GET
        public Response get1() {
            if (context.getRequest().getHeaderValue("Authorization") == null) {
                return
                        // return http 401 - not authorized
                        Response.status(401).header("WWW-Authenticate",
                                "Digest realm=\"" + DIGEST_TEST_REALM + "\", " +
                                        "nonce=\"" + DIGEST_TEST_NONCE + "\", " +
                                        "algorithm=MD5, " +
                                        "domain=\"" + DIGEST_TEST_DOMAIN + "\", qop=\"auth\""
                        ).build();
            } else {
                String authHeader = context.getRequest().getHeaderValue("Authorization");

                // HA1
                String HA1 = concatMD5(
                        DIGEST_TEST_LOGIN,
                        DIGEST_TEST_REALM,
                        DIGEST_TEST_PASS);


                // HA2 : Switch on qop
                String HA2 = concatMD5(
                        "GET",
                        context.getRequest().getRequestUri().getPath().toString()
                );

                String response = concatMD5(
                        HA1,
                        DIGEST_TEST_NONCE,
                        getDigestAuthHeaderValue(authHeader, "nc="),
                        getDigestAuthHeaderValue(authHeader, "cnonce="),
                        getDigestAuthHeaderValue(authHeader, "qop="),
                        HA2);

                if (response.equals(getDigestAuthHeaderValue(authHeader, "response=")))
                    return Response.ok().build();
                else
                    return Response.status(401).build();

            }
        }

        static String getDigestAuthHeaderValue(String authHeader, String keyName) {
            int i1 = authHeader.indexOf(keyName);

            if (i1 == -1)
                return null;

            String value = authHeader.substring(authHeader.indexOf('=', i1) + 1,
                    (authHeader.indexOf(',', i1) != -1 ? authHeader.indexOf(',', i1) : authHeader.length())
            );

            value = value.trim();
            if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                value = value.substring(1, value.length() - 1);

            return value;
        }
    }


    public HTTPDigestAuthFilterTest(String testName) {
        super(testName);
    }

    static private final String DIGEST_TEST_LOGIN = "user";
    static private final String DIGEST_TEST_PASS = "password";

    // Digest realm="test", nonce="eDePFNeJBAA=a874814ec55647862b66a747632603e5825acd39", algorithm=MD5, domain="/auth-digest/", qop="auth"
    static private final String DIGEST_TEST_NONCE = "eDePFNeJBAA=a874814ec55647862b66a747632603e5825acd39";
    static private final String DIGEST_TEST_REALM = "test";
    static private final String DIGEST_TEST_DOMAIN = "/auth-digest/";

    public void testHTTPDigestAuthFilter() {
        startServer(Resource.class);

        Client c = Client.create();
        c.addFilter(new HTTPDigestAuthFilter(DIGEST_TEST_LOGIN, DIGEST_TEST_PASS));
        WebResource r = c.resource(getUri().path("auth-digest").build());

        ClientResponse response = r.get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
    }


    // copied from HTTPDigestAuthFilter..
    // we should consider moving this test to com.sun.jersey.client.api.filter
    // package and use these package-private functions directly

    /**
     * Converts array of bytes in hexadecimal format
     */
    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Compute md5 hash of a string and returns the hexadecimal representation of it
     */
    static String MD5(String text) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            byte[] md5hash = new byte[32];
            md.update(text.getBytes("iso-8859-1"), 0, text.length());
            md5hash = md.digest();
            String result = convertToHex(md5hash);
            return result;
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Concatenate the strings with ':' and then pass it to md5
     */
    static String concatMD5(String... vals) {

        // Loop on vals : populate a buffer
        StringBuffer buff = new StringBuffer();
        for (String val : vals) {
            buff.append(val);
            buff.append(':');
        } // End of loop on vals

        // Remove last separator
        buff.deleteCharAt(buff.length() - 1);

        return MD5(buff.toString());
    }

//	@Test
//	public void testAuthentication() {
//
//
//		Client client = new Client();
//		client.addFilter(new HTTPDigestAuthFilter(
//				DIGEST_TEST_LOGIN,
//				DIGEST_TEST_PASS));
//
//		// Root website
//		WebResource root = client.resource(DIGEST_TEST_URL);
//
//		// Try to get root homepage
//		ClientResponse response = root.get(ClientResponse.class);
//
//		assertFalse(
//				response.getClientResponseStatus().equals(
//						Status.UNAUTHORIZED));
//
//	}

}