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
package com.sun.jersey.spring;

import java.util.HashMap;
import java.util.Map;


import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Test singleton resources that are managed by spring.<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AbstractResourceTest {
    
    public static final String APPLICATION_CONTEXT_SPRING20_XML = "applicationContext-spring20.xml";
    public static final String APPLICATION_CONTEXT_SPRING25_XML = "applicationContext-spring25.xml";

    private static final Logger LOGGER = Logger.getLogger(AbstractResourceTest.class.getName());

    protected String _springConfig;
    private final int _port;
    private final String _servletPath;
    private final boolean springManaged;
    
    private GrizzlyWebServer ws ;

    public AbstractResourceTest() {
        this(true);
    }

    public AbstractResourceTest(boolean springManaged) {
        _springConfig = System.getProperty( "applicationContext", APPLICATION_CONTEXT_SPRING25_XML );
        _port = 9999;
        _servletPath = "/jersey-spring";
        this.springManaged = springManaged;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @BeforeClass
    @SuppressWarnings("unused")
    public void setUp() throws Exception {
        startGrizzly(_port, _servletPath);
    }

   /**
    * Starts the embedded Grizzly server.
    * @param port
    * @param servletPath
    * @throws java.lang.Exception
    */
    private void startGrizzly(int port, String servletPath) throws Exception {
        LOGGER.info("Starting grizzly...");
        ws = new GrizzlyWebServer(port);
        ServletAdapter sa = new ServletAdapter();
        sa.setServletInstance(SpringServlet.class.newInstance());
        sa.addServletListener("org.springframework.web.context.ContextLoaderListener");
        sa.addContextParameter("contextConfigLocation","classpath:"+_springConfig);
        if (!springManaged) {
            sa.addInitParameter( "com.sun.jersey.config.property.resourceConfigClass",
                     PackagesResourceConfig.class.getName() );
            sa.addInitParameter( PackagesResourceConfig.PROPERTY_PACKAGES,
                     "com.sun.jersey.spring.jerseymanaged" );
        }
        sa.setServletPath(servletPath);
        ws.addGrizzlyAdapter(sa, new String[] {""} );
        ws.start();
    }

    /**
     * Stop the embedded Grizzly server.
     * @throws java.lang.Exception
     */
    private void stopGrizzly() throws Exception {
        try {
            ws.stop();
        } catch( Exception e ) {
            LOGGER.log(Level.WARNING, "Could not stop grizzly...", e );
        }
    }
    
    @AfterClass
    public void tearDown() throws Exception {
        LOGGER.info( "tearDown..." );
        stopGrizzly();
        LOGGER.info( "done..." );
    }

    public WebResource resource( final String path ) {
        final Client c = Client.create();
        final WebResource rootResource = c.resource( getResourcePath( path ) );
        return rootResource;
    }

    public String getResourcePath( final String path ) {
        return getBaseUri() + path;
    }

    protected String getBaseUri() {
        return "http://localhost:" + _port + _servletPath + "/";
    }

}