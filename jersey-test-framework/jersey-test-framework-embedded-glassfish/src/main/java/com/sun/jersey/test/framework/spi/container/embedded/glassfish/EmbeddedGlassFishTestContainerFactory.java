/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.test.framework.spi.container.embedded.glassfish;

import com.sun.jersey.test.framework.impl.container.embedded.glassfish.WebXmlGenerator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;
import org.glassfish.embed.EmbeddedException;
import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.embed.ScatteredArchive;
import org.glassfish.embed.Server;

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

        private ScatteredArchive war;

        private Server server;

        private EmbeddedInfo embeddedInfo;
        
        private WebAppDescriptor appDescriptor;

        final URI baseUri;

        final String WEB_XML = "web.xml";

        final String WEB_INF_PATH= "WEB-INF";

        final String TARGET_WEBAPP_PATH = "target/webapp";

        final String SRC_WEBAPP_PATH = "src/main/webapp";

        final String TARGET_CLASSES_PATH = "target/classes";

        /**
         * Creates an instance of {@link EmbeddedGlassFishTestContainer}
         * @param Base URI of the application
         * @param An instance of {@link AppDescriptor}
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

        public void start() {
            LOGGER.info("Starting the EmbeddedGlassFish instance...");
            try {
                server.start();
                server.getDeployer().deploy(war, null);
            } catch (EmbeddedException ex) {
                throw new TestContainerException(ex);
            }             
        }

        public void stop() {
            LOGGER.info("Stopping the EmbeddedGlassFish instance...");
            try {
                server.getDeployer().undeployAll();
                server.stop();
            } catch (EmbeddedException ex) {
                throw new TestContainerException(ex);
            }             
        }

        /**
         * Instantiates EmbeddedGlassFish
         */
        private void instantiateServer() {
            embeddedInfo = new EmbeddedInfo();
            embeddedInfo.setLogging(false);
            embeddedInfo.setHttpPort(this.baseUri.getPort());
            embeddedInfo.setServerName("EmbeddedGFServer");

            //get an instance fof the server
            server = Server.getServer("EmbeddedGFServer");
            if(server == null) {
                try {
                    server = new Server(embeddedInfo);
                } catch (EmbeddedException ex) {
                    throw new TestContainerException(ex);
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

        /**
         * Creates an archive of the application for deployment.
         */
        private void createArchive() {
            // create an archive of the deployment descriptor and test classes
            if ( !webXmlGeneratedOnTheFly() ) {
                try {
                    war = new ScatteredArchive(baseUri.getRawPath(),
                            new File(SRC_WEBAPP_PATH),
                            new File(SRC_WEBAPP_PATH + "/"  + WEB_INF_PATH + "/" + WEB_XML),
                            Collections.singleton(new File(TARGET_CLASSES_PATH).toURI().toURL()));
                } catch (MalformedURLException ex) {
                    throw new TestContainerException(ex);
                }
            } else {
                try {
                    war = new ScatteredArchive(baseUri.getRawPath(),
                            new File(TARGET_WEBAPP_PATH),
                            new File(TARGET_WEBAPP_PATH + "/" + WEB_INF_PATH + "/" + WEB_XML),
                            Collections.singleton(new File(TARGET_CLASSES_PATH).toURI().toURL()));
                } catch (MalformedURLException ex) {
                    throw new TestContainerException(ex);
                }
            }            
        }

    }

}
