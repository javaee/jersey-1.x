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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class TemplatePathTest extends AbstractResourceTester {
    
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
        initiateWebApplication(ResourceA.class, ResourceAB.class);
        
        assertEquals("A", resourceProxy("/a/a").get(String.class));
        assertEquals("A", resourceProxy("/a/a/a").get(String.class));
        assertEquals("AB", resourceProxy("/a/b/ab").get(String.class));
    }
    
    public void testTemplateInMiddle() {
        initiateWebApplication(ResourceA.class, ResourceAArg1B.class);
        
        assertEquals("A", resourceProxy("/a/a").get(String.class));
        assertEquals("A", resourceProxy("/a/a/a").get(String.class));
        assertEquals("AArg1B", resourceProxy("/a/infix/b").get(String.class));
    }    
    
    public void testTwoTemplatesInMiddle() {
        initiateWebApplication(ResourceAArg1B.class, ResourceAArg1C.class);
        
        assertEquals("AArg1B", resourceProxy("/a/infix/b").get(String.class));
        assertEquals("AArg1C", resourceProxy("/a/infix/c").get(String.class));
    }    
}
