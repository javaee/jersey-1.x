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
import com.sun.ws.rest.api.core.HttpContext;
import com.sun.ws.rest.api.client.WebResource;
import com.sun.ws.rest.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContentTypeOverrideTest extends AbstractResourceTester {
    
    public ContentTypeOverrideTest(String testName) {
        super(testName);
    }
        
    @Path("/")
    public static class WebResourceOverride {
        @Context HttpContext context;
        
        @ProduceMime({"application/foo", "application/bar"})
        @GET
        public Response doGet() {
            return Response.ok("content", "application/foo").build();
        }
    }
    
    public void testOverridden() {
        initiateWebApplication(WebResourceOverride.class);
        WebResource r = resource("/");
        
        ClientResponse response = r.accept("application/foo", "application/bar").
                get(ClientResponse.class);

        assertEquals(MediaType.valueOf("application/foo"), response.getType());
    }
}
