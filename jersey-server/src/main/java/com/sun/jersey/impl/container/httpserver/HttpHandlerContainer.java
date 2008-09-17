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

package com.sun.jersey.impl.container.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.api.InBoundHeaders;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.UriBuilder;

/**
 * A {@link HttpHandler} for a {@link WebApplicationImpl}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpHandlerContainer implements HttpHandler, ContainerListener {
    
    private WebApplication application;
    
    public HttpHandlerContainer(WebApplication app) throws ContainerException {
        this.application = app;
    }
    
    private final static class Writer implements ContainerResponseWriter {
        final HttpExchange exchange;
        
        Writer(HttpExchange exchange) {
            this.exchange = exchange;
        }
        
        public OutputStream writeStatusAndHeaders(long contentLength, 
                ContainerResponse cResponse) throws IOException {
            Headers eh = exchange.getResponseHeaders();
            for (Map.Entry<String, List<Object>> e : cResponse.getHttpHeaders().entrySet()) {
                List<String> values = new ArrayList<String>();
                for (Object v : e.getValue())
                    values.add(ContainerResponse.getHeaderValue(v));
                eh.put(e.getKey(), values);
            }
            
            exchange.sendResponseHeaders(cResponse.getStatus(), 
                    getResponseLength(contentLength));
            return exchange.getResponseBody();
        }

        public void finish() throws IOException {            
        }

        private long getResponseLength(long contentLength) {
            if (contentLength == 0)
                return -1;
            if (contentLength  < 0)
                return 0;
            return contentLength;
        }
    }
    
    public void handle(HttpExchange exchange) throws IOException {
        WebApplication _application = application;
                
        /**
         * This is a URI that contains the path, query and
         * fragment components.
         */
        URI exchangeUri = exchange.getRequestURI();
        
        /**
         * The base path specified by the HTTP context of the HTTP handler.
         * It is in decoded form.
         */
        String decodedBasePath = exchange.getHttpContext().getPath();
        
        // Ensure that the base path ends with a '/'
        if (!decodedBasePath.endsWith("/")) {
            if (decodedBasePath.equals(exchangeUri.getPath())) {
                /**
                 * This is an edge case where the request path
                 * does not end in a '/' and is equal to the context 
                 * path of the HTTP handler.
                 * Both the request path and base path need to end in a '/'
                 * Currently the request path is modified.
                 * TODO support redirection in accordance with resource
                 * configuration feature.
                 */
                exchangeUri = UriBuilder.fromUri(exchangeUri).
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
        URI baseUri = null;
        try {
            baseUri = new URI(scheme, null, addr.getHostName(), addr.getPort(), 
                    decodedBasePath, null, null);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
            
        final URI requestUri = baseUri.resolve(exchangeUri);
                        
        final ContainerRequest cRequest = new ContainerRequest(
                _application,
                exchange.getRequestMethod(),
                baseUri,
                requestUri,
                getHeaders(exchange),
                exchange.getRequestBody()
                );
        
        try {
            _application.handleRequest(cRequest, new Writer(exchange));
        } catch (RuntimeException e) {
            e.printStackTrace();
            exchange.getResponseHeaders().clear();
            exchange.sendResponseHeaders(500, -1);
        }   
        exchange.getResponseBody().flush();
        exchange.getResponseBody().close();
        exchange.close();                    
    }
    
    private InBoundHeaders getHeaders(HttpExchange exchange) {
        InBoundHeaders rh = new InBoundHeaders();
        
        Headers eh = exchange.getRequestHeaders();
        for (Entry<String, List<String>> e : eh.entrySet()) {
            rh.put(e.getKey(), e.getValue());
        }
        
        return rh;
    }
    
    // ContainerListener
    
    public void onReload() {
        application = application.clone();
    }
}