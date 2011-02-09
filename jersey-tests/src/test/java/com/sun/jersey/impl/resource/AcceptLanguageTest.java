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

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AcceptLanguageTest extends AbstractResourceTester {
    
    public AcceptLanguageTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class Resource {
        @GET
        public Response empty(@Context HttpHeaders h) {
            List<Locale> ld = h.getAcceptableLanguages();
            
            assertEquals(1, ld.size());
            assertEquals("*", ld.get(0).getLanguage());
            return Response.ok().language(Locale.ENGLISH).build();
        }
        
        @Path("en-gb")
        @GET
        public Response one(@Context HttpHeaders h) {
            List<Locale> ld = h.getAcceptableLanguages();
            
            assertEquals(1, ld.size());
            assertEquals("en", ld.get(0).getLanguage());
            assertEquals("GB", ld.get(0).getCountry());
            return Response.ok().language(ld.get(0)).build();
        }

        @Path("fr/de/en-gb")
        @GET
        public Response two(@Context HttpHeaders h) {
            List<Locale> ld = h.getAcceptableLanguages();
            
            assertEquals(3, ld.size());
            assertEquals("fr", ld.get(0).toString());
            assertEquals("de", ld.get(1).toString());
            assertEquals("en", ld.get(2).getLanguage());
            assertEquals("GB", ld.get(2).getCountry());
            return Response.ok().language(ld.get(0)).build();
        }
    }
    
    public void testAcceptGet() throws IOException {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");
        
        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals("en", cr.getLanguage());

        cr = r.path("en-gb").acceptLanguage("en-gb").get(ClientResponse.class);
        assertEquals("en-GB", cr.getLanguage());
        
        cr = r.path("fr/de/en-gb").
                acceptLanguage("de;q=0.8").
                acceptLanguage("fr").
                acceptLanguage("en-gb;q=0.7").
                get(ClientResponse.class);
        assertEquals("fr", cr.getLanguage());
        
        cr = r.path("fr/de/en-gb").
                acceptLanguage("de;q=0.8").
                acceptLanguage("fr;q=1.0").
                acceptLanguage("en-gb;q=0.7").
                get(ClientResponse.class);
        assertEquals("fr", cr.getLanguage());
    }
}