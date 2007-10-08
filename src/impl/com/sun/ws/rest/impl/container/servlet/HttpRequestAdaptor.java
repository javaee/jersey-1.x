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

package com.sun.ws.rest.impl.container.servlet;

import com.sun.ws.rest.spi.container.AbstractContainerRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;

/**
 * Adapts a HttpServletRequest to provide the methods of HttpRequest
 */
public final class HttpRequestAdaptor extends AbstractContainerRequest {
    
    private final HttpServletRequest request;
    
    /** Creates a new instance of HttpRequestAdaptor */
    public HttpRequestAdaptor(HttpServletRequest request) throws IOException {
        super(request.getMethod(), request.getInputStream());
        this.request = request;
        
        initiateUriInfo();
        copyHttpHeaders();
    }
    
    private void initiateUriInfo() {
        /**
         * The HttpServletRequest.getRequestURL() contains the complete URI
         * minus the query and fragment components.
         */
        UriBuilder absoluteUriBuilder = UriBuilder.fromUri(
                request.getRequestURL().toString());
        
        /**
         * The HttpServletRequest.getPathInfo() and 
         * HttpServletRequest.getServletPath() are in decoded form.
         *
         * On some servlet implementations the getPathInfo() removed
         * contiguous '/' characters. This is problematic if URIs
         * are embedded, for example as the last path segment.
         * We need to work around this and not use getPathInfo
         * for the decodedPath.
         */
        final String decodedBasePath = (request.getPathInfo() != null)
            ? request.getContextPath() + request.getServletPath() + "/"
            : request.getContextPath() + "/";
        
        String decodedCompletePath;

        try {
            decodedCompletePath = (new URI(request.getRequestURI())).getPath();
        } catch (URISyntaxException ex) {
            decodedCompletePath = decodedBasePath +     // only a backup solution -- getPathInfo() might have eaten contiguos '/' chars
                ((request.getPathInfo() != null) 
                    ? request.getPathInfo().substring(1)
                    : request.getServletPath().substring(1));            
        }
        
        // some servlet implementations return fake context and servlet paths
        final String decodedPath = (decodedCompletePath.startsWith(decodedBasePath)) ? 
                                        decodedCompletePath.substring(decodedBasePath.length()) : decodedCompletePath;

        String queryParameters = request.getQueryString();
        if (queryParameters == null) queryParameters = "";
        
        this.baseUri = absoluteUriBuilder.
                replacePath(decodedBasePath).
                build();
        
        this.completeUri = absoluteUriBuilder.encode(true).
                path(decodedPath).
                encode(false).
                replaceQueryParams(queryParameters).
                build();
    }    
        
    @SuppressWarnings("unchecked")
    private void copyHttpHeaders() {
        MultivaluedMap<String, String> headers = getRequestHeaders();
        for (Enumeration<String> names = request.getHeaderNames() ; names.hasMoreElements() ;) {
            String name = names.nextElement();
            List<String> valueList = new LinkedList<String>();
            for (Enumeration<String> values = request.getHeaders(name); values.hasMoreElements() ;) {
                valueList.add(values.nextElement());
            }
            headers.put(name, valueList);
        }
        List<Cookie> cookies = getCookies();
        javax.servlet.http.Cookie servletCookies[] = request.getCookies();
        if (servletCookies != null) {
            for (javax.servlet.http.Cookie c: servletCookies) {
                NewCookie n = new NewCookie(c.getName(), c.getValue());
                n.setComment(c.getComment());
                n.setDomain(c.getDomain());
                n.setPath(c.getPath());
                n.setSecure(c.getSecure());
                n.setVersion(c.getVersion());
                n.setMaxAge(c.getMaxAge());
                cookies.add(n);
            }
        }
    }
}
