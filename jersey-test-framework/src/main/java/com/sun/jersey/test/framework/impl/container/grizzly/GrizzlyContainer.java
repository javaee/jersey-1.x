/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.test.framework.impl.container.grizzly;

import com.sun.jersey.test.framework.impl.BasicLightWeightContainer;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.Adapter;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class provides methods for creating, starting and stopping an instance of
 * the embedded Grizzly server.
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class GrizzlyContainer implements BasicLightWeightContainer {

    /**
     * Map of init params to be passed to the servlet.
     */
    private Map<String, String> initParams;

    private SelectorThread selectorThread;

    /**
     * Base uri for the resources.
     */
    private URI BASE_URI;

    private static final Logger logger = Logger.getLogger(GrizzlyContainer.class
            .getName());

    /**
     * Default constructor.
     */
    public GrizzlyContainer() {
        initParams = new HashMap<String,String>();
    }

    /**
     * Start grizzly and deploy endpoint.
     * @throws java.lang.Exception
     */
    public void start() throws Exception {
        // Create an instance of grizzly web container
        //System.out.println("Starting grizzly...");
        logger.log(Level.INFO, "Starting grizzly...");
        String resources = initParams.get(PackagesResourceConfig.PROPERTY_PACKAGES);
        //System.out.println("Resources package :: " + resources);
        logger.log(Level.INFO, "Resources package :: " + resources);
        ResourceConfig resourceConfig = new PackagesResourceConfig(resources);
        Adapter adapter = ContainerFactory.createContainer(Adapter.class, resourceConfig);
        try {
            selectorThread = GrizzlyServerFactory.create(BASE_URI, resourceConfig);
            //System.out.println("App root path :: " + selectorThread.getWebAppRootPath());
            logger.log(Level.INFO, "App root path :: " + selectorThread.getWebAppRootPath());            
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        logger.log(Level.INFO, "Started GrizzlyServer");
    }

    /**
     * Stop grizzly server.
     * @throws java.lang.Exception
     */
    public void stop() throws Exception {
        // stop the grizzly thread selector instance
        
        if (selectorThread.isRunning()) {
            selectorThread.stopEndpoint();
        }
        
    }

    /**
     * Set base uri.
     * @param baseUri
     */
    public void setBaseUri(URI baseUri) {
        BASE_URI = baseUri;
    }

    /**
     * Set the servlet init params.
     * @param initParams
     */
    public void setInitParams(Map<String, String> initParams) {
        this.initParams.putAll(initParams);
    }

    public void setHttpListenerPort(int httpPort) {
        //do nothing
    }
   
}