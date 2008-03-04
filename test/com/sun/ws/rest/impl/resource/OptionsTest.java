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
import com.sun.ws.rest.api.client.ClientResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OptionsTest extends AbstractResourceTester {
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("PATCH")
    public @interface PATCH { 
    }
    
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("PATCH")
    public @interface OPTIONS { 
    }
    
    public OptionsTest(String testName) {
        super(testName);
    }

    @Path("/")
    static public class ResourceNoOptions { 
        @GET
        public String get() {
            return "GET";
        }
        
        @PUT
        public String put(String e) {
            return "PUT";
        }
        
        @POST
        public String post(String e) {
            return "POST";
        }
        
        @DELETE
        public void delete() {
        }
        
        @PATCH
        public String patch(String e) {
            return "PATCH";
        }
    }
        
    public void testNoOptions() {
        initiateWebApplication(ResourceNoOptions.class);

        ClientResponse response = resource("/").options(
                ClientResponse.class);
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("PUT"));
        assertTrue(allow.contains("POST"));
        assertTrue(allow.contains("DELETE"));
        assertTrue(allow.contains("PATCH"));
    }
    
    @Path("/")
    static public class ResourceWithOptions { 
        
        @OPTIONS
        public Response options() {
            return Response.ok("OPTIONS").
                    header("Allow", "GET, PUT, POST, DELETE, PATCH").build();
        }
        
        @GET
        public String get() {
            return "GET";
        }
        
        @PUT
        public String put(String e) {
            return "PUT";
        }
        
        @POST
        public String post(String e) {
            return "POST";
        }
        
        @DELETE
        public void delete() {
        }
        
        @PATCH
        public String patch(String e) {
            return "PATCH";
        }
    }
    
    public void testWithOptions() {
        initiateWebApplication(ResourceWithOptions.class);

        ClientResponse response = resource("/").options(
                ClientResponse.class);
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("PUT"));
        assertTrue(allow.contains("POST"));
        assertTrue(allow.contains("DELETE"));
        assertTrue(allow.contains("PATCH"));
    }
}
