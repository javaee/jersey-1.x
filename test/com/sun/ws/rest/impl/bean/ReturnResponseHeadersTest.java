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

import com.sun.ws.rest.api.Entity;
import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ReturnResponseHeadersTest extends AbstractBeanTester {
    
    public ReturnResponseHeadersTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    static public class TestRepresentationBean { 
        @HttpMethod("POST")
        public Response doPost(Entity<String> in) {
            return Response.Builder.representation("content", "text/plain").language("en").build();
        }
    }

    @SuppressWarnings("unchecked")
    public void testRepresentationHeaders() throws Exception {
        
        HttpResponseContext response = callNoStatusCheck(TestRepresentationBean.class, "POST", "/", 
            "text/plain", null, "content");

        assertEquals(200, response.getResponse().getStatus());
        String sr = (String)response.getResponse().getEntity();
        
        assertEquals("en", response.getHttpHeaders().getFirst("Content-Language"));
    }
}