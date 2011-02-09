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

package com.sun.jersey.client.apache.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.client.apache.ApacheHttpClient4;
import com.sun.jersey.client.apache.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.spi.resource.Singleton;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AuthTest extends AbstractGrizzlyServerTester {

    public AuthTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class PreemptiveAuthResource {
        @GET
        public String get(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            assertNotNull(value);
            return e;
        }
    }

//    public void testPreemptiveAuth() {
//        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        startServer(rc);
//
//        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
//        config.getState().setCredentials(null, null, -1, "name", "password");
//        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_AUTHENTICATION, true);
//        ApacheHttpClient4 c = ApacheHttpClient4.create(config);
//
//        WebResource r = c.resource(getUri().build());
//        assertEquals("GET", r.get(String.class));
//    }

//    public void testPreemptiveAuthPost() {
//        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        startServer(rc);
//
//        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
//        config.getState().setCredentials(null, null, -1, "name", "password");
//        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_AUTHENTICATION, true);
//        ApacheHttpClient4 c = ApacheHttpClient4.create(config);
//
//        WebResource r = c.resource(getUri().build());
//        assertEquals("POST", r.post(String.class, "POST"));
//    }

    @Path("/test")
    @Singleton
    public static class AuthResource {
        int requestCount = 0;
        @GET
        public String get(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return "GET";
        }

        @GET
        @Path("filter")
        public String getFilter(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return "GET";
        }

        @POST
        public String post(@Context HttpHeaders h, String e) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }

            return e;
        }

        @POST
        @Path("filter")
        public String postFilter(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return e;
        }

        @DELETE
        public void delete(@Context HttpHeaders h) {
            requestCount++;
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                assertEquals(1, requestCount);
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            } else {
                assertTrue(requestCount > 1);
            }
        }

        @DELETE
        @Path("filter")
        public void deleteFilter(@Context HttpHeaders h) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }
        }

        @DELETE
        @Path("filter/withEntity")
        public String deleteFilterWithEntity(@Context HttpHeaders h, String e) {
            String value = h.getRequestHeaders().getFirst("Authorization");
            if (value == null) {
                throw new WebApplicationException(Response.status(401).header("WWW-Authenticate", "Basic realm=\"WallyWorld\"").build());
            }

            return e;
        }
    }

//    public void testAuthGet() {
//        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        startServer(rc);
//
//        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
//        config.getState().setCredentials(null, null, -1, "name", "password");
//        ApacheHttpClient4 c = ApacheHttpClient4.create(config);
//
//        WebResource r = c.resource(getUri().path("test").build());
//        assertEquals("GET", r.get(String.class));
//    }

    public void testAuthGetWithClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        ApacheHttpClient4 c = ApacheHttpClient4.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter").build());
        assertEquals("GET", r.get(String.class));
    }

//    public void testAuthPost() {
//        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        startServer(rc);
//
//        DefaultApacheHttpClient4Config config = new DefaultApacheHttp4ClientConfig();
//        config.getState().setCredentials(null, null, -1, "name", "password");
//        ApacheHttpClient4 c = ApacheHttpClient4.create(config);
//
//        WebResource r = c.resource(getUri().path("test").build());
//        assertEquals("POST", r.post(String.class, "POST"));
//    }

    public void testAuthPostWithClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        ApacheHttpClient4 c = ApacheHttpClient4.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

//    public void testAuthDelete() {
//        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
//                LoggingFilter.class.getName());
//        startServer(rc);
//        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
//        config.getState().setCredentials(null, null, -1, "name", "password");
//        ApacheHttpClient4 c = ApacheHttpClient4.create(config);
//
//        WebResource r = c.resource(getUri().path("test").build());
//        ClientResponse response = r.delete(ClientResponse.class);
//        assertEquals(response.getStatus(), 204);
//    }

    public void testAuthDeleteWithClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        ApacheHttpClient4 c = ApacheHttpClient4.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter").build());
        ClientResponse response = r.delete(ClientResponse.class);
        assertEquals(204, response.getStatus());
    }

    // test disabled; DELETE with entity is no longer supported
    public void _testAuthDeleteWithEntityUsingClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);
        ApacheHttpClient4 c = ApacheHttpClient4.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter/withEntity").build());
        ClientResponse response = r.delete(ClientResponse.class, "DELETE");
        assertEquals(200, response.getStatus());
        assertEquals("DELETE", response.getEntity(String.class));
    }

    static class BasicCredentialsProvider implements CredentialsProvider {
        @Override
        public void setCredentials(AuthScope authScope, Credentials credentials) {

        }

        @Override
        public Credentials getCredentials(AuthScope authScope) {
            return new UsernamePasswordCredentials("name",
                                                    "password");
        }

        @Override
        public void clear() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

//    public void testAuthInteractiveGet() {
//        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        startServer(rc);
//
//        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
//        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_INTERACTIVE, true);
//        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
//        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER, bcp);
//        ApacheHttpClient4 c = ApacheHttpClient4.create(config);
//
//        WebResource r = c.resource(getUri().path("test").build());
//        assertEquals("GET", r.get(String.class));
//        assertTrue(bcp.isCalled());
//    }

//    public void testAuthInteractivePost() {
//        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        startServer(rc);
//
//        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
//        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_INTERACTIVE, true);
//        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
//        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER, bcp);
//        ApacheHttpClient4 c = ApacheHttpClient4.create(config);
//
//        WebResource r = c.resource(getUri().path("test").build());
//        assertEquals("POST", r.post(String.class, "POST"));
//        assertTrue(bcp.isCalled());
//    }
}
