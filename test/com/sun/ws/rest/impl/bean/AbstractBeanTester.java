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

package com.sun.ws.rest.impl.bean;

import com.sun.ws.rest.impl.TestResourceProxy;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.client.RequestOutBound;
import com.sun.ws.rest.impl.client.ResponseInBound;
import com.sun.ws.rest.spi.container.AbstractContainerRequest;
import com.sun.ws.rest.spi.container.AbstractContainerResponse;
import com.sun.ws.rest.impl.MultivaluedMapImpl;
import com.sun.ws.rest.impl.TestHttpRequestContext;
import com.sun.ws.rest.impl.TestHttpResponseContext;
import com.sun.ws.rest.impl.application.WebApplicationImpl;
import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResourceProxyFilter;
import com.sun.ws.rest.spi.container.WebApplication;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MultivaluedMap;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractBeanTester extends TestCase {
    protected static final URI BASE_URI = URI.create("/base/");

    protected WebApplication w;
    
    protected AbstractBeanTester(String testName) {
        super(testName);
    }
    
    protected void initiateWebApplication(Class... resources) {
        w = createWebApplication(resources);
    }
    
    protected void initiateWebApplication(ResourceConfig c) {
        w = createWebApplication(c);
    }
    
    protected WebApplication createWebApplication(Class... resources) {
        return createWebApplication(new HashSet<Class>(Arrays.asList(resources)));
    }
    
    protected WebApplication createWebApplication(Set<Class> resources) {
        return createWebApplication(new DefaultResourceConfig(resources));
    }
    
    protected WebApplication createWebApplication(ResourceConfig c) {
        WebApplicationImpl a = new WebApplicationImpl();
        a.initiate(null, c);
        return a;
    }

    protected ResourceProxy resourceProxy(String relativeUri) {
        return resourceProxy(relativeUri, true);
    }
    
    protected ResourceProxy resourceProxy(String relativeUri, boolean checkStatus) {
        ResourceProxy r = new TestResourceProxy(
                createCompleteUri(BASE_URI, relativeUri), BASE_URI, 
                w);
        if (checkStatus) {
            r.addFilter(new ResourceProxyFilter() {
                public ResponseInBound invoke(URI u, String method, RequestOutBound ro) {
                    ResponseInBound r = getNext().invoke(u, method, ro);
                    if (r.hasEntity()) {
                        assertEquals(200, r.getStatus());
                    } else {
                        assertEquals(204, r.getStatus());
                    }
                    return r;
                }
            });
        }
        
        return r;
    }
    
    private URI createCompleteUri(URI baseUri, String relativeUri) {
        if (relativeUri.startsWith("/"))
            relativeUri = relativeUri.substring(1);
        
        return URI.create(baseUri.toString() + relativeUri);
    }
    
    protected AbstractContainerResponse callGet(Class<?> r, String path, 
            String accept) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        if (accept != null) headers.add("Accept", accept);
        
        return call(r, "GET", path, headers, "");
    }
    
    protected AbstractContainerResponse callGet(Class<?> r, String path, 
            MultivaluedMap<String, String> headers) {
        return call(r, "GET", path, headers, "");
    }
    
    protected AbstractContainerResponse callPost(Class<?> r, String path, 
            String contentType, String content) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        if (contentType != null) headers.add("Content-Type", contentType);
        
        return call(r, "POST", path, headers, content);
    }
    
    protected AbstractContainerResponse callPost(Class<?> r, String path, 
            String contentType, String accept, String content) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        if (contentType != null) headers.add("Content-Type", contentType);
        if (accept != null) headers.add("Accept", accept);
        
        return call(r, "POST", path, headers, content);
    }

    protected AbstractContainerResponse callPost(Class<?> r, String path, 
            MultivaluedMap<String, String> headers, String content) {
        return call(r, "POST", path, headers, content);
    }
    
    protected AbstractContainerResponse call(Class<?> r, String method, String path, 
            String contentType, String accept, String content) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        if (contentType != null) headers.add("Content-Type", contentType);
        if (accept != null) headers.add("Accept", accept);
        
        AbstractContainerResponse response = callNoStatusCheck(r, method, path, headers, content);
        check20xStatus(response);
        return response;
    }
    
    protected AbstractContainerResponse call(Class<?> r, String method, String path, 
            MultivaluedMap<String, String> headers, String content) {
        AbstractContainerResponse response = callNoStatusCheck(r, method, path, headers, content);
        check20xStatus(response);
        return response;
    }
    
    protected AbstractContainerResponse call(Set<Class> r, String method, String path, 
            String contentType, String accept, String content) {
        AbstractContainerResponse response = callNoStatusCheck(r, method, path, contentType, accept, content);
        check20xStatus(response);
        return response;
    }
    
    void check20xStatus(HttpResponseContext response) {
        if (response.getEntity() != null) {
            assertEquals(200, response.getStatus());        
        } else {            
            assertEquals(204, response.getStatus());        
        }
    }
    
    protected AbstractContainerResponse callNoStatusCheck(Class<?> r, String method, String path, 
            String contentType, String accept, String content) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        if (contentType != null) headers.add("Content-Type", contentType);
        if (accept != null) headers.add("Accept", accept);
        
        Set<Class> rs = new HashSet<Class>();
        rs.add(r);
        return callNoStatusCheck(rs, method, path, headers, content);
    }
    
    protected AbstractContainerResponse callNoStatusCheck(Class<?> r, String method, String path, 
            MultivaluedMap<String, String> headers, String content) {
        Set<Class> rs = new HashSet<Class>();
        rs.add(r);
        return callNoStatusCheck(rs, method, path, headers, content);
    }
    
    protected AbstractContainerResponse callNoStatusCheck(Set<Class> r, String method, String path, 
            String contentType, String accept, String content) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        if (contentType != null) headers.add("Content-Type", contentType);
        if (accept != null) headers.add("Accept", accept);
        
        return invoke(r, method, path, headers, content);
    }
    
    protected AbstractContainerResponse callNoStatusCheck(Set<Class> r, String method, String path, 
            MultivaluedMap<String, String> headers, String content) {
        return invoke(r, method, path, headers, content);
    }
    
    AbstractContainerResponse invoke(final Set<Class> r, String method, String path, 
        MultivaluedMap<String, String> headers, String content) {

        // The URI
        String uri = _BASE_URI;
        if (path.startsWith("/")) {
            uri += path.substring(1);
        } else {
            uri += path;
        }
        
        // The base URI
        String baseUri = _BASE_URI;
        
        WebApplicationImpl a = new WebApplicationImpl();
        ResourceConfig c = new DefaultResourceConfig(r);

        a.initiate(null, c);

        ByteArrayInputStream e = new ByteArrayInputStream(content.getBytes());
        final AbstractContainerRequest request = new TestHttpRequestContext(method, e,
                uri, baseUri);
        for (Map.Entry<String, List<String>> h : headers.entrySet()) {
            request.getRequestHeaders().put(h.getKey(), h.getValue());
        }            

        final AbstractContainerResponse response = new TestHttpResponseContext(request);

        a.handleRequest(request, response);
        return response;
    }
    
    private static String _BASE_URI = "/base/";
    
    public URI getBaseUri() {
        try {
            return new URI(_BASE_URI);
        } catch (URISyntaxException ex) {            
            ex.printStackTrace();
        }
        
        return null;
    }
    
}
