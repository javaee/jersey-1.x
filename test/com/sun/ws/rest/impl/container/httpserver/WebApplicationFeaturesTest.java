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

package com.sun.ws.rest.impl.application;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.test.util.AuxContentHandlerFactory;
import com.sun.ws.rest.impl.test.util.HttpTestHelper;
import com.sun.ws.rest.impl.test.util.HttpTestHelper.HttpResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import junit.framework.*;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class WebApplicationFeaturesTest extends TestCase {
    
    public WebApplicationFeaturesTest(String testName) {
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

        @UriTemplate(value = "slashes/{uriParam}/")
        @HttpMethod
        @ProduceMime("text/plain")
        public String getSlashes(@UriParam("uriParam") String param) {
            return param;
        }

        @UriTemplate(value = "dblslashes//{uriParam}//")
        @HttpMethod
        @ProduceMime("text/plain")
        public String getDblSlashes(@UriParam("uriParam") String param) {
            return param;
        }
        
    }
    
    public void testContdSlashesProtection() {
        
        Set<Class> resourceClassesSet = new HashSet<Class>();
        resourceClassesSet.add(TestWebResource.class);
        ResourceConfig myResourceConfig = new DefaultResourceConfig(resourceClassesSet);
        myResourceConfig.getFeatures().put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, false);
        
        HttpURLConnection.setContentHandlerFactory(new AuxContentHandlerFactory());
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
        server.setExecutor(null);
        server.start();
        try {
            HttpResponse response = 
                    HttpTestHelper.makeHttpRequest("GET", "http://localhost:" + port + "/test/uri/http://jersey.dev.java.net");
            assertEquals("http://jersey.dev.java.net", response.content);
            response = 
                    HttpTestHelper.makeHttpRequest("GET", "http://localhost:" + port + "/test/dblslashes//customers//");
            assertEquals("customers", response.content);
            
            myResourceConfig.getFeatures().put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, true);
            
            response = 
                    HttpTestHelper.makeHttpRequest("GET", "http://localhost:" + port + "/test/uri/http://jersey.dev.java.net");
            assertEquals("http:/jersey.dev.java.net", response.content);
            response = 
                    HttpTestHelper.makeHttpRequest("GET", "http://localhost:" + port + "/test/slashes//customers//");
            assertEquals("customers", response.content);            
        } catch (Exception e) {
            e.printStackTrace(System.err);
            fail("HTTP request failed :-(");
        } finally {
            server.stop(0);
        }
    }
    
}
