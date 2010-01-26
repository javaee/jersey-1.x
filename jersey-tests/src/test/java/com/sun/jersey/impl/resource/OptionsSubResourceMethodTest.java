/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.ClientResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OptionsSubResourceMethodTest extends AbstractResourceTester {
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("PATCH")
    public @interface PATCH { 
    }
    
    public OptionsSubResourceMethodTest(String testName) {
        super(testName);
    }

    @Path("/")
    static public class ResourceNoOptions { 
        @Path("sub")
        @GET
        public String get() {
            return "GET";
        }
        
        @Path("sub")
        @PUT
        public String put(String e) {
            return "PUT";
        }
        
        @Path("sub")
        @POST
        public String post(String e) {
            return "POST";
        }
        
        @Path("sub")
        @DELETE
        public void delete() {
        }
        
        @Path("sub")
        @PATCH
        public String patch(String e) {
            return "PATCH";
        }
    }
        
    public void testNoOptions() {
        initiateWebApplication(ResourceNoOptions.class);

        ClientResponse response = resource("/sub").options(
                ClientResponse.class);
        String allow = response.getHeaders().getFirst("Allow").toString();
        assertTrue(allow.contains("OPTIONS"));
        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("PUT"));
        assertTrue(allow.contains("POST"));
        assertTrue(allow.contains("DELETE"));
        assertTrue(allow.contains("PATCH"));
    }
    
    @Path("/")
    static public class ResourceWithOptions { 
        
        @Path("sub")
        @OPTIONS
        public Response options() {
            return Response.ok("OPTIONS").
                    header("Allow", "OPTIONS, GET, PUT, POST, DELETE, PATCH").build();
        }
        
        @Path("sub")
        @GET
        public String get() {
            return "GET";
        }
        
        @Path("sub")
        @PUT
        public String put(String e) {
            return "PUT";
        }
        
        @Path("sub")
        @POST
        public String post(String e) {
            return "POST";
        }
        
        @Path("sub")
        @DELETE
        public void delete() {
        }
        
        @Path("sub")
        @PATCH
        public String patch(String e) {
            return "PATCH";
        }
    }
    
    public void testWithOptions() {
        initiateWebApplication(ResourceWithOptions.class);

        ClientResponse response = resource("/sub").options(
                ClientResponse.class);
        String allow = response.getHeaders().getFirst("Allow").toString();
        assertTrue(allow.contains("OPTIONS"));
        assertTrue(allow.contains("GET"));
        assertTrue(allow.contains("PUT"));
        assertTrue(allow.contains("POST"));
        assertTrue(allow.contains("DELETE"));
        assertTrue(allow.contains("PATCH"));
    }
    
    @Path("/")
    static public class ResourceNoOptionsDifferentSub { 
        @Path("sub1")
        @GET
        public String getFoo() {
            return "FOO";
        }
        
        @Path("sub2")
        @PUT
        public String putBar() {
            return "BAR";
        }
    }
    
    public void testNoOptionsDifferentSub() {
        initiateWebApplication(ResourceNoOptionsDifferentSub.class);

        ClientResponse response = resource("/sub1").options( 
                ClientResponse.class);
        String allow = response.getHeaders().getFirst("Allow").toString();
        assertTrue(allow.contains("OPTIONS"));
        assertTrue(allow.contains("GET"));
        assertFalse(allow.contains("PUT"));
        
        response = resource("/sub2").options(ClientResponse.class);
        allow = response.getHeaders().getFirst("Allow").toString();
        assertTrue(allow.contains("OPTIONS"));
        assertTrue(allow.contains("PUT"));
        assertFalse(allow.contains("GET"));
    }
}