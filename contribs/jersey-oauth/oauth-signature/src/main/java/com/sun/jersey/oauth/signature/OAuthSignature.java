/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.oauth.signature;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
     * @throws SignatureException if an error occurred generating the signature.
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
     * @throws SignatureException if an error occurred generating the signature.
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
     * @throws OAuthSignatureException if an error occurred verifying the signature.
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
    private static String normalizeParameters(OAuthRequest request, OAuthParameters params) {

        ArrayList<String> list = new ArrayList<String>();

        // parameters in the OAuth HTTP authorization header
        for (String key : params.keySet()) {

            // exclude realm and oauth_signature parameters from OAuth HTTP authorization header
            if (key.equals(OAuthParameters.REALM) || key.equals(OAuthParameters.SIGNATURE)) { continue; }

            String value = params.get(key);

            // "...parameter names and values are escaped using RFC3986 percent-encoding..."
            if (value != null) {
                list.add(URLCodec.encode(key) + '=' + URLCodec.encode(value));
            }
        }

        // parameters in the HTTP POST request body and HTTP GET parameters in the query part
        for (String key : request.getParameterNames()) {

            // ignore parameter if an OAuth-specific parameter that appears in the OAuth parameters
            if (key.startsWith("oauth_") && params.containsKey(key)) { continue; }

            // the same parameter name can have multiple values
            List<String> values = request.getParameterValues(key);

            // "... parameter names and values are escaped using RFC3986 percent-encoding..."
            if (values != null) {
                for (String value : values) {
                    list.add(URLCodec.encode(key) + '=' + URLCodec.encode(value));
                }
            }
        }

        // sort name-value pairs in natural (binary) sort order
        Collections.sort(list);

        StringBuffer buf = new StringBuffer();

        // append each name-value pair, delimited with ampersand
        for (Iterator<String> i = list.iterator(); i.hasNext(); ) {
            buf.append(i.next());
            if (i.hasNext()) {
                buf.append('&');
            }
        }

        return buf.toString();
    }

    /**
     * Constructs the request URL, per section 9.1.2 of the OAuth 1.0
     * specification.
     *
     * @param request the incoming request construct URL from.
     * @return the constructed URL.
     */
    private static String constructRequestURL(OAuthRequest request) throws OAuthSignatureException {
        URL url;
        try { url = new URL(request.getRequestURL()); }
        catch (MalformedURLException mue) { throw new OAuthSignatureException(mue); }
        StringBuffer buf = new StringBuffer(url.getProtocol()).append("://").append(url.getHost().toLowerCase());
        int port = url.getPort();
        if (port > 0 && port != url.getDefaultPort()) { buf.append(':').append(port); }
        buf.append(url.getPath());
        return buf.toString();
    }

    /**
     * Assembles request elements for which a digital signature is to be
     * generated/verified, per section 9.1.3 of the OAuth 1.0 specification.
     *
     * @param request the request from which to assemble elements.
     * @param params the OAuth authorization parameters from which to assemble elements.
     * @return the concetenated elements, ready to sign/verify
     */
    private static String elements(OAuthRequest request,
    OAuthParameters params) throws OAuthSignatureException {
        StringBuffer buf = new StringBuffer(URLCodec.encode(request.getRequestMethod().toUpperCase()));
        buf.append('&').append(URLCodec.encode(constructRequestURL(request)));
        buf.append('&').append(URLCodec.encode(normalizeParameters(request, params)));
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
        if (method == null) { throw new UnsupportedSignatureMethodException(params.getSignatureMethod()); }
        return method;
    }
}

