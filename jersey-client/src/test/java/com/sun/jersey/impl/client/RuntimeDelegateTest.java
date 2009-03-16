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
package com.sun.jersey.impl.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.server.impl.provider.RuntimeDelegateImpl; // not sure whether this is ok
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
// import com.sun.ws.rs.ext.RuntimeDelegateImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RuntimeDelegateTest extends TestCase {
    public void testRuntimeDelegateImpl() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        assertEquals(RuntimeDelegateImpl.class, rd.getClass());
    }
    
    public void testMediaType() {
        MediaType m = new MediaType("text", "plain");
    }

    public void testClient() throws IOException {
        Client c = Client.create();

        int port = getEnvVariable("JERSEY_HTTP_PORT", 9998);

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            HttpHandler handler = new HttpHandler() {
                public void handle(HttpExchange e) throws IOException {
                    InputStream in = e.getRequestBody();
                    ByteArrayOutputStream _out = new ByteArrayOutputStream();
                    byte[] buf = new byte[2048];
                    int read = 0;
                    while ((read = in.read(buf)) != -1) {
                        _out.write(buf, 0, read);
                    }

                    e.sendResponseHeaders(200, 0);

                    OutputStream out = e.getResponseBody();
                    in = new ByteArrayInputStream(_out.toByteArray());
                    while ((read = in.read(buf)) != -1) {
                        out.write(buf, 0, read);
                    }
                    out.flush();
                    e.close();
                }
            };
            server.createContext("/", handler);
            server.start();
            
            URI u = UriBuilder.fromUri("http://localhost/").port(port).build();
            WebResource r = c.resource(u);

            String s = r.type("text/plain").post(String.class, "CONTENT");
            assertEquals("CONTENT", s);
        } finally {
            if (server != null)
                server.stop(getEnvVariable("JERSEY_HTTP_STOPSEC", 0));
        }
    }

    private static int getEnvVariable(final String varName, int defaultValue) {
        if (null == varName) {
            return defaultValue;
        }
        String varValue = System.getenv(varName);
        if (null != varValue) {
            try {
                return Integer.parseInt(varValue);
            }catch (NumberFormatException e) {
                // will return default value bellow
            }
        }
        return defaultValue;
    }
}