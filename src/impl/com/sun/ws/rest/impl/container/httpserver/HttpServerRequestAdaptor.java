/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.container.httpserver;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsServer;
import com.sun.jersey.spi.container.AbstractContainerRequest;
import com.sun.ws.rest.impl.http.header.HttpHeaderFactory;
import com.sun.jersey.spi.container.MessageBodyContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Map.Entry;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

/**
 * A HTTP request adapter for {@link HttpExchange}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpServerRequestAdaptor extends AbstractContainerRequest {
    
    private final HttpExchange exchange;
    
    public HttpServerRequestAdaptor(MessageBodyContext bodyContext, HttpExchange exchange) throws IOException {
        super(bodyContext, exchange.getRequestMethod(), exchange.getRequestBody());
        this.exchange = exchange;
        
        initiateUriInfo();
        copyHttpHeaders();
    }
    
    private void initiateUriInfo() {
        /**
         * This is a URI that contains the path, query and
         * fragment components.
         */
        URI requestUri = exchange.getRequestURI();
        
        /**
         * The base path specified by the HTTP context of the HTTP handler.
         * It is in decoded form.
         */
        String decodedBasePath = exchange.getHttpContext().getPath();
        
        // Ensure that the base path ends with a '/'
        if (!decodedBasePath.endsWith("/")) {
            if (decodedBasePath.equals(requestUri.getPath())) {
                /**
                 * This is an edge case where the request path
                 * does not end in a '/' and is equal to the context 
                 * path of the HTTP handler.
                 * Both the request path and base path need to end in a '/'
                 * Currently the request path is modified.
                 * TODO support redirection in accordance with resource
                 * configuration feature.
                 */
                requestUri = UriBuilder.fromUri(requestUri).
                        path("/").build();
            }
            decodedBasePath += "/";                
        }

        /*
         * The following is madness, there is no easy way to get 
         * the complete URI of the HTTP request!!
         *
         * TODO this is missing the user information component, how
         * can this be obtained?
         */
        HttpServer server = exchange.getHttpContext().getServer();
        String scheme = (server instanceof HttpsServer) ? "https" : "http";
        InetSocketAddress addr = exchange.getLocalAddress();
        try {
            this.baseUri = new URI(scheme, null, addr.getHostName(), addr.getPort(), 
                    decodedBasePath, null, null);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
            
        this.completeUri = this.baseUri.resolve(requestUri);
    }
    
    private void copyHttpHeaders() {
        MultivaluedMap<String, String> headers = getRequestHeaders();
        
        Headers eh = exchange.getRequestHeaders();
        for (Entry<String, List<String>> e : eh.entrySet()) {
            headers.put(e.getKey(), e.getValue());
            if (e.getKey().equalsIgnoreCase("cookie")) {
                for (String headerValue: e.getValue()) {
                    getCookies().putAll(HttpHeaderFactory.createCookies(headerValue));
                }
            }
        }
    }    
    
    // SecurityContext
    
    @Override
    public Principal getUserPrincipal() {
        return exchange.getPrincipal();
    }
    
    @Override
    public boolean isUserInRole(String role) {
        // TODO how to support roles with LW HTTP server?
        // This most likely requires specialized container support
        return false;
    }
    
    @Override
    public boolean isSecure() {
        return exchange instanceof HttpsExchange;
    }
    
    @Override
    public String getAuthenticationScheme() {
        // TODO the authentication scheme cannot be obtained
        return null;
    }    
}