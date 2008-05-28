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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWorkers;
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

        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            return arg0 == String.class;
        }

        public long getSize(String arg0) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            assertNotNull(rc);
            assertNotNull(mbc);
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
        
        MessageBodyContext mbc;
        @Context
        public void setmbc(MessageBodyContext mbc) {   
            this.mbc = mbc;
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
}
