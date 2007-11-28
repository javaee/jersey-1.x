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

package com.sun.ws.rest.impl.modelapi.annotation;

import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.api.model.AbstractSubResourceLocator;
import com.sun.ws.rest.api.model.AbstractSubResourceMethod;
import junit.framework.*;
import com.sun.ws.rest.api.model.AbstractWebAppModel;
import com.sun.ws.rest.api.model.Parameter;
import com.sun.ws.rest.impl.modelapi.annotation.IntrospectionModellerTest.TestSubResourceOne;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
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
                @UriParam("p1") String pOne, @MatrixParam("p2") int pTwo, @HeaderParam("p3") String pThree) {
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
        public String getSubResourceMethodWithParams(@UriParam("one") String paramOne) {
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

    final Set<Class> resourceClasses = new HashSet<Class>();
    AbstractWebAppModel webAppModel;

    
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
    
    protected void setUp() {
        resourceClasses.add(TestRootResourceOne.class);
        resourceClasses.add(TestSubResourceOne.class);
        webAppModel = IntrospectionModeller.createModel(resourceClasses);
    }
    
    /**
     * Test of createModel method, of class com.sun.ws.rest.impl.model.IntrospectionModeller.
     */
    public void testCountRootAndSubResources() {
        assertEquals(1, webAppModel.getRootResources().size());
        assertEquals(1, webAppModel.getSubResources().size());
    }
    
    
    public void testRootResource() {
        AbstractResource rootResource = webAppModel.getRootResources().get(0);
        assertEquals("/one", rootResource.getUriTemplate().getRawTemplate());
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
        assertTrue(resourceMethod.getSupportedInputTypes().contains(new MediaType("application/json")));
        assertTrue(resourceMethod.getSupportedInputTypes().contains(new MediaType("application/xml")));
        assertEquals(1, resourceMethod.getSupportedOutputTypes().size());
        assertEquals("*/*", resourceMethod.getSupportedOutputTypes().get(0).toString());
        
        AbstractSubResourceLocator locator = rootResource.getSubResourceLocators().get(0);
//        @Path("/subres-locator/{p1}")
//        public TestSubResourceOne getSubResourceMethodTester(
//                @UriParam("p1") String pOne, @MatrixParam("p2") int pTwo, @HeaderParam("p3") String pThree) {
//            return new TestSubResourceOne();
//        }
        assertEquals("/subres-locator/{p1}", locator.getUriTemplate().getRawTemplate());
        assertEquals(3, locator.getParameters().size());
        assertEquals(Parameter.Source.URI, locator.getParameters().get(0).getSource());
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
        assertEquals("/subres-method", subResMethod1.getUriTemplate().getRawTemplate());
        assertEquals("PUT", subResMethod1.getHttpMethod());
        assertEquals(1, subResMethod1.getParameters().size());
        assertEquals(Parameter.Source.ENTITY, subResMethod1.getParameters().get(0).getSource());
//        @HttpMethod
//        @Path("/with-params/{one}")
//        @ProduceMime("text/plain")
//        public String getSubResourceMethodWithParams(@UriParam("one") String paramOne) {
//            return "Hi there, here is a subresource method!";
//        }
        assertEquals("/with-params/{one}", subResMethod2.getUriTemplate().getRawTemplate());
        assertEquals("GET", subResMethod2.getHttpMethod());
        assertEquals(1, subResMethod2.getParameters().size());
        assertEquals(Parameter.Source.URI, subResMethod2.getParameters().get(0).getSource());
        assertEquals("one", subResMethod2.getParameters().get(0).getSourceName());
        assertEquals("text/plain", subResMethod2.getSupportedOutputTypes().get(0).toString());
    }

    public void testSubResource() {
        AbstractResource subResource = webAppModel.getSubResources().get(0);
        assertEquals(null, subResource.getUriTemplate());
        assertEquals(2, subResource.getResourceMethods().size());
        assertEquals(0, subResource.getSubResourceLocators().size());
        assertEquals(0, subResource.getSubResourceMethods().size());
    }
    
}
