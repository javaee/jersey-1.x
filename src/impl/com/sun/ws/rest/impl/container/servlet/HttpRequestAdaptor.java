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

import com.sun.ws.rest.impl.HttpRequestContextImpl;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

/**
 * Adapts a HttpServletRequest to provide the methods of HttpRequest
 */
public class HttpRequestAdaptor extends HttpRequestContextImpl {
    
    HttpServletRequest request;
    
    /** Creates a new instance of HttpRequestAdaptor */
    public HttpRequestAdaptor(HttpServletRequest request) throws IOException {
        super(request.getMethod(), request.getInputStream());
        this.request = request;
        
        extractQueryParameters(request.getQueryString());
        setURIs();
        copyHttpHeaders();
    }
    
    @SuppressWarnings("unchecked")
    protected void copyHttpHeaders() {
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

    protected void setURIs() {
        this.uri = URI.create(request.getRequestURL().toString());        
        this.uriPath = (request.getPathInfo() != null) 
            ? request.getPathInfo().substring(1)
            : request.getServletPath().substring(1);
        this.baseURI = getBaseURI(this.uri, this.uriPath);
    }    
}
