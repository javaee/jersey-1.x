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

package com.sun.ws.rest.impl.subresources;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.bean.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceHttpMethodsTest extends AbstractResourceTester {
    
    public SubResourceHttpMethodsTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    static public class SubResourceMethods { 
        @HttpMethod
        public String getMe() {
            return "/";
        }

        @UriTemplate("sub")
        @HttpMethod
        public String getMeSub() {
            return "/sub";
        }
        
        @UriTemplate("sub/sub")
        @HttpMethod
        public String getMeSubSub() {
            return "/sub/sub";
        }
    }
    
    public void testSubResourceMethods() {
        initiateWebApplication(SubResourceMethods.class);
        
        assertEquals("/", resourceProxy("/").get(String.class));
        assertEquals("/sub", resourceProxy("/sub").get(String.class));
        assertEquals("/sub/sub", resourceProxy("/sub/sub").get(String.class));
    }
    
    @UriTemplate("/")
    static public class SubResourceMethodsWithTemplates { 
        @HttpMethod
        public String getMe() {
            return "/";
        }

        @UriTemplate("sub{t}")
        @HttpMethod
        public String getMeSub(@UriParam("t") String t) {
            return t;
        }
        
        @UriTemplate("sub/{t}")
        @HttpMethod
        public String getMeSubSub(@UriParam("t") String t) {
            return t;
        }
    }
    
    public void testSubResourceMethodsWithTemplates() {
        initiateWebApplication(SubResourceMethodsWithTemplates.class);
        
        assertEquals("/", resourceProxy("/").get(String.class));
        assertEquals("value/a", resourceProxy("/subvalue/a").get(String.class));
        assertEquals("a/b/c/d", resourceProxy("/sub/a/b/c/d").get(String.class));
    }
    
    @UriTemplate("/")
    static public class SubResourceMethodsWithDifferentTemplates { 
        @UriTemplate("{foo}")
        @HttpMethod
        public String getFoo(@UriParam("foo") String foo) {
            return foo;
        }
        
        @UriTemplate("{bar}")
        @HttpMethod
        public String postBar(@UriParam("bar") String bar) {
            return bar;
        }
    }
    
    public void testSubResourceMethodsWithDifferentTemplates() {
        initiateWebApplication(SubResourceMethodsWithDifferentTemplates.class);
        
        assertEquals("foo", resourceProxy("/foo").get(String.class));
        assertEquals("bar", resourceProxy("/bar").get(String.class));
    }
}
