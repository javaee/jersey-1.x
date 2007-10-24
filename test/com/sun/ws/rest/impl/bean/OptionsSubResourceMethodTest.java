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

import com.sun.ws.rest.impl.client.ResponseInBound;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OptionsSubResourceMethodTest extends AbstractBeanTester {
    
    public OptionsSubResourceMethodTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    static public class ResourceNoOptions { 
        @UriTemplate("sub")
        @HttpMethod
        public String get() {
            return "GET";
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public String put(String e) {
            return "PUT";
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public String post(String e) {
            return "POST";
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public void delete() {
        }
        
        @UriTemplate("sub")
        @HttpMethod("PATCH")
        public String patch(String e) {
            return "PATCH";
        }
    }
        
    public void testNoOptions() {
        initiateWebApplication(ResourceNoOptions.class);

        ResponseInBound response = resourceProxy("/sub").invoke("OPTIONS", 
                ResponseInBound.class);
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("PUT"));
        assertTrue(allow.contains("POST"));
        assertTrue(allow.contains("DELETE"));
        assertTrue(allow.contains("PATCH"));
    }
    
    @UriTemplate("/")
    static public class ResourceWithOptions { 
        
        @UriTemplate("sub")
        @HttpMethod("OPTIONS")
        public Response options() {
            return Response.Builder.representation("OPTIONS").
                    header("Allow", "GET, PUT, POST, DELETE, PATCH").build();
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public String get() {
            return "GET";
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public String put(String e) {
            return "PUT";
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public String post(String e) {
            return "POST";
        }
        
        @UriTemplate("sub")
        @HttpMethod
        public void delete() {
        }
        
        @UriTemplate("sub")
        @HttpMethod("PATCH")
        public String patch(String e) {
            return "PATCH";
        }
    }
    
    public void testWithOptions() {
        initiateWebApplication(ResourceWithOptions.class);

        ResponseInBound response = resourceProxy("/sub").invoke("OPTIONS", 
                ResponseInBound.class);
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("PUT"));
        assertTrue(allow.contains("POST"));
        assertTrue(allow.contains("DELETE"));
        assertTrue(allow.contains("PATCH"));
    }
    
    @UriTemplate("/")
    static public class ResourceNoOptionsDifferentSub { 
        @UriTemplate("sub1")
        @HttpMethod
        public String getFoo() {
            return "FOO";
        }
        
        @UriTemplate("sub2")
        @HttpMethod
        public String putBar() {
            return "BAR";
        }
    }
    
    public void testNoOptionsDifferentSub() {
        initiateWebApplication(ResourceNoOptionsDifferentSub.class);

        ResponseInBound response = resourceProxy("/sub1").invoke("OPTIONS", 
                ResponseInBound.class);
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
        assertFalse(allow.contains("PUT"));
        
        response = resourceProxy("/sub2").invoke("OPTIONS", 
                ResponseInBound.class);
        allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("PUT"));
        assertFalse(allow.contains("GET"));
    }
}
