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

package com.sun.ws.rest.impl.uri.conneg;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.ws.rest.impl.AbstractResourceTester;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriConnegLanguageTest extends AbstractResourceTester {
    
    public UriConnegLanguageTest(String testName) {
        super(testName);
        
    }

    @Path("/abc")
    public static class LanguageVariantResource {
        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance().
                    languages("zh").
                    languages("fr").
                    languages("en").add().
                    build();
            
            Variant v = r.selectVariant(vs);
            if (v == null)
                return Response.notAcceptable(vs).build();
            else 
                return Response.ok(v.getLanguage(), v).build();
        }
    }
    
    public void testLanguages() {
        ResourceConfig rc = new DefaultResourceConfig(LanguageVariantResource.class);
        rc.getLanguageMappings().put("english", "en");
        rc.getLanguageMappings().put("french", "fr");
        initiateWebApplication(rc);        
        
        WebResource rp = resource("/");
        
        ClientResponse r = rp.
                path("abc.english").
                get(ClientResponse.class);
        assertEquals("en", r.getEntity(String.class));
        assertEquals("en", r.getLanguage());
        
        r = rp.
                path("abc.french").
                get(ClientResponse.class);
        assertEquals("fr", r.getEntity(String.class));
        assertEquals("fr", r.getLanguage());
        
        r = rp.
                path("abc").
                header("Accept-Language", "en").
                get(ClientResponse.class);
        assertEquals("en", r.getEntity(String.class));
        assertEquals("en", r.getLanguage());
        
        r = rp.
                path("abc").
                header("Accept-Language", "fr").
                get(ClientResponse.class);
        assertEquals("fr", r.getEntity(String.class));
        assertEquals("fr", r.getLanguage());
    }
}