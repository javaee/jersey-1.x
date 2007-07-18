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

package com.sun.ws.rest.impl.methodparams;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.bean.AbstractBeanTester;
import java.net.URI;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TempRedirectTest extends AbstractBeanTester {
    
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
        // HttpResponseContext response = callGet(Resource.class, "/", "");

        HttpResponseContext response = callNoStatusCheck(Resource.class, "GET", "/", 
            null, "", "");
        assertEquals(307, response.getStatus());
        String location = response.getHttpHeaders().getFirst("Location").toString();
        assertEquals(getBaseUri().toASCIIString() + "subpath", location);
    }   
}