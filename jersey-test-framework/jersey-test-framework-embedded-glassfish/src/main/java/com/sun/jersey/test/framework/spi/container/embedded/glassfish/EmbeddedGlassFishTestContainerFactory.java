/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.jersey.test.framework.spi.container.embedded.glassfish;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.impl.container.embedded.glassfish.WebXmlGenerator;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.List;

/**
 * A Web-based test container factory for creating test container instances 
 * using Embedded GlassFish.
 *
 * @author Srinivas.Bhimisetty@Sun.COM
 */
public class EmbeddedGlassFishTestContainerFactory implements TestContainerFactory {

    public Class<WebAppDescriptor> supports() {
        return WebAppDescriptor.class;
    }

    public TestContainer create(URI baseUri, AppDescriptor ad) {
        if (!(ad instanceof WebAppDescriptor))
            throw new IllegalArgumentException(
                    "The application descriptor must be an instance of WebAppDescriptor");

        return new EmbeddedGlassFishTestContainer(baseUri, (WebAppDescriptor)ad);
    }

    /**
     * This class has methods for instantiating , starting and stopping EmbeddedGlassFish.
     */
    private static class EmbeddedGlassFishTestContainer implements TestContainer {
        
        private static final Logger LOGGER =
                Logger.getLogger(EmbeddedGlassFishTestContainer.class.getName());

        private ScatteredArchive warArchive;
        
        //GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish();
        private GlassFish glassfish;
        //GlassFishRuntime gfr = GlassFishRuntime.bootstrap();
        private GlassFishRuntime gfr;
        
        private WebAppDescriptor appDescriptor;

        final URI baseUri;

        final String WEB_XML = "web.xml";
        final String SUN_WEB_XML = "sun-web.xml";

        final String WEB_INF_PATH= "WEB-INF";

        final String TARGET_WEBAPP_PATH = "target/webapp";

        final String SRC_WEBAPP_PATH = "src/main/webapp";

        final String TARGET_CLASSES_PATH = "target/classes";
        //final String TARGET_PATH = "target/";
        final String TARGET_PATH = "target";
        final String CLASSES_PATH = "classes";        

        /**
         * Creates an instance of {@link EmbeddedGlassFishTestContainer}
         * @param baseUri URI of the application
         * @param ad instance of {@link AppDescriptor}
         */
        private EmbeddedGlassFishTestContainer(URI baseUri, WebAppDescriptor ad) {
            this.baseUri = UriBuilder.fromUri(baseUri)
                    .path(ad.getContextPath())
                    .path(ad.getServletPath())
                    .build();
            this.appDescriptor = ad;

            LOGGER.info("Creating EmbeddedGlassFish test container configured at the base URI " + this.baseUri);
            instantiateServer();
            createArchive();
        }

        public Client getClient() {
            return null;
        }

        public URI getBaseUri() {
            return this.baseUri;
        }

        private void copyFile(FileInputStream fin, FileOutputStream fout) {
            int i;
            /*FileInputStream fin;
            FileOutputStream fout;

            fin = new FileInputStream(args[0]);
            fout = new FileOutputStream(args[1]);*/
            try {
                do {
                    i = fin.read();
                    if (i != -1) {
                        fout.write(i);
                    }
                } while (i != -1);

                fin.close();
                fout.close();
            } catch (IOException ioeX) {
                LOGGER.info("Encountered IOException [" + ioeX.getMessage() + "] trying to copyFile(InputStream,OutputStream) for [" + warArchive.toString() + "]");
                throw new TestContainerException(ioeX);
            }
        }
        
