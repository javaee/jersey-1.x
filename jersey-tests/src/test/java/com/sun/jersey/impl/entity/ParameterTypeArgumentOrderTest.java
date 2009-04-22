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

package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ParameterTypeArgumentOrderTest extends AbstractResourceTester {
    public ParameterTypeArgumentOrderTest(String testName) {
        super(testName);
    }

    @Provider
    public static class ObjectWriter implements MessageBodyWriter {
        
        public boolean isWriteable(Class c, Type t, Annotation[] as, MediaType mt) {
            return true;
        }

        public long getSize(Object o, Class type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return -1;
        }

        public void writeTo(Object t, Class type, Type genericType, Annotation[] annotations,
                MediaType mediaType, MultivaluedMap httpHeaders,
                OutputStream out) throws IOException, WebApplicationException {
            out.write(t.toString().getBytes());
        }
    }

    public static class ObjectClass {
        @Override
        public String toString() {
            return "OBJECTCLASS";
        }
    }
    
    @Path("/")
    public static class ObjectResource {
        @GET
        @Path("object")
        public Object object() {
            return new ObjectClass();
        }
        
        @GET
        @Path("streamingoutput")
        public StreamingOutput streamingOutput() {
            return new StreamingOutput() {

                public void write(OutputStream output) throws IOException, WebApplicationException {
                    output.write("STREAMINGOUTPUT".getBytes());
                }
            };
        }        
    }
    
    public void testObjectResource() {
        initiateWebApplication(ObjectResource.class, ObjectWriter.class);
        
        WebResource r = resource("/");
                
        assertEquals("OBJECTCLASS", r.path("object").get(String.class));
        assertEquals("STREAMINGOUTPUT", r.path("streamingoutput").get(String.class));
    }


    public static class GenericClassWriter<T> implements MessageBodyWriter<T> {
        private final Class c;

        GenericClassWriter(Class c) {
            this.c = c;
        }
        
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return c.isAssignableFrom(type);
        }

        public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write((c.getSimpleName() + type.getSimpleName()).getBytes());
        }

    }

    public static class A {}

    @Provider
    public static class AWriter extends GenericClassWriter<A> {
        public AWriter() { super(A.class); }
    }

    public static class B extends A {}

    @Provider
    public static class BWriter extends GenericClassWriter<B> {
        public BWriter() { super(B.class); }
    }

    public static class C extends B {}

    @Provider
    public static class CWriter extends GenericClassWriter<C> {
        public CWriter() { super(C.class); }
    }

    @Path("/")
    public static class ClassResource {
        @Path("a")
        @GET
        public A getA() {
            return new A();
        }

        @Path("b")
        @GET
        public A getB() {
            return new B();
        }

        @Path("c")
        @GET
        public A getC() {
            return new C();
        }
    }

    public void testClassResource() {
        initiateWebApplication(AWriter.class, BWriter.class, CWriter.class, ClassResource.class);

        WebResource r = resource("/");

        assertEquals("AA", r.path("a").get(String.class));
        assertEquals("BB", r.path("b").get(String.class));
        assertEquals("CC", r.path("c").get(String.class));
    }

    public void testReverseClassResource() {
        initiateWebApplication(CWriter.class, BWriter.class, AWriter.class, ClassResource.class);

        WebResource r = resource("/");

        assertEquals("AA", r.path("a").get(String.class));
        assertEquals("BB", r.path("b").get(String.class));
        assertEquals("CC", r.path("c").get(String.class));
    }
}