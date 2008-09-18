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

package com.sun.jersey.samples.helloworld;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.io.File;
import java.util.Collections;
import junit.framework.TestCase;
import org.glassfish.embed.ScatteredWar;
import org.glassfish.embed.GlassFish;

/**
 *
 * @author Naresh
 */
public class HelloWorldWebAppTest extends TestCase {

    private final String contextName = "helloworld-webapp";

    private final int httpPort = 9999;

    private final String baseUri = "http://localhost:" + httpPort + "/" + contextName;
    private GlassFish glassfish;

    private Client c;

    private WebResource wr;
    
    public HelloWorldWebAppTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        glassfish = newGlassFish(httpPort);
        assertNotNull("Glassfish instance returned is null", glassfish);
        deployApplication();
        c = Client.create();
        wr = c.resource(baseUri);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        glassfish.stop();
    }

    public void testHelloWorld() throws Exception {
        String responseMsg = wr.path("helloworld").get(String.class);
        assertEquals("Hello World", responseMsg);
    }

    private GlassFish newGlassFish(int port) throws Exception {
        GlassFish glassfishInstance = new GlassFish(port);
        return glassfishInstance;
    }

    private void deployApplication() throws Exception {
        ScatteredWar war = new ScatteredWar(contextName,
                new File("src/main/webapp"),
                new File("src/main/webapp/WEB-INF/web.xml"),
                Collections.singleton(new File("target/classes").toURI().toURL()));
        glassfish.deploy(war);
    }

}
