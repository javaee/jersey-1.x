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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderServlet;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;


/**
 * Test singleton resources that are managed by spring.<br>
 * Created on: Apr 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AbstractResourceTest {
    
    private static final Log LOG = LogFactory.getLog( AbstractResourceTest.class );

    private final String _springConfig;
    private final String _resourcePackages;
    private final int _port;
    private final String _servletPath;
    
    private Server _server;
    
    public AbstractResourceTest() {
        _springConfig = System.getProperty( "applicationContext", "applicationContext-spring25.xml" );
        _resourcePackages = System.getProperty( "resourcePackages", "com.sun.jersey.spring;com.sun.jersey.spring25" );
        _port = 9999;
        _servletPath = "/jersey-spring";
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @BeforeClass
    @SuppressWarnings("unused")
    public void setUp() throws Exception {
        startJetty( _port, _servletPath );
    }
    
    private void startJetty( int port, String servletPath ) throws Exception {
        LOG.info( "Starting jetty on port " + port + "..." );

         _server = new Server(port);
         final Context context = new Context(_server, "/", Context.SESSIONS);
         
         final Map<String,String> contextParams = new HashMap<String, String>();
         contextParams.put( "contextConfigLocation", "classpath:" + _springConfig );
         context.setInitParams( contextParams );
         
         
         final ServletHolder springServletHolder = new ServletHolder( ContextLoaderServlet.class );
         
         springServletHolder.setInitOrder( 1 );
         context.addServlet( springServletHolder, "/*" );
         
         
         final ServletHolder sh = new ServletHolder(SpringServlet.class);
         sh.setInitParameter( "com.sun.jersey.config.property.resourceConfigClass",
                 PackagesResourceConfig.class.getName() );
         sh.setInitParameter( PackagesResourceConfig.PROPERTY_PACKAGES,
                 _resourcePackages );
         sh.setInitOrder( 2 );
         context.addServlet(sh, servletPath + "/*");
         
         _server.start();
         LOG.info( "Successfully started jetty." );
    }
    
    private void stopJetty() throws Exception {
        try {
            _server.stop();
        } catch( Exception e ) {
            LOG.warn( "Could not stop jetty...", e );
        }
    }
    
    @AfterClass
    public void tearDown() throws Exception {
        LOG.info( "tearDown..." );
        stopJetty();
        LOG.info( "done..." );
    }

    public WebResource resource( final String path ) {
        final Client c = Client.create();
        final WebResource rootResource = c.resource( getResourcePath( path ) );
        return rootResource;
    }

    public String getResourcePath( final String path ) {
        return "http://localhost:" + _port + _servletPath + "/" + path;
    }

}
