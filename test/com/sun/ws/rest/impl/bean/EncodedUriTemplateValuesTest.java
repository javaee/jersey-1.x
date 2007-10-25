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
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EncodedUriTemplateValuesTest extends AbstractResourceTester {

    public EncodedUriTemplateValuesTest(String testName) {
        super(testName);
    }

    @UriTemplate("/{a}/{b}")
    public static class Resource {
        @HttpMethod("GET")
        public String doGet(
                @UriParam("a") String a, 
                @UriParam("b") String b,
                @HttpContext UriInfo info) {
            assertEquals("a b", a);
            assertEquals("x y", b);
            assertEquals("a b", info.getTemplateParameters().getFirst("a"));
            assertEquals("x y", info.getTemplateParameters().getFirst("b"));
            assertEquals("a%20b", info.getTemplateParameters(false).getFirst("a"));
            assertEquals("x%20y", info.getTemplateParameters(false).getFirst("b"));
            return "content";
        }
    }
        
    public void testEncodedTemplateValues() {
        initiateWebApplication(Resource.class);
        resourceProxy("/a%20b/x%20y").get(String.class);
    }
}
