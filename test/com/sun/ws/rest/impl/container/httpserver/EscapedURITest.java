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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EscapedURITest extends TestCase {
    @UriTemplate("/x%20y")
    public static class EscapedURIResource {
        @HttpMethod
        public String get(@HttpContext UriInfo info) {
            assertEquals("http://localhost:9998/context/x%20y", info.getURI().toString());
            assertEquals("http://localhost:9998/context/", info.getBaseURI().toString());
            assertEquals("x y", info.getURIPath());
            return "CONTENT";
        }
    }
        
    public EscapedURITest(String testName) {
        super(testName);
    }
    
    public void testExpliciWebResourceReference() throws IOException {
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, EscapedURIResource.class);
        
        HttpServer server = HttpServer.create(new InetSocketAddress(9998), 0);
        server.createContext("/context", handler);
        server.setExecutor(null);
        server.start();
                
        get("http://localhost:9998/context/x%20y");
        
        server.stop(1);
    }
        
    private void get(String uri) throws IOException {
        URL u = new URL(uri);
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        uc.setRequestMethod("GET");
        
        assertEquals(200, uc.getResponseCode());
        
        InputStream in = uc.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int r;
        while ((r = in.read(buffer)) != -1) {
            baos.write(buffer, 0, r);
        }
        String s = new String(baos.toByteArray());
        assertEquals("CONTENT", s);
    }    
}
