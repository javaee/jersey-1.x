/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.impl.resource;

import com.sun.jersey.api.JResponse;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class JResponseTest extends AbstractResourceTester {

    public JResponseTest(String testName) {
        super(testName);
    }

    public void testRemoval() {
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoTransform(false);
        JResponse<String> j = JResponse.ok("content").
                cacheControl(cc).
                language(Locale.ENGLISH).
                contentLocation(URI.create("/path")).
                encoding("gzip").
                type("text/plain").
                tag("tag").
                lastModified(new Date(0)).
                location(URI.create("/path")).
                header("X-FOO", "foo").
                header("X-FOO", "bar").
                cacheControl(null).
                language((String)null).
                contentLocation(null).
                encoding(null).
                type((String)null).
                tag((String)null).
                lastModified(null).
                location(null).
                header("X-Foo", null).
                build();
        assertEquals(0, j.getMetadata().size());
    }
    
    public void testRemovalAdd() {
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoTransform(false);
        JResponse<String> j = JResponse.ok("content").
                cacheControl(cc).
                language(Locale.ENGLISH).
                contentLocation(URI.create("/path")).
                encoding("gzip").
                type("text/plain").
                tag("tag").
                lastModified(new Date(0)).
                location(URI.create("/path")).
                header("X-FOO", "foo").
                header("X-FOO", "bar").
                cacheControl(null).
                language((String)null).
                contentLocation(null).
                encoding(null).
                type((String)null).
                tag((String)null).
                lastModified(null).
                location(null).
                header("X-Foo", null).
                cacheControl(cc).
                language(Locale.ENGLISH).
                contentLocation(URI.create("/path")).
                encoding("gzip").
                type("text/plain").
                tag("tag").
                lastModified(new Date(0)).
                location(URI.create("/path")).
                header("X-FOO", "foo").
                header("X-FOO", "bar").
                build();
        assertEquals(9, j.getMetadata().size());
    }

    @Path("/")
    static public class HeadersResource {

        @Path("method")
        @GET
        public JResponse<String> getWithMethod() {
            CacheControl cc = new CacheControl();
            cc.setNoCache(true);
            cc.setNoTransform(false);
            return JResponse.ok("content").
                    cacheControl(cc).
                    language(Locale.ENGLISH).
                    contentLocation(URI.create("/path")).
                    encoding("gzip").
                    type("text/plain").
                    tag("tag").
                    lastModified(new Date(0)).
                    location(URI.create("/path")).
                    header("X-FOO", "foo").
                    header("X-FOO", "bar").
                    build();
        }

        @Path("methodClone")
        @GET
        public JResponse<String> getWithMethodClone() {
            CacheControl cc = new CacheControl();
            cc.setNoCache(true);
            cc.setNoTransform(false);
            JResponse.JResponseBuilder<String> rb = JResponse.ok("content").
                    cacheControl(cc).
                    language(Locale.ENGLISH).
                    contentLocation(URI.create("/path")).
                    encoding("gzip").
                    type("text/plain").
                    tag("tag").
                    lastModified(new Date(0)).
                    location(URI.create("/path"));


            JResponse.JResponseBuilder<String> rbClone = rb.clone();
            rbClone.header("X-FOO", "foo").
                    header("X-FOO", "bar");

            rb.cacheControl(null).
                    language((String)null).
                    contentLocation(null).
                    encoding(null).
                    type((String)null).
                    tag((String)null).
                    lastModified(null).
                    location(null).
                    header("X-Foo", null);

            return rbClone.build();
        }

        @Path("methodRemove")
        @GET
        public JResponse<String> getWithMethodRemove() {
            CacheControl cc = new CacheControl();
            cc.setNoCache(true);
            cc.setNoTransform(false);
            return JResponse.ok("content").
                    cacheControl(cc).
                    language(Locale.ENGLISH).
                    contentLocation(URI.create("/path")).
                    encoding("gzip").
                    type("text/plain").
                    tag("tag").
                    lastModified(new Date(0)).
                    location(URI.create("/path")).
                    header("X-FOO", "foo").
                    header("X-FOO", "bar").
                    cacheControl(null).
                    language((String)null).
                    contentLocation(null).
                    encoding(null).
                    type((String)null).
                    tag((String)null).
                    lastModified(null).
                    location(null).
                    header("X-Foo", null).
                    cacheControl(cc).
                    language(Locale.ENGLISH).
                    contentLocation(URI.create("/path")).
                    encoding("gzip").
                    type("text/plain").
                    tag("tag").
                    lastModified(new Date(0)).
                    location(URI.create("/path")).
                    header("X-FOO", "foo").
                    header("X-FOO", "bar").
                    build();
        }

        @Path("header")
        @GET
        public JResponse<String> getWithHeader() {
            return JResponse.ok("content").
                    header("Cache-Control", "no-cache").
                    header("Content-Language", "en").
                    header("Content-Location", URI.create("/path")).
                    header("Content-Encoding", "gzip").
                    header("Content-Type", "text/plain").
                    header("ETag", "\"tag\"").
                    header("Last-Modified", new Date(0)).
                    header("Location", URI.create("/path")).
                    header("X-FOO", "foo").
                    header("X-FOO", "bar").
                    build();
        }

        @Path("metadata")
        @GET
        public JResponse<String> getMetadata() {
            JResponse<String> r = JResponse.ok("content").build();
            MultivaluedMap<String, Object> headers = r.getMetadata();
            headers.putSingle("Cache-Control", "no-cache");
            headers.putSingle("Content-Language", "en");
            headers.putSingle("Content-Location", URI.create("/path"));
            headers.putSingle("Content-Encoding", "gzip");
            headers.putSingle("Content-Type", "text/plain");
            headers.putSingle("ETag", "\"tag\"");
            headers.putSingle("Last-Modified", new Date(0));
            headers.putSingle("Location", URI.create("/path"));
            headers.add("X-FOO", "foo");
            headers.add("X-FOO", "bar");
            return r;
        }
    }


//    public void testAllHeaders() throws Exception {
//        initiateWebApplication(HeadersResource.class);
//        WebResource r = resource("/");
//        r.addFilter(new LoggingFilter());
//
//        checkHeaders(r.path("method").get(ClientResponse.class).getHeaders());
//        checkHeaders(r.path("methodClone").get(ClientResponse.class).getHeaders());
//        checkHeaders(r.path("methodRemove").get(ClientResponse.class).getHeaders());
//        checkHeaders(r.path("header").get(ClientResponse.class).getHeaders());
//        checkHeaders(r.path("metadata").get(ClientResponse.class).getHeaders());
//    }

    private void checkHeaders(MultivaluedMap<String, String> headers) {
        checkHeader(headers, "Cache-Control", "no-cache");
        checkHeader(headers, "Content-Language", "en");
        checkHeader(headers, "Content-Location", "/path");
        checkHeader(headers, "Content-Encoding", "gzip");
        checkHeader(headers, "Content-Type", "text/plain");
        checkHeader(headers, "ETag", "\"tag\"");
        checkHeader(headers, "Last-Modified", "Thu, 01 Jan 1970 00:00:00 GMT");
        checkHeader(headers, "Location", "test:/base/path");
        checkHeader(headers, "X-FOO", "foo", "bar");
    }

    private void checkHeader(MultivaluedMap<String, String> headers, 
            String name, String... expectedValues) {
        List<String> values = headers.get(name);
        assertNotNull(values);
        assertEquals(expectedValues.length, values.size());

        for (int i = 0; i < expectedValues.length; i++) {
            String value = values.get(i);
            assertNotNull(value);
            assertEquals(expectedValues[i], value);
        }
    }


    public static class ListStringWriter implements MessageBodyWriter<List<String>> {
        private final Type listStringType;

        public ListStringWriter() {
            ParameterizedType iface = (ParameterizedType)this.getClass().getGenericInterfaces()[0];
            listStringType = iface.getActualTypeArguments()[0];
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return List.class.isAssignableFrom(type) && listStringType.equals(genericType);
        }

        public long getSize(List<String> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(List<String> t, Class<?> type, Type genericType, 
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            boolean first = true;
            for (String s : t) {
                if (!first)
                    entityStream.write(',');
                entityStream.write(s.getBytes());
                first = false;
            }
        }
    }

    @Path("/")
    static public class ParameterizedResource {
        @GET
        public JResponse<List<String>> get() {
            return JResponse.ok(Arrays.asList("a", "b", "c")).
                    header("X-FOO", "foo").build();
        }
    }

    public void testParameterizedType() {
        initiateWebApplication(ParameterizedResource.class, ListStringWriter.class);
        WebResource r = resource("/");
        r.addFilter(new LoggingFilter());

        String s = r.get(String.class);
        assertEquals("a,b,c", s);
    }

    public static class ListStringResponse extends JResponse<List<String>> {
        
        protected ListStringResponse(ListStringResponseBuilder rb) {
            super(rb);
        }

        public static ListStringResponseBuilder item(String s) {
            ListStringResponseBuilder b = new ListStringResponseBuilder();
            b.status(200).item(s);
            return b;
        }

        public static final class ListStringResponseBuilder extends AJResponseBuilder<List<String>, ListStringResponseBuilder> {

            protected List<String> l = new ArrayList<String>();
            
            protected ListStringResponseBuilder() {}

            public ListStringResponse build() {
                this.entity(l);
                ListStringResponse xr = new ListStringResponse(this);
                reset();
                return xr;
            }

            public ListStringResponseBuilder item(String s) {
                l.add(s);
                return this;
            }
        }
    }

    @Path("/")
    static public class ListStringResponseResource {
        @GET
        public ListStringResponse get() {
            return ListStringResponse.item("a").item("b").item("c").build();
        }
    }

    public void testListStringResponse() throws Exception {
        initiateWebApplication(ListStringResponseResource.class, ListStringWriter.class);
        WebResource r = resource("/");
        r.addFilter(new LoggingFilter());

        String s = r.get(String.class);
        assertEquals("a,b,c", s);
    }


    public static class ExtendedResponse<E> extends JResponse<E> {
        protected ExtendedResponse(ExtendedResponseBuilder rb) {
            super(rb);
        }

        public static <T> ExtendedResponseBuilder item(T s) {
            ExtendedResponseBuilder<T> b = new ExtendedResponseBuilder<T>();
            b.status(200).entity(s);
            return b;
        }

        public static final class ExtendedResponseBuilder<E> extends AJResponseBuilder<E, ExtendedResponseBuilder<E>> {

            protected ExtendedResponseBuilder() {}

            public ExtendedResponse build() {
                ExtendedResponse xr = new ExtendedResponse(this);
                reset();
                return xr;
            }
        }
    }

    @Path("/")
    static public class ExtendedResponseResource {
        @GET
        public ExtendedResponse<String> get() {
            return ExtendedResponse.item("a").build();
        }
    }

    public void testExtendedResponse() throws Exception {
        initiateWebApplication(ExtendedResponseResource.class, ListStringWriter.class);
        WebResource r = resource("/");
        r.addFilter(new LoggingFilter());

        String s = r.get(String.class);
        assertEquals("a", s);
    }

}