        //Starts the embedded server, opening ports, and running the startup services.
        public void start() {
            LOGGER.info("Starting the EmbeddedGlassFish instance...");
            try {
                glassfish.start();
                
                /*
                DeployCommandParameters deployCommandParameters = new DeployCommandParameters();
                deployCommandParameters.contextroot = this.appDescriptor.getContextPath();
                String name = server.getDeployer().deploy(war, deployCommandParameters);
                */
                Deployer deployer = glassfish.getDeployer();
                // Deploy my scattered web application
                //deployer.deploy(archive.toURI());
                if (warArchive == null) {
                    LOGGER.info("warArchive is null, nothing deployed");
                } else {
                    LOGGER.info("About to deploy [" + warArchive.toURI().toString() + "] from path ["+warArchive.toURI().getPath()+ "]  to EmbeddedGlassFish instance [" + deployer.toString() + "] with context-root set to [" + this.appDescriptor.getContextPath() + "]");
                    String deployedApp = deployer.deploy(warArchive.toURI());
                    LOGGER.info("Deployed [" + deployedApp + "] to EmbeddedGlassFish instance [" + deployer.toString() + "] with context-root set to [" + this.appDescriptor.getContextPath() + "]");
                }
            } catch (org.glassfish.embeddable.GlassFishException ex) {
                LOGGER.info("Caught GlassFishException ["+ex.getMessage()+ "] trying to start the embedded server instance");
                throw new TestContainerException(ex);
            } catch (java.io.IOException ioe) {
                LOGGER.info("Caught IOException ["+ioe.getMessage()+ "] trying to start the embedded server instance");
                throw new TestContainerException(ioe);
            } 
        }

        private void undeployAllApplications() {
            java.util.Collection<java.lang.String> deployedApps;
            Deployer deployer;
            try {
                deployer = glassfish.getDeployer();
                // Return names of all the deployed applications.                
                deployedApps = deployer.getDeployedApplications();
               
            } catch (GlassFishException glassFishException) {
                throw new TestContainerException(glassFishException);
            }

            //  undeploy each app in for-each loop
            for (String deployedApp : deployedApps) {
                try {
                    //see http://java.net/jira/browse/EMBEDDED_GLASSFISH-123                    
                    deployer.undeploy(deployedApp, "--droptables", "true");                    
                } catch (GlassFishException glassFishException) {
                    throw new TestContainerException(glassFishException);
                }
                LOGGER.info("Undeployed = " + deployedApp);
            }
        }

        //  stops the embedded server instance, any deployed application
        //  will be stopped ports will be closed and shutdown services will be run.
        public void stop() {
            LOGGER.info("Stopping the EmbeddedGlassFish instance...");
            try {                
                 undeployAllApplications();

                 // this will stop and dispose all the glassfish instances created with this gfr
                 // if you were to bootstrap GlassFishRuntime again, Shutdown GlassFish.
                 // this will avoid "already bootstrapped" errors seen when running multiple tests
                 // in same VM
                 gfr.shutdown();                               

            } catch (GlassFishException ex) {
                throw new TestContainerException(ex);
            }
        }

        /**
         * Instantiates EmbeddedGlassFish
         */
        private void instantiateServer() {

             /*
             See Usage example :
             * from
         http://embedded-glassfish.java.net/nonav/apidocs/org/glassfish/embeddable/archive/ScatteredArchive.html
             */

            if (gfr == null) {
                try {
                    LOGGER.info("Create instantiated GlassFishRuntime");
                    gfr = GlassFishRuntime.bootstrap();
                } catch (GlassFishException ex) {
                    throw new TestContainerException(ex);
                }
            } else {
                LOGGER.info("Re-use Already instantiated GlassFishRuntime");
                // try doing gfr.shutdown() if you were to
                // bootstrap GlassFishRuntime again.
                // Shutdown GlassFish.
                try {
                    gfr.shutdown();
                    // can comment out to see if this fixes 'already bootstrapped' error
                    // but never reaches here on 2nd test
                    gfr = GlassFishRuntime.bootstrap();
                } catch (GlassFishException shutdownex) {
                    throw new TestContainerException(shutdownex);
                }
            }

            if (glassfish == null) {
                try {                 
                    GlassFishProperties gfProperties = new GlassFishProperties();
                    gfProperties.setPort("http-listener", getBaseUri().getPort());

                    glassfish = gfr.newGlassFish(gfProperties);
                    // use glassfish
                } catch (GlassFishException ngfex) {
                    throw new TestContainerException(ngfex);
                }
            } else {
                LOGGER.info("Dispose Already instantiated GlassFish");
                try {
                    // dispose it.
                    glassfish.dispose();
                } catch (GlassFishException disposeex) {
                    throw new TestContainerException(disposeex);
                }
                LOGGER.info("Create another instantiated GlassFish");
                try {
                  glassfish = gfr.newGlassFish();
                } catch (GlassFishException ngfex2) {
                    throw new TestContainerException(ngfex2);
                }                
            }
           
        }

