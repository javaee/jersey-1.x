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

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AcceptTest extends AbstractResourceTester {
    
    public AcceptTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class Resource {
        @Produces("application/foo")
        @GET
        public String doGetFoo() {
            return "foo";
        }

        @Produces("application/bar")
        @GET
        public String doGetBar() {
            return "bar";
        }

        @Produces("application/baz")
        @GET
        public String doGetBaz() {
            return "baz";
        }

        @Produces("*/*")
        @GET
        public Response doGetWildCard() {
            return Response.ok("wildcard", "application/wildcard").build();
        }
    }

    public void testAcceptGet() throws IOException {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");

        String s = r.accept("application/foo").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo;q=0.1").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo", "application/bar;q=0.4", "application/baz;q=0.2").
                get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo;q=0.4", "application/bar", "application/baz;q=0.2").
                get(String.class);
        assertEquals("bar", s);

        s = r.accept("application/foo;q=0.4", "application/bar;q=0.2", "application/baz").
                get(String.class);
        assertEquals("baz", s);
    }

    public void testAcceptGetWildCard() {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");

        String s = r.accept("application/wildcard", "application/foo;q=0.6",
                "application/bar;q=0.4", "application/baz;q=0.2").
                get(String.class);
        assertEquals("wildcard", s);
    }

    public void testQualityErrorGreaterThanOne() {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/", false);

        ClientResponse response = r.accept("application/foo;q=1.1").get(ClientResponse.class);
        assertEquals(400, response.getStatus());
    }

    public void testQualityErrorMoreThanThreeDigits() {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/", false);

        ClientResponse response = r.accept("application/foo;q=0.1234").get(ClientResponse.class);
        assertEquals(400, response.getStatus());
    }

    @Path("/")
    public static class MultipleResource {
        @Produces({"application/foo", "application/bar"})
        @GET
        public String get() {
            return "GET";
        }
    }

    public void testAcceptMultiple() {
        initiateWebApplication(MultipleResource.class);
        WebResource r = resource("/");

        MediaType foo = MediaType.valueOf("application/foo");
        MediaType bar = MediaType.valueOf("application/bar");

        ClientResponse response = r.accept(foo).get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getType());

        response = r.accept(bar).get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(bar, response.getType());

        response = r.accept("*/*").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getType());

        response = r.accept("application/*").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getType());

        response = r.accept("application/foo;q=0.1").
                accept("application/bar").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(bar, response.getType());

        response = r.accept("application/foo;q=0.5").
                accept("application/bar;q=0.1").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getType());
    }

    @Path("/")
    public static class SubTypeResource {
        @Produces("text/*")
        @GET
        public String get() {
            return "GET";
        }
    }

    public void testAcceptSubType() {
        initiateWebApplication(SubTypeResource.class);
        WebResource r = resource("/");

        ClientResponse response = r.accept("text/plain").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getType());

        response = r.accept("image/png, text/plain").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getType());

        response = r.accept("text/plain;q=0.5, text/html").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.TEXT_HTML_TYPE, response.getType());
    }

    @Path("/")
    public static class NoProducesResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    public void testAcceptNoProduces() {
        initiateWebApplication(NoProducesResource.class);
        WebResource r = resource("/");

        ClientResponse response = r.accept("image/png, text/plain").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.valueOf("image/png"), response.getType());

        response = r.accept("text/plain;q=0.5, text/html").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.TEXT_HTML_TYPE, response.getType());
    }

    @Path("/")
    public static class ProducesOneMethodFooBarResource {
        @GET
        @Produces({"application/foo", "application/bar"})
        public String get() {
            return "GET";
        }
    }

    public void testProducesOneMethodFooBarResource() {
        test(ProducesOneMethodFooBarResource.class);
    }

    @Path("/")
    public static class ProducesTwoMethodsFooBarResource {
        @GET
        @Produces("application/foo")
        public String getFoo() {
            return "GET";
        }

        @GET
        @Produces("application/bar")
        public String getBar() {
            return "GET";
        }
    }

    public void testProducesTwoMethodsFooBarResource() {
        test(ProducesTwoMethodsFooBarResource.class);
    }

    @Path("/")
    public static class ProducesTwoMethodsBarFooResource {
        @GET
        @Produces("application/bar")
        public String getBar() {
            return "GET";
        }

        @GET
        @Produces("application/foo")
        public String getFoo() {
            return "GET";
        }
    }

    public void testProducesTwoMethodsBarFooResource() {
        test(ProducesTwoMethodsBarFooResource.class);
    }

    private void test(Class<?> c) {
        initiateWebApplication(c);
        WebResource r = resource("/");

        ClientResponse response = r.accept("application/foo").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/foo"), response.getType());

        response = r.accept("application/bar").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/bar"), response.getType());

        response = r.accept("application/foo", "application/bar").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/foo"), response.getType());

        response = r.accept("application/bar", "application/foo").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/bar"), response.getType());
    }
}
