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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.client.apache.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.client.apache.ApacheHttpClient4;
import com.sun.jersey.client.apache.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClient4Config;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpMethodTest extends AbstractGrizzlyServerTester {
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("PATCH")
    public @interface PATCH {
    }

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

        @DELETE
        @Path("withentity")
        public String delete(String entity) {
            return entity;
        }

        @POST
        @Path("noproduce")
        public void postNoProduce(String entity) {
        }

        @POST
        @Path("noconsumeproduce")
        public void postNoConsumeProduce() {
        }

        @PATCH
        public String patch(String entity) {
            return entity;
        }
    }

    public HttpMethodTest(String testName) {
        super(testName);
    }

    protected ApacheHttpClient4 createClient() {
        return ApacheHttpClient4.create();
    }

    protected ApacheHttpClient4 createClient(ApacheHttpClient4Config cc) {
        return ApacheHttpClient4.create(cc);
    }

    public void testHead() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        ClientResponse cr = r.head();
        assertFalse(cr.hasEntity());
    }

    public void testOptions() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        ClientResponse cr = r.options(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testGet() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));

        ClientResponse cr = r.get(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPost() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPostChunked() {
        ResourceConfig rc = new DefaultResourceConfig(HttpMethodResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CHUNKED_ENCODING_SIZE, 1024);
        ApacheHttpClient4 c = createClient(config);

        WebResource r = c.resource(getUri().path("test").build());        
        assertEquals("POST", r.post(String.class, "POST"));

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testPostVoid() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        // This test will lock up if ClientResponse is not closed by WebResource.
        // TODO need a better way to detect this.
        for (int i = 0; i < 100; i++) {
            r.post("POST");
        }
    }

    public void testPostNoProduce() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals(204, r.path("noproduce").post(ClientResponse.class, "POST").getStatus());

        ClientResponse cr = r.path("noproduce").post(ClientResponse.class, "POST");
        assertFalse(cr.hasEntity());
        cr.close();
    }

    public void testPostNoConsumeProduce() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals(204, r.path("noconsumeproduce").post(ClientResponse.class).getStatus());

        ClientResponse cr = r.path("noconsumeproduce").post(ClientResponse.class, "POST");
        assertFalse(cr.hasEntity());
        cr.close();
    }

    public void testPut() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("PUT", r.put(String.class, "PUT"));

        ClientResponse cr = r.put(ClientResponse.class, "PUT");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testDelete() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        assertEquals("DELETE", r.delete(String.class));

        ClientResponse cr = r.delete(ClientResponse.class);
        assertTrue(cr.hasEntity());
        cr.close();
    }

    // HttpDelete does not allow entity

//    public void testDeleteWithEntity() {
//        startServer(HttpMethodResource.class);
//        WebResource r = createClient().resource(getUri().path("test/withentity").build());
//        r.addFilter(new com.sun.jersey.api.client.filter.LoggingFilter());
//        assertEquals("DELETE with entity", r.delete(String.class, "DELETE with entity"));
//
//        ClientResponse cr = r.delete(ClientResponse.class, "DELETE with entity");
//        assertTrue(cr.hasEntity());
//        cr.close();
//    }

    public void testPatch() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());
        r.addFilter(new com.sun.jersey.api.client.filter.LoggingFilter());
        assertEquals("PATCH", r.method("PATCH", String.class, "PATCH"));

        ClientResponse cr = r.method("PATCH", ClientResponse.class, "PATCH");
        assertTrue(cr.hasEntity());
        cr.close();
    }

    public void testAll() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        assertEquals("GET", r.get(String.class));

        assertEquals("POST", r.post(String.class, "POST"));

        assertEquals(204, r.path("noproduce").post(ClientResponse.class, "POST").getStatus());

        assertEquals(204, r.path("noconsumeproduce").post(ClientResponse.class).getStatus());

        assertEquals("PUT", r.post(String.class, "PUT"));

        assertEquals("DELETE", r.delete(String.class));
    }


    @Path("/test")
    public static class ErrorResource {
        @POST
        public Response post(String entity) {
            return Response.serverError().build();
        }

        @Path("entity")
        @POST
        public Response postWithEntity(String entity) {
            return Response.serverError().entity("error").build();
        }
    }

    public void testPostError() {
        startServer(ErrorResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        // This test will lock up if ClientResponse is not closed by WebResource.
        // TODO need a better way to detect this.
        for (int i = 0; i < 100; i++) {
            try {
                r.post("POST");
            } catch (UniformInterfaceException ex) {
            }
        }
    }

    public void testPostErrorWithEntity() {
        startServer(ErrorResource.class);
        WebResource r = createClient().resource(getUri().path("test/entity").build());

        // This test will lock up if ClientResponse is not closed by WebResource.
        // TODO need a better way to detect this.
        for (int i = 0; i < 100; i++) {
            try {
                r.post("POST");
            } catch (UniformInterfaceException ex) {
                String s = ex.getResponse().getEntity(String.class);
                assertEquals("error", s);
            }
        }
    }
}