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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import junit.framework.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author mh124079
 */
public class HttpRequestAdaptorTest extends TestCase {
    
    public HttpRequestAdaptorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testHeaders() {
        System.out.println("headers");

        HttpServletRequestImpl impl = new HttpServletRequestImpl("/widgets/10",
                "verbose=true&item=1&item=2", "<hello>world</hello>");
        HttpRequestAdaptor a = null;
        try {
            a = new HttpRequestAdaptor(impl);
        } catch (IOException ex) {
            fail("Unable to initialize adaptor");
        }
        
        MultivaluedMap<String, String> h = a.getRequestHeaders();
        assertEquals(h.getFirst("Content-Type"), "application/xml");
        assertEquals(h.get("Accept").size(), 2);
        assertEquals(h.getFirst("Foo"), null);
        assertEquals(h.get("Foo"), null);
    }
    
    public void testQueryParams() {
        System.out.println("queryParams");
        HttpServletRequestImpl impl = new HttpServletRequestImpl("/widgets/10",
                "verbose=true&item=1&item=2", "<hello>world</hello>");
        HttpRequestAdaptor a = null;
        try {
            a = new HttpRequestAdaptor(impl);
        } catch (IOException ex) {
            fail("Unable to initialize adaptor");
        }
        
        MultivaluedMap<String, String> p = a.getQueryParameters();
        assertEquals(p.get("verbose").size(), 1);
        assertEquals(p.getFirst("verbose"),"true");
        assertEquals(p.get("item").size(), 2);
        assertEquals(p.getFirst("item"),"1");
        assertEquals(p.get("foo"), null);
        assertEquals(p.getFirst("foo"), null);
    }
    
    protected static class HttpServletRequestImpl implements HttpServletRequest {
        
        Map<String, List<String>> headers;
        String queryString;
        String path;
        ServletStreamImpl ss;
        
        public HttpServletRequestImpl(String path, String queryString, String content) {
            this.path = path;
            this.queryString = queryString;
            this.ss = new ServletStreamImpl(content);
            this.headers = new HashMap<String, List<String>>();
            ArrayList<String> contentTypeValues = new ArrayList<String>();
            contentTypeValues.add("application/xml");
            this.headers.put("Content-Type", contentTypeValues);
            ArrayList<String> acceptValues = new ArrayList<String>();
            acceptValues.add("application/xml");
            acceptValues.add("text/xml");
            this.headers.put("Accept", acceptValues);
        }
        
        public String getAuthType() {
            return null;
        }

        public Cookie[] getCookies() {
            return null;
        }

        public long getDateHeader(String string) {
            return 0;
        }

        public String getHeader(String string) {
            List<String> values = headers.get(string);
            if (values != null && values.size() > 0)
                 return values.get(0);
            else
                return null;
       }

        public Enumeration getHeaders(String string) {
            Vector<String> values;
            if (headers.get(string) != null)
                values = new Vector<String>(headers.get(string));
            else
                values = new Vector<String>();
            return values.elements();
        }

        public Enumeration getHeaderNames() {
            Vector<String> values = new Vector<String>(headers.keySet());
            return values.elements();
        }

        public int getIntHeader(String string) {
            return 0;
        }

        public String getMethod() {
            return "POST";
        }

        public String getPathInfo() {
            return path;
        }

        public String getPathTranslated() {
            return null;
        }

        public String getContextPath() {
            return "/contextPath";
        }

        public String getQueryString() {
            return queryString;
        }

        public String getRemoteUser() {
            return null;
        }

        public boolean isUserInRole(String string) {
            return false;
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public String getRequestedSessionId() {
            return null;
        }

        public String getRequestURI() {
            return getPathInfo();
        }

        public StringBuffer getRequestURL() {
            return new StringBuffer(getRequestURI());
        }

        public String getServletPath() {
            return "/servletPath";
        }

        public HttpSession getSession(boolean b) {
            return null;
        }

        public HttpSession getSession() {
            return null;
        }

        public boolean isRequestedSessionIdValid() {
            return false;
        }

        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }

        public Object getAttribute(String string) {
            return null;
        }

        public Enumeration getAttributeNames() {
            return null;
        }

        public String getCharacterEncoding() {
            return null;
        }

        public void setCharacterEncoding(String string) throws UnsupportedEncodingException {
        }

        public int getContentLength() {
            return 0;
        }

        public String getContentType() {
            return null;
        }

        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        public String getParameter(String string) {
            return null;
        }

        public Enumeration getParameterNames() {
            return null;
        }

        public String[] getParameterValues(String string) {
            return null;
        }

        public Map getParameterMap() {
            return null;
        }

        public String getProtocol() {
            return null;
        }

        public String getScheme() {
            return null;
        }

        public String getServerName() {
            return null;
        }

        public int getServerPort() {
            return 80;
        }

        public BufferedReader getReader() throws IOException {
            return null;
        }

        public String getRemoteAddr() {
            return null;
        }

        public String getRemoteHost() {
            return null;
        }

        public void setAttribute(String string, Object object) {
        }

        public void removeAttribute(String string) {
        }

        public Locale getLocale() {
            return null;
        }

        public Enumeration getLocales() {
            return null;
        }

        public boolean isSecure() {
            return false;
        }

        public RequestDispatcher getRequestDispatcher(String string) {
            return null;
        }

        public String getRealPath(String string) {
            return null;
        }

        public int getRemotePort() {
            return 0;
        }

        public String getLocalName() {
            return null;
        }

        public String getLocalAddr() {
            return null;
        }

        public int getLocalPort() {
            return 80;
        }
        
        protected static class ServletStreamImpl extends ServletInputStream {
            
            private ByteArrayInputStream in;
            
            public ServletStreamImpl(String buffer) {
                in = new ByteArrayInputStream(buffer.getBytes());
            }
            
            public int read() throws IOException {
                return in.read();
            }
            
        }
        
    }
}
