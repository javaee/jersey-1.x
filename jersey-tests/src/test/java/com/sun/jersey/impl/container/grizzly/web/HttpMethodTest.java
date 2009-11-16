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

package com.sun.jersey.impl.container.grizzly.web;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
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
public class HttpMethodTest extends AbstractGrizzlyWebContainerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public String get() {
            return "GET";
        }
               
        @POST
        public String post(String entity) {
            return entity;
        }
        
        @PUT
        public String put(String entity) {
            return entity;
        }
        
        @DELETE
        public String delete() {
            return "DELETE";
        }    
    }
        
    public HttpMethodTest(String testName) {
        super(testName);
    }
    
    protected Client createClient() {
        return Client.create();
    }

    public void testGet() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }
    
    public void testPost() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }    
    
    public void testPostEmpty() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("", r.post(String.class, ""));
    }

    public void testPut() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT"));
    }
    
    public void testDelete() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class));
    }
    
    public void testAll() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));

        assertEquals("POST", r.post(String.class, "POST"));
        
        assertEquals("PUT", r.put(String.class, "PUT"));
        
        assertEquals("DELETE", r.delete(String.class));
    }

    @Path("/test")
    public static class HttpMethodResponseErrorResource {
        @GET
        public Response get() {
            return getResponse();
        }

        @POST
        public Response post() {
            return getResponse();
        }

        @PUT
        public Response put() {
            return getResponse();
        }

        @DELETE
        public Response delete() {
            return getResponse();
        }

        private Response getResponse() {
            return Response.status(400).header("X-FOO", "foo").build();
        }
    }

    public void testResponseError() {
        startServer(HttpMethodResponseErrorResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        testResponseError(r.get(ClientResponse.class));
        testResponseError(r.post(ClientResponse.class));
        testResponseError(r.put(ClientResponse.class));
        testResponseError(r.delete(ClientResponse.class));
    }

    private void testResponseError(ClientResponse cr) {
        assertEquals(400, cr.getStatus());

        assertNotNull(cr.getHeaders().getFirst("X-FOO"));
        assertEquals("foo", cr.getHeaders().getFirst("X-FOO"));
    }
}
