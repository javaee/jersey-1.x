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
package com.sun.jersey.samples.bookstore.resources.glassfish;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

import com.sun.jersey.samples.bookstore.resources.WebContainerFacade;
//import org.glassfish.embed.EmbeddedInfo;
//import org.glassfish.embed.ScatteredArchive;
//import org.glassfish.embed.Server;
import org.glassfish.api.embedded.LifecycleException;
//import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.api.embedded.ScatteredArchive;
import org.glassfish.api.embedded.ScatteredArchive.Builder;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.ContainerBuilder;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import java.util.logging.Logger;



/**
 * @version $Revision: 1.1 $
 */
public class GlassFishFacade implements WebContainerFacade {

    private Server glassfish;
    //private ScatteredArchive war;
    private ScatteredArchive.Builder scatteredArchiveBuilder;

    //private Server server;
    private Server.Builder serverBuilder;
    private static final Logger LOGGER =
                Logger.getLogger(GlassFishFacade.class.getName());


     //private WebAppDescriptor appDescriptor;

        //final URI baseUri;

        final String WEB_XML = "web.xml";

        final String WEB_INF_PATH= "WEB-INF";

        final String TARGET_WEBAPP_PATH = "target/webapp";

        final String SRC_WEBAPP_PATH = "src/main/webapp";

        final String TARGET_CLASSES_PATH = "target/classes";

    private final URI BASE_URI;

    public GlassFishFacade(URI baseUri) {
        this.BASE_URI = baseUri;
    }

    public void setUp() throws Exception {
        if (glassfish == null) {
            /*
            EmbeddedInfo embeddedInfo = new EmbeddedInfo();
            embeddedInfo.setLogging(true);
            embeddedInfo.setHttpPort(BASE_URI.getPort());
            embeddedInfo.setVerbose(true);
            
            glassfish = new Server(embeddedInfo);
             * */

            serverBuilder = new Server.Builder("EmbeddedGFServer");
            //serverBuilder.logger(false);
            //serverBuilder.verbose(false);
            serverBuilder.logger(true);
            serverBuilder.verbose(true);
            glassfish = serverBuilder.build();
            try {
                glassfish.createPort(this.BASE_URI.getPort());
            } catch (java.io.IOException ioe2) {
                LOGGER.info("Encountered IOException [" + ioe2.getMessage() + "] trying to dump contents of WEB-INF/web.xml");
                throw new TestContainerException(ioe2);
            }
            glassfish.addContainer(glassfish.createConfig(ContainerBuilder.Type.web));


            // Deploy Glassfish referencing the web.xml
            /*
            ScatteredArchive war = new ScatteredArchive(BASE_URI.getRawPath(),
                        new File("src/main/webapp"),
                        new File("src/main/webapp/WEB-INF/web.xml"),
                        Arrays.asList(new File("target/classes").toURI().toURL(),
                    new File("target/test-classes").toURI().toURL()));
             * 
             */

            /*
            ScatteredArchive.Builder builderSA = new ScatteredArchive.Builder(
                "bookstore",
                new File("target/classes")
                Collections.singleton(new File("target/classes").toURI().toURL())
        ).addMetadata("WEB-INF/web.xml", new File("src/main/webapp/WEB-INF/web.xml"));
             * 
             */


            scatteredArchiveBuilder = new ScatteredArchive.Builder("bookstore", new File(TARGET_WEBAPP_PATH));
                    //The name for this metadata will be obtained by doing metadata.getName()
                    scatteredArchiveBuilder.addMetadata(new File(TARGET_WEBAPP_PATH + "/"  + WEB_INF_PATH + "/" + WEB_XML));
                    scatteredArchiveBuilder.addClassPath(new File(TARGET_CLASSES_PATH).toURI().toURL());
                   ScatteredArchive war = scatteredArchiveBuilder.buildWar();

            glassfish.start();
            glassfish.getDeployer().deploy(war, null);
        }
    }

    public void tearDown() throws Exception {
        if (glassfish != null) {
            glassfish.stop();
            glassfish = null;
        }
    }
}
