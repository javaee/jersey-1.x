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

package com.sun.jersey.impl.uri.conneg;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.List;
import javax.ws.rs.Path;
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
public class UriConnegLanguageMediaTypeTest extends AbstractResourceTester {
    
    public UriConnegLanguageMediaTypeTest(String testName) {
        super(testName);
        
    }

    @Path("/abc")
    public static class LanguageVariantResource {
        @GET
        public Response doGet(@Context Request r) {
            List<Variant> vs = Variant.VariantListBuilder.newInstance().
                    mediaTypes(MediaType.valueOf("application/foo")).
                    languages("en").languages("fr").add().
                    mediaTypes(MediaType.valueOf("application/bar")).
                    languages("en").languages("fr").add().
                    build();            
            
            Variant v = r.selectVariant(vs);
            if (v == null)
                return Response.notAcceptable(vs).build();
            else 
                return Response.ok(v.getMediaType().toString() + ", " + v.getLanguage(), v).
                        build();
        }
    }
    
    public void testLanguages() {
        ResourceConfig rc = new DefaultResourceConfig(LanguageVariantResource.class);
        rc.getMediaTypeMappings().put("foo", MediaType.valueOf("application/foo"));
        rc.getMediaTypeMappings().put("bar", MediaType.valueOf("application/bar"));
        rc.getLanguageMappings().put("english", "en");
        rc.getLanguageMappings().put("french", "fr");
        initiateWebApplication(rc);        
        
        WebResource rp = resource("/");
        
        _test("english", "foo", "en", "application/foo");
        _test("french", "foo", "fr", "application/foo");
        
        _test("english", "bar", "en", "application/bar");
        _test("french", "bar", "fr", "application/bar");
    }
    
    private void _test(String ul, String um, String l, String m) {
        WebResource rp = resource("/");

        ClientResponse r = rp.
                path("abc." + ul + "." + um).
                get(ClientResponse.class);
        assertEquals(m + ", " + l, r.getEntity(String.class));
        assertEquals(l, r.getLanguage());
        assertEquals(m, r.getType().toString());
        
        r = rp.
                path("abc." + um + "." + ul).
                get(ClientResponse.class);
        assertEquals(m + ", " + l, r.getEntity(String.class));
        assertEquals(l, r.getLanguage());
        assertEquals(m, r.getType().toString());

        r = rp.
                path("abc").
                accept(m).
                header("Accept-Language", l).
                get(ClientResponse.class);
        assertEquals(m + ", " + l, r.getEntity(String.class));
        assertEquals(l, r.getLanguage());
        assertEquals(m, r.getType().toString());
    }
}