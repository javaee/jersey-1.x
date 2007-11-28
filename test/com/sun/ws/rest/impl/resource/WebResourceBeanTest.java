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
import javax.ws.rs.Path;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.client.ResourceProxy;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WebResourceBeanTest extends AbstractResourceTester {
    
    public WebResourceBeanTest(String testName) {
        super(testName);
    }
    
    @Path("/{arg1}/{arg2}")
    public static class TestOneWebResourceBean {
        @POST
        public void doPost(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            response.setEntity("POST");
        }
        
        @GET
        public void doGet(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());            
            response.setEntity("GET");
        }
        
        @PUT
        public void doPut(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("PUT", request.getHttpMethod());            
            response.setEntity("PUT");
        }
        
        @DELETE
        public void doDelete(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("DELETE", request.getHttpMethod());            
            response.setEntity("DELETE");
        }
    }
    
    public void testOneWebResource() {
        initiateWebApplication(TestOneWebResourceBean.class);
        
        ResourceProxy r = resourceProxy("a/b");
        
        assertEquals("POST", r.post(String.class));
        assertEquals("GET", r.get(String.class));
        assertEquals("PUT", r.put(String.class));
        assertEquals("DELETE", r.delete(String.class));
    }    
}
