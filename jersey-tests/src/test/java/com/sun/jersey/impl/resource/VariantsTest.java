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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.MediaTypes;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class VariantsTest extends AbstractResourceTester {
    
    public VariantsTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class LanguageVariantResource {
        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance().
                    languages(new Locale("zh")).
                    languages(new Locale("fr")).
                    languages(new Locale("en")).add().
                    build();

            Variant v = r.selectVariant(vs);
            if (v == null)
                return Response.notAcceptable(vs).build();
            else
                return Response.ok(v.getLanguage().toString(), v).build();
        }
    }

    public void testGetLanguageEn() throws IOException {
        initiateWebApplication(LanguageVariantResource.class);
        WebResource rp = resource("/");

        ClientResponse r = rp.
                header("Accept-Language", "en").
                get(ClientResponse.class);
        assertEquals("en", r.getEntity(String.class));
        assertEquals("en", r.getLanguage());
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(!contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    public void testGetLanguageZh() throws IOException {
        initiateWebApplication(LanguageVariantResource.class);
        WebResource rp = resource("/");

        ClientResponse r = rp.
                header("Accept-Language", "zh").
                get(ClientResponse.class);
        assertEquals("zh", r.getEntity(String.class));
        assertEquals("zh", r.getLanguage());
        System.out.println(r.getHeaders().getFirst("Vary"));
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(!contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    public void testGetLanguageMultiple() throws IOException {
        initiateWebApplication(LanguageVariantResource.class);
        WebResource rp = resource("/");

        ClientResponse r = rp.
                header("Accept-Language", "en;q=0.3, zh;q=0.4, fr").
                get(ClientResponse.class);
        assertEquals("fr", r.getEntity(String.class));
        assertEquals("fr", r.getLanguage());
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(!contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    @Path("/")
    public static class ComplexVariantResource {
        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance().
                    mediaTypes(MediaType.valueOf("image/jpeg")).add().
                    mediaTypes(MediaType.valueOf("application/xml")).
                    languages(new Locale("en", "us")).add().
                    mediaTypes(MediaType.valueOf("text/xml")).
                    languages(new Locale("en")).add().
                    mediaTypes(MediaType.valueOf("text/xml")).
                    languages(new Locale("en", "us")).add().
                    build();

            Variant v = r.selectVariant(vs);
            if (v == null)
                return Response.notAcceptable(vs).build();
            else
                return Response.ok("GET", v).build();
        }
    }

    public void testGetComplex1() throws IOException {
        initiateWebApplication(ComplexVariantResource.class);
        WebResource rp = resource("/");

        ClientResponse r = rp.accept("text/xml",
                "application/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5").
                header("Accept-Language", "en-us,en;q=0.5").
                get(ClientResponse.class);
        assertEquals("GET", r.getEntity(String.class));
        assertEquals(MediaType.valueOf("text/xml"), r.getType());
        assertEquals("en-US", r.getLanguage());
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    public void testGetComplex2() throws IOException {
        initiateWebApplication(ComplexVariantResource.class);
        WebResource rp = resource("/");

        ClientResponse r = rp.accept("text/xml",
                "application/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5").
                header("Accept-Language", "en,en-us").
                get(ClientResponse.class);
        assertEquals("GET", r.getEntity(String.class));
        assertEquals(MediaType.valueOf("text/xml"), r.getType());
        assertEquals("en", r.getLanguage());
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    public void testGetComplex3() throws IOException {
        initiateWebApplication(ComplexVariantResource.class);
        WebResource rp = resource("/");

        ClientResponse r = rp.accept("application/xml",
                "text/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5").
                header("Accept-Language", "en-us,en;q=0.5").
                get(ClientResponse.class);
        assertEquals("GET", r.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/xml"), r.getType());
        assertEquals("en-US", r.getLanguage());
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
    }

    public void testGetComplexNotAcceptable() throws IOException {
        initiateWebApplication(ComplexVariantResource.class);
        WebResource rp = resource("/", false);

        ClientResponse r = rp.accept("application/atom+xml").
                header("Accept-Language", "en-us,en").
                get(ClientResponse.class);
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
        assertEquals(406, r.getStatus());

        r = rp.accept("application/xml").
                header("Accept-Language", "fr").
                get(ClientResponse.class);
        vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
        assertEquals(406, r.getStatus());
    }
    
    @Path("/")
    public static class MediaTypeQualitySourceResource {
        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance().
                    mediaTypes(MediaType.valueOf("application/xml")).
                    mediaTypes(MediaType.valueOf("text/html;qs=2.0")).
                    add().
                    build();

            Variant v = r.selectVariant(vs);
            if (v == null)
                return Response.notAcceptable(vs).build();
            else
                return Response.ok("GET", v).build();
        }
    }

    public void testMediaTypeQualitySource() throws IOException {
        initiateWebApplication(MediaTypeQualitySourceResource.class);
        WebResource rp = resource("/");

        ClientResponse r = rp.accept(
                "application/xml",
                "text/html;q=0.9").
                get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.valueOf("text/html"), r.getType()));
        String vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));

        r = rp.accept(
                "text/html",
                "application/xml").
                get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.valueOf("text/html"), r.getType()));
        vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));

        r = rp.accept(
                "application/xml",
                "text/html").
                get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.valueOf("text/html"), r.getType()));
        vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));

        r = rp.accept(
                "application/xml").
                get(ClientResponse.class);
        assertTrue(MediaTypes.typeEquals(MediaType.valueOf("application/xml"), r.getType()));
        vary = r.getHeaders().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
    }

    private boolean contains(String l, String v) {
        String[] vs = l.split(",");
        for (String s : vs) {
            s = s.trim();
            if (s.equalsIgnoreCase(v))
                return true;
        }
        
        return false;
    }
}
