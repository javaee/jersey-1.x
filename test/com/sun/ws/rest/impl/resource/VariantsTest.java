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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResponseInBound;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class VariantsTest extends AbstractResourceTester {
    
    public VariantsTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class WebResource {
        @GET
        public Response doGet(@HttpContext Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance().
                    mediaTypes(MediaType.parse("image/jpeg")).add().
                    mediaTypes(MediaType.parse("application/xml")).languages("en-us").add().
                    mediaTypes(MediaType.parse("text/xml")).languages("en").add().
                    mediaTypes(MediaType.parse("text/xml")).languages("en-us").add().
                    build();
                    
            Variant v = r.selectVariant(vs);
            if (v == null)
                return Response.notAcceptable(vs).build();
            else 
                return Response.ok("GET", v).build();
        }
    }
    
    public void testGet1() throws IOException {
        initiateWebApplication(WebResource.class);
        ResourceProxy rp = resourceProxy("/");
        
        ResponseInBound r = rp.acceptable("text/xml",
                "application/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5").
                header("Accept-Language", "en-us,en;q=0.5").
                get(ResponseInBound.class);
        assertEquals("GET", r.getEntity(String.class));
        assertEquals(MediaType.parse("text/xml"), r.getContentType());
        assertEquals("en-us", r.getLangauge());
    }   
    
    public void testGet2() throws IOException {
        initiateWebApplication(WebResource.class);
        ResourceProxy rp = resourceProxy("/");
        
        ResponseInBound r = rp.acceptable("text/xml",
                "application/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5").
                header("Accept-Language", "en,en-us").
                get(ResponseInBound.class);
        assertEquals("GET", r.getEntity(String.class));
        assertEquals(MediaType.parse("text/xml"), r.getContentType());
        assertEquals("en", r.getLangauge());
    }
    
    public void testGet3() throws IOException {
        initiateWebApplication(WebResource.class);
        ResourceProxy rp = resourceProxy("/");
        
        ResponseInBound r = rp.acceptable("application/xml",
                "text/xml",
                "application/xhtml+xml",
                "image/png",
                "text/html;q=0.9",
                "text/plain;q=0.8",
                "*/*;q=0.5").
                header("Accept-Language", "en-us,en;q=0.5").
                get(ResponseInBound.class);
        assertEquals("GET", r.getEntity(String.class));
        assertEquals(MediaType.parse("application/xml"), r.getContentType());
        assertEquals("en-us", r.getLangauge());
    }   
    
    public void testGetNotAcceptable() throws IOException {
        initiateWebApplication(WebResource.class);
        ResourceProxy rp = resourceProxy("/", false);
        
        ResponseInBound r = rp.acceptable("application/atom+xml").
                header("Accept-Language", "en-us,en").
                get(ResponseInBound.class);
        String vary = r.getMetadata().getFirst("Vary");
        assertNotNull(vary);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
        assertEquals(406, r.getStatus());
        
        r = rp.acceptable("application/xml").
                header("Accept-Language", "fr").
                get(ResponseInBound.class);
        assertTrue(contains(vary, "Accept"));
        assertTrue(contains(vary, "Accept-Language"));
        assertEquals(406, r.getStatus());
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
