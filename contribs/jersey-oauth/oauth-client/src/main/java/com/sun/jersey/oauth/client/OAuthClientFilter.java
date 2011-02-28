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

package com.sun.jersey.oauth.client;

import javax.ws.rs.ext.Providers;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;
import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.UriBuilder;

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
 * @author Martin Matula <martin.matula@oracle.com>
 */
public final class OAuthClientFilter extends ClientFilter {

    /** The registered providers, which contains entity message body readers and writers. */
    private final Providers providers;

    /** The OAuth parameters to be used in generating signature. */
    private final OAuthParameters parameters;

    /** The OAuth secrets to be used in generating signature. */
    private final OAuthSecrets secrets;

    private final URI requestTokenUri;
    private final URI accessTokenUri;
    private final URI authorizationUri;

    private enum State {
        UNMANAGED, UNAUTHORIZED, REQUEST_TOKEN, AUTHORIZED;
    }

    private State state;

    /**
     * Constructs a new OAuth client filter with the specified providers.
     *
     * @param providers the registered providers from Client.getProviders() method.
     * @param parameters the OAuth parameters to be used in signing requests.
     * @param secrets the OAuth secrets to be used in signing requests.
     */
    public OAuthClientFilter(final Providers providers,
    final OAuthParameters parameters, final OAuthSecrets secrets) {
        this(providers, parameters, secrets, null, null, null);
    }

    public OAuthClientFilter(Providers providers, OAuthParameters parameters,
            OAuthSecrets secrets, String requestTokenUri, String accessTokenUri,
            String authorizationUri) {
        if (providers == null || parameters == null || secrets == null) {
            throw new NullPointerException();
        }
        if ((requestTokenUri != null || accessTokenUri != null || authorizationUri != null) &&
                (requestTokenUri == null || accessTokenUri == null || authorizationUri == null)) {
            throw new NullPointerException();
        }
        this.providers = providers;
        this.parameters = parameters;
        this.secrets = secrets;
        
        if (parameters.getSignatureMethod() == null) {
            parameters.signatureMethod("HMAC-SHA1");
        }

        if (parameters.getVersion() == null) {
            parameters.version();
        }

        if (requestTokenUri == null) {
            this.requestTokenUri = this.accessTokenUri = this.authorizationUri = null;
            state = State.UNMANAGED;
        } else {
            this.requestTokenUri = UriBuilder.fromUri(requestTokenUri).build();
            this.accessTokenUri = UriBuilder.fromUri(accessTokenUri).build();
            this.authorizationUri = UriBuilder.fromUri(authorizationUri).build();
            state = parameters.getToken() == null ? State.UNAUTHORIZED : State.AUTHORIZED;
        }
    }

    /**
     * Note: This method automatically sets the nonce and timestamp.
     */
    @Override
    public ClientResponse handle(final ClientRequest request) throws ClientHandlerException {
        // secrets and parameters exist; no auth header already: sign request; add as authorization header
        if (!request.getHeaders().containsKey("Authorization")) {
            switch (state) {
                case UNAUTHORIZED:
                    // put together a request token request
                    state = State.UNMANAGED;
                    try {
                        ClientResponse cr = handle(ClientRequest.create().build(requestTokenUri, HttpMethod.POST));
                        Form response = cr.getEntity(Form.class);
                        String token = response.getFirst(OAuthParameters.TOKEN);
                        parameters.token(token);
                        secrets.tokenSecret(response.getFirst(OAuthParameters.TOKEN_SECRET));
                        state = State.REQUEST_TOKEN;
                        throw new NeedAuthorizationException(parameters, authorizationUri);
                    } finally {
                        if (state == State.UNMANAGED) {
                            state = State.UNAUTHORIZED;
                        }
                    }
                case REQUEST_TOKEN:
                    if (parameters.getVerifier() == null) {
                        throw new NeedAuthorizationException(parameters, authorizationUri);
                    }
                    state = State.UNMANAGED;
                    try {
                        ClientResponse cr = handle(ClientRequest.create().build(accessTokenUri, HttpMethod.POST));
                        Form response = cr.getEntity(Form.class);
                        String token = response.getFirst(OAuthParameters.TOKEN);
                        String secret = response.getFirst(OAuthParameters.TOKEN_SECRET);
                        // TODO: add a method to persist the access token
                        parameters.token(token);
                        secrets.tokenSecret(secret);
                        state = State.AUTHORIZED;
                    } finally {
                        parameters.remove(OAuthParameters.VERIFIER);
                        if (state == State.UNMANAGED) {
                            parameters.remove(OAuthParameters.TOKEN);
                            secrets.tokenSecret(null);
                            state = State.UNAUTHORIZED;
                        }
                    }

            }
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
        ClientResponse response;
        UniformInterfaceException uie = null;
        try {
            response = getNext().handle(request);
        } catch (UniformInterfaceException e) {
            response = e.getResponse();
            uie = e;
        }

        if (state == State.AUTHORIZED && response.getClientResponseStatus() == ClientResponse.Status.UNAUTHORIZED) {
            state = State.UNAUTHORIZED;
            request.getHeaders().remove("Authorization");
            parameters.remove(OAuthParameters.TOKEN);
            secrets.tokenSecret(null);
            uie = null;
            return handle(request);
        }

        if (uie != null) {
            throw uie;
        }
        
        return response;
    }
}

