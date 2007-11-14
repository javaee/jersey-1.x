/*
 * IntrospectionModellerTest.java
 * JUnit based test
 *
 * Created on November 5, 2007, 11:12 AM
 */

package com.sun.ws.rest.impl.model;

import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.impl.modelapi.annotation.IntrospectionModeller;
import junit.framework.*;
import com.sun.ws.rest.api.model.AbstractWebAppModel;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author japod
 */
public class IntrospectionModellerTest extends TestCase {
    
    public IntrospectionModellerTest(String testName) {
        super(testName);
    }
    
    
    public void printMimeTypes(List<MediaType> mediaTypes, PrintWriter pWriter) {
        boolean firstItem = true;
        for(MediaType mediaType : mediaTypes) {
            if (firstItem) {
                firstItem = false;
            } else {
                pWriter.print(",");
            }
            pWriter.print(mediaType.getType() + "/" + mediaType.getSubtype());
        }
    }
    
    public void printResource(AbstractResource resource, PrintWriter pWriter) {
        for (AbstractResourceMethod rMethod : resource.getResourceMethods()) {
            pWriter.println("----method \""  + rMethod.getMethod().getName() + "\"");
            pWriter.println("-----http method \""  + rMethod.getHttpMethod() + "\"");
            pWriter.print("-----consumes: \"");
            printMimeTypes(rMethod.getSupportedInputTypes(), pWriter);
            pWriter.println("\"");
            pWriter.print("-----produces: \"");
            printMimeTypes(rMethod.getSupportedOutputTypes(), pWriter);
            pWriter.println("\"");
        }
    }
    
    public void printResourceModel(AbstractWebAppModel rm, OutputStream os) {
        PrintWriter pWriter = new PrintWriter(os, true);
        pWriter.println("-Resource Model:");
        pWriter.println("--Root Resources:");
        for (AbstractResource rootResource : rm.getRootResources()) {
            pWriter.println("---Root Resource: " + rootResource.getResourceClass().getName());
            printResource(rootResource, pWriter);
        }
        pWriter.println("--Sub Resources:");
        for (AbstractResource subResource : rm.getSubResources()) {
            pWriter.println("---Sub Resource: " + subResource.getResourceClass().getName());
            printResource(subResource, pWriter);
        }
    }

    /**
     * Test of createModel method, of class com.sun.ws.rest.impl.model.IntrospectionModeller.
     */
    public void testCreateModel() {
        System.out.println("createModel");
        
        Set<Class> resourceClasses = new HashSet<Class>();
        
        resourceClasses.add(TestRootResourceOne.class);
        resourceClasses.add(TestSubResourceOne.class);
        
        AbstractWebAppModel expResult = null;
        AbstractWebAppModel result = IntrospectionModeller.createModel(resourceClasses);
        printResourceModel(result, System.out);
        assertTrue(result.getRootResources().size() == 1);
        assertTrue(result.getSubResources().size() == 1);
    }
    
}
