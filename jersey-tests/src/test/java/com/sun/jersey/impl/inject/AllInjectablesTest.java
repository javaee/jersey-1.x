/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.uri.ExtendedUriInfo;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.MessageBodyWorkers;
import com.sun.jersey.spi.resource.Singleton;
import com.sun.jersey.spi.template.TemplateContext;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import javax.ws.rs.ext.Providers;

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
        
        @Context MessageBodyWorkers mbw;
        
        @Context TemplateContext tc;
        
        @Context HttpContext hca;
        
        @Context HttpHeaders hs;
        
        @Context UriInfo ui;
        
        @Context ExtendedUriInfo eui;
        
        @Context Request r;
        
        @Context SecurityContext sc;
        
        @Context Providers p;
        
        @GET
        public String get() {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
            return "GET";
        }                
    }
    
    @Path("/")
    public static class PerRequestContextConstructorParameterResource {
        public PerRequestContextConstructorParameterResource(
                @Context ResourceConfig rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc,
                @Context Providers p) {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }                
        
        @GET
        public String get() { return "GET"; }
    }
    
    @Path("/")
    public static class PerRequestContextMethodParameterResource {
        @GET
        public String get(
                @Context ResourceConfig rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc,
                @Context Providers p) {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
            return "GET";
        }                
    }
    
    @Path("/")
    @Singleton
    public static class SingletonContextResource {
        @Context ResourceConfig rc;
                
        @Context MessageBodyWorkers mbw;
        
        @Context TemplateContext tc;
        
        @Context HttpContext hca;
        
        @Context HttpHeaders hs;
        
        @Context UriInfo ui;
        
        @Context ExtendedUriInfo eui;
        
        @Context Request r;
        
        @Context SecurityContext sc;
        
        @Context Providers p;
        
        @GET
        public String get() {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
            return "GET";
        }                
    }
    
    @Path("/")
    public static class SingletonContextConstructorParameterResource {
        public SingletonContextConstructorParameterResource(
                @Context ResourceConfig rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc,
                @Context Providers p) {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }                
        
        @GET
        public String get() { return "GET"; }
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
    
    public void testSingletonConstructorParameterInjected() throws IOException {
        initiateWebApplication(SingletonContextConstructorParameterResource.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }
    
    @Path("/")
    public static class StringWriterResource {
        @GET
        public String get() { return "GET"; }
    }
        
    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterField implements MessageBodyWriter<String> {
        public StringWriterField() {
            int i = 0;
        }
        
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            return arg0 == String.class;
        }

        public long getSize(String arg0) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
            arg6.write(arg0.getBytes());
        }
        
        @Context ResourceConfig rc;
        
        @Context MessageBodyWorkers mbw;
        
        @Context TemplateContext tc;
        
        @Context HttpContext hca;
        
        @Context HttpHeaders hs;
        
        @Context UriInfo ui;
        
        @Context ExtendedUriInfo eui;
        
        @Context Request r;        
        
        @Context SecurityContext sc;
        
        @Context Providers p;
    }
    
    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterConstructor implements MessageBodyWriter<String> {
        public StringWriterConstructor(
                @Context ResourceConfig rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc,
                @Context Providers p) {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }                
        
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            return arg0 == String.class;
        }

        public long getSize(String arg0) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }        
    }
    
    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterMutlipleConstructor implements MessageBodyWriter<String> {
        public StringWriterMutlipleConstructor(
                @Context ResourceConfig rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc,
                @Context Providers p) {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }                
        
        public StringWriterMutlipleConstructor(
                @Context ResourceConfig rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc) {
            assertTrue(false);
        }                
        
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            return arg0 == String.class;
        }

        public long getSize(String arg0) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }        
    }
    
    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterMutliplePartialConstructor implements MessageBodyWriter<String> {
        public StringWriterMutliplePartialConstructor(
                @Context ResourceConfig rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc,
                @Context Providers p) {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }                
        
        public StringWriterMutliplePartialConstructor(
                String rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc,
                @Context Providers p) {
            assertTrue(false);
        }                
        
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            return arg0 == String.class;
        }

        public long getSize(String arg0) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }        
    }
    
    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterMutliplePartialConstructor2 implements MessageBodyWriter<String> {
        public StringWriterMutliplePartialConstructor2(
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc) {
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
        }                
        
        public StringWriterMutliplePartialConstructor2(
                String rc,
                @Context MessageBodyWorkers mbw,
                @Context TemplateContext tc,
                @Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc) {
            assertTrue(false);
        }                
        
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            return arg0 == String.class;
        }

        public long getSize(String arg0) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write(arg0.getBytes());
        }        
    }
    
    public void testProviderField() throws IOException {
        initiateWebApplication(StringWriterResource.class, StringWriterField.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }           
    
    public void testProviderInstanceField() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(StringWriterResource.class);
        rc.getSingletons().add(new StringWriterField());
        initiateWebApplication(rc);
        
        assertEquals("GET", resource("/").get(String.class));        
    }           
    
    public void testProviderConstructor() throws IOException {
        initiateWebApplication(StringWriterResource.class, StringWriterConstructor.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }           
    
    public void testProviderMultipleConstructor() throws IOException {
        initiateWebApplication(StringWriterResource.class, StringWriterMutlipleConstructor.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }           
    
    public void testProviderMultiplePartialConstructor() throws IOException {
        initiateWebApplication(StringWriterResource.class, StringWriterMutliplePartialConstructor.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }           
    
    public void testProviderMultiplePartialConstructor2() throws IOException {
        initiateWebApplication(StringWriterResource.class, StringWriterMutliplePartialConstructor2.class);
        
        assertEquals("GET", resource("/").get(String.class));        
    }           
    
    @Path("/{p}")
    public static class PerRequestFieldResource {        
        @PathParam("p") String p;
        
        @QueryParam("q") String q;        
        
        @GET
        public String get() {
            return p+q;
        }                
    }
    
    public void testPerRequestParamInjected() throws IOException {
        initiateWebApplication(PerRequestFieldResource.class);
        
        assertEquals("foobar", resource("/foo?q=bar").get(String.class));        
    }           

    
    @Provider
    @Consumes({"text/plain", "*/*"})
    @Produces({"text/plain", "*/*"})
    public static class StringWriterParamConstructor implements MessageBodyWriter<String> {
        
        int illegalStateExceptionCount = 0;
        int runtimeExceptionCount = 0;
        
        public StringWriterParamConstructor(@Context HttpContext hca,
                @Context HttpHeaders hs,
                @Context UriInfo ui,
                @Context ExtendedUriInfo eui,
                @Context Request r,
                @Context SecurityContext sc) {
            try {
                hca.getRequest();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }
            
            try {
                ui.getAbsolutePath();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }
            
            try {
                eui.getMatchedTemplates();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }
            
            try {
                r.getMethod();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }
            
            try {
                sc.getAuthenticationScheme();
            } catch (IllegalStateException ex) {
                illegalStateExceptionCount++;
            } catch (RuntimeException ex) {
                runtimeExceptionCount++;
            }
        }
        
        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2) {
            return arg0 == String.class;
        }

        public long getSize(String arg0) {
            return -1;
        }

        public void writeTo(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4, MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
            String s = illegalStateExceptionCount + " " + runtimeExceptionCount + " " + arg0;
            arg6.write(s.getBytes());
        }
    }
    
    @Path("/")
    public static class StringWriterParamConstructorResource {        
        @GET
        public String get() {
            return "GET";
        }                
    }
    
    public void testStringWriterParamConstructor() throws IOException {
        initiateWebApplication(StringWriterParamConstructorResource.class, StringWriterParamConstructor.class);
        
        assertEquals("5 0 GET", resource("/").get(String.class));
    }               
}