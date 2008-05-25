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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RepresentationBeanTest extends AbstractResourceTester {
    
    public RepresentationBeanTest(String testName) {
        super(testName);
    }
    
    @Path("/{arg1}/{arg2}")
    public static class TestOneWebResourceBean {
        @POST
        public String doPost(String in) {
            assertEquals("BEAN-ONE", in);
            return "POST";
        }
        
        @GET
        public String doGet() {
            return "GET";
        }
        
        @PUT
        public String doPut(String in) {
            assertEquals("BEAN-ONE", in);
            return "PUT";
        }
        
        @DELETE
        public String doDelete() {
            return "DELETE";
        }
    }
    
    public void testOneWebResource() {
        initiateWebApplication(TestOneWebResourceBean.class);
        
        WebResource r = resource("/a/b");
        assertEquals("POST", r.post(String.class, "BEAN-ONE"));
        assertEquals("GET", r.get(String.class));
        assertEquals("PUT", r.put(String.class, "BEAN-ONE"));
        assertEquals("DELETE", r.delete(String.class));
    }    
}
