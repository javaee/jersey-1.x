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

package com.sun.ws.rest.impl.bean;

import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class TemplatePathTest extends AbstractBeanTester {
    
    public TemplatePathTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/a/{arg1}")
    public static class ResourceA {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg1") String arg1) {
            return "A";
        }
    }
    
    @UriTemplate("/a/b/{arg1}")
    public static class ResourceAB {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg1") String arg1) {
            return "AB";
        }
    }
    
    @UriTemplate("/a/{arg1}/b")
    public static class ResourceAArg1B {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg1") String arg1) {
            return "AArg1B";
        }
    }
    
    @UriTemplate("/a/{arg1}/c")
    public static class ResourceAArg1C {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg1") String arg1) {
            return "AArg1C";
        }
    }
    
    public void testTemplateAtEnd() {
        Set<Class> s = new HashSet<Class>();
        s.add(ResourceA.class);
        s.add(ResourceAB.class);
        
        HttpResponseContext response = call(s, "GET", "/a/a", null, null, "foo");
        String sr = (String)response.getEntity();
        assertEquals("A", sr);
        
        response = call(s, "GET", "/a/a/a", null, null, "foo");
        sr = (String)response.getEntity();
        assertEquals("A", sr);
        
        response = call(s, "GET", "/a/b/ab", null, null, "foo");
        sr = (String)response.getEntity();
        assertEquals("AB", sr);
    }    
    
    public void testTemplateInMiddle() {
        Set<Class> s = new HashSet<Class>();
        s.add(ResourceA.class);
        s.add(ResourceAArg1B.class);
        
        HttpResponseContext response = call(s, "GET", "/a/a", null, null, "foo");
        String sr = (String)response.getEntity();
        assertEquals("A", sr);
        
        response = call(s, "GET", "/a/a/a", null, null, "foo");
        sr = (String)response.getEntity();
        assertEquals("A", sr);
        
        response = call(s, "GET", "/a/infix/b", null, null, "foo");
        sr = (String)response.getEntity();
        assertEquals("AArg1B", sr);
    }    
    
    public void testTwoTemplatesInMiddle() {
        Set<Class> s = new HashSet<Class>();
        s.add(ResourceAArg1B.class);
        s.add(ResourceAArg1C.class);
        
        HttpResponseContext response = call(s, "GET", "/a/infix/b", null, null, "foo");
        String sr = (String)response.getEntity();
        assertEquals("AArg1B", sr);
                
        response = call(s, "GET", "/a/infix/c", null, null, "foo");
        sr = (String)response.getEntity();
        assertEquals("AArg1C", sr);
    }    
}