        /**
         * Checks is web.xml exists or not, if not generates one on the fly.
         * @return Whether web.xml is generated on the fly.
         */
        private boolean webXmlGeneratedOnTheFly() {
            if( !webXmlExists() ) {
                File webXmlDir = new File(TARGET_WEBAPP_PATH + "/" + WEB_INF_PATH);
                webXmlDir.mkdirs();
                File webXml = new File(TARGET_WEBAPP_PATH + "/" + WEB_INF_PATH +
                        "/" + WEB_XML);
                try {
                    OutputStream outputStream = new FileOutputStream(webXml);
                    WebXmlGenerator webXmlGenerator = new WebXmlGenerator(appDescriptor);
                    try {
                        webXmlGenerator.marshalData(outputStream);
                    } catch (JAXBException ex) {
                        throw new TestContainerException(ex);
                    }
                    outputStream.close();
                } catch (FileNotFoundException ex) {
                    throw new TestContainerException(ex);
                } catch (IOException ex) {
                    throw new TestContainerException(ex);
                }
                return true;
            }
            return false;
        }

        private boolean webXmlExists() {
            File webXml = new File(SRC_WEBAPP_PATH + "/" + WEB_INF_PATH + "/"
                    + WEB_XML);
            return webXml.exists();
        }

        private boolean sunWebXmlExists() {
            File sunWebXml = new File(SRC_WEBAPP_PATH + "/" + WEB_INF_PATH + "/"
                    + SUN_WEB_XML);
            return sunWebXml.exists();
        }

