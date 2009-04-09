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

package com.sun.jersey.samples.https_grizzly;
        
import com.sun.grizzly.SSLConfig;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.ssl.SSLSelectorThread;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.samples.https_grizzly.auth.SecurityFilter;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author pavel.bucek@sun.com
 */
public class Server {

    private static GrizzlyWebServer webServer;

    public static final URI BASE_URI = getBaseURI();
    public static final String CONTENT = "JERSEY HTTPS EXAMPLE\n";

    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://localhost/").port(getPort(4463)).build();
    }

    private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;
    }

    protected static void startServer() {

        webServer = new GrizzlyWebServer(getPort(4463), ".", true);

        // add Jersey resource servlet

        ServletAdapter jerseyAdapter = new ServletAdapter();
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.packages",
                "com.sun.jersey.samples.https_grizzly.resource;com.sun.jersey.samples.https_grizzly.auth");
        jerseyAdapter.setContextPath("/");
        jerseyAdapter.setServletInstance(new ServletContainer());

        // add security filter (which handles http basic authentication)

        jerseyAdapter.addInitParameter(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                SecurityFilter.class.getName());

        webServer.addGrizzlyAdapter(jerseyAdapter, new String[]{"/"});


        // Grizzly ssl configuration
        
        SSLConfig sslConfig = new SSLConfig();

        // sslConfig.setNeedClientAuth(true); // don't work - known grizzly bug, will be fixed in 2.0.0

        // set up security context
        sslConfig.setKeyStoreFile("./keystore_server"); // contains server keypair
        sslConfig.setKeyStorePass("asdfgh");
        sslConfig.setTrustStoreFile("./truststore_server"); // contains client certificate
        sslConfig.setTrustStorePass("asdfgh");

        webServer.setSSLConfig(sslConfig);

        // turn server side client certificate authentication on

        ((SSLSelectorThread) webServer.getSelectorThread()).setNeedClientAuth(true);

        try {
            // start Grizzly embedded server //
            System.out.println("Jersey app started. Try out " + BASE_URI + "\nHit CTRL + C to stop it...");
            webServer.start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    protected static void stopServer() {
        webServer.stop();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        startServer();
    }
}

