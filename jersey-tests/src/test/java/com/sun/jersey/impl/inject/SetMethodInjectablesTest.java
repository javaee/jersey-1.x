/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.inject;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.resource.Singleton;
import com.sun.jersey.spi.template.TemplateContext;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SetMethodInjectablesTest extends AbstractResourceTester {
    
    public SetMethodInjectablesTest(String testName) {
        super(testName);
    }

    @Provider
    public static class StringWriter implements MessageBodyWriter<String> {

        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType mediaType) {
            return arg0 == String.class;
        }

        public long getSize(String arg0, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(r);
            arg6.write(arg0.getBytes());
        }
        
        ResourceConfig rc;
        @Context
        public void setrc(ResourceConfig rc) {   
            this.rc = rc;
        }
        
        MessageBodyWorkers mbw;
        @Context
        public void set(MessageBodyWorkers mbw) {   
            this.mbw = mbw;
        }
        
        TemplateContext tc;
        @Context
        public void set(TemplateContext tc) {   
            this.tc = tc;
        }
        
        HttpContext hca;
        @Context
        public void set(HttpContext hca) {   
            this.hca = hca;
        }
        
        HttpHeaders hs;
        @Context
        public void set(HttpHeaders hs) {   
            this.hs = hs;
        }
        
        UriInfo ui;
        @Context
        public void set(UriInfo ui) {   
            this.ui = ui;
        }
        
        Request r;
        @Context
        public void setr(Request r) {   
            this.r = r;
        }        
        
        SecurityContext sc;
        @Context
        public void sesc(SecurityContext sc) {   
            this.sc = sc;
        }        
    }
    
    @Path("/")
    public static class StringWriterResource {
        @GET
        public String get() { return "GET"; }
    }
    
    public void testProvider() throws IOException {
        initiateWebApplication(StringWriterResource.class, StringWriter.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }               
    
    
    @Path("/")
    public static class PerRequestContextResource {
        ResourceConfig rc;
        @Context
        public void setrc(ResourceConfig rc) {   
            this.rc = rc;
        }
                
        MessageBodyWorkers mbw;
        @Context
        public void set(MessageBodyWorkers mbw) {   
            this.mbw = mbw;
        }
        
        TemplateContext tc;
        @Context
        public void set(TemplateContext tc) {   
            this.tc = tc;
        }
        
        HttpContext hca;
        @Context
        public void set(HttpContext hca) {   
            this.hca = hca;
        }
        
        HttpHeaders hs;
        @Context
        public void set(HttpHeaders hs) {   
            this.hs = hs;
        }
        
        UriInfo ui;
        @Context
        public void set(UriInfo ui) {   
            this.ui = ui;
        }
        
        Request r;
        @Context
        public void setr(Request r) {   
            this.r = r;
        }        
        
        SecurityContext sc;
        @Context
        public void setsc(SecurityContext sc) {   
            this.sc = sc;
        }        
        
        @GET
        public String get() {
            assertNotNull(rc);
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
    
    public void testPerRequestInjected() throws IOException {
        initiateWebApplication(PerRequestContextResource.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }       
    
    
    @Path("/")
    @Singleton
    public static class SingletonContextResource {
        ResourceConfig rc;
        @Context
        public void setrc(ResourceConfig rc) {   
            this.rc = rc;
        }
        
        MessageBodyWorkers mbw;
        @Context
        public void set(MessageBodyWorkers mbw) {   
            this.mbw = mbw;
        }
        
        TemplateContext tc;
        @Context
        public void set(TemplateContext tc) {   
            this.tc = tc;
        }
        
        HttpContext hca;
        @Context
        public void set(HttpContext hca) {   
            this.hca = hca;
        }
        
        HttpHeaders hs;
        @Context
        public void set(HttpHeaders hs) {   
            this.hs = hs;
        }
        
        UriInfo ui;
        @Context
        public void set(UriInfo ui) {   
            this.ui = ui;
        }
        
        Request r;
        @Context
        public void setr(Request r) {   
            this.r = r;
        }        
        
        SecurityContext sc;
        @Context
        public void setsc(SecurityContext sc) {   
            this.sc = sc;
        }        
        
        @GET
        public String get() {
            assertNotNull(rc);
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
    
    public void testSingletonInjected() throws IOException {
        initiateWebApplication(SingletonContextResource.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }
    
    @Path("/{p}")
    public static class PerRequestParamResource {
        String p;        
        @PathParam("p")
        public void setP(String p) {
            this.p = p;
        }
        
        String q;        
        @QueryParam("q")
        public void setA(String q) {
            this.q = q;
        }
        
        @GET
        public String get() {
            return p+q;
        }                
    }
    
    public void testPerRequestParamInjected() throws IOException {
        initiateWebApplication(PerRequestParamResource.class);
        
        assertEquals("foobar", resource("/foo?q=bar").get(String.class));        
    }           
    
}
