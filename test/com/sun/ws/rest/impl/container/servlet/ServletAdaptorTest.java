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

package com.sun.ws.rest.impl.container.servlet;

import java.util.Enumeration;
import java.util.Vector;
import junit.framework.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author mh124079
 */
public class ServletAdaptorTest extends TestCase {
    
    public ServletAdaptorTest(String testName) {
        super(testName);
    }

    /**
     * Test of init method, of class com.sun.ws.rest.impl.servlet.ServletAdaptor.
     */
    public void testInit() throws ServletException {
        System.out.println("init");
        
        ServletAdaptor instance = new ServletAdaptor();
        ServletConfig servletConfig = new MyServletConfig("TestServlet", "com.sun.ws.rest.impl.container.servlet.WebResources");        
        
        instance.init(servletConfig);
        
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of service method, of class com.sun.ws.rest.impl.servlet.ServletAdaptor.
     */
    public void testService() throws Exception {
        System.out.println("service");
        
        HttpServletRequest req = null;
        HttpServletResponse resp = null;
        ServletAdaptor instance = new ServletAdaptor();
    }
    
    public static class MyServletConfig implements ServletConfig {
        String resources;
        String servletName;
        MyServletConfig(String servletName, String resources) {
            this.servletName = servletName;
            this.resources = resources;
        }
        
        public ServletContext getServletContext() {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public Enumeration getInitParameterNames() {
            Vector v = new Vector();
            v.add("webresourceclass");
            return v.elements();
        }
        
        public String getInitParameter(String param) {
            if (param.equals("webresourceclass"))
                return resources;
            return null;
        }
        
        public String getServletName() {
            return servletName;
        }
    }    
}
