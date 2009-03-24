/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.test.framework.impl.container.http;

import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.impl.BasicLightWeightContainer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The class provides methods for creating, starting and stopping an instance of
 * embedded HTTPServer.
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class HTTPContainer implements BasicLightWeightContainer {

    /**
     * Map of resource packages to be sent to the HTTPServerFactory.
     */
    private Map<String, Object> props;

    /**
     * Holds resources info.
     */
    private ResourceConfig resourceConfig;

    /**
     * Handle to the HTTP container.
     */
    private HttpServer server;

    /**
     * HTTP Handler
     */
    private HttpHandler container;

    /**
     * Base uri.
     */
    private URI BASE_URI;

    /**
     * Default constructor.
     */
    public HTTPContainer() {
        props = new HashMap<String, Object>();
    }

    /**
     * Start the HTTP container and deploy the resources.
     * @throws java.lang.Exception
     */
    public void start() throws Exception {
        // Create an instance of HTTP Server and start it
            resourceConfig = new PackagesResourceConfig( props );
            container = ContainerFactory.createContainer( HttpHandler.class,
                    resourceConfig );
            server = HttpServerFactory.create(BASE_URI, container);
            server.start();
    }

    /**
     * Stop the server.
     */
    public void stop() {
        //stop the http server instance
            server.stop(0);
    }

    /**
     * Set base uri.
     * @param baseUri
     */
    public void setBaseUri(URI baseUri) {
        BASE_URI = baseUri;
    }

    
    public void setInitParams(Map<String, String> initParams) {
        props.putAll(initParams);
    }

    public void setHttpListenerPort(int httpPort) {
        //do nothing
    }
    
}