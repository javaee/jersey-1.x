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

package com.sun.jersey.impl.inject;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.MessageBodyContext;
import com.sun.jersey.spi.resource.Singleton;
import com.sun.jersey.spi.template.TemplateContext;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWorkers;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AllInjectablesTest extends AbstractResourceTester {
    
    public AllInjectablesTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class PerRequestContextResource {
        @Context ResourceConfig rc;
        
        @Context MessageBodyContext mbc;
        
        @Context MessageBodyWorkers mbw;
        
        @Context TemplateContext tc;
        
        @Context HttpContext hca;
        
        @Context HttpHeaders hs;
        
        @Context UriInfo ui;
        
        @Context Request r;
        
        @Context SecurityContext sc;
        
        @GET
        public String get() {
            assertNotNull(rc);
            assertNotNull(mbc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(r);
            assertNotNull(sc);
            return "GET";
        }                
    }
    
    @Path("/")
    public static class PerRequestContextConstructorParameterResource {
        public PerRequestContextConstructorParameterResource(
                @Context ResourceConfig rc,
                @Context MessageBodyContext mbc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context Request r,
                @Context SecurityContext sc) {
            assertNotNull(rc);
            assertNotNull(mbc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(r);
            assertNotNull(sc);
        }                
        
        @GET
        public String get() { return "GET"; }
    }
    
    @Path("/")
    public static class PerRequestContextMethodParameterResource {
        @GET
        public String get(
                @Context ResourceConfig rc,
                @Context MessageBodyContext mbc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context Request r,
                @Context SecurityContext sc) {
            assertNotNull(rc);
            assertNotNull(mbc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(r);
            assertNotNull(sc);
            return "GET";
        }                
    }
    
    @Path("/")
    @Singleton
    public static class SingletonContextResource {
        @Context ResourceConfig rc;
        
        @Context MessageBodyContext mbc;
        
        @Context MessageBodyWorkers mbw;
        
        @Context TemplateContext tc;
        
        @Context HttpContext hca;
        
        @Context HttpHeaders hs;
        
        @Context UriInfo ui;
        
        @Context Request r;
        
        @GET
        public String get() {
            assertNotNull(rc);
            assertNotNull(mbc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(r);
            return "GET";
        }                
    }
    
    public void testPerRequestInjected() throws IOException {
        initiateWebApplication(PerRequestContextResource.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }       
    
    public void testPerRequestConstructorParameterInjected() throws IOException {
        initiateWebApplication(PerRequestContextConstructorParameterResource.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }       
    
    public void testPerRequestMethodParameterInjected() throws IOException {
        initiateWebApplication(PerRequestContextMethodParameterResource.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }       
    
    public void testSingletonInjected() throws IOException {
        initiateWebApplication(SingletonContextResource.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }       
}
