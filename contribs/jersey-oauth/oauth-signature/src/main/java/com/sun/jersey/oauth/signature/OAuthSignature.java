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

package com.sun.jersey.oauth.signature;

import com.sun.jersey.api.uri.UriComponent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Class used for processing an OAuth signature (signing or verifying).
 * <p>
 * Example of usage:
 *
 * <pre>
 * // wrap an existing request with some concrete implementation
 * OAuthRequest request = new ConcreteOAuthRequestImplementation();
 *
 * // establish the parameters that will be used to sign the request
 * OAuthParameters params = new OAuthParameters().consumerKey("dpf43f3p2l4k3l03").
 *  token("nnch734d00sl2jdk").signatureMethod(HMAC_SHA1.NAME).
 *  timestamp().nonce().version();
 *
 * // establish the secrets that will be used to sign the request
 * OAuthSecrets secrets = new OAuthSecrets().consumerSecret("kd94hf93k423kf44").
 *  tokenSecret("pfkkdhi9sl3r4s00");
 *
 * // generate the digital signature and set in the request
 * OAuthSignature.sign(request, params, secrets);
 * </pre>
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class OAuthSignature {

    /**
     * Generates and returns an OAuth signature for the given request,
     * parameters and secrets.
     *
     * @param request the request to generate signature for.
     * @param params the OAuth authorization parameters.
     * @param secrets the secrets used to generate the OAuth signature.
     * @return the OAuth digital signature.
     * @throws OAuthSignatureException if an error occurred generating the signature.
     */
    public static String generate(OAuthRequest request,
    OAuthParameters params, OAuthSecrets secrets) throws OAuthSignatureException {
        return getSignatureMethod(params).sign(elements(request, params), secrets);
    }

    /**
     * Generates an OAuth signature for the given request, parameters and
     * secrets, and stores it as a signature parameter, and writes the
     * OAuth parameters to the request as an Authorization header.
     *
     * @param request the request to generate signature for and write header to.
     * @param params the OAuth authorization parameters.
     * @param secrets the secrets used to generate the OAuth signature.
     * @throws OAuthSignatureException if an error occurred generating the signature.
     */
    public static void sign(OAuthRequest request,
    OAuthParameters params, OAuthSecrets secrets) throws OAuthSignatureException {
        params = (OAuthParameters)params.clone(); // don't modify caller's parameters
        params.setSignature(generate(request, params, secrets));
        params.writeRequest(request);
    }

    /**
     * Verifies the OAuth signature for a given request, parameters and
     * secrets.
     *
     * @param request the request to verify the signature from.
     * @param params the OAuth authorization parameters
     * @param secrets the secrets used to verify the OAuth signature.
     * @return true if the signature is verified.
     * @throws OAuthSignatureException if an error occurred generating the signature.
     */
    public static boolean verify(OAuthRequest request,
    OAuthParameters params, OAuthSecrets secrets) throws OAuthSignatureException {
        return getSignatureMethod(params).verify(elements(request, params), secrets, params.getSignature());
    }

    /**
     * Collects, sorts and concetenates the request parameters into a
     * normalized string, per section 9.1.1. of the OAuth 1.0 specification.
     *
     * @param request the request to retreive parameters from.
     * @param params the OAuth authorization parameters to retrieve parameters from.
     * @return the normalized parameters string.
     */
    static String normalizeParameters(OAuthRequest request, OAuthParameters params) {

        ArrayList<String[]> list = new ArrayList<String[]>();

        // parameters in the OAuth HTTP authorization header
        for (String key : params.keySet()) {

            // exclude realm and oauth_signature parameters from OAuth HTTP authorization header
            if (key.equals(OAuthParameters.REALM) || key.equals(OAuthParameters.SIGNATURE)) {
                continue;
            }

            String value = params.get(key);

            // Encode key and values as per section 3.6 http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.6
            if (value != null) {
                addParam(key, value, list);
            }
        }

        // parameters in the HTTP POST request body and HTTP GET parameters in the query part
        for (String key : request.getParameterNames()) {

            // ignore parameter if an OAuth-specific parameter that appears in the OAuth parameters
            if (key.startsWith("oauth_") && params.containsKey(key)) {
                continue;
            }

            // the same parameter name can have multiple values
            List<String> values = request.getParameterValues(key);

            // Encode key and values as per section 3.6 http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.6
            if (values != null) {
                for (String value : values) {
                    addParam(key, value, list);
                }
            }
        }

        // sort name-value pairs by name
        Collections.sort(list, new Comparator<String[]>() {
            @Override
            public int compare(String[] t, String[] t1) {
                int c = t[0].compareTo(t1[0]);
                return c == 0 ? t[1].compareTo(t1[1]) : c;
            }
        });

        StringBuilder buf = new StringBuilder();

        // append each name-value pair, delimited with ampersand
        for (Iterator<String[]> i = list.iterator(); i.hasNext(); ) {
            String[] param = i.next();
            buf.append(param[0]).append("=").append(param[1]);
            if (i.hasNext()) {
                buf.append('&');
            }
        }

        return buf.toString();
    }

    /**
     * Constructs the request URI, per section 9.1.2 of the OAuth 1.0
     * specification.
     *
     * @param request the incoming request to construct the URI from.
     * @return the constructed URI.
     */
    private static URI constructRequestURL(OAuthRequest request) throws OAuthSignatureException {
        try {
            URL url = request.getRequestURL();
            if (url == null)
                throw new OAuthSignatureException();
            StringBuffer buf = new StringBuffer(url.getProtocol()).append("://").append(url.getHost().toLowerCase());
            int port = url.getPort();
            if (port > 0 && port != url.getDefaultPort()) {
                buf.append(':').append(port);
            }
            buf.append(url.getPath());
            return new URI(buf.toString());

        } catch (URISyntaxException mue) {
            throw new OAuthSignatureException(mue);
        }
    }

    /**
     * Assembles request elements for which a digital signature is to be
     * generated/verified, per section 9.1.3 of the OAuth 1.0 specification.
     *
     * @param request the request from which to assemble elements.
     * @param params the OAuth authorization parameters from which to assemble elements.
     * @return the concatenated elements, ready to sign/verify
     */
    private static String elements(OAuthRequest request,
    OAuthParameters params) throws OAuthSignatureException {
        // HTTP request method
        StringBuilder buf = new StringBuilder(request.getRequestMethod().toUpperCase());

        // request URL, see section 3.4.1.2 http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.4.1.2
        buf.append('&').append(UriComponent.encode(constructRequestURL(request).toASCIIString(),
                UriComponent.Type.UNRESERVED));

        // normalized request parameters, see section 3.4.1.3.2 http://tools.ietf.org/html/draft-hammer-oauth-10#section-3.4.1.3.2
        buf.append('&').append(UriComponent.encode(normalizeParameters(request, params),
                UriComponent.Type.UNRESERVED));

        return buf.toString();
    }

    /**
     * Retrieves an instance of a signature method that can be used to generate
     * or verify signatures for data.
     *
     * @return the retrieved signatured method.
     * @throws UnsupportedSignatureMethodException if signature method not supported.
     */
    private static OAuthSignatureMethod getSignatureMethod(OAuthParameters params)
    throws UnsupportedSignatureMethodException {
        OAuthSignatureMethod method = Methods.getInstance(params.getSignatureMethod());
        if (method == null) {
            throw new UnsupportedSignatureMethodException(params.getSignatureMethod());
        }
        return method;
    }

    private static void addParam(String key, String value, List<String[]> list) {
        list.add(new String[] {
                UriComponent.encode(key, UriComponent.Type.UNRESERVED),
                value == null ? "" : UriComponent.encode(value, UriComponent.Type.UNRESERVED)
            });
    }
}

