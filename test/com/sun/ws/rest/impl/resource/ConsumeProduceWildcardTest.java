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
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.client.ResourceProxy;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ConsumeProduceWildcardTest extends AbstractResourceTester {
    
    public ConsumeProduceWildcardTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    @ConsumeMime("text/*")
    public static class ConsumeWildCardBean {
        @HttpMethod("POST")
        public void doPostHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Content-Type"));
            response.setResponse(Response.Builder.ok("HTML").build());
        }
        
        @HttpMethod("POST")
        @ConsumeMime("text/xhtml")
        public void doPostXHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Content-Type"));
            response.setResponse(Response.Builder.ok("XHTML").build());
        }
    }
        
    public void testConsumeWildCardBean() {
        initiateWebApplication(ConsumeWildCardBean.class);
        ResourceProxy r = resourceProxy("/a/b");
        
        assertEquals("HTML", r.content("", "text/html").post(String.class));
        assertEquals("XHTML", r.content("", "text/xhtml").post(String.class));
    }    
}
