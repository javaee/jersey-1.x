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

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResponseInBound;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ClientErrorTest extends AbstractResourceTester {
    
    public ClientErrorTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    public static class WebResourceNotFoundMethodNotAllowed {
        @ProduceMime("application/foo")
        @HttpMethod("GET")
        public String doGet() {
            return "content";
        }
    }
        
    @UriTemplate("/")
    public static class WebResourceUnsupportedMediaType {
        @ConsumeMime("application/bar")
        @ProduceMime("application/foo")
        @HttpMethod("POST")
        public String doPost(String entity) {
            return "content";
        }
    }
    
    public void testNotFound() {
        initiateWebApplication(WebResourceNotFoundMethodNotAllowed.class);
        ResourceProxy r = resourceProxy("/foo", false);

        ResponseInBound response = r.acceptable("application/foo").get(ResponseInBound.class);
        assertEquals(404, response.getStatus());
    }
    
    public void testMethodNotAllowed() {
        initiateWebApplication(WebResourceNotFoundMethodNotAllowed.class);
        ResourceProxy r = resourceProxy("/", false);
        
        ResponseInBound response = r.content("content", "application/foo").
                accept("application/foo").post(ResponseInBound.class);
        assertEquals(405, response.getStatus());
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
    }    
    
    public void testUnsupportedMediaType() {
        initiateWebApplication(WebResourceUnsupportedMediaType.class);
        ResourceProxy r = resourceProxy("/", false);
        
        ResponseInBound response = r.content("content", "application/foo").
                accept("application/foo").post(ResponseInBound.class);
        assertEquals(415, response.getStatus());
    }
    
    public void testNotAcceptable() {
        initiateWebApplication(WebResourceUnsupportedMediaType.class);
        ResourceProxy r = resourceProxy("/", false);
        
        ResponseInBound response = r.content("content", "application/bar").
                accept("application/bar").post(ResponseInBound.class);
        assertEquals(406, response.getStatus());
    }    
}
