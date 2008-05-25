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

package com.sun.jersey.impl.modelapi.annotation;

import com.sun.jersey.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import java.util.logging.LogRecord;
import junit.framework.*;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.modelapi.annotation.IntrospectionModellerTest.TestSubResourceOne;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author japod
 */
public class IntrospectionModellerTest extends TestCase {
    
    @Path("/one")
    public class TestRootResourceOne {
        
        /** Creates a new instance of TestRootResourceOne */
        public TestRootResourceOne() {
        }
        
        @POST
        @ConsumeMime({"application/json", "application/xml"})
        public String postResourceMethodTester() {
            return "Hi there, here is a resource method.";
        }
        
        @Path("/subres-locator/{p1}")
        public TestSubResourceOne getSubResourceMethodTester(
                @PathParam("p1") String pOne, @MatrixParam("p2") int pTwo, @HeaderParam("p3") String pThree) {
            return new TestSubResourceOne();
        }
        
        @PUT
        @Path("/subres-method")
        public String putSubResourceMethod(String entityParam) {
            return "Hi there, here is a subresource method!";
        }
        
        @GET
        @ProduceMime("text/plain")
        @Path("/with-params/{one}")
        public String getSubResourceMethodWithParams(@PathParam("one") String paramOne) {
            return "Hi there, here is a subresource method!";
        }
    }
    
    
    public class TestSubResourceOne {

        /** Creates a new instance of TestSubResourceOne */
        public TestSubResourceOne() {
        }
        
        @GET
        public String getResourceMethodTester() {
            return "hi, here is a resource method of TestSubResourceOne";
        }
        
        @PUT
        @ConsumeMime("text/plain")
        public String putResourceMethodTester() {
            return "hi, here is a put resource method of TestSubResourceOne";
        }
        
    }
    
    @Path("/np")
    public class TestRootResourceNonPubMethods {
        
        /** Creates a new instance of TestRootResourceOne */
        public TestRootResourceNonPubMethods() {
        }
        
        @GET
        String nonPublicResourceMethod() {
            return "jedna";
        }

        @Path("loc")
        String nonPublicSubresLocator() {
            return "dve";
        }

