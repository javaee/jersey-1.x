/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.oauth.client;

import com.sun.jersey.oauth.signature.OAuthParameters;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author martin
 */
public class NeedAuthorizationException extends RuntimeException {
    private final OAuthParameters params;
    private final URI authUri;

    public NeedAuthorizationException(OAuthParameters parameters, URI authorizationUri) {
        params = parameters;
        authUri = UriBuilder.fromUri(authorizationUri)
                .queryParam(OAuthParameters.TOKEN, parameters.getToken())
                .build();
    }

    public OAuthParameters getOAuthParameters() {
        return params;
    }

    public URI getAuthorizationUri() {
        return authUri;
    }
}
