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
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.container.ContainerFactory;
import com.sun.ws.rest.impl.client.ResourceProxy;
import java.io.IOException;
import java.net.InetSocketAddress;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class QueryParamTest extends TestCase {
    @UriTemplate("/test")
    public static class QueryParamResource {
        @HttpMethod
        public String get(@QueryParam("x") String x, @QueryParam("y") String y) {
            return y;
        }
    }
        
    public QueryParamTest(String testName) {
        super(testName);
    }
    
    public void testQueryParam() throws IOException {
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, QueryParamResource.class);
        
        HttpServer server = HttpServer.create(new InetSocketAddress(9998), 0);
        server.createContext("/context", handler);
        server.start();
                
        UriBuilder base = UriBuilder.fromUri("http://localhost:9998/context/test");
            
        ResourceProxy r = ResourceProxy.create(base.clone().
                queryParam("x", "1").encode(false).queryParam("y", "1+%2B+2").build());
        assertEquals("1 + 2", r.get(String.class));
        r = ResourceProxy.create(base.clone().
                queryParam("x", "1").encode(false).queryParam("y", "1+%26+2").build());
        assertEquals("1 & 2", r.get(String.class));
        r = ResourceProxy.create(base.clone().
                queryParam("x", "1").encode(false).queryParam("y", "1+%7C%7C+2").build());
        assertEquals("1 || 2", r.get(String.class));
        
        server.stop(0);
    }
}