        /**
         * Creates an archive of the application for deployment.
         *
         Deployer deployer = glassfish.getDeployer();
         // Deploy my scattered web application
         deployer.deploy(archive.toURI());
         */
        private void createArchive() {
            // create an archive of the deployment descriptor and test classes
            if ( !webXmlGeneratedOnTheFly() ) {
                try {
                    /*
                    Construct a new scattered archive builder with the minimum information By default, 
                     a scattered archive is not different from any other archive where all the files
                     are located under a top level directory (topDir).
                     **/
                    LOGGER.info("#1 inside method createArchive ==> webXmlGeneratedOnTheFly ==> Creating scatteredArchive [" + SRC_WEBAPP_PATH + "]");                                      
                   
                     // Create a scattered web application.
                     //ScatteredArchive archive = new ScatteredArchive("testapp", ScatteredArchive.Type.WAR);
                     //use global variable
                     //warArchive = new ScatteredArchive(baseUri.getRawPath()+File.separator+ SRC_WEBAPP_PATH+File.separator+"myWarArchive", ScatteredArchive.Type.WAR);
                     warArchive = new ScatteredArchive(baseUri.getRawPath(), ScatteredArchive.Type.WAR);


                     // required if exist already //The name for this metadata will be obtained by doing metadata.getName()
                    try {
                        warArchive.addMetadata(new File(SRC_WEBAPP_PATH+ "/"  + WEB_INF_PATH + "/", WEB_XML));
                    } catch (java.io.IOException ioe) {
                        LOGGER.info("Encountered IOException [" + ioe.getMessage() + "] trying to addMetadata [" + SRC_WEBAPP_PATH+ "/"  + WEB_INF_PATH + "/" + WEB_XML + "]");
                        throw new TestContainerException(ioe);
                    }

                    //The name for this metadata will be obtained by doing metadata.getName() 
                    //scatteredArchiveBuilder.addMetadata(new File(SRC_WEBAPP_PATH + "/"  + WEB_INF_PATH + "/" + SUN_WEB_XML));
                    // resources/sun-web.xml is my WEB-INF/sun-web.xml
                    //archive.addMetadata(new File("resources", "sun-web.xml"));
                    try {
                        warArchive.addMetadata(new File(SRC_WEBAPP_PATH+ "/"  + WEB_INF_PATH + "/", SUN_WEB_XML));
                    } catch (java.io.IOException ioe) {
                        LOGGER.info("Encountered IOException [" + ioe.getMessage() + "] trying to addMetadata [" + SUN_WEB_XML + "]");
                        throw new TestContainerException(ioe);
                    }
                    // target/classes directory contains my complied servlets
                    //archive.addClassPath(new File("target", "classes"));
                    try {
                        warArchive.addClassPath(new File(TARGET_PATH, CLASSES_PATH));
                    } catch (java.io.IOException ioe) {
                        LOGGER.info("Encountered IOException [" + ioe.getMessage() + "] trying to addClassPath [" + TARGET_PATH +"/"+ CLASSES_PATH+ "]");
                        throw new TestContainerException(ioe);
                    }
                    // resources/MyLogFactory is my META-INF/services/org.apache.commons.logging.LogFactory
                    //archive.addMetadata(new File("resources", "MyLogFactory"),
                    //"META-INF/services/org.apache.commons.logging.LogFactory");
                    LOGGER.info("#1 inside method createArchive ==> webXmlGeneratedOnTheFly ==> just created scatteredArchive [" + SRC_WEBAPP_PATH + "] using WEB-INF/web.xml from [" + warArchive.toString() + "]");
                    System.out.println("jsb, #1 inside method createArchive ==> webXmlGeneratedOnTheFly ==> just created scatteredArchive [" + SRC_WEBAPP_PATH + "] using WEB-INF/web.xml from [" + warArchive.toString() + "]");
                } catch (Exception ex) {
                    throw new TestContainerException(ex);
                }
            } else {
                try {                  
                    LOGGER.info("#2 inside method createArchive ==> webXmlGeneratedOnTheFly ==> Creating scatteredArchive [" + TARGET_WEBAPP_PATH + "]");
                    //use global variable
                    //warArchive = new ScatteredArchive(baseUri.getRawPath()+File.separator+TARGET_WEBAPP_PATH+File.separator+"myWarArchive", ScatteredArchive.Type.WAR);
                    warArchive = new ScatteredArchive(baseUri.getRawPath(), ScatteredArchive.Type.WAR);

                    //The name for this metadata will be obtained by doing metadata.getName()
                    try {
                        warArchive.addMetadata(new File(TARGET_WEBAPP_PATH+ "/"  + WEB_INF_PATH + "/", WEB_XML));
                    } catch (java.io.IOException ioe) {
                        LOGGER.info("Encountered IOException [" + ioe.getMessage() + "] trying to addMetadata [" + TARGET_WEBAPP_PATH+ "/"  + WEB_INF_PATH + "/" + WEB_XML + "]");
                        throw new TestContainerException(ioe);
                    }

                    // do i need to add sun-web.xml to scatteredArchive via addMetaData to set context-root correctly
                    //scatteredArchiveBuilder.addMetadata(new File(TARGET_WEBAPP_PATH + "/"  + WEB_INF_PATH + "/" + SUN_WEB_XML));
                    try { 
                        warArchive.addClassPath(new File(TARGET_PATH, CLASSES_PATH));
                    } catch (java.io.IOException ioe) {
                        LOGGER.info("Encountered IOException [" + ioe.getMessage() + "] trying to addClassPath [" + TARGET_PATH +"/"+ CLASSES_PATH+ "]");
                        throw new TestContainerException(ioe);
                    }

                    LOGGER.info("#2 inside method createArchive ==> webXmlGeneratedOnTheFly ==> just created scatteredArchive [" + TARGET_WEBAPP_PATH + "] using WEB-INF/web.xml from [" + warArchive.toString() + "]");
                } catch (Exception ex) {
                    throw new TestContainerException(ex);
                }
            }            
        }

    }

}