        @GET
        @Path("poc")
        String nonPublicSubresMethod() {
            return "tri";
        }
    }

    
    public IntrospectionModellerTest(String testName) {
        super(testName);
    }
    
//    private void printMimeTypes(List<MediaType> mediaTypes, PrintWriter pWriter) {
//        boolean firstItem = true;
//        for(MediaType mediaType : mediaTypes) {
//            if (firstItem) {
//                firstItem = false;
//            } else {
//                pWriter.print(",");
//            }
//            pWriter.print(mediaType.getType() + "/" + mediaType.getSubtype());
//        }
//    }
//    
//    private void printResource(AbstractResource resource, PrintWriter pWriter) {
//        for (AbstractResourceMethod rMethod : resource.getResourceMethods()) {
//            pWriter.println("----method \""  + rMethod.getMethod().getName() + "\"");
//            pWriter.println("-----http method \""  + rMethod.getHttpMethod() + "\"");
//            pWriter.print("-----consumes: \"");
//            printMimeTypes(rMethod.getSupportedInputTypes(), pWriter);
//            pWriter.println("\"");
//            pWriter.print("-----produces: \"");
//            printMimeTypes(rMethod.getSupportedOutputTypes(), pWriter);
//            pWriter.println("\"");
//        }
//    }
//    
//    private void printResourceModel(AbstractWebAppModel rm, OutputStream os) {
//        PrintWriter pWriter = new PrintWriter(os, true);
//        pWriter.println("-Resource Model:");
//        pWriter.println("--Root Resources:");
//        for (AbstractResource rootResource : rm.getRootResources()) {
//            pWriter.println("---Root Resource: " + rootResource.getResourceClass().getName());
//            printResource(rootResource, pWriter);
//        }
//        pWriter.println("--Sub Resources:");
//        for (AbstractResource subResource : rm.getSubResources()) {
//            pWriter.println("---Sub Resource: " + subResource.getResourceClass().getName());
//            printResource(subResource, pWriter);
//        }
//    }
    
    
    public void testRootResource() {
        AbstractResource rootResource = IntrospectionModeller.createResource(TestRootResourceOne.class);
        assertEquals("/one", rootResource.getUriPath().getValue());
        assertEquals(1, rootResource.getResourceMethods().size());
        assertEquals(1 ,rootResource.getSubResourceLocators().size());
        assertEquals(2 ,rootResource.getSubResourceMethods().size());
        
        AbstractResourceMethod resourceMethod = rootResource.getResourceMethods().get(0);
//        @HttpMethod
//        @ConsumeMime({"application/json", "application/xml"})
//        public String postResourceMethodTester() {
//            return "Hi there, here is a resource method.";
//        }
        assertEquals("POST", resourceMethod.getHttpMethod());
        assertEquals(0, resourceMethod.getParameters().size());
        assertEquals(2, resourceMethod.getSupportedInputTypes().size());
        assertTrue(resourceMethod.getSupportedInputTypes().contains(MediaType.valueOf("application/json")));
        assertTrue(resourceMethod.getSupportedInputTypes().contains(MediaType.valueOf("application/xml")));
        assertEquals(1, resourceMethod.getSupportedOutputTypes().size());
        assertEquals("*/*", resourceMethod.getSupportedOutputTypes().get(0).toString());
        
        AbstractSubResourceLocator locator = rootResource.getSubResourceLocators().get(0);
//        @Path("/subres-locator/{p1}")
//        public TestSubResourceOne getSubResourceMethodTester(
//                @PathParam("p1") String pOne, @MatrixParam("p2") int pTwo, @HeaderParam("p3") String pThree) {
//            return new TestSubResourceOne();
//        }
        assertEquals("/subres-locator/{p1}", locator.getUriPath().getValue());
        assertEquals(3, locator.getParameters().size());
        assertEquals(Parameter.Source.PATH, locator.getParameters().get(0).getSource());
        assertEquals("p1", locator.getParameters().get(0).getSourceName());
        assertEquals(String.class, locator.getParameters().get(0).getParameterClass());
        assertEquals(String.class, locator.getParameters().get(0).getParameterType());
        assertEquals(Parameter.Source.MATRIX, locator.getParameters().get(1).getSource());
        assertEquals("p2", locator.getParameters().get(1).getSourceName());
        assertEquals(int.class, locator.getParameters().get(1).getParameterClass());
        assertEquals(int.class, locator.getParameters().get(1).getParameterType());
        assertEquals(Parameter.Source.HEADER, locator.getParameters().get(2).getSource());
        assertEquals("p3", locator.getParameters().get(2).getSourceName());

        AbstractSubResourceMethod subResMethod1 = rootResource.getSubResourceMethods().get(0);
        AbstractSubResourceMethod subResMethod2 = null;
        if("putSubResourceMethod".equals(subResMethod1.getMethod().getName())) {
            subResMethod2 = rootResource.getSubResourceMethods().get(1);
        } else {
            subResMethod2 = subResMethod1;
            subResMethod1 = rootResource.getSubResourceMethods().get(1);
        }
//        @PUT
//        @Path("/subres-method")
//        public String getSubResourceMethod(String entityParam) {
//            return "Hi there, here is a subresource method!";
//        }
        assertEquals("/subres-method", subResMethod1.getUriPath().getValue());
        assertEquals("PUT", subResMethod1.getHttpMethod());
        assertEquals(1, subResMethod1.getParameters().size());
        assertEquals(Parameter.Source.ENTITY, subResMethod1.getParameters().get(0).getSource());
//        @HttpMethod
//        @Path("/with-params/{one}")
//        @ProduceMime("text/plain")
//        public String getSubResourceMethodWithParams(@PathParam("one") String paramOne) {
//            return "Hi there, here is a subresource method!";
//        }
        assertEquals("/with-params/{one}", subResMethod2.getUriPath().getValue());
        assertEquals("GET", subResMethod2.getHttpMethod());
        assertEquals(1, subResMethod2.getParameters().size());
        assertEquals(Parameter.Source.PATH, subResMethod2.getParameters().get(0).getSource());
        assertEquals("one", subResMethod2.getParameters().get(0).getSourceName());
        assertEquals("text/plain", subResMethod2.getSupportedOutputTypes().get(0).toString());
    }

    public void testSubResource() {
        AbstractResource subResource = IntrospectionModeller.createResource(TestSubResourceOne.class);
        assertEquals(null, subResource.getUriPath());
        assertEquals(2, subResource.getResourceMethods().size());
        assertEquals(0, subResource.getSubResourceLocators().size());
        assertEquals(0, subResource.getSubResourceMethods().size());
    }
    
    public static class TestHandler extends Handler {
        
        public int nonPubCounter = 0;

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("nonPublic")) {
                nonPubCounter++;
            }
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    }
    
    public void testNonPubMethodLogging() {
        Logger logger = Logger.getLogger(IntrospectionModeller.class.getName());
        TestHandler myHandler = new TestHandler();
        logger.addHandler(myHandler);
        logger.setLevel(Level.WARNING);
        IntrospectionModeller.createResource(TestRootResourceNonPubMethods.class);
        assertEquals(3, myHandler.nonPubCounter);
    }
    
}
