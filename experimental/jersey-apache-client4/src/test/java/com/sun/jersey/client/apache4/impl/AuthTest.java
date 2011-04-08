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

package com.sun.jersey.client.apache4.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.sun.jersey.spi.resource.Singleton;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.junit.Ignore;

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

    public void testPreemptiveAuth() {
        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        CredentialsProvider credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("name", "password")
        );

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                credentialsProvider
        );

        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION, true);

        ApacheHttpClient4 c = ApacheHttpClient4.create(config);

        WebResource r = c.resource(getUri().build());
        assertEquals("GET", r.get(String.class));
    }

    public void testPreemptiveAuthPost() {
        ResourceConfig rc = new DefaultResourceConfig(PreemptiveAuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        CredentialsProvider credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("name", "password")
        );

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                credentialsProvider
        );

        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION, true);

        ApacheHttpClient4 c = ApacheHttpClient4.create(config);

        WebResource r = c.resource(getUri().build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

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

    public void testAuthGet() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        CredentialsProvider credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("name", "password")
        );

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                credentialsProvider
        );
        ApacheHttpClient4 c = ApacheHttpClient4.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }

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

    // doesn't work - apache http client uses interactive auth by default
    // and currently ends with NonRepeatableRequestException: Cannot retry request with a non-repeatable request entity.
    @Ignore
    public void _testAuthPost() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        CredentialsProvider credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("name", "password")
        );

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                credentialsProvider
        );
        ApacheHttpClient4 c = ApacheHttpClient4.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }

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

    public void testAuthDelete() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
//                LoggingFilter.class.getName());
        startServer(rc);

        CredentialsProvider credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("name", "password")
        );

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                credentialsProvider
        );
        ApacheHttpClient4 c = ApacheHttpClient4.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        ClientResponse response = r.delete(ClientResponse.class);
        assertEquals(response.getStatus(), 204);
    }

    public void testAuthDeleteWithClientFilter() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        
        // TODO XXX FIXME - doesn't work when logging filters are present - bug?
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
//                LoggingFilter.class.getName());
//        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
//                LoggingFilter.class.getName());
        startServer(rc);
        ApacheHttpClient4 c = ApacheHttpClient4.create();
        c.addFilter(new HTTPBasicAuthFilter("name", "password"));

        WebResource r = c.resource(getUri().path("test/filter").build());
        ClientResponse response = r.delete(ClientResponse.class);
        assertEquals(204, response.getStatus());
    }

    // test disabled; DELETE with entity is no longer supported
    @Ignore
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

    public void testAuthInteractiveGet() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        CredentialsProvider credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("name", "password")
        );

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                credentialsProvider
        );
        ApacheHttpClient4 c = ApacheHttpClient4.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }

    // disabled - interactive POST is not good thing to do by design - we don't want to send entity twice
    @Ignore
    public void _testAuthInteractivePost() {
        ResourceConfig rc = new DefaultResourceConfig(AuthResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                LoggingFilter.class.getName());
        startServer(rc);

        CredentialsProvider credentialsProvider = new org.apache.http.impl.client.BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials("name", "password")
        );

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER,
                credentialsProvider
        );
        ApacheHttpClient4 c = ApacheHttpClient4.create(config);

        WebResource r = c.resource(getUri().path("test").build());
        assertEquals("POST", r.post(String.class, "POST"));
    }
}
