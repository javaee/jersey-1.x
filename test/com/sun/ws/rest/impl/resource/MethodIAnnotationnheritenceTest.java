/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.resource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class MethodIAnnotationnheritenceTest extends AbstractResourceTester {
    
    public MethodIAnnotationnheritenceTest(String testName) {
        super(testName);
    }
    
    public static class SubResource {
        UriInfo ui;
        String p;
        String q;
        String h;
        
        SubResource(UriInfo ui, String p, String q, String h) {
            this.ui = ui;
            this.p = p;
            this.q = q;
            this.h = h;
        }
        
        @GET
        public String get() {
            return p + q + h;
        }
    }
    public static interface Interface {
        @GET
        @ProduceMime("application/get")
        String get();
        
        @GET
        @ProduceMime("application/getParams")
        String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @POST
        @ProduceMime("application/xml")
        @ConsumeMime("text/plain")
        String post(String s);
        
        @Path("sub")
        SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @GET
        @Path("submethod")
        String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
    }

    @Path("/{p}")
    public static class InterfaceImplementation implements Interface {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }
    
    public void testInterfaceImplementation() {
        initiateWebApplication(InterfaceImplementation.class);
        
        _test();
    }
    
    public void _test() {
        String s = resource("/a").accept("application/get").
                get(String.class);
        assertEquals("implementation", s);
        
        s = resource("/a?q=b").accept("application/getParams").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);
        
        ClientResponse cr = resource("/a").
                type("text/plain").
                accept("application/xml").
                post(ClientResponse.class, "content");
        assertEquals("<root>content</root>", cr.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/xml"), cr.getType());
        
        s = resource("/a/sub?q=b").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);
        
        s = resource("/a/submethod?q=b").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);        
    }
    
    public static abstract class AbstractClass {
        @GET
        @ProduceMime("application/get")
        public abstract String get();
        
        @GET
        @ProduceMime("application/getParams")
        public abstract String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @POST
        @ProduceMime("application/xml")
        @ConsumeMime("text/plain")
        public abstract String post(String s);
        
        @Path("sub")
        public abstract SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @GET
        @Path("submethod")
        public abstract String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);        
    }
    
    @Path("/{p}")
    public static class AbstractClassImplementation extends AbstractClass {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }
    
    public void testAbstractClassImplementation() {
        initiateWebApplication(AbstractClassImplementation.class);
        
        _test();
    }
    
    
    public static class ConcreteClass {
        @GET
        @ProduceMime("application/get")
        public String get() {
            return "void";
        }
        
        @GET
        @ProduceMime("application/getParams")
        public String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h) {
            return "void";
        }
        
        @POST
        @ProduceMime("application/xml")
        @ConsumeMime("text/plain")
        public String post(String s) {
            return "void";
        }
        
        @Path("sub")
        public SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h) {
            return null;            
        }
        
        @GET
        @Path("submethod")
        public String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h) {
            return "void";            
        }
    }
    
    @Path("/{p}")
    public static class ConcreteClassOverride extends ConcreteClass {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }

    public void testConcreteClassOverride() {
        initiateWebApplication(ConcreteClassOverride.class);
        
        _test();
    }

    
    public static abstract class AbstractOverrideClass implements Interface {
        public abstract String get();
        
        public abstract String getParams(UriInfo ui, String p, String q, String h);
        
        public abstract String post(String s);
        
        public abstract SubResource subResource(UriInfo ui, String p, String q, String h);
        
        public abstract String subMethod(UriInfo ui, String p, String q, String h);
    }
    
    @Path("/{p}")
    public static class AbstractOverrideClassInterface extends AbstractOverrideClass {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }
    
    public void testAbstractOverrideClassInterface() {
        initiateWebApplication(AbstractOverrideClassInterface.class);
        
        _test();
    }

    @Path("/{_p}")
    public static class InterfaceImplementationOverride implements Interface {
        
        @GET
        @ProduceMime("application/getoverride")
        public String get() {
            return "override";
        }
        
        @GET
        @ProduceMime("application/getParamsoverride")
        public String getParams(
                @Context UriInfo ui,
                @PathParam("_p") String p,
                @QueryParam("_q") String q, 
                @HeaderParam("_h") String h) {
            return p + q + h;
        }
        
        @POST
        @ProduceMime("application/xhtml")
        @ConsumeMime("application/octet-stream")
        public String post(String s) {
            return "<root>" + s + "</root>";
        }  
        
        @Path("suboverride")
        public SubResource subResource(
                @Context UriInfo ui,
                @PathParam("_p") String p,
                @QueryParam("_q") String q, 
                @HeaderParam("_h") String h) {
            return new SubResource(ui, p, q, h);            
        }
        
        @GET
        @Path("submethodoverride")
        public String subMethod(
                @Context UriInfo ui,
                @PathParam("_p") String p,
                @QueryParam("_q") String q, 
                @HeaderParam("_h") String h) {
            return p + q + h;            
        }
    }
    
    public void testInterfaceImplementationOverride() {
        initiateWebApplication(InterfaceImplementationOverride.class);
        String s = resource("/a").accept("application/getoverride")
                .get(String.class);
        assertEquals("override", s);
        
        s = resource("/a?_q=b").
                accept("application/getParamsoverride").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);
        
        ClientResponse cr = resource("/a").
                type("application/octet-stream").
                accept("application/xhtml").
                post(ClientResponse.class, "content");
        assertEquals("<root>content</root>", cr.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/xhtml"), cr.getType());
        
        s = resource("/a/suboverride?_q=b").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);        
        
        s = resource("/a/submethodoverride?_q=b").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);        
        
        
        cr = resource("/a", false).accept("application/get").
                get(ClientResponse.class);
        assertEquals(406, cr.getStatus());
        
        cr = resource("/a?q=b", false).accept("application/getParams")
                .header("h", "c").
                get(ClientResponse.class);
        assertEquals(406, cr.getStatus());
        
        cr = resource("/a", false).
                type("text/plain").
                accept("application/xml").
                post(ClientResponse.class, "content");
        assertEquals(415, cr.getStatus());        
        
        cr = resource("/a/sub", false).
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
        
        cr = resource("/a/submethod", false).
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
    }
}