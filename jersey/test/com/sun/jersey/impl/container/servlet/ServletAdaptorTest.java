/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.jersey.impl.container.servlet;

import com.sun.jersey.api.core.ClasspathResourceConfig;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;


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
                "build/test/classes/com/sun/jersey/impl/container/config/toplevel");
        
        instance.init(servletConfig);
        
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of service method, of class com.sun.jersey.impl.servlet.ServletAdaptor.
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
