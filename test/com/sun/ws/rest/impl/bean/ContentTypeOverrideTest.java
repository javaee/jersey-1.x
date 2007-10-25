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

import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResponseInBound;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
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
        
    @UriTemplate("/")
    public static class WebResourceOverride {
        @HttpContext HttpContextAccess context;
        
        @ProduceMime({"application/foo", "application/bar"})
        @HttpMethod("GET")
        public Response doGet() {
            return Response.Builder.representation("content", "application/foo").build();
        }
    }
    
    public void testOverridden() {
        initiateWebApplication(WebResourceOverride.class);
        ResourceProxy r = resourceProxy("/");
        
        ResponseInBound response = r.acceptable("application/foo", "application/bar").
                get(ResponseInBound.class);

        assertEquals(new MediaType("application/foo"), response.getContentType());
    }
}
