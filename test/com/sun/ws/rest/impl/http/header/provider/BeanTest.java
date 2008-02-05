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

package com.sun.ws.rest.impl.http.header.provider;

import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ClientResponse;
import java.net.URI;
import java.util.GregorianCalendar;
import javax.ws.rs.GET;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class BeanTest extends AbstractResourceTester {
    
    public BeanTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class TestResource {
        @GET
        public Response doGet() {
            GregorianCalendar lastModified = new GregorianCalendar(2007, 0, 0, 0, 0, 0);
            return Response.ok().
                    lastModified(lastModified.getTime()).
                    tag(new EntityTag("TAG")).
                    location(URI.create("/location")).
                    language("en").build();
        }
    }
    
    public void testHeaders() {
        initiateWebApplication(TestResource.class);
        
        ClientResponse response = resourceProxy("/").get(ClientResponse.class);
        
        assertEquals(new GregorianCalendar(2007, 0, 0, 0, 0, 0).getTime(),
                response.getLastModified());
        
        assertEquals(new EntityTag("TAG"),
                response.getEntityTag());
        
        assertEquals(URI.create("/location"),
                response.getLocation());

        assertEquals("en",
                response.getLangauge());
    }
}
