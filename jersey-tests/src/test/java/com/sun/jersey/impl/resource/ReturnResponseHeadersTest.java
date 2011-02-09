/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.header.OutBoundHeaders;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ReturnResponseHeadersTest extends AbstractResourceTester {

    public ReturnResponseHeadersTest(String testName) {
        super(testName);
    }

    @Path("/")
    static public class HeadersResource {

        @Path("method")
        @GET
        public Response getWithMethod() {
            CacheControl cc = new CacheControl();
            cc.setNoCache(true);
            cc.setNoTransform(false);
            return Response.ok("content").
                    cacheControl(cc).
                    language(Locale.ENGLISH).
                    contentLocation(URI.create("/path")).
                    type("text/plain").
                    tag("tag").
                    lastModified(new Date(0)).
                    location(URI.create("/path")).
                    header("X-FOO", "foo").
                    build();
        }

        @Path("header")
        @GET
        public Response getWithHeader() {
            return Response.ok("content").
                    header("Cache-Control", "no-cache").
                    header("Content-Language", "en").
                    header("Content-Location", URI.create("/path")).
                    header("Content-Type", "text/plain").
                    header("ETag", "\"tag\"").
                    header("Last-Modified", new Date(0)).
                    header("Location", URI.create("/path")).
                    header("X-FOO", "foo").
                    build();
        }

        @Path("metadata")
        @GET
        public Response getMetadata() {
            Response r = Response.ok("content").build();
            MultivaluedMap<String, Object> headers = r.getMetadata();
            headers.putSingle("Cache-Control", "no-cache");
            headers.putSingle("Content-Language", "en");
            headers.putSingle("Content-Location", URI.create("/path"));
            headers.putSingle("Content-Type", "text/plain");
            headers.putSingle("ETag", "\"tag\"");
            headers.putSingle("Last-Modified", new Date(0));
            headers.putSingle("Location", URI.create("/path"));
            headers.putSingle("X-FOO", "foo");
            return r;
        }

        @Path("response")
        @GET
        public Response getResponse() {
            final OutBoundHeaders headers = new OutBoundHeaders();
            headers.putSingle("Cache-Control", "no-cache");
            headers.putSingle("Content-Language", "en");
            headers.putSingle("Content-Location", URI.create("/path"));
            headers.putSingle("Content-Type", "text/plain");
            headers.putSingle("ETag", "\"tag\"");
            headers.putSingle("Last-Modified", new Date(0));
            headers.putSingle("Location", URI.create("/path"));
            headers.putSingle("X-FOO", "foo");

            return new Response() {
                @Override
                public Object getEntity() {
                    return "content";
                }

                @Override
                public int getStatus() {
                    return 204;
                }

                @Override
                public MultivaluedMap<String, Object> getMetadata() {
                    return headers;
                }

            };
        }
    }


    public void testAllHeaders() throws Exception {
        initiateWebApplication(HeadersResource.class);
        WebResource r = resource("/");
        r.addFilter(new LoggingFilter());

        checkHeaders(r.path("method").get(ClientResponse.class).getHeaders());
        checkHeaders(r.path("header").get(ClientResponse.class).getHeaders());
        checkHeaders(r.path("metadata").get(ClientResponse.class).getHeaders());
        checkHeaders(r.path("response").get(ClientResponse.class).getHeaders());
    }

    private void checkHeaders(MultivaluedMap<String, String> headers) {
        checkHeader("Cache-Control", "no-cache", headers);
        checkHeader("Content-Language", "en", headers);
        checkHeader("Content-Location", "/path", headers);
        checkHeader("Content-Type", "text/plain", headers);
        checkHeader("ETag", "\"tag\"", headers);
        checkHeader("Last-Modified", "Thu, 01 Jan 1970 00:00:00 GMT", headers);
        checkHeader("Location", "test:/base/path", headers);
        checkHeader("X-FOO", "foo", headers);
    }

    private void checkHeader(String name, String expectedValue,
            MultivaluedMap<String, String> headers) {
        String value = headers.getFirst(name);
        assertNotNull(value);
        assertEquals(expectedValue, value);
    }
}
