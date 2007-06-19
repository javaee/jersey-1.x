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

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ConsumeProduceSimpleTest extends AbstractBeanTester {
    
    public ConsumeProduceSimpleTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    @ConsumeMime("text/html")
    public static class ConsumeSimpleBean {
        @HttpMethod("POST")
        public void doPostHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Content-Type"));
        }
        
        @HttpMethod("POST")
        @ConsumeMime("text/xhtml")
        public void doPostXHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Content-Type"));
        }
    }
        
    @UriTemplate("/{arg1}/{arg2}")
    @ProduceMime("text/html")
    public static class ProduceSimpleBean {
        @HttpMethod("GET")
        public void doGetHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
        }
        
        @HttpMethod("GET")
        @ProduceMime("text/xhtml")
        public void doGetXhtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Accept"));
        }
    }
    
    @UriTemplate("/{arg1}/{arg2}")
    @ConsumeMime("text/html")
    @ProduceMime("text/html")
    public static class ConsumeProduceSimpleBean {
        @HttpMethod("GET")
        public void doGetHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
        }
        
        @HttpMethod("GET")
        @ProduceMime("text/xhtml")
        public void doGetXhtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("GET", request.getHttpMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Accept"));
        }
        
        @HttpMethod("POST")
        public void doPostHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            assertEquals("text/html", request.getRequestHeaders().getFirst("Content-Type"));
            assertEquals("text/html", request.getRequestHeaders().getFirst("Accept"));
        }
        
        @HttpMethod("POST")
        @ConsumeMime("text/xhtml")
        @ProduceMime("text/xhtml")
        public void doPostXHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());            
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Content-Type"));
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Accept"));
        }
    }
    
    public void testConsumeSimpleBean() {
        Class r = ConsumeSimpleBean.class;
        callPost(r, "/a/b", "text/html", "");
        callPost(r, "/a/b", "text/xhtml", "");
    }
    
    public void testProduceSimpleBean() {
        Class r = ProduceSimpleBean.class;
        callGet(r, "/a/b", "text/html");
        callGet(r, "/a/b", "text/xhtml");
    }
    
    public void testConsumeProduceSimpleBean() {
        Class r = ConsumeProduceSimpleBean.class;
        callGet(r, "/a/b", "text/html");
        callGet(r, "/a/b", "text/xhtml");
        callPost(r, "/a/b", "text/html", "text/html", "");
        callPost(r, "/a/b", "text/xhtml", "text/xhtml", "");
    }
}
