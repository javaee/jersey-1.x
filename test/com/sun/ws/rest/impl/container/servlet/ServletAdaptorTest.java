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

import com.sun.jersey.api.core.ClasspathResourceConfig;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
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

    // TODO fix later
    public void testInit() throws ServletException {
        System.out.println("init");
        
        ServletAdaptor instance = new ServletAdaptor();
        ServletConfig servletConfig = new MyServletConfig("TestServlet", 
                "build/test/classes/com/sun/ws/rest/impl/container/config/toplevel");
        
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
    
    static class MyServletConfig implements ServletConfig {
        String resourcePaths;
        String servletName;
        MyServletConfig(String servletName, String resources) {
            this.servletName = servletName;
            this.resourcePaths = resources;
        }
        
        public ServletContext getServletContext() {
            return new MyServletContext();
        }
        
        @SuppressWarnings("unchecked")
        public Enumeration getInitParameterNames() {
            Vector v = new Vector();
            v.add("webresourceclass");
            return v.elements();
        }
        
        public String getInitParameter(String param) {
            if (param.equals(ClasspathResourceConfig.PROPERTY_CLASSPATH))
                return resourcePaths;
            return null;
        }
        
        public String getServletName() {
            return servletName;
        }
    }   
    
    static class MyServletContext implements ServletContext {

        public String getContextPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ServletContext getContext(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getMajorVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int getMinorVersion() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getMimeType(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Set getResourcePaths(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URL getResource(String arg0) throws MalformedURLException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public InputStream getResourceAsStream(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public RequestDispatcher getRequestDispatcher(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public RequestDispatcher getNamedDispatcher(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Servlet getServlet(String arg0) throws ServletException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration getServlets() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration getServletNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void log(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void log(Exception arg0, String arg1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void log(String arg0, Throwable arg1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getRealPath(String arg0) {
            return arg0;
        }

        public String getServerInfo() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getInitParameter(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration getInitParameterNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Object getAttribute(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Enumeration getAttributeNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setAttribute(String arg0, Object arg1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeAttribute(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getServletContextName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
