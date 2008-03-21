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

package com.sun.ws.rest.impl.container.httpserver;

import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.client.Client;
import com.sun.ws.rest.api.client.WebResource;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import junit.framework.*;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class CanonicalizationFeatureTest extends AbstractHttpServerTester {
    
    public CanonicalizationFeatureTest(String testName) {
        super(testName);
    }
    
    @Path("test")
    public static class TestWebResource {
        
        @Path(value = "uri/{uriParam}", limited = false)
        @GET
        @ProduceMime("text/plain")
        public String getUri(@PathParam("uriParam") String uri) {
            return uri;
        }

        @Path("slashes/{uriParam}/")
        @GET
        @ProduceMime("text/plain")
        public String getSlashes(@PathParam("uriParam") String param) {
            return param;
        }

        @Path("dblslashes//{uriParam}//")
        @GET
        @ProduceMime("text/plain")
        public String getDblSlashes(@PathParam("uriParam") String param) {
            return param;
        }
        
        @Path("qparam/a")
        @GET
        @ProduceMime("text/plain")
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
        
        startServer(rc);
        rc.getFeatures().
                put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, true);
        
        assertEquals("http:/jersey.dev.java.net", 
                r.path("uri/http://jersey.dev.java.net").get(String.class));
        assertEquals("customers", r.path("slashes//customers//").get(String.class));        
        URI u = UriBuilder.fromPath("qparam//a")
                .queryParam("qParam", "val").build();
        assertEquals("val", r.uri(u).get(String.class));
    }   
}