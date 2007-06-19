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

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ConsumeProduceWildcardTest extends AbstractBeanTester {
    
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
        }
        
        @HttpMethod("POST")
        @ConsumeMime("text/xhtml")
        public void doPostXHtml(HttpRequestContext request, HttpResponseContext response) {
            assertEquals("POST", request.getHttpMethod());
            assertEquals("text/xhtml", request.getRequestHeaders().getFirst("Content-Type"));
        }
    }
        
    public void testConsumeWildCardBean() {
        Class r = ConsumeWildCardBean.class;
        callPost(r, "/a/b", "text/html", "");
        callPost(r, "/a/b", "text/xhtml", "");
    }    
}
