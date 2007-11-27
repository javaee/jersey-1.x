/*
 * IntrospectionModellerTest.java
 * JUnit based test
 *
 * Created on November 5, 2007, 11:12 AM
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
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
        
        @HttpMethod
        @ConsumeMime({"application/json", "application/xml"})
        public String postResourceMethodTester() {
            return "Hi there, here is a resource method.";
        }
        
        @Path("/subres-locator/{p1}")
        public TestSubResourceOne getSubResourceMethodTester(
                @UriParam("p1") String pOne, @MatrixParam("p2") int pTwo, @HeaderParam("p3") String pThree) {
            return new TestSubResourceOne();
        }
        
        @HttpMethod("PUT")
        @Path("/subres-method")
        public String getSubResourceMethod(String entityParam) {
            return "Hi there, here is a subresource method!";
        }
        
        @HttpMethod
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
        
        @HttpMethod
        public String getResourceMethodTester() {
            return "hi, here is a resource method of TestSubResourceOne";
        }
        
        @HttpMethod
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
        assertTrue(1 == webAppModel.getRootResources().size());
        assertTrue(1 == webAppModel.getSubResources().size());
    }
    
    
    public void testRootResource() {
        AbstractResource rootResource = webAppModel.getRootResources().get(0);
        assertTrue("/one".equals(rootResource.getUriTemplate().getRawTemplate()));
        assertTrue(1 == rootResource.getResourceMethods().size());
        assertTrue(1 == rootResource.getSubResourceLocators().size());
        assertTrue(2 == rootResource.getSubResourceMethods().size());
        
        AbstractResourceMethod resourceMethod = rootResource.getResourceMethods().get(0);
//        @HttpMethod
//        @ConsumeMime({"application/json", "application/xml"})
//        public String postResourceMethodTester() {
//            return "Hi there, here is a resource method.";
//        }
        assertTrue("POST".equals(resourceMethod.getHttpMethod()));
        assertTrue(0 == resourceMethod.getParameters().size());
        assertTrue(2 == resourceMethod.getSupportedInputTypes().size());
        assertTrue(resourceMethod.getSupportedInputTypes().contains(new MediaType("application/json")));
        assertTrue(resourceMethod.getSupportedInputTypes().contains(new MediaType("application/xml")));
        assertTrue(1 == resourceMethod.getSupportedOutputTypes().size());
        assertTrue("*/*".equals(resourceMethod.getSupportedOutputTypes().get(0).toString()));
        
        AbstractSubResourceLocator locator = rootResource.getSubResourceLocators().get(0);
//        @Path("/subres-locator/{p1}")
//        public TestSubResourceOne getSubResourceMethodTester(
//                @UriParam("p1") String pOne, @MatrixParam("p2") int pTwo, @HeaderParam("p3") String pThree) {
//            return new TestSubResourceOne();
//        }
        assertTrue("/subres-locator/{p1}".equals(locator.getUriTemplate().getRawTemplate()));
        assertTrue(3 == locator.getParameters().size());
        assertTrue(Parameter.Source.URI == locator.getParameters().get(0).getSource());
        assertTrue("p1".equals(locator.getParameters().get(0).getSourceName()));
        assertTrue(String.class == locator.getParameters().get(0).getParameterClass());
        assertTrue(String.class == locator.getParameters().get(0).getParameterType());
        assertTrue(Parameter.Source.MATRIX == locator.getParameters().get(1).getSource());
        assertTrue("p2".equals(locator.getParameters().get(1).getSourceName()));
        assertTrue(int.class == locator.getParameters().get(1).getParameterClass());
        assertTrue(int.class == locator.getParameters().get(1).getParameterType());
        assertTrue(Parameter.Source.HEADER == locator.getParameters().get(2).getSource());
        assertTrue("p3".equals(locator.getParameters().get(2).getSourceName()));

        AbstractSubResourceMethod subResMethod1 = rootResource.getSubResourceMethods().get(0);
        AbstractSubResourceMethod subResMethod2 = null;
        if("getSubResourceMethod".equals(subResMethod1.getMethod().getName())) {
            subResMethod2 = rootResource.getSubResourceMethods().get(1);
        } else {
            subResMethod2 = subResMethod1;
            subResMethod1 = rootResource.getSubResourceMethods().get(1);
        }
//        @HttpMethod("PUT")
//        @Path("/subres-method")
//        public String getSubResourceMethod(String entityParam) {
//            return "Hi there, here is a subresource method!";
//        }
        assertTrue("/subres-method".equals(subResMethod1.getUriTemplate().getRawTemplate()));
        assertTrue("PUT".equals(subResMethod1.getHttpMethod()));
        assertTrue(1 == subResMethod1.getParameters().size());
        assertTrue(Parameter.Source.ENTITY == subResMethod1.getParameters().get(0).getSource());
//        @HttpMethod
//        @Path("/with-params/{one}")
//        @ProduceMime("text/plain")
//        public String getSubResourceMethodWithParams(@UriParam("one") String paramOne) {
//            return "Hi there, here is a subresource method!";
//        }
        assertTrue("/with-params/{one}".equals(subResMethod2.getUriTemplate().getRawTemplate()));
        assertTrue("GET".equals(subResMethod2.getHttpMethod()));
        assertTrue(1 == subResMethod2.getParameters().size());
        assertTrue(Parameter.Source.URI == subResMethod2.getParameters().get(0).getSource());
        assertTrue("one".equals(subResMethod2.getParameters().get(0).getSourceName()));
        assertTrue("text/plain".equals(subResMethod2.getSupportedOutputTypes().get(0).toString()));
    }

    public void testSubResource() {
        AbstractResource subResource = webAppModel.getSubResources().get(0);
        assertTrue(null == subResource.getUriTemplate());
        assertTrue(2 == subResource.getResourceMethods().size());
        assertTrue(0 == subResource.getSubResourceLocators().size());
        assertTrue(0 == subResource.getSubResourceMethods().size());
    }
    
}
