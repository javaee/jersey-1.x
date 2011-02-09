/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.modelapi.annotation;

import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import java.util.logging.LogRecord;
import junit.framework.*;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.impl.modelapi.annotation.IntrospectionModellerTest.TestSubResourceOne;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Handler;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author japod
 */
public class IntrospectionModellerTest extends TestCase {

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ApplicationAnnotation {
    }

    @Path("/one")
    public class TestRootResourceOne {
        
        /** Creates a new instance of TestRootResourceOne */
        public TestRootResourceOne() {
        }
        
        @POST
        @Consumes({"application/json", "application/xml"})
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

        @PUT
        @Path("/subres-method2")
        public String putSubResourceMethod2(/*@ApplicationAnnotation*/ String entityParam) {
            return "Hi there, here is a subresource method!";
        }

        @GET
        @Produces("text/plain")
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
        @Consumes("text/plain")
        public String putResourceMethodTester() {
            return "hi, here is a put resource method of TestSubResourceOne";
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
        assertEquals("/one", rootResource.getPath().getValue());
        assertEquals(1, rootResource.getResourceMethods().size());
        assertEquals(1 ,rootResource.getSubResourceLocators().size());
        assertEquals(3 ,rootResource.getSubResourceMethods().size());
        
        AbstractResourceMethod resourceMethod = rootResource.getResourceMethods().get(0);
//        @HttpMethod
//        @Consumes({"application/json", "application/xml"})
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
        assertEquals("/subres-locator/{p1}", locator.getPath().getValue());
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

        AbstractSubResourceMethod putSubResMethod1 = null;
        AbstractSubResourceMethod putSubResMethod2 = null;
        AbstractSubResourceMethod getSubResMethod = null;
        for (AbstractSubResourceMethod m: rootResource.getSubResourceMethods()) {
            if (m.getMethod().getName().equals("putSubResourceMethod"))
                putSubResMethod1 = m;
            else if (m.getMethod().getName().equals("putSubResourceMethod2"))
                putSubResMethod2 = m;
            else if (m.getMethod().getName().equals("getSubResourceMethodWithParams"))
                getSubResMethod = m;
        }
//        @PUT
//        @Path("/subres-method")
//        public String getSubResourceMethod(String entityParam) {
//            return "Hi there, here is a subresource method!";
//        }
        assertEquals("/subres-method", putSubResMethod1.getPath().getValue());
        assertEquals("PUT", putSubResMethod1.getHttpMethod());
        assertEquals(1, putSubResMethod1.getParameters().size());
        assertEquals(Parameter.Source.ENTITY, putSubResMethod1.getParameters().get(0).getSource());
//        @PUT
//        @Path("/subres-method2")
//        public String getSubResourceMethod(@ApplicationAnnotation String entityParam) {
//            return "Hi there, here is a subresource method!";
//        }
        assertEquals("/subres-method2", putSubResMethod2.getPath().getValue());
        assertEquals("PUT", putSubResMethod2.getHttpMethod());
        assertEquals(1, putSubResMethod2.getParameters().size());
        assertEquals(Parameter.Source.ENTITY, putSubResMethod2.getParameters().get(0).getSource());
//        @HttpMethod
//        @Path("/with-params/{one}")
//        @Produces("text/plain")
//        public String getSubResourceMethodWithParams(@PathParam("one") String paramOne) {
//            return "Hi there, here is a subresource method!";
//        }
        assertEquals("/with-params/{one}", getSubResMethod.getPath().getValue());
        assertEquals("GET", getSubResMethod.getHttpMethod());
        assertEquals(1, getSubResMethod.getParameters().size());
        assertEquals(Parameter.Source.PATH, getSubResMethod.getParameters().get(0).getSource());
        assertEquals("one", getSubResMethod.getParameters().get(0).getSourceName());
        assertEquals("text/plain", getSubResMethod.getSupportedOutputTypes().get(0).toString());
    }

    public void testSubResource() {
        AbstractResource subResource = IntrospectionModeller.createResource(TestSubResourceOne.class);
        assertEquals(null, subResource.getPath());
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
}
