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

import com.sun.ws.rest.api.client.ClientResponse;
import com.sun.ws.rest.api.client.WebResource;
import com.sun.ws.rest.impl.AbstractResourceTester;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NewCookieTest extends AbstractResourceTester {
    
    public NewCookieTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class Resource {
        @GET
        public Response get() {
            return Response.ok().
                    cookie(new NewCookie("x", "1"), 
                    new NewCookie("y", "2")).build();
        }
    }
    
    public void testNewCookie() throws IOException {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");
        
        ClientResponse cr = r.get(ClientResponse.class);
        List<NewCookie> cs = cr.getCookies();
        assertEquals(2, cs.size());
        assertEquals("x", cs.get(0).getName());
        assertEquals("1", cs.get(0).getValue());
        assertEquals("y", cs.get(1).getName());
        assertEquals("2", cs.get(1).getValue());
    }
}