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

package com.sun.jersey.impl.container.grizzly;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class CanonicalizationFeatureTest extends AbstractGrizzlyServerTester {
    
    public CanonicalizationFeatureTest(String testName) {
        super(testName);
    }
    
    @Path("test")
    public static class TestWebResource {
        
        @Path(value = "uri/{uriParam: .*}")
        @GET
        @Produces("text/plain")
        public String getUri(@PathParam("uriParam") String uri) {
            return uri;
        }

        @Path("slashes/{uriParam}/")
        @GET
        @Produces("text/plain")
        public String getSlashes(@PathParam("uriParam") String param) {
            return param;
        }

        @Path("dblslashes//{uriParam}//")
        @GET
        @Produces("text/plain")
        public String getDblSlashes(@PathParam("uriParam") String param) {
            return param;
        }
        
        @Path("qparam/a")
        @GET
        @Produces("text/plain")
        public String getQParam(@QueryParam("qParam") String param) {
            assertNotNull(param);
            return param;
        }
    }
    
    public void testContdSlashesProtection() {        
        ResourceConfig rc = new DefaultResourceConfig(TestWebResource.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_NORMALIZE_URI, true);
        rc.getFeatures().put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, false);
        rc.getFeatures().put(ResourceConfig.FEATURE_REDIRECT, true);
        
        startServer(rc);
        
        WebResource r = Client.create().resource(getUri().
                path("/test").build());
        
        assertEquals("http://jersey.dev.java.net", 
                r.path("uri/http://jersey.dev.java.net").get(String.class));
        assertEquals("customers", 
                r.path("dblslashes//customers//").get(String.class));

        stopServer();
                
        rc.getFeatures().
                put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, true);
        startServer(rc);
        
        assertEquals("http:/jersey.dev.java.net", 
                r.path("uri/http://jersey.dev.java.net").get(String.class));
        assertEquals("customers", r.path("slashes//customers//").get(String.class));        
        URI u = UriBuilder.fromPath("qparam//a")
                .queryParam("qParam", "val").build();
        assertEquals("val", r.uri(u).get(String.class));
    }    
}