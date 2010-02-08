/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.impl.container.filter;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MultipleFilters extends AbstractResourceTester {

    @Path("/")
    public static class Resource {
        @GET
        public String get(@Context HttpHeaders hh) { 
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }
    }
    
    @Path("/")
    public static class ResourceWebApplicationException {
        @GET
        public String get(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            throw new WebApplicationException(Response.ok(xTest.get(0) + xTest.get(1)).
                    build());
        }
    }

    public MultipleFilters(String testName) {
        super(testName);
    }
        
    public static class FilterOne implements ContainerRequestFilter, ContainerResponseFilter {
        public ContainerRequest filter(ContainerRequest request) {
            request.getRequestHeaders().add("X-TEST", "one");
            return request;
        }

        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            response.getHttpHeaders().add("X-TEST", "one");
            Throwable t = response.getMappedThrowable();
            if (t != null) {
                response.getHttpHeaders().add("X-TEST-EXCEPTION", t.getClass().getName());
            }
            return response;
        }        
    }
    
    public static class FilterTwo implements ContainerRequestFilter, ContainerResponseFilter {
        public ContainerRequest filter(ContainerRequest request) {
            request.getRequestHeaders().add("X-TEST", "two");
            return request;
        }

        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            response.getHttpHeaders().add("X-TEST", "two");
            Throwable t = response.getMappedThrowable();
            if (t != null) {
                response.getHttpHeaders().add("X-TEST-EXCEPTION", t.getClass().getName());
            }
            return response;
        }        
    }
    
    public void testResourceConfig() {
        ResourceConfig rc = new DefaultResourceConfig();

        assertTrue(rc.getContainerRequestFilters().isEmpty());
        assertTrue(rc.getContainerResponseFilters().isEmpty());
        assertTrue(rc.getResourceFilterFactories().isEmpty());

        assertTrue(rc.getProperty(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS) instanceof List);
        assertTrue(rc.getProperty(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS) instanceof List);
        assertTrue(rc.getProperty(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES) instanceof List);

        rc = new DefaultResourceConfig();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, "request");
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, "response");
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES, "resource");

        assertEquals("request", rc.getContainerRequestFilters().get(0));
        assertEquals("response", rc.getContainerResponseFilters().get(0));
        assertEquals("resource", rc.getResourceFilterFactories().get(0));

    }

    public void testWithString1() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                FilterOne.class.getName() + ";" + FilterTwo.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                FilterOne.class.getName() + ";" + FilterTwo.class.getName());
        initiateWebApplication(rc);
        _test();
    }

    public void testWithString2() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                FilterOne.class.getName() + "," + FilterTwo.class.getName());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                FilterOne.class.getName() + " " + FilterTwo.class.getName());
        initiateWebApplication(rc);
        _test();
    }

    public void testWithString3() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                FilterOne.class.getName() + " " + FilterTwo.class.getName() + " ,;;, ");
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                FilterOne.class.getName() + ";" + FilterTwo.class.getName() + ",");
        initiateWebApplication(rc);
        _test();
    }

    public void testWithStringArray() {
        String[] fs = new String[2];
        fs[0] = FilterOne.class.getName();
        fs[1] = FilterTwo.class.getName();

        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                fs);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                fs);
        initiateWebApplication(rc);
        _test();
    }

    public void testWithListInstance() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        
        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, 
                Arrays.asList(f1, f2));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, 
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);
        _test();
    }
    
    public void testWithListClass() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);

        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(FilterOne.class, FilterTwo.class));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(FilterOne.class, FilterTwo.class));
        initiateWebApplication(rc);
        _test();
    }

    public void testWithListString() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);

        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(FilterOne.class.getName(), FilterTwo.class.getName()));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(FilterOne.class.getName(), FilterTwo.class.getName()));
        initiateWebApplication(rc);
        _test();
    }

    public void testWithListStringArray() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);

        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new String[] {FilterOne.class.getName(), FilterTwo.class.getName()}));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(new String[] {FilterOne.class.getName(), FilterTwo.class.getName()}));
        initiateWebApplication(rc);
        _test();
    }

    public void testWithListMixed() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);

        List reql = new ArrayList();
        reql.add(FilterOne.class);
        reql.add(new FilterTwo());
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                reql);

        List resl = new ArrayList();
        resl.add(new String[] { FilterOne.class.getName() } );
        resl.add(FilterTwo.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(FilterOne.class, FilterTwo.class));

        initiateWebApplication(rc);
        _test();
    }

    public void testWithResourceWebApplicationException() {
        ResourceConfig rc = new DefaultResourceConfig(ResourceWebApplicationException.class);

        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(f1, f2));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);
        _test();
    }

    public void testWithResourceRuntimeException() {
        ResourceConfig rc = new DefaultResourceConfig(ResourceWebApplicationException.class);

        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(f1, f2));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertEquals("onetwo", cr.getEntity(String.class));
        List<String> xTest = cr.getMetadata().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("one", xTest.get(0));
        assertEquals("two", xTest.get(1));
        List<String> xTestE = cr.getMetadata().get("X-TEST-EXCEPTION");
        assertEquals(2, xTestE.size());
        assertEquals(WebApplicationException.class.getName().toString(),
                xTestE.get(0));
        assertEquals(WebApplicationException.class.getName().toString(),
                xTestE.get(1));


        cr = r.path("/foo").get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
        xTest = cr.getMetadata().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("one", xTest.get(0));
        assertEquals("two", xTest.get(1));
        xTestE = cr.getMetadata().get("X-TEST-EXCEPTION");
        assertEquals(2, xTestE.size());
        assertEquals(NotFoundException.class.getName().toString(),
                xTestE.get(0));
        assertEquals(NotFoundException.class.getName().toString(),
                xTestE.get(1));
    }

    public void _test() {
        WebResource r = resource("/", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertEquals("onetwo", cr.getEntity(String.class));
        List<String> xTest = cr.getMetadata().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("one", xTest.get(0));
        assertEquals("two", xTest.get(1));


        cr = r.path("/foo").get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
        xTest = cr.getMetadata().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("one", xTest.get(0));
        assertEquals("two", xTest.get(1));
    }
    
    public static class FilterWebApplicationException 
            implements ContainerRequestFilter, ContainerResponseFilter {
        public ContainerRequest filter(ContainerRequest request) {
            throw new WebApplicationException(Response.serverError().entity("request").build());
        }
        
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            throw new WebApplicationException(Response.serverError().entity("response").build());
        }
    }

    public void testResponseWithFilterWebApplicationException() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);

        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        FilterWebApplicationException fe = new FilterWebApplicationException();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(f1, f2));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(fe, f1));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(500, cr.getStatus());
        assertEquals("response", cr.getEntity(String.class));

        List<String> xTest = cr.getMetadata().get("X-TEST");
        assertNull(xTest);
    }

    public void testRequestWithFilterWebApplicationException() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);

        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        FilterWebApplicationException fe = new FilterWebApplicationException();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(f1, fe));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(500, cr.getStatus());
        assertEquals("request", cr.getEntity(String.class));

        List<String> xTest = cr.getMetadata().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("one", xTest.get(0));
        assertEquals("two", xTest.get(1));
    }

    public static class TestException extends RuntimeException {
        String s;

        TestException(String s) {
            this.s = s;
        }
    }

    public static class TestExceptionMapper implements ExceptionMapper<TestException> {
        public Response toResponse(TestException e) {
            return Response.serverError().entity(e.s).build();
        }
    }

    public static class FilterRuntimeException 
            implements ContainerRequestFilter, ContainerResponseFilter {
        public ContainerRequest filter(ContainerRequest request) {
            throw new TestException("request");
        }

        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            throw new TestException("response");
        }
    }

    public void testResponseWithFilterRuntimeException() {
        ResourceConfig rc = new DefaultResourceConfig(
                TestExceptionMapper.class, Resource.class);

        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        FilterRuntimeException fe = new FilterRuntimeException();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(f1, f2));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(fe, f1));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(500, cr.getStatus());
        assertEquals("response", cr.getEntity(String.class));

        List<String> xTest = cr.getMetadata().get("X-TEST");
        assertNull(xTest);
    }
    
    public void testRequestWithFilterRuntimeException() {
        ResourceConfig rc = new DefaultResourceConfig(
                TestExceptionMapper.class, Resource.class);

        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        FilterRuntimeException fe = new FilterRuntimeException();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(f1, fe));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(500, cr.getStatus());
        assertEquals("request", cr.getEntity(String.class));

        List<String> xTest = cr.getMetadata().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("one", xTest.get(0));
        assertEquals("two", xTest.get(1));
    }
}
