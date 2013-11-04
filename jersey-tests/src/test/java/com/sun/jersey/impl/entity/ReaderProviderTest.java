/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.impl.AbstractResourceTester;

import org.junit.Test;

/**
 * Testing {@link java.io.Reader} on client and server.
 *
 * @author Miroslav Fuksa (miroslav.fuksa at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class ReaderProviderTest extends AbstractResourceTester {

    public final static String GET_READER_RESPONSE = "GET_READER_RESPONSE";
    public static final String GET_POST_RESPONSE = "GET_POST_RESPONSE";

    public ReaderProviderTest(final String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        initiateWebApplication(ReaderResource.class);
    }

    @Test
    public void testReader() {
        ClientResponse response = resource("test/postReaderGetReader").entity(GET_POST_RESPONSE,
                MediaType.TEXT_PLAIN).post(ClientResponse.class);

        assertEquals(200, response.getStatus());
        assertEquals(GET_POST_RESPONSE, response.getEntity(String.class));
    }

    @Test
    public void testGetReader() {
        ClientResponse response = resource("test/getReader").get(ClientResponse.class);

        assertEquals(200, response.getStatus());
        assertEquals(GET_READER_RESPONSE, response.getEntity(String.class));
    }

    @Test
    public void testEmptyReader() throws IOException {
        ClientResponse response = resource("test/getEmpty").get(ClientResponse.class);

        assertEquals(204, response.getStatus());
        assertFalse(response.hasEntity());
    }

    @Test
    public void testReaderOnClientAsResponseEntity() throws IOException {
        ClientResponse response = resource("test/getReader").get(ClientResponse.class);
        assertEquals(200, response.getStatus());

        final Reader reader = response.getEntity(Reader.class);
        assertNotNull(reader);
        BufferedReader br = new BufferedReader(reader);
        assertEquals(GET_READER_RESPONSE, br.readLine());
    }

    @Test
    public void testReaderOnClientAsRequestEntity() throws IOException {
        ClientResponse response = resource("test/postReaderGetReader")
                .entity(new StringReader(GET_POST_RESPONSE), MediaType.TEXT_PLAIN).post(ClientResponse.class);

        assertEquals(200, response.getStatus());
        assertEquals(GET_POST_RESPONSE, response.getEntity(String.class));
    }


    @Path("test")
    public static class ReaderResource {
        @POST
        @Path("postReaderGetReader")
        public Reader postReader(Reader reader) throws IOException {
            return reader;
        }


        @GET
        @Path("getReader")
        public Reader getReader() throws IOException {
            return new StringReader(GET_READER_RESPONSE);
        }

        @GET
        @Path("getEmpty")
        public String getemptyResponse() throws IOException {
            return null;
        }
    }
}
