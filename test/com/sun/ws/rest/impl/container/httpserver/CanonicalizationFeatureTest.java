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
import com.sun.ws.rest.impl.client.ResourceProxy;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
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
            return param;
        }
    }
    
    public void testContdSlashesProtection() {        
        ResourceConfig rc = new DefaultResourceConfig(TestWebResource.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, false);
        
        startServer(rc);
        
        ResourceProxy r = ResourceProxy.create(getUri().
                path("/test/uri/http://jersey.dev.java.net").build());
        assertEquals("http://jersey.dev.java.net", r.get(String.class));
        r = ResourceProxy.create(getUri().
                path("/test/dblslashes//customers//").build());
        assertEquals("customers", r.get(String.class));
        
        rc.getFeatures().
                put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, true);
        
        r = ResourceProxy.create(getUri().
                path("/test/uri/http://jersey.dev.java.net").build());
        assertEquals("http:/jersey.dev.java.net", r.get(String.class));
        r = ResourceProxy.create(getUri().
                path("/test/slashes//customers//").build());
        assertEquals("customers", r.get(String.class));
        r = ResourceProxy.create(getUri().
                path("/test/qparam//a").queryParam("qParam", "val").build());
        assertEquals("val", r.get(String.class));
    }
    
}
