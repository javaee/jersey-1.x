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

package com.sun.jersey.impl.uri.conneg;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.List;
import java.util.Locale;
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