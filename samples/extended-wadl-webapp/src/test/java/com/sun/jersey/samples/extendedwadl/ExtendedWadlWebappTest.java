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

package com.sun.jersey.samples.extendedwadl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;
import org.glassfish.embed.GlassFish;
import org.glassfish.embed.ScatteredWar;

/**
 *
 * @author Naresh (Srinivas.Bhimisetty@Sun.com)
 */public class ExtendedWadlWebappTest extends TestCase {
    
    private static int getPort(int defaultPort) {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
            }
        }
        return defaultPort;        
    } 
    
    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost/").port(getPort(9998)).
                build();
    }
    
    private static final URI BASE_URI = getBaseURI();
    
    private GlassFish glassfish;

    private WebResource r;
    
     public ExtendedWadlWebappTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Start Glassfish
        glassfish = new GlassFish(BASE_URI.getPort());
        // Deploy Glassfish referencing the web.xml
        ScatteredWar war = new ScatteredWar(BASE_URI.getRawPath(),
                new File("src/main/webapp"),
                new File("src/main/webapp/WEB-INF/web.xml"),
                Collections.singleton(new File("target/classes").toURI().toURL()));
        glassfish.deploy(war);
        Client c = Client.create();
        r = c.resource(BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        glassfish.stop();
    }

    /**
     * Test checks that the WADL generated using the WadlGenerator api doesn't
     * contain the expected text.
     * @throws java.lang.Exception
     */
    public void testExtendedWadl() throws Exception {
        String wadl = r.path("application.wadl").accept(MediaTypes.WADL).get(String.class);
        assertTrue("Generated wadl is of null length", wadl.length() > 0);
        assertTrue("Generated wadl doesn't contain the expected text",
                wadl.contains("This is a paragraph"));
    }

}
