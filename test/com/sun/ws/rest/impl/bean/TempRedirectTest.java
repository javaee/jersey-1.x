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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.bean.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ResponseInBound;
import java.net.URI;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TempRedirectTest extends AbstractResourceTester {
    
    public TempRedirectTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/")
    static public class Resource { 
        @HttpMethod("GET")
        public Response doGet() {
            return Response.Builder.temporaryRedirect(URI.create("subpath")).build();
        }
    }
    
    public void testReturnType() {
        initiateWebApplication(Resource.class);
        
        ResponseInBound response = resourceProxy("/", false).get(ResponseInBound.class);        
        assertEquals(307, response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("subpath").build(), 
                response.getLocation());
    }   
}