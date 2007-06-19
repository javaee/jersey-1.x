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

package com.sun.ws.rest.tools.webapp.writer;

import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import com.sun.ws.rest.tools.annotation.Resource;
import java.io.ByteArrayOutputStream;
import junit.framework.*;

/**
 *
 * @author Doug Kohlert
 */
public class WebAppWriterTest extends TestCase {
    
    public WebAppWriterTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of writeTo method, of class com.sun.ws.rest.webapp.writer.WebAppWriter.
     */
    public void testWriteTo() throws Exception {
        System.out.println("writeTo");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        AnnotationProcessorContext context = new AnnotationProcessorContext();
        context.setResourceBeanClassName("webresources.WebResources");
        Resource r1 = new Resource("class1", "/widgets/{widget}");
        context.getResources().add(r1);
        Resource r2 = new Resource("class2", "/widgets");
        context.getResources().add(r2);

        WebAppWriter instance = new WebAppWriter("MyServlet", "TempName", "myURL/*", context);
        
        instance.writeTo(out);
//        instance.writeTo(System.out);
        String output = out.toString();
        assert(output.indexOf("<servlet-name>TempName</servlet-name>") != -1);
        assert(output.indexOf("<param-value>webresources.WebResources</param-value>") != -1);
        assert(output.indexOf("<url-pattern>myURL/*</url-pattern>")!=-1);

    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(WebAppWriterTest.class);
        return suite;        
        
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
