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
package com.sun.jersey.samples.console;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.MediaTypes;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import junit.framework.TestCase;
import org.codehaus.jettison.json.JSONArray;

/**
 *
 * @author Naresh (srinivas.bhimisetty@sun.com)
 */
public class MainTest extends TestCase {

    private SelectorThread threadSelector;
    private WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        threadSelector = Main.startServer();

        Client c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        threadSelector.stopEndpoint();
    }

    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     */
    public void testApplicationWadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
        assertTrue(serviceWadl.length() > 0);
    }

    /**
     * Test if GET on the resource "/form" gives response with status code 200.
     */
    public void testGetOnForm() {
        ClientResponse response = r.path("form").accept(MediaType.TEXT_HTML)
                .get(ClientResponse.class);
        assertEquals("GET on the 'form' resource doesn't give expected response",
                Response.Status.OK, response.getResponseStatus());        
    }

    /**
     * Test checks that POST on the '/form' resource gives a reponse page
     * with the entered data.
     */
    public void testPostOnForm() {
        Form formData = new Form();
        formData.add("name", "testName");
        formData.add("colour", "red");
        formData.add("hint", "re");
        ClientResponse response = r.path("form").type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(ClientResponse.class, formData);
        assertEquals(Response.Status.OK, response.getResponseStatus());

        // check that the generated reponse is the expected one
        InputStream responseInputStream = response.getEntityInputStream();
        try {
            byte[] responseData = new byte[responseInputStream.available()];
            responseInputStream.read(responseData);
            assertTrue(new String(responseData).contains("Hello, you entered"));
        } catch (IOException ex) {
            Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test checks that a GET on the resource "/form/colours" with mime-type "text/html"
     * shows the appropriate colours based on the query param "match".
     */
    public void testGetColoursAsPlainText() {
        // without the query param "match"
        ClientResponse response = r.path("form").path("colours").accept(MediaType.TEXT_PLAIN)
                .get(ClientResponse.class);
        assertEquals("GET on path '/form/colours' with mime type 'text/html' doesn't give expected response",
                Response.Status.OK, response.getResponseStatus());
        String responseMsg = r.path("form").path("colours").accept(MediaType.TEXT_PLAIN)
                .get(String.class);
        assertEquals("Response content doesn't match the expected value",
                "red\norange\nyellow\ngreen\nblue\nindigo\nviolet\n", responseMsg);

        // with the query param "match" value "re"
        URI coloursUri = r.path("form").path("colours").getURI();
        WebResource coloursResource = r.uri(UriBuilder.fromUri(coloursUri).queryParam("match", "re").build());
        responseMsg = coloursResource.accept(MediaType.TEXT_PLAIN).get(String.class);
        assertEquals("Response content doesn't match the expected value with the query param 'match=re'",
                "red\ngreen\n", responseMsg);
    }

    /**
     * Test checks that a GET on the resource "/form/colours" with mime-type "application/json"
     * shows the appropriate colours based on the query param "match".
     */
    public void testGetColoursAsJson() {
        ClientResponse response = r.path("form").path("colours").accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertEquals("GET on path '/form/colours' with mime type 'application/json' doesn't give expected response",
                Response.Status.OK, response.getResponseStatus());
        JSONArray jsonArray = r.path("form").path("colours").accept(MediaType.APPLICATION_JSON)
                .get(JSONArray.class);
        assertEquals("Returned JSONArray doesn't have expected number of entries",
                7, jsonArray.length());

        // with the query param "match" value "re"
        URI coloursUri = r.path("form").path("colours").getURI();
        WebResource coloursResource = r.uri(UriBuilder.fromUri(coloursUri).queryParam("match", "re").build());
        jsonArray = coloursResource.accept(MediaType.APPLICATION_JSON).get(JSONArray.class);
        assertEquals("Returned JSONArray doesn't have expected number of entries with the query param 'match=re'",
                2, jsonArray.length());
    }

}