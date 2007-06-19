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
import com.sun.ws.rest.api.Entity;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ClientErrorTest extends AbstractBeanTester {
    
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
        public String doPost(Entity<String> entity) {
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class WebResourceNotAcceptable {
        @ConsumeMime("application/bar")
        @ProduceMime("application/bar")
        @HttpMethod("POST")
        public String doPost(Entity<String> entity) {
            return "content";
        }
    }
    
    public void testNotFound() {
        HttpResponseContext response = callNoStatusCheck(
                WebResourceNotFoundMethodNotAllowed.class, "GET", "/foo", 
                null, "application/foo", "");
        assertEquals(404, response.getResponse().getStatus());
    }
    
    public void testMethodNotAllowed() {
        HttpResponseContext response = callNoStatusCheck(
                WebResourceNotFoundMethodNotAllowed.class, "POST", "/", 
                "application/foo", "application/foo", "");
        assertEquals(405, response.getResponse().getStatus());
    }    
    
    public void testUnsupportedMediaType() {
        HttpResponseContext response = callNoStatusCheck(
                WebResourceUnsupportedMediaType.class, "POST", "/", 
                "application/foo", "application/foo", "");
        assertEquals(415, response.getResponse().getStatus());
    }
    
    public void testNotAcceptable() {
        HttpResponseContext response = callNoStatusCheck(
                WebResourceUnsupportedMediaType.class, "POST", "/", 
                "application/bar", "application/bar", "");
        assertEquals(406, response.getResponse().getStatus());
    }    
}
