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

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.client.ResourceProxy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.UriBuilder;
import junit.framework.*;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class CanonicalizationFeatureTest extends TestCase {
    
    public CanonicalizationFeatureTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("test")
    public static class TestWebResource {
        
        @UriTemplate(value = "uri/{uriParam}", limited = false)
        @HttpMethod
        @ProduceMime("text/plain")
        public String getUri(@UriParam("uriParam") String uri) {
            return uri;
        }

        @UriTemplate("slashes/{uriParam}/")
        @HttpMethod
        @ProduceMime("text/plain")
        public String getSlashes(@UriParam("uriParam") String param) {
            return param;
        }

        @UriTemplate("dblslashes//{uriParam}//")
        @HttpMethod
        @ProduceMime("text/plain")
        public String getDblSlashes(@UriParam("uriParam") String param) {
            return param;
        }
        
        @UriTemplate("qparam/a")
        @HttpMethod
        @ProduceMime("text/plain")
        public String getQParam(@QueryParam("qParam") String param) {
            return param;
        }
    }
    
    public void testContdSlashesProtection() throws IOException {
        ResourceConfig myResourceConfig = new DefaultResourceConfig(TestWebResource.class);
        myResourceConfig.getFeatures().put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, false);
        
        HttpHandler handler = ContainerFactory.createContainer(
                HttpHandler.class, myResourceConfig);
        
        HttpServer server = null;
        int port;
        for (port = 9998; port < 10998; port++) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (IOException ex) {
            }
            if (null != server) { // free port number found :-)
                break;
            }
        }
        assertNotNull(server);
        
        server.createContext("/", handler);
        server.start();
        
        URI baseUri = UriBuilder.fromUri("http://localhost").port(port).build();
                
        ResourceProxy r = ResourceProxy.create(UriBuilder.fromUri(baseUri).
                path("/test/uri/http://jersey.dev.java.net").build());
        assertEquals("http://jersey.dev.java.net", r.get(String.class));
        r = ResourceProxy.create(UriBuilder.fromUri(baseUri).
                path("/test/dblslashes//customers//").build());
        assertEquals("customers", r.get(String.class));
        
        myResourceConfig.getFeatures().
                put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, true);
        
        r = ResourceProxy.create(UriBuilder.fromUri(baseUri).
                path("/test/uri/http://jersey.dev.java.net").build());
        assertEquals("http:/jersey.dev.java.net", r.get(String.class));
        r = ResourceProxy.create(UriBuilder.fromUri(baseUri).
                path("/test/slashes//customers//").build());
        assertEquals("customers", r.get(String.class));
        r = ResourceProxy.create(UriBuilder.fromUri(baseUri).
                path("/test/qparam//a").queryParam("qParam", "val").build());
        assertEquals("val", r.get(String.class));
        
        server.stop(0);        
    }
    
}
