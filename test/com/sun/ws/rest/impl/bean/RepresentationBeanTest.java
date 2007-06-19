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

import com.sun.ws.rest.api.Entity;
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
        public String doPost(Entity<String> in) {
            assertEquals("BEAN-ONE", in.getContent());
            return "RETURN";
        }
        
        @HttpMethod("GET")
        public String doGet() {
            return "RETURN";
        }
        
        @HttpMethod("PUT")
        public String doPut(Entity<String> in) {
            assertEquals("BEAN-ONE", in.getContent());
            return "RETURN";
        }
        
        @HttpMethod("DELETE")
        public String doDelete() {
            return "RETURN";
        }
    }
    
    public void testOneWebResource() {
        Class r = TestOneWebResourceBean.class;
        call(r, "POST", "/a/b", null, null, "BEAN-ONE");
        call(r, "GET", "/a/b", null, null, "BEAN-ONE");
        call(r, "PUT", "/a/b", null, null, "BEAN-ONE");
        call(r, "DELETE", "/a/b", null, null, "BEAN-ONE");
    }    
}
