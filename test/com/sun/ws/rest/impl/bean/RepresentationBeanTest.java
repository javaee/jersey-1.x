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

import com.sun.ws.rest.impl.client.ResourceProxy;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RepresentationBeanTest extends AbstractBeanTester {
    
    public RepresentationBeanTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    public static class TestOneWebResourceBean {
        @HttpMethod("POST")
        public String doPost(String in) {
            assertEquals("BEAN-ONE", in);
            return "POST";
        }
        
        @HttpMethod("GET")
        public String doGet() {
            return "GET";
        }
        
        @HttpMethod("PUT")
        public String doPut(String in) {
            assertEquals("BEAN-ONE", in);
            return "PUT";
        }
        
        @HttpMethod("DELETE")
        public String doDelete() {
            return "DELETE";
        }
    }
    
    public void testOneWebResource() {
        initiateWebApplication(TestOneWebResourceBean.class);
        
        ResourceProxy r = resourceProxy("/a/b");
        assertEquals("POST", r.post(String.class, "BEAN-ONE"));
        assertEquals("GET", r.get(String.class));
        assertEquals("PUT", r.put(String.class, "BEAN-ONE"));
        assertEquals("DELETE", r.delete(String.class));
    }    
}
