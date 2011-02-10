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

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.container.MappableContainerException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ResourceExceptionTest extends AbstractResourceTester {
    
    public ResourceExceptionTest(String testName) {
        super(testName);
    }

    static public class CheckedException extends Exception {
        public CheckedException() {
            super();
        }
    }
    
    @Path("/exception/checked")
    static public class ExceptionCheckedResource {
        @GET
        public String get() throws CheckedException {
            throw new CheckedException();
        }
    }

    public void testExceptionChecked() {
        initiateWebApplication(ExceptionCheckedResource.class);

        boolean caught = false;
        try {
            resource("/exception/checked").get(ClientResponse.class);
        } catch (MappableContainerException e) {
            caught = true;
            assertEquals(CheckedException.class, e.getCause().getClass());
        }
        assertTrue(caught);
    }

    @Path("/exception/runtime")
    static public class ExceptionRutimeResource {
        @GET
        public String get() {
            throw new UnsupportedOperationException();
        }
    }

    public void testExceptionRuntime() {
        initiateWebApplication(ExceptionRutimeResource.class);

        boolean caught = false;
        try {
            resource("/exception/runtime").get(ClientResponse.class);
        } catch (UnsupportedOperationException e) {
            caught = true;
        }
        assertTrue(caught);
    }


    @Path("/exception/checked")
    static public class ExceptionCheckedConstructorResource {
        public ExceptionCheckedConstructorResource() throws CheckedException {
            throw new CheckedException();
        }
    }

    public void testExceptionCheckedConstructor() {
        initiateWebApplication(ExceptionCheckedConstructorResource.class);

        boolean caught = false;
        try {
            resource("/exception/checked").get(ClientResponse.class);
        } catch (MappableContainerException e) {
            caught = true;
            assertEquals(CheckedException.class, e.getCause().getClass());
        }
        assertTrue(caught);
    }

    @Path("/exception/runtime")
    static public class ExceptionRutimeConstructorResource {
        public ExceptionRutimeConstructorResource() {
            throw new UnsupportedOperationException();
        }
    }

    public void testExceptionRuntimeConstructor() {
        initiateWebApplication(ExceptionRutimeConstructorResource.class);

        boolean caught = false;
        try {
            resource("/exception/runtime").get(ClientResponse.class);
        } catch (UnsupportedOperationException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Path("/exception/checked")
    static public class ExceptionCheckedConstructorParamsResource {
        public ExceptionCheckedConstructorParamsResource(@QueryParam("x") String x) throws CheckedException {
            throw new CheckedException();
        }
    }

    public void testExceptionCheckedParamsConstructor() {
        initiateWebApplication(ExceptionCheckedConstructorParamsResource.class);

        boolean caught = false;
        try {
            resource("/exception/checked").get(ClientResponse.class);
        } catch (MappableContainerException e) {
            caught = true;
            assertEquals(CheckedException.class, e.getCause().getClass());
        }
        assertTrue(caught);
    }

    @Path("/exception/runtime")
    static public class ExceptionRutimeConstructorParamsResource {
        public ExceptionRutimeConstructorParamsResource(@QueryParam("x") String x) {
            throw new UnsupportedOperationException();
        }
    }

    public void testExceptionRuntimeParamsConstructor() {
        initiateWebApplication(ExceptionRutimeConstructorParamsResource.class);

        boolean caught = false;
        try {
            resource("/exception/runtime").get(ClientResponse.class);
        } catch (UnsupportedOperationException e) {
            caught = true;
        }
        assertTrue(caught);
    }


    @Path("/exception/webapplication/{status}")
    static public class ExceptionWebApplicationResource { 
        @GET
        public String get(@PathParam("status") int status, @QueryParam("content") boolean content) {
            if (!content) {
                throw new WebApplicationException(status);
            } else
                throw new WebApplicationException(Response.status(status).
                        entity(Integer.toString(status)).build());
        }
    }
    
    public void test400StatusCode() {
        initiateWebApplication(ExceptionWebApplicationResource.class);

        ClientResponse cr = resource("/exception/webapplication/400", false).
                get(ClientResponse.class);        
        assertEquals(400, cr.getStatus());
    }
    
    public void test500StatusCode() {
        initiateWebApplication(ExceptionWebApplicationResource.class);

        ClientResponse cr = resource("/exception/webapplication/500", false).
                get(ClientResponse.class);        
        assertEquals(500, cr.getStatus());
    }
    
    @Provider
    public static class ExceptionMapper404 implements ExceptionMapper<WebApplicationException> {
        public Response toResponse(WebApplicationException we) {
            if (we.getResponse().getStatus() == 404) {
                return Response.status(404).entity("NOT FOUND").build();
            } else
                return null;
        }        
    }
    
    public void test404WithWebApplicationExceptionMapper() {
        initiateWebApplication(ExceptionMapper404.class, ExceptionWebApplicationResource.class);

        ClientResponse cr = resource("/exception/webapplication/400", false).
                get(ClientResponse.class);        
        assertEquals(204, cr.getStatus());
        
        cr = resource("/exception/webapplication/404", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("NOT FOUND", cr.getEntity(String.class));
        
        cr = resource("/exception/webapplication/404?content=true", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("404", cr.getEntity(String.class));
    }
    
    
    @Path("/exception/webapplication")
    static public class NotFoundWebApplicationResource { 
        @GET
        public String get() {
            throw new NotFoundException();
        }
    }
    
    public void testNotFoundExceptionMapper() {
        initiateWebApplication(ExceptionMapper404.class, NotFoundWebApplicationResource.class);

        ClientResponse cr = resource("/exception/webapplication", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("NOT FOUND", cr.getEntity(String.class));
    }
    
    
    @Provider
    public static class CheckedExceptionMapper404 implements ExceptionMapper<CheckedException> {
        public Response toResponse(CheckedException we) {
            return Response.status(404).entity("CheckedException").build();
        }        
    }
    
    public void testCheckedExceptionMapper404() {
        initiateWebApplication(CheckedExceptionMapper404.class, ExceptionCheckedResource.class);

        ClientResponse cr = resource("/exception/checked", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("CheckedException", cr.getEntity(String.class));
    }
    
    static public class SubCheckedException extends CheckedException {
        public SubCheckedException() {
            super();
        }
    }
    
    @Provider
    public static class SubCheckedExceptionMapper404 implements ExceptionMapper<SubCheckedException> {
        public Response toResponse(SubCheckedException we) {
            return Response.status(404).entity("SubCheckedException").build();
        }        
    }
    
    @Path("/exception/subchecked")
    static public class ExceptionSubCheckedResource { 
        @GET
        public String get() throws CheckedException {
            throw new SubCheckedException();
        }
    }
    
    public void testSubCheckedExceptionMapper404() {
        initiateWebApplication(CheckedExceptionMapper404.class, 
                SubCheckedExceptionMapper404.class, 
                ExceptionCheckedResource.class,
                ExceptionSubCheckedResource.class);

        ClientResponse cr = resource("/exception/checked", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("CheckedException", cr.getEntity(String.class));
        
        cr = resource("/exception/subchecked", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("SubCheckedException", cr.getEntity(String.class));
    }
    
    public void testNoSubCheckedExceptionMapper404() {
        initiateWebApplication(CheckedExceptionMapper404.class,
                ExceptionCheckedResource.class,
                ExceptionSubCheckedResource.class);

        ClientResponse cr = resource("/exception/checked", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("CheckedException", cr.getEntity(String.class));
        
        cr = resource("/exception/subchecked", false).
                get(ClientResponse.class);        
        assertEquals(404, cr.getStatus());
        assertEquals("CheckedException", cr.getEntity(String.class));
    }

    public static class TestException extends Exception {   
    }

    @Provider
    public static class NullTestExceptionMapper implements ExceptionMapper<TestException> {
        public Response toResponse(TestException we) {
            return null;
        }        
    }

    @Provider
    public static class RuntimeExceptionTestExceptionMapper implements ExceptionMapper<TestException> {
        public Response toResponse(TestException we) {
            throw new RuntimeException();
        }        
    }

    @Path("/")
    static public class TestExceptionResource { 
        @GET
        public String get() throws TestException {
            throw new TestException();
        }
    }
    
    public void testNullExceptionMapperResponse() {
        initiateWebApplication(TestExceptionResource.class,
                NullTestExceptionMapper.class);

        ClientResponse cr = resource("/", false).
                get(ClientResponse.class);        
        assertEquals(204, cr.getStatus());
    }
    
    public void testRuntimeExceptionMapperResponse() {
        initiateWebApplication(TestExceptionResource.class,
                RuntimeExceptionTestExceptionMapper.class);

        ClientResponse cr = resource("/", false).
                get(ClientResponse.class);        
        assertEquals(500, cr.getStatus());
    }


    @Provider
    public static class ThrowingTestExceptionMapper implements ExceptionMapper<TestException> {
        public Response toResponse(TestException we) {
            throw new MappableContainerException(new UnsupportedOperationException());
        }
    }

    @Path("/")
    static public class ThrowingTestExceptionResource {
        @GET
        public String get() throws TestException {
            throw new TestException();
        }
    }

    public void testThrowingTestExceptionResource() {
        initiateWebApplication(ThrowingTestExceptionResource.class,
                ThrowingTestExceptionMapper.class);

        boolean caught = false;
        try {
            resource("/").get(ClientResponse.class);
        } catch (MappableContainerException e) {
            assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
            caught = true;
        }
        assertTrue(caught);
    }


    public static class MappedException extends RuntimeException {
    }

    public static class MappedExceptionMapper implements ExceptionMapper<MappedException> {

        public Response toResponse(MappedException exception) {
            return Response.ok("CONTENT").build();
        }

    }

    @Path("/")
    public static class ProducesResource {
        @Path("wae")
        @Produces("application/foo, application/bar")
        @GET
        public String getWae() {
            throw new WebApplicationException(Response.ok("CONTENT").build());
        }

        @Path("me")
        @Produces("application/foo, application/bar")
        @GET
        public String getMe() {
            throw new MappedException();
        }

        @Path("404")
        @Produces("application/foo, application/bar")
        @GET
        public String getNotFound() {
            throw new NotFoundException();
        }
    }

    public void testProduces() {
        initiateWebApplication(ProducesResource.class,
                MappedExceptionMapper.class);

        ClientResponse cr = resource("/wae", false).
                accept("application/foo").
                get(ClientResponse.class);
        assertEquals("application/foo", cr.getType().toString());

        cr = resource("/wae", false).
                accept("application/bar").
                get(ClientResponse.class);
        assertEquals("application/bar", cr.getType().toString());


        cr = resource("/me", false).
                accept("application/foo").
                get(ClientResponse.class);
        assertEquals("application/foo", cr.getType().toString());

        cr = resource("/me", false).
                accept("application/bar").
                get(ClientResponse.class);
        assertEquals("application/bar", cr.getType().toString());

        cr = resource("/m404", false).
                accept("application/foo").
                get(ClientResponse.class);
        assertEquals(null, cr.getType());
    }

}