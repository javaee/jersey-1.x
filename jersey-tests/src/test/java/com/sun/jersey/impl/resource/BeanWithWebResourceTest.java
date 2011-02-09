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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class BeanWithWebResourceTest extends AbstractResourceTester {
    
    public BeanWithWebResourceTest(String testName) {
        super(testName);
    }
    
    @Path("/{arg1}/{arg2}")
    public static class BeanWithWebResource{
        @GET
        public void doGet(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
        
        @PUT
        public void putReqWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @POST
        public void postReqWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @DELETE
        public void deleteReqWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            String method = request.getMethod();
            
            boolean match = "POST".equals(method) | "DELETE".equals(method) |
                    "PUT".equals(method);
            assertTrue(match);
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
    }
    
    @Path("/{arg1}/{arg2}")
    public static class BeanProduceWithWebResource {
        @GET
        @Produces("text/html")
        public void doGet(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
        
        //TODO: reunify the following 3 methods once PUT, POST, DELETE annotations are available
        @Produces("text/xhtml")
        @PUT
        public void putRequestWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @Produces("text/xhtml")
        @POST
        public void postRequestWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        @Produces("text/xhtml")
        @DELETE
        public void deleteRequestWrapper(HttpRequestContext request, HttpResponseContext response) {
            handleRequest(request, response);
        }
        
        public void handleRequest(HttpRequestContext request, HttpResponseContext response) {
            String method = request.getMethod();
            boolean match = "POST".equals(method) | "DELETE".equals(method) |
                    "PUT".equals(method);
            assertTrue(match);
            
            String accept = request.getRequestHeaders().getFirst("Accept");
            match = accept == null | "text/xhtml".equals(accept);
            assertTrue(match);
            
            response.setResponse(Response.ok("RESPONSE").build());
        }
    }
    
    public void testBeanWithWebResource() {
        initiateWebApplication(BeanWithWebResource.class);
        WebResource r = resource("/a/b");
        
        r.accept("text/html").get(String.class);
        r.accept("text/xhtml").post();
        r.accept("text/xhtml").put();
        r.delete();
    }
    
    public void testBeanProduceWithWebResource() {
        initiateWebApplication(BeanProduceWithWebResource.class);
        WebResource r = resource("/a/b");
        
        r.accept("text/html").get(String.class);
        r.accept("text/xhtml").post();
        r.accept("text/xhtml").put();
        r.delete();
    }
}
