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
package com.sun.jersey.test.framework.impl;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.test.framework.util.ApplicationDescriptor;
import com.sun.jersey.test.framework.TestConstants;
import com.sun.jersey.test.framework.impl.container.http.HTTPContainer;
import com.sun.jersey.test.framework.impl.container.grizzly.web.GrizzlyWebContainer;
import com.sun.jersey.test.framework.impl.container.embedded.glassfish.EmbeddedGlassfish;
import com.sun.jersey.test.framework.impl.container.external.ExternalContainer;
import com.sun.jersey.test.framework.impl.util.CommonUtils;
import com.sun.jersey.test.framework.impl.util.WebXmlGenerator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class JerseyAppContainer implements TestConstants {

    /**
     * Reference to container type on which deployments are to be done.
     */
    private String containerType;

    /**
     * Base URI for the resources.
     */
    private URI baseUri;

    private String servletPath;

    private String contextPath;

    /**
     * A map of servlet init-params.
     */
    private Map<String, String> initParams;

    private Map<String, String> contextParams;

    private String contextListenerClassName;

    private Class servletClass;


    /**
     * Map of container types to container handles.
     */
    private Hashtable<String, Class> containerTable;

    /**
     * Handle to container.
     */
    private BasicLightWeightContainer container;
    
    private ApplicationDescriptor appDescriptor;

    private String webXml = "web.xml";

    private String webXmlDirPath = "target/webapp/WEB-INF";

    private static final Logger logger = Logger.getLogger( JerseyAppContainer.class
            .getName() );

    public JerseyAppContainer(String containerType, ApplicationDescriptor applicationDescriptor) {
        // add key-value pairs representing the various containers on which the tests
        // could be run.
        containerTable = new Hashtable<String, Class>();
        containerTable.put(EMBEDDED_GF_V3, EmbeddedGlassfish.class);
        containerTable.put(GRIZZLY_WEB_CONTAINER, GrizzlyWebContainer.class);
        containerTable.put(HTTP_SERVER, HTTPContainer.class);
        containerTable.put("External", ExternalContainer.class);
        if (!containerTable.containsKey(containerType)) {
            logger.log(Level.SEVERE, "Invalid container.type attribute passed. Expected one of :: ");
            logger.log(Level.SEVERE, "====================");
            logger.log(Level.SEVERE, EMBEDDED_GF_V3 + "\n" + GRIZZLY_WEB_CONTAINER +
                    "\n" + HTTP_SERVER);
            logger.log(Level.SEVERE, "====================");
            System.exit(-1);
        }
        this.containerType = containerType;
        this.appDescriptor = applicationDescriptor;
        readApplicationDescriptor();
    }
    
    /**
     * Deploy the resource(s) to the container and start it.
     * @throws java.lang.Exception
     */
    public void startServer() throws Exception {
        // create an instance of a container and start it 
        container = (BasicLightWeightContainer) (containerTable.get(containerType).newInstance());
        container.setBaseUri(baseUri);
        container.setInitParams(initParams);

        if (container instanceof BasicServletContainer) {
            ((BasicServletContainer)container).setContextParams(contextParams);
            ((BasicServletContainer)container).setServletClass(servletClass);
            ((BasicServletContainer)container).setServletListener(contextListenerClassName);
            ((BasicServletContainer)container).setServletPath(servletPath);
            ((BasicServletContainer)container).setContextPath(contextPath);
        }else {
            // if the container is an instance of HTTPContainer, check that
            // the contextParams do not really contain any data, if so, start the
            // container, else exit the test run
            if(contextParams != null && contextParams.size() > 0) {
                logger.log(Level.SEVERE, "The test cannot be run on " + containerType + " since there " +
                        "is no way to pass context params to the server instance.");
                System.exit(-1);
            }
        }

        // check if the container needs an external deployment descriptor
        if (container instanceof Deployable) {
            //check if the deployment descriptor is present or not
            File webXmlDir = new File(webXmlDirPath);
            webXmlDir.mkdirs();
            File file = new File(webXmlDirPath + "/" + webXml);
            // generate one if it doesn't exist
            if ( file.createNewFile() ) {
                OutputStream outputStream = new FileOutputStream(file);
                WebXmlGenerator webXmlGenerator = new WebXmlGenerator(appDescriptor);
                webXmlGenerator.marshalData(outputStream);
                outputStream.close();
            }
        }
            
        container.start();

    }

    /**
     * Stop the container.
     * @throws java.lang.Exception
     */
    public void stopServer() throws Exception {
        container.stop();
    }

    private void readApplicationDescriptor() {
        initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.resourceConfigClass",
                PackagesResourceConfig.class.getName());
        initParams.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                appDescriptor.getRootResourcePackageName());
        if (appDescriptor.getServletInitParams() != null) {
            initParams.putAll(appDescriptor.getServletInitParams());
        } else {
            appDescriptor.setServletInitParams(initParams);
        }
        contextParams = appDescriptor.getContextParams();
        servletClass = appDescriptor.getServletClass();
        contextListenerClassName = appDescriptor.getContextListenerClassName();
        servletPath = appDescriptor.getServletPath();
        contextPath = appDescriptor.getContextPath();
        baseUri = CommonUtils.getBaseURI(contextPath, servletPath);
    }
}