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

package com.sun.jersey.impl.entity;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.provider.entity.AbstractMessageReaderWriterProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OverrideProviderTest extends AbstractResourceTester {
    public OverrideProviderTest(String testName) {
        super(testName);
    }
    
    @Provider
    public static class StringProvider extends AbstractMessageReaderWriterProvider<String> {
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
            return type == String.class;
        }

        public String readFrom(
                Class<String> type, 
                Type genericType, 
                Annotation annotations[],
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders, 
                InputStream entityStream) throws IOException {
            String s = readFromAsString(entityStream, mediaType);
            return s.toUpperCase();
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
            return type == String.class;
        }
    
        public void writeTo(
                String t, 
                Class<?> type, 
                Type genericType, 
                Annotation annotations[], 
                MediaType mediaType, 
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            writeToAsString(t.toLowerCase(), entityStream, mediaType);
        }
    }
    
    @Path("/")
    public static class StringResource {
        @GET
        public String get() {
            return "FOO";
        }
    }
    
    public void testString() throws Exception {
        initiateWebApplication(StringResource.class, StringProvider.class);
                
        WebResource r = resource("/");
        assertEquals("foo", r.get(String.class));
    }    
    
    @Provider
    public static class JAXBBeanProvider extends AbstractMessageReaderWriterProvider<JAXBBean> {
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
            return JAXBBean.class.isAssignableFrom(type);
        }

        public JAXBBean readFrom(
                Class<JAXBBean> type, 
                Type genericType, 
                Annotation annotations[],
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders, 
                InputStream entityStream) throws IOException {
            String s = readFromAsString(entityStream, mediaType);
            return new JAXBBean(s);
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
            return JAXBBean.class.isAssignableFrom(type);
        }
    
        public void writeTo(
                JAXBBean t, 
                Class<?> type, 
                Type genericType, 
                Annotation annotations[], 
                MediaType mediaType, 
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            writeToAsString(t.value, entityStream, mediaType);
        }
    }
    
    @Path("/")
    public static class JAXBBeanResource {
        @GET
        public JAXBBean get() {
            return new JAXBBean("foo");
        }
    }
    
    public void testJAXBBean() throws Exception {
        initiateWebApplication(JAXBBeanResource.class, JAXBBeanProvider.class);
                
        WebResource r = resource("/");
        assertEquals("foo", r.get(String.class));
    }    
}