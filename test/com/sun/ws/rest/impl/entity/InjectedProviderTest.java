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

package com.sun.ws.rest.impl.entity;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.api.client.WebResource;
import com.sun.ws.rest.impl.provider.entity.AbstractMessageReaderWriterProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectedProviderTest extends AbstractResourceTester {
    public static class Bean implements Serializable {
        private String string;

        public Bean() { }

        public Bean(String string) {
            this.string = string;
        }

        public String getString() { return string; }

        public void setString(String string) { this.string = string; }
    }

    @Provider
    public static class InjectedBeanProvider extends AbstractMessageReaderWriterProvider<Bean> {
        @Context UriInfo uriInfo;
        
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
            return type == Bean.class;
        }

        public Bean readFrom(
                Class<Bean> type, 
                Type genericType, 
                MediaType mediaType, 
                Annotation annotations[],
                MultivaluedMap<String, String> httpHeaders, 
                InputStream entityStream) throws IOException {
            ObjectInputStream oin = new ObjectInputStream(entityStream);
            try {
                return (Bean)oin.readObject();
            } catch (ClassNotFoundException cause) {
                IOException effect = new IOException(cause.getLocalizedMessage());
                effect.initCause(cause);
                throw effect;
            }
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
            return type == Bean.class;
        }
    
        public void writeTo(
                Bean t, 
                Class<?> type, 
                Type genericType, 
                Annotation annotations[], 
                MediaType mediaType, 
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            t.setString(uriInfo.getRequestUri().toString());
            ObjectOutputStream out = new ObjectOutputStream(entityStream);
            out.writeObject(t);
            out.flush();
        }
    }
    
    @Path("/one/two/three")
    public static class BeanResource {
        @GET
        public Bean get() {
            return new Bean("");
        }
    }
    
    public InjectedProviderTest(String testName) {
        super(testName);
    }
    
    public void testBean() throws Exception {
        initiateWebApplication(BeanResource.class, InjectedBeanProvider.class);
                
        WebResource r = resource("/one/two/three");
        Bean b = r.get(Bean.class);
        String requestUri = UriBuilder.fromUri(BASE_URI).
                path(BeanResource.class).build().toString();
        assertEquals(requestUri, b.getString());
    }    
}
