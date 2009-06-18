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
package com.sun.jersey.spring.tests;




import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.aspectj.lang.annotation.After;
import org.testng.annotations.AfterClass;


/**
 * Test singleton resources that are managed by spring.<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AbstractTest {
    
    private GrizzlyWebServer ws ;

    /**
     * Get the HTTP port for the Web application.
     * @param defaultPort the default HTTP port to use.
     * @return the HTTP port.
     */
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

    /**
     * Get the base URI for the Web application.
     * @return the base URI.
     */
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/spring").port(getPort(9998)).build();
    }

    /**
     * The base URI of the Web application.
     */
    private static final URI BASE_URI = getBaseURI();

    public void start() {
        start(null);
    }

    public void start(Map<String, String> initParams) {        
        String appConfig =  this.getClass().getName();
        appConfig = appConfig.replace(".", "/") + "-config.xml";

        try {
            stop();

            ws = new GrizzlyWebServer(BASE_URI.getPort());
            ServletAdapter sa = new ServletAdapter();
            sa.setServletInstance(SpringServlet.class.newInstance());
            sa.setServletPath("/spring");

            if (initParams != null) {
                for (Map.Entry<String, String> e : initParams.entrySet()) {
                    sa.addInitParameter(e.getKey(), e.getValue());
                }
            }

            sa.addServletListener("org.springframework.web.context.ContextLoaderListener");
            sa.addContextParameter("contextConfigLocation", "classpath:" + appConfig);

            ws.addGrizzlyAdapter(sa, new String[] {""} );
            ws.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() throws Exception {
        try {
            if (ws != null) {
                ws.stop();
            }
        } finally {
            ws = null;
        }
    }
    
//    @BeforeClass
//    public void setUp() throws Exception {
//        startGrizzly(BASE_URI);
//    }

    @AfterClass
    public void tearDown() throws Exception {
        stop();
    }

    public WebResource resource(String path) {
        return Client.create().resource(BASE_URI).path(path);
    }
}