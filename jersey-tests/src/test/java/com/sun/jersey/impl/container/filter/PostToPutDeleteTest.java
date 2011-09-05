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
package com.sun.jersey.impl.container.filter;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.PostReplaceFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.Arrays;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 * @author Martin Matula
 */
public class PostToPutDeleteTest extends AbstractResourceTester {

    @Path("/")
    public static class Resource {
        @GET
        public String get(@QueryParam("a") String a) { return "GET: " + a; }

        @PUT
        public String put() { return "PUT"; }

        @DELETE
        public String delete() { return "DELETE"; }

        @POST
        public String post() { return "POST"; }
    }

    public PostToPutDeleteTest(String testName) {
        super(testName);
    }

    public void initWithInstance(PostReplaceFilter.ConfigFlag... flags) {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new PostReplaceFilter(flags)));
        initiateWebApplication(rc);
    }

    public void initWithString(PostReplaceFilter.ConfigFlag... flags) {
        StringBuilder flagsSB = null;
        if (flags != null) {
            flagsSB = new StringBuilder();
            for (PostReplaceFilter.ConfigFlag f : flags) {
                flagsSB.append(f.name()).append(",");
            }
        }
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                PostReplaceFilter.class.getName());
        if (flagsSB != null) {
            rc.getProperties().put(PostReplaceFilter.PROPERTY_POST_REPLACE_FILTER_CONFIG, flagsSB.toString());
        }
        initiateWebApplication(rc);
    }


    public ClientResponse[] _testWithInstance(String uri, String method, PostReplaceFilter.ConfigFlag... flags) {
        initWithInstance(flags);
        return _test(uri, method);
    }

    public ClientResponse[] _testWithString(String uri, String method, PostReplaceFilter.ConfigFlag... flags) {
        initWithString(flags);
        return _test(uri, method);
    }

    public void testPutWithInstance() {
        assertResponseEquals("PUT,PUT,PUT,", _testWithInstance("/", "PUT"));
    }

    public void testDeleteWithString() {
        assertResponseEquals("DELETE,DELETE,DELETE,", _testWithString("/", "DELETE"));
    }

    public void testGetWithString() {
        assertResponseEquals("GET: null,GET: null,GET: null,", _testWithString("/", "GET"));
    }

    public void testGetWithParamsWithInstance() {
        assertResponseEquals("GET: test,GET: test,GET: test,", _testWithInstance(UriBuilder.fromPath("/").queryParam("a", "test").build().toString(), "GET"));
    }

    public void testPutHeaderOnlyWithInstance() {
        assertResponseEquals("PUT,POST,PUT,", _testWithInstance("/", "PUT", PostReplaceFilter.ConfigFlag.HEADER));
    }

    public void testPutHeaderAndQueryWithInstance() {
        assertResponseEquals("PUT,PUT,PUT,", _testWithInstance("/", "PUT", PostReplaceFilter.ConfigFlag.HEADER, PostReplaceFilter.ConfigFlag.QUERY));
    }

    public void testDeleteQueryOnlyWithString() {
        assertResponseEquals("POST,DELETE,DELETE,", _testWithString("/", "DELETE", PostReplaceFilter.ConfigFlag.QUERY));
    }

    public void testDeleteHeaderAndQueryWithString() {
        assertResponseEquals("DELETE,DELETE,DELETE,", _testWithString("/", "DELETE", PostReplaceFilter.ConfigFlag.QUERY, PostReplaceFilter.ConfigFlag.HEADER));
    }

    public void testConflictingMethodsWithInstance() {
        initWithInstance();
        ClientResponse cr = resource("/", false).queryParam("_method", "PUT").header("X-HTTP-Method-Override", "DELETE").post(ClientResponse.class);
        assertEquals(ClientResponse.Status.BAD_REQUEST.getStatusCode(), cr.getStatus());
    }

    public void testUnsupportedMethodWithInstance() {
        assertResponseEquals("405,405,405,", _testWithInstance("/", "PATCH"));
    }

    public void testUnsupportedMethodWithString() {
        assertResponseEquals("405,405,405,", _testWithString("/", "PATCH"));
    }

    public void testGetWithFormParamsWithString() {
        initWithString();
        Form f = new Form();
        f.add("a", "test");
        String result = resource("/", false).queryParam("_method", "GET").type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).entity(f).post(String.class);
        assertEquals("GET: test", result);
    }

    public void testGetWithOtherEntityWithInstance() {
        initWithInstance();
        String result = resource("/", false).queryParam("_method", "GET").type(MediaType.TEXT_PLAIN).entity("a=test").post(String.class);
        assertEquals("GET: null", result);
    }

    public void testPostWithInstance() {
        initWithInstance();
        String result = resource("/", false).post(String.class);
        assertEquals("POST", result);
    }

    public void testPostWithString() {
        initWithString();
        String result = resource("/", false).post(String.class);
        assertEquals("POST", result);
    }

    public ClientResponse[] _test(String uri, String method) {
        ClientResponse[] result = new ClientResponse[3];
        WebResource r = resource(uri.toString(), false);

        result[0] = r.header("X-HTTP-Method-Override", method).post(ClientResponse.class);
        result[1] = r.queryParam("_method", method).post(ClientResponse.class);
        result[2] = r.queryParam("_method", method).header("X-HTTP-Method-Override", method).post(ClientResponse.class);
        return result;
    }

    public void assertResponseEquals(String expected, ClientResponse[] responses) {
        StringBuilder result = new StringBuilder();

        for (ClientResponse r : responses) {
            if (r.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
                result.append(r.getEntity(String.class));
            } else {
                result.append(r.getStatus());
            }
            result.append(",");
        }

        assertEquals(expected, result.toString());
    }
}