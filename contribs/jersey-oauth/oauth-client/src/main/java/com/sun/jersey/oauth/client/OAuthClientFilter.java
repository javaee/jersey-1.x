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

package com.sun.jersey.oauth.client;

import javax.ws.rs.ext.Providers;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;

/**
 * Client filter adding OAuth authorization header to the HTTP request, if no
 * authorization header is already present.
 * <p>
 * Note: This filter signs the request based on its request parameters.
 * For this reason, you should invoke this filter after any others that
 * modify any request parameters.
 * <p>
 * Example of usage:
 *
 * <pre>
 * // baseline OAuth parameters for access to resource
 * OAuthParameters params = new OAuthParameters().signatureMethod("HMAC-SHA1").
 *  consumerKey("key").setToken("accesskey")..version();
 *
 * // OAuth secrets to access resource
 * OAuthSecrets secrets = new OAuthSecrets().consumerSecret("secret").setTokenSecret("accesssecret");
 *
 * // if parameters and secrets remain static, filter can be added to each web resource
 * OAuthClientFilter filter = new OAuthClientFilter(client.getProviders(), params, secrets);
 *
 * // OAuth test server
 * WebResource resource = client.resource("http://term.ie/oauth/example/request_token.php");
 *
 * resource.addFilter(filter);
 *
 * String response = resource.get(String.class);
 * </pre>
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public final class OAuthClientFilter extends ClientFilter {

    /** The registered providers, which contains entity message body readers and writers. */
    private final Providers providers;

    /** The OAuth parameters to be used in generating signature. */
    private final OAuthParameters parameters;

    /** The OAuth secrets to be used in generating signature. */
    private final OAuthSecrets secrets;

    /**
     * Constructs a new OAuth client filter with the specified providers.
     *
     * @param providers the registered providers from Client.getProviders() method.
     * @param parameters the OAuth parameters to be used in signing requests.
     * @param secrets the OAuth secrets to be used in signing requests.
     */
    public OAuthClientFilter(final Providers providers,
    final OAuthParameters parameters, final OAuthSecrets secrets) {
        this.providers = providers;
        this.parameters = parameters;
        this.secrets = secrets;
    }

    /**
     * Note: This method automatically sets the nonce and timestamp.
     */
    @Override
    public ClientResponse handle(final ClientRequest request) throws ClientHandlerException {

        // secrets and parameters exist; no auth header already: sign request; add as authorization header
        if (parameters != null && secrets != null && !request.getHeaders().containsKey("Authorization")) {

            final OAuthParameters p = (OAuthParameters)parameters.clone(); // make modifications to clone

            if (p.getTimestamp() == null) {
                p.setTimestamp();
            }

            if (p.getNonce() == null) {
                p.setNonce();
            }

            try {
                OAuthSignature.sign(new RequestWrapper(request, providers), p, secrets);
            }
            catch (OAuthSignatureException se) {
                throw new ClientHandlerException(se);
            }
        }

        // next filter in chain
        return getNext().handle(request);
    }
}

