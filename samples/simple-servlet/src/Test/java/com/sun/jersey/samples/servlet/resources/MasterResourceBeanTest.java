/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.samples.servlet.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;
import org.glassfish.embed.GlassFish;
import org.glassfish.embed.ScatteredWar;
import static org.junit.Assert.*;

/**
 *
 * @author Naresh
 */
public class MasterResourceBeanTest extends TestCase {

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
                path("SimpleServlet").build();
    }

    private static final URI BASE_URI = getBaseURI();

    private GlassFish glassfish;

    private WebResource wr;

    private Client c;

    public MasterResourceBeanTest(String testName) throws Exception {
        super(testName);        
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Start Glassfish
        glassfish = new GlassFish(BASE_URI.getPort());
        // Deploy Glassfish referencing the web.xml
        ScatteredWar war = new ScatteredWar(BASE_URI.getRawPath(),
                new File("src/main/webapp"),
                new File("src/main/webapp/WEB-INF/web.xml"),
                Collections.singleton(new File("target/classes").toURI().toURL()));
        glassfish.deploy(war);
        c = Client.create();
        wr = c.resource(BASE_URI);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        glassfish.stop();        
    }

    /**
     * The test method calls the tests on the various resources.
     * @throws java.lang.Exception
     */
    public void testResources() {
        doTestStartPage();
        doTestResource1Page();
        doTestResource2Page();
        doTestResource3Page();
    }

    /**
     * Test that a request to the resource path "/start" redirects to the html page.
     */
    public void doTestStartPage() {
        int responseStatus = wr.path("resources").path("start")
                .accept(MediaType.TEXT_HTML).head().getStatus();
        assertEquals(200, responseStatus);
        String responseHtml = wr.path("resources").path("start")
                .accept(MediaType.TEXT_HTML).get(String.class);
        // check for the various components in the html page
        assertTrue(responseHtml.contains("Select Resource to test:"));
        assertTrue(responseHtml.contains("<select name=\"ressel\" id=\"resourceId\">"));
    }

    /**
     * Test that the request for resource 1 gives appropriate response.
     */
    public void doTestResource1Page() {
        int responseStatus = wr.path("resources").path("resource1")
                .accept(MediaType.TEXT_PLAIN).head().getStatus();
        assertEquals("Response status 200 not found for request to resource 1", 200, responseStatus);
        String responseText = wr.path("resources").path("resource1")
                .accept(MediaType.TEXT_PLAIN).get(String.class);
        String expectedText = "Hello World from resource 1 in servlet: 'Jersey Web Application', path: '/resources'";
        // check that the expected reponse is seen
        assertEquals("Expected response not seen for the GET on resource 1", expectedText, responseText);
    }

    /**
     * Test the the request for resource 2 gives the appropriate reponse.
     */
    public void doTestResource2Page() {
        int responseStatus = wr.path("resources").path("resource2")
                .accept(MediaType.TEXT_PLAIN).head().getStatus();
        assertEquals("Response status 200 not found for request to resource 2", 200, responseStatus);
        String responseText = wr.path("resources").path("resource2")
                .accept(MediaType.TEXT_PLAIN).get(String.class);
        String expectedText = "Hello World from resource 2";
        // check that the expected response is seen
        assertEquals("Expected response not seen for the GET on resource 2", expectedText, responseText);
    }

    /**
     * Test the request for resource 3 with different values for the query param "rep"
     * gives the appropriate response.
     */
    public void doTestResource3Page() {
        String arg1 = "firstArg";
        String arg2 = "secondArg";
        String expectedResponseWithRep0 = "<pre>Received args: ";
        String expectedResponseWithRep1 = "representation: StringRepresentation: arg1: ";
        
        //test with rep=0
        UriBuilder requestUriBuilder =  wr.path("resources").path("resource3")
                .path(arg1).path(arg2).getBuilder().queryParam("rep", 0);
        URI requestUri = requestUriBuilder.build();
        wr = c.resource(requestUri);
        int responseStatus = wr.head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep=0", 200, responseStatus);
        String responseText = wr.get(String.class);
        assertTrue("Expected reponse not seen with query param '?rep=0'",
                responseText.startsWith(expectedResponseWithRep0));

        // test with rep=1
        requestUriBuilder = requestUriBuilder.replaceQueryParam("rep", 1);
        requestUri = requestUriBuilder.build();
        wr = c.resource(requestUri);
        responseStatus = wr.head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep=1", 200, responseStatus);
        responseText = wr.get(String.class);
        assertTrue("Expected reponse not seen with query param '?rep=1'",
                responseText.startsWith(expectedResponseWithRep1));

        // test with rep=2
        requestUriBuilder = requestUriBuilder.replaceQueryParam("rep", 2);
        requestUri = requestUriBuilder.build();
        wr = c.resource(requestUri);
        responseStatus = wr.head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep=2", 200, responseStatus);        
        Form f = wr.get(Form.class);
        assertEquals("FormURLEncodedRepresentation", f.getFirst("representation"));
        assertEquals("Master Duke", f.getFirst("name"));
        assertEquals("male", f.getFirst("sex"));
        assertEquals("firstArg", f.getFirst("arg1"));
        assertEquals("secondArg", f.getFirst("arg2"));
        
        // test with rep>3
        requestUriBuilder = requestUriBuilder.replaceQueryParam("rep", 4);
        requestUri = requestUriBuilder.build();
        wr = c.resource(requestUri);
        responseStatus = wr.head().getStatus();
        assertEquals("Response status 200 not found for request to resource 3 with rep>3", 200, responseStatus);
        responseText = wr.get(String.class);
        assertTrue("Expected reponse not seen with query param 'rep>3'",
                responseText.startsWith(expectedResponseWithRep0));        
    }
}