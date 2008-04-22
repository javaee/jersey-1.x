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
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EncodedUriTemplateValuesTest extends AbstractResourceTester {

    public EncodedUriTemplateValuesTest(String testName) {
        super(testName);
    }

    @Path("/{a}/{b}")
    public static class Resource {
        @GET
        public String doGet(
                @PathParam("a") String a, 
                @PathParam("b") String b,
                @Context UriInfo info) {
            assertEquals("a b", a);
            assertEquals("x y", b);
            assertEquals("a b", info.getPathParameters().getFirst("a"));
            assertEquals("x y", info.getPathParameters().getFirst("b"));
            assertEquals("a%20b", info.getPathParameters(false).getFirst("a"));
            assertEquals("x%20y", info.getPathParameters(false).getFirst("b"));
            return "content";
        }
    }
        
    public void testEncodedTemplateValues() {
        initiateWebApplication(Resource.class);
        resource("/a%20b/x%20y").get(String.class);
    }
}
