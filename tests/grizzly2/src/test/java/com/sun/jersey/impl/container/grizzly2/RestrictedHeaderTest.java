/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Assert;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;

/**
 * Test setting headers that are restricted by {@link java.net.HttpURLConnection}.
 *
 * @author Miroslav Fuksa (miroslav.fuksa at oracle.com)
 */
public class RestrictedHeaderTest extends AbstractGrizzlyServerTester {
    public RestrictedHeaderTest(String name) {
        super(name);
    }

    @Path("/")
    public static class MyResource {

        @GET
        public Response getOptions(@Context HttpHeaders headers) {
            MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
            System.out.println("Headers: " + requestHeaders);
            if (requestHeaders.containsKey("Origin") || requestHeaders.containsKey("Access-Control-Request-Method")) {
                return Response.ok().build();
            }
            return Response.serverError().entity("CORS headers are missing").build();
        }
    }

    /**
     * Tests sending of restricted headers (Origin and Access-Control-Request-Method) which are
     * used for CORS. These headers are by default skipped by the {@link java.net.HttpURLConnection}.
     * The system property {@code sun.net.http.allowRestrictedHeaders} must be defined in order to
     * allow these headers.
     */
    public void testForbiddenHeaders() {
        Client client = Client.create();
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        testHeaders(client);
    }

    /**
     * Same as {@link #testForbiddenHeaders()} but uses {@link ApacheHttpClient4} connector
     * which allows modification of these headers.
     */
    public void testForbiddenHeadersWithApacheConnector() {
        Client client = ApacheHttpClient4.create();
        testHeaders(client);
    }

    private void testHeaders(Client client) {
        DefaultResourceConfig rc = new DefaultResourceConfig(MyResource.class);
        rc.getContainerRequestFilters().add(LoggingFilter.class);
        startServer(rc);
        client.addFilter(new com.sun.jersey.api.client.filter.LoggingFilter());

        ClientResponse response = client.resource(getUri().path("/").build()).
                header("Origin", "http://example.com").
                header("Access-Control-Request-Method", "POST").
                header("Testus", "Hello").
                get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());
    }
}