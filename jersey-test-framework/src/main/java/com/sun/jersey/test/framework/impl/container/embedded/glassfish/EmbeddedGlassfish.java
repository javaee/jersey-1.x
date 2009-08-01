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

package com.sun.jersey.test.framework.impl.container.embedded.glassfish;

import com.sun.jersey.test.framework.impl.Deployable;
import com.sun.jersey.test.framework.impl.BasicServletContainer;
import com.sun.jersey.test.framework.*;
import com.sun.jersey.test.framework.impl.util.CommonUtils;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import org.glassfish.embed.EmbeddedInfo;
import org.glassfish.embed.ScatteredArchive;
import org.glassfish.embed.Server;

/**
 * The class provides methods for creating, starting and stopping an instance of
 * Embedded GlassFish.
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class EmbeddedGlassfish implements BasicServletContainer, Deployable, TestConstants {

    /**
     * War file to be deployed.
     */
    private ScatteredArchive war;

    /**
     * Container on which deployment would be done.
     */
    private Server server;;

    private EmbeddedInfo embeddedInfo;

    /**
     * Base uri for the resource.
     */
    private URI BASE_URI;

    /**
     * Default constructor.
     */
    public EmbeddedGlassfish() throws Exception {
        embeddedInfo = new EmbeddedInfo();
        embeddedInfo.setLogging(false);
        embeddedInfo.setHttpPort(CommonUtils.getPort(JERSEY_HTTP_PORT));
        embeddedInfo.setServerName("EmbeddedGFServer");

        //get an instance fof the server
        server = Server.getServer("EmbeddedGFServer");
        if(server == null) {
            server = new Server(embeddedInfo);
        }
        
    }

    public void setHttpListenerPort(int httpPort) {
        
    }

    /**
     * Set base uri.
     * @param baseUri
     */
    public void setBaseUri(URI baseUri) {
        BASE_URI = baseUri;       
    }

    /**
     * Start container and deploy the war file.
     * @throws java.lang.Exception
     */
    public void start() throws Exception {
        // deploy the expanded war file to embedded glassfish
        if(new File("src/main/webapp/WEB-INF/web.xml").exists()) {
            war = new ScatteredArchive(BASE_URI.getRawPath(),
                        new File("src/main/webapp"),
                        new File("src/main/webapp/WEB-INF/web.xml"),
                        Collections.singleton(new File("target/classes").toURI().toURL()));
        } else {
            war = new ScatteredArchive(BASE_URI.getRawPath(),
                        new File("target/webapp"),
                        new File("target/webapp/WEB-INF/web.xml"),
                        Collections.singleton(new File("target/classes").toURI().toURL()));
        }
        server.start();
        server.getDeployer().deploy(war, null);
    }

    /**
     * Stop container.
     * @throws java.lang.Exception
     */
    public void stop() throws Exception {
        //stop embedded glassfish
        server.stop();
    }

    public void setInitParams(Map<String, String> initParams) {
        //do nothing
    }

    /**
     * Sets context parameters.
     * @param contextParams
     */
    public void setContextParams(Map<String, String> contextParams) {
        //do nothing
    }

    /**
     * Sets servlet class.
     * @param servletClass
     */
    public void setServletClass(Class servletClass) {
        //do nothing
    }

    /**
     * Sets the servlet listener.
     * @param servletListenerClass
     */
    public void setServletListener(String servletListenerClass) {
        //do nothing
    }

    public void setServletPath(String servletPath) {
        // do nothing
    }

    public void setContextPath(String contextPath) {
        //do nothing
    }
    
}