/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.impl.container.grizzly2;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HeadTest extends AbstractGrizzlyServerTester {
    public HeadTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class Resource {
        @Path("string")
        @GET
        public String getString() {
            return "GET";
        }

        @Path("byte")
        @GET
        public byte[] getByte() {
            return "GET".getBytes();
        }

        @Path("ByteArrayInputStream")
        @GET
        public InputStream getInputStream() {
            return new ByteArrayInputStream("GET".getBytes());
        }

        @Path("redirect")
        @GET
        public Response redirect() {
            return Response.status(303).location(URI.create("final")).build();
        }

        @Path("final")
        @GET
        public Response afterRedirection() {
            return Response.ok("final-entity").build();
        }


    }

    public void testHead() throws Exception {
        _startServer();

        WebResource r = Client.create().resource(getUri().path("/").build());

        ClientResponse cr = r.path("string").accept("text/plain").head();
        assertEquals(200, cr.getStatus());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, cr.getType());
        assertFalse(cr.hasEntity());

        cr = r.path("byte").accept("application/octet-stream").head();
        assertEquals(200, cr.getStatus());
        String length = cr.getMetadata().getFirst("Content-Length");
        assertNotNull(length);
        assertEquals(3, Integer.parseInt(length));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, cr.getType());
        assertFalse(cr.hasEntity());

        cr = r.path("ByteArrayInputStream").accept("application/octet-stream").head();
        assertEquals(200, cr.getStatus());
        length = cr.getMetadata().getFirst("Content-Length");
        assertNotNull(length);
        assertEquals(3, Integer.parseInt(length));
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_TYPE, cr.getType());
        assertFalse(cr.hasEntity());
    }

    private void _startServer() {
        DefaultResourceConfig config = new DefaultResourceConfig(Resource.class);
        config.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());
        config.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
        startServer(config);
    }


    public void testDontFollowRedirect1Head() throws Exception {
        _startServer();
        Client client = configureClientUsingMethod(false);

        ClientResponse response = _head(client);
        Assert.assertEquals(303, response.getStatus());
        Assert.assertTrue(response.getLocation().toString().endsWith("/final"));
    }

    public void testDontFollowRedirect1Get() throws Exception {
        _startServer();
        Client client = configureClientUsingMethod(false);

        ClientResponse response = _get(client);
        Assert.assertEquals(303, response.getStatus());
        Assert.assertTrue(response.getLocation().toString().endsWith("/final"));
    }

    private ClientResponse _head(Client client) {
        WebResource r = client.resource(getUri().path("/").build());
        return r.path("redirect").head();
    }

    private ClientResponse _get(Client client) {
        WebResource r = client.resource(getUri().path("/").build());
        return r.path("redirect").get(ClientResponse.class);
    }

    public void testDontFollowRedirect2Head() throws Exception {
        _startServer();
        Client client = configureClientUsingProperty(false);

        ClientResponse response = _head(client);
        Assert.assertEquals(303, response.getStatus());
        Assert.assertTrue(response.getLocation().toString().endsWith("/final"));
    }

    public void testFollowRedirect1Head() throws Exception {
        _startServer();
        Client client = configureClientUsingMethod(true);

        ClientResponse response = _head(client);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertFalse(response.hasEntity());
    }

    public void testFollowRedirect1Get() throws Exception {
        _startServer();
        Client client = configureClientUsingMethod(true);

        ClientResponse response = _get(client);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("final-entity", response.getEntity(String.class));
    }

    public void testFollowRedirect2Head() throws Exception {
        _startServer();
        Client client = configureClientUsingProperty(true);

        ClientResponse response = _head(client);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertFalse(response.hasEntity());
    }


    private Client configureClientUsingMethod(boolean followRedirect) {
        Client client = Client.create();
        client.setFollowRedirects(followRedirect);
        return client;
    }

    private Client configureClientUsingProperty(boolean followRedirect) {
        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, followRedirect);
        Client client = Client.create(config);
        return client;
    }
}