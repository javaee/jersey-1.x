/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.application;

import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.core.header.InBoundHeaders;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebApplicationTest extends TestCase {
    
    public WebApplicationTest(String testName) {
        super(testName);
    }
    
    @Path("/{arg1}/{arg2}")
    public static class TestOneWebResource {
        @Context UriInfo info;
        
        @GET
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());
            
            assertEquals("a", info.getPathParameters().getFirst("arg1"));
            assertEquals("b", info.getPathParameters().getFirst("arg2"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-ONE", s);
        }
    }
    
    @Path("/{arg1}")
    public static class TestTwoWebResource {
        @Context UriInfo info;
        
        @GET
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());
            
            System.out.println(info.getPath());
            String v = info.getPathParameters().getFirst("arg1");
            boolean b = v.equals("a") || v.equals("a.foo");
            assertTrue(b);
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-TWO", s);
        }
    }
    
    @Path("/{arg1}.xml")
    public static class TestThreeWebResource {
        @Context UriInfo info;
        
        @GET
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());
            
            System.out.println(info.getPath());
            assertEquals("a", info.getPathParameters().getFirst("arg1"));
            
            String s = request.getEntity(String.class);
            assertEquals("RESOURCE-THREE", s);
        }
    }
        
    public void testOneResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class);
        
        call(a, "GET", "/a/b", "RESOURCE-ONE");
    }
    
    public void testTwoResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class, 
                TestTwoWebResource.class);
        
        call(a, "GET", "/a/b", "RESOURCE-ONE");
        call(a, "GET", "/a", "RESOURCE-TWO");
    }
    
    public void testThreeResource() {
        WebApplicationImpl a = createWebApplication(
                TestOneWebResource.class,
                TestTwoWebResource.class,
                TestThreeWebResource.class);
        
        call(a, "GET", "/a/b", "RESOURCE-ONE");
        call(a, "GET", "/a", "RESOURCE-TWO");
        call(a, "GET", "/a.foo", "RESOURCE-TWO");
        call(a, "GET", "/a.xml", "RESOURCE-THREE");
    }

    public void testResourcesWithOnePath() {
        WebApplicationImpl a = createWebApplication(
            com.sun.jersey.impl.application.ResourceOne.class,
            com.sun.jersey.impl.application.ResourceTwo.class);
        
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource1", "RESOURCE-ONE");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
        call(a, "GET", "/resource2", "RESOURCE-TWO");
    }

    public void testResourcesWithSamePath() {
        try {
            WebApplicationImpl a = createWebApplication(
                com.sun.jersey.impl.application.ResourceTwo.class,
                com.sun.jersey.impl.application.AnotherResourceTwo.class);
        } catch (Exception e) {
            return;
        }
        fail("Detection of same path for various root resources failed!");
    }

    
    public WebApplicationImpl createWebApplication(Class... resources) {
        final Set<Class<?>> s = new HashSet<Class<?>>();
        for (Class resource : resources)
            s.add(resource);
        
        WebApplicationImpl a = new WebApplicationImpl();
        ResourceConfig c = new DefaultResourceConfig(s);
        a.initiate(c);
        return a;
    }
    
    public void call(WebApplicationImpl a, String method, String path, String content) {
        ByteArrayInputStream e = new ByteArrayInputStream(content.getBytes());
        final ContainerRequest request = new ContainerRequest(
                a, 
                method, 
                URI.create("/"),
                URI.create(path),
                new InBoundHeaders(),
                e
                );
        
        final ContainerResponse response = new ContainerResponse(
                a,
                request,
                new ContainerResponseWriter() {
                    public OutputStream writeStatusAndHeaders(long contentLength, 
                            ContainerResponse response) throws IOException {
                        return new ByteArrayOutputStream();
                    }

                    public void finish() throws IOException {
                    }
                });
        
        try { 
            a.handleRequest(request, response);
        } catch (IOException ex) {
            throw new ContainerException(ex);
        }

        if (response.getEntity() != null) {
            assertEquals(200, response.getStatus());        
        } else {            
            assertEquals(204, response.getStatus());        
        }
    }
}
