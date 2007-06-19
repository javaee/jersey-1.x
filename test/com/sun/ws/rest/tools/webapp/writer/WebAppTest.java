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

import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.output.StreamSerializer;
import java.io.ByteArrayOutputStream;
import junit.framework.*;


/**
 *
 * @author Doug Kohlert
 */
public class WebAppTest extends TestCase {
    
    public WebAppTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test class com.sun.ws.rest.webapp.writer.WebApp.
     */
    public void testWebApp() {
        System.out.println("testWebApp");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WebApp webApp = TXW.create(WebApp.class, new StreamSerializer(out));
        Servlet servlet = webApp.servlet();
        servlet.servletClass("com.sun.ws.rest.servlet.WebResourceServlet");
        InitParam param = servlet.initParam();
        param.name("param1");
        param.value("value1");
        ServletMapping servletMapping = webApp.servletMapping();
        param = servlet.initParam();
        param.name("param2");
        param.value("value2");
        
        webApp.commit();
        String output = out.toString();
        assert(output.indexOf("servlet-class>com.sun.ws.rest.servlet.WebResourceServlet</")!=-1);
        assert(output.indexOf("param-name>param1</")!=-1);
        assert(output.indexOf("param-value>value1</")!=-1);
        assert(output.indexOf("param-name>param2</")!=-1);
        assert(output.indexOf("param-value>value2</")!=-1);
        
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(WebAppTest.class);
        return suite;        
        
    }    

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
   
}
