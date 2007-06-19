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
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class CreatedTest extends AbstractBeanTester {
    
    public CreatedTest(String testName) {
        super(testName);
    }
    
    @SuppressWarnings("unchecked")
    @UriTemplate("/")
    static public class Resource { 
        @HttpMethod("GET")
        public Response doGet() {
            try {
                return Response.Builder.created("CONTENT", new URI("subpath")).build();
            } catch (URISyntaxException ex) {
                return null;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testReturnType() {
        // HttpResponseContext response = callGet(Resource.class, "/", "");

        HttpResponseContext response = callNoStatusCheck(Resource.class, "GET", "/", 
            null, "", "");
        assertEquals(201, response.getResponse().getStatus());
        String location = response.getHttpHeaders().getFirst("Location").toString();
        assertEquals(getBaseUri().toASCIIString() + "subpath", location);
        
        String r = (String)response.getResponse().getEntity();
        assertEquals("CONTENT", r);        
    }   
}