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

package com.sun.jersey.impl.container.grizzly;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Tests custom response status reason phrase with jersey containers and connectors.
 *
 * @author Miroslav Fuksa (miroslav.fuksa at oracle.com)
 */
public class ResponseStatusTypeTest extends AbstractGrizzlyServerTester {

    public static final String REASON_PHRASE = "my-phrase";

    public ResponseStatusTypeTest(String name) {
        super(name);
    }


    @Path("resource")
    public static class TestResource {

        @GET
        @Path("custom")
        public Response testStatusType() {
            return Response.status(new Custom428Type()).build();
        }

        @GET
        @Path("bad-request")
        public Response badRequest() {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        @GET
        @Path("custom-bad-request")
        public Response customBadRequest() {
            return Response.status(new CustomBadRequestWithoutReasonType()).build();
        }
    }


    public static class Custom428Type implements Response.StatusType {
        @Override
        public int getStatusCode() {
            return 428;
        }

        @Override
        public String getReasonPhrase() {
            return REASON_PHRASE;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }
    }


    public static class CustomBadRequestWithoutReasonType implements Response.StatusType {
        @Override
        public int getStatusCode() {
            return 400;
        }

        @Override
        public String getReasonPhrase() {
            return null;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }
    }

    @Test
    public void testCustom() {
        startServer(TestResource.class);
        WebResource resource = Client.create().resource(getUri().path("resource/custom").build());
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        Assert.assertEquals(428, clientResponse.getStatus());
        Assert.assertEquals(REASON_PHRASE, clientResponse.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void testBadRequest() {
        startServer(TestResource.class);
        WebResource resource = Client.create().resource(getUri().path("resource/bad-request").build());
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        Assert.assertEquals(400, clientResponse.getStatus());
        Assert.assertEquals(Response.Status.BAD_REQUEST.getReasonPhrase(),
                clientResponse.getStatusInfo().getReasonPhrase());
    }

    @Test
    public void testCustomBadRequest() {
        startServer(TestResource.class);
        WebResource resource = Client.create().resource(getUri().path("resource/custom-bad-request").build());
        ClientResponse clientResponse = resource.get(ClientResponse.class);
        Assert.assertEquals(400, clientResponse.getStatus());
        Assert.assertEquals(Response.Status.BAD_REQUEST.getReasonPhrase(),
                clientResponse.getStatusInfo().getReasonPhrase());
    }
}
