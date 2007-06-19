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

package com.sun.ws.rest.tools.webapp;

import junit.framework.*;
import static com.sun.codemodel.ClassType.CLASS;
import com.sun.codemodel.*;
import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import com.sun.ws.rest.tools.annotation.Resource;
import com.sun.ws.rest.tools.Messager;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

/**
 *
 * @author Doug Kohlert
 */
public class WebResourcesGeneratorTest extends TestCase implements Messager {
    
    public WebResourcesGeneratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of generateResourceClassSet method, of class com.sun.ws.rest.webapp.WebResourcesGenerator.
     */
    public void testGenerateResourceClassSet() {
        System.out.println("generateResourceClassSet");
        
        String pkg = "";
        
        TestFiler filer = new TestFiler();
        AnnotationProcessorEnvironment apEnv = new TestAnnotationProcessorEnvironment(filer);
        AnnotationProcessorContext context = new AnnotationProcessorContext();
        context.setAnnotationProcessorEnvironment(apEnv);
        context.setResourceBeanClassName("webresources.MyResourceBeans");
        Resource r1 = new Resource("class1", "/widgets/{widget}");
        context.getResources().add(r1);
        Resource r2 = new Resource("class2", "/widgets");
        context.getResources().add(r2);        
        WebResourcesGenerator instance = new WebResourcesGenerator(this, context);
        instance.generateResourceClassSet(".");
        String result = filer.getOutputString();
//        System.out.println("output: "+result);
        
        assert(result.indexOf("public class WebResources")!= -1);
        assert(result.indexOf("implements ResourceConfig")!= -1);
        assert(result.indexOf("resources.add(class1.class);")!= -1);
        assert(result.indexOf("resources.add(class2.class);")!= -1);
        assert(result.indexOf("public Set<Class> getResourceClasses() {")!= -1);
    }

    /**
     * Test of getCMClass method, of class com.sun.ws.rest.webapp.WebResourcesGenerator.
     */
    public void testGetCMClass() {
        System.out.println("getCMClass");
        
        String className = "java.util.Collection";
        TestFiler filer = new TestFiler();
        AnnotationProcessorEnvironment apEnv = new TestAnnotationProcessorEnvironment(filer);
        AnnotationProcessorContext context = new AnnotationProcessorContext();
        context.setAnnotationProcessorEnvironment(apEnv);
        context.setResourceBeanClassName("webresources.MyResourceBeans");
        Resource r1 = new Resource("class1", "/widgets/{widget}");
        context.getResources().add(r1);
        Resource r2 = new Resource("class2", "/widgets");
        context.getResources().add(r2);        
        WebResourcesGenerator instance = new WebResourcesGenerator(this, context);

        instance.generateResourceClassSet("");
        JDefinedClass result = instance.getCMClass(className, ClassType.CLASS);
        assert(result != null);
        
    }

    public void log(String msg) {
//        if (verbose)
//            apEnv.getMessager().printNotice("[UriTemplateProcessor] "+msg); 
    }

    public void reportError(String msg) {
        assert(false);
//        apEnv.getMessager().printError("[UriTemplateProcessor] "+msg); 
    }    
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(WebResourcesGeneratorTest.class);
        return suite;        
        
    }    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
