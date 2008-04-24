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

package com.sun.ws.rest.impl.container.grizzly.web;

import com.sun.ws.rest.api.client.Client;
import javax.ws.rs.Path;
import com.sun.ws.rest.api.client.WebResource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectionTest extends AbstractGrizzlyWebContainerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @Context HttpServletRequest req;
        
        @Context HttpServletResponse res;
        
        @Context ServletConfig sconf;
        
        @Context ServletContext scont;
        
        @GET
        public String get() {
            assertNotNull(req);
            assertNotNull(res);
            assertNotNull(sconf);
            assertNotNull(scont);
            return "GET";
        }               
    }
        
    public InjectionTest(String testName) {
        super(testName);
    }
    
    public void testInject() {
        startServer(HttpMethodResource.class);
        WebResource r = Client.create().resource(getUri().path("test").build());
        assertEquals("GET", r.get(String.class));
    }    
}
