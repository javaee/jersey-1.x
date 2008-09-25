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

package com.sun.jersey.samples.storageservice;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.MediaTypes;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import junit.framework.TestCase;

/**
 *
 * @author Naresh (srinivas.bhimisetty@sun.com)
 */
public class MainTest extends TestCase {

    private SelectorThread threadSelector;

    private WebResource r;

    private Client c;

    private int SUCCESS_STATUS_CODE = 200;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //start the Grizzly web container and create the client
        threadSelector = Main.startServer();

        c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        threadSelector.stopEndpoint();
    }

    /**
     * Test checks that an application.wadl file is present for the resource.
     */
    public void testApplicationWadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
        assertTrue("Looks like the expected wadl is not generated",
                serviceWadl.length() > 0);
    }

    /**
     * Test checks that an xml content is shown for the client request to
     * resource "containers".
     */
    public void testContainersResource() {
        String serviceXml = r.path("containers").
                accept(MediaType.APPLICATION_XML).get(String.class);
        assertTrue("Looks like the given xml response is not the expected one",
                serviceXml.length() > 0);
    }

    /**
     * Test checks that containers and items could be added using PUT.
     * It also checks that the number of items in the container is the same
     * as the number which were added by PUT.
     */
    public void testPutOnContainerAndItemResource() {
        // Create a child WebResource for the container "quotes"
        WebResource content = r.path("containers").path("quotes");

        //PUT the container "quotes"
        ClientResponse response = content.put(ClientResponse.class);
        int responseStatusCode = response.getStatus();
        assertTrue("Expected HTTP response code not seen. Seeing: " + responseStatusCode,
                (responseStatusCode - SUCCESS_STATUS_CODE) >= 0 && (responseStatusCode - SUCCESS_STATUS_CODE) <= 4);

        // the items to be added to the container
        String item1 = "Something is rotten in the state of Denmark";
        String item2 = "I could be bounded in a nutshell";
        String item3 = "catch the conscience of the king";
        String item4 = "Get thee to a nunnery";
        //PUT the items in the container
        response = content.path("1").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item1);
        responseStatusCode = response.getStatus();
        assertTrue("Expected HTTP response code not seen for item 1. Seeing: " + responseStatusCode,
                (responseStatusCode - SUCCESS_STATUS_CODE) >= 0 && (responseStatusCode - SUCCESS_STATUS_CODE) <= 4);

        response = content.path("2").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item2);
        responseStatusCode = response.getStatus();
        assertTrue("Expected HTTP response code not seen for item 2. Seeing: " + responseStatusCode,
                (responseStatusCode - SUCCESS_STATUS_CODE) >= 0 && (responseStatusCode - SUCCESS_STATUS_CODE) <= 4);

        response = content.path("3").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item3);
        responseStatusCode = response.getStatus();
        assertTrue("Expected HTTP response code not seen for item 3. Seeing: " + responseStatusCode,
                (responseStatusCode - SUCCESS_STATUS_CODE) >= 0 && (responseStatusCode - SUCCESS_STATUS_CODE) <= 4);

        response = content.path("4").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item4);
        responseStatusCode = response.getStatus();
        assertTrue("Expected HTTP response code not seen for item 4. Seeing: " + responseStatusCode,
                (responseStatusCode - SUCCESS_STATUS_CODE) >= 0 && (responseStatusCode - SUCCESS_STATUS_CODE) <= 4);

        // check that there are four items in the container "quotes"
        Container container = content.accept(MediaType.APPLICATION_XML).get(Container.class);
        int numberOfItems = container.getItem().size();
        int expectedNumber = 4;
        assertEquals("Expected: " + expectedNumber + " items, Seeing: " + numberOfItems,
                expectedNumber, numberOfItems);

         //search the container for all items containing the word "king"
        URI searchUri = content.getBuilder().queryParam("search", "king").build();
        try {
            System.out.println(searchUri.toURL().toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        container = c.resource(searchUri).accept(MediaType.APPLICATION_XML).get(Container.class);
        numberOfItems = (container.getItem() == null)?0:container.getItem().size();
        expectedNumber = 1;
        assertEquals("Expected: " + expectedNumber + " items which pass the search criterion, Seeing: " + numberOfItems,
                expectedNumber, numberOfItems);

        
    }


    /**
     * Test deletes the item 3, which is the only one which supposedly has the word "king"
     * and then searches for the word in the other items of the container.
     */
    public void testDeleteItem3AndSearchForKing() {
        // Create a child WebResource for the container "quotes"
        WebResource content = r.path("containers").path("quotes");

        //PUT the container "quotes"
        ClientResponse response = content.put(ClientResponse.class);
        int responseStatusCode = response.getStatus();
        assertTrue("Expected HTTP response code not seen. Seeing: " + responseStatusCode,
                (responseStatusCode - SUCCESS_STATUS_CODE) >= 0 && (responseStatusCode - SUCCESS_STATUS_CODE) <= 4);

        // the items to be added to the container
        String item1 = "Something is rotten in the state of Denmark";
        String item2 = "I could be bounded in a nutshell";
        String item3 = "catch the conscience of the king";
        String item4 = "Get thee to a nunnery";
        //PUT the items in the container
        response = content.path("1").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item1);
        response = content.path("2").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item2);
        response = content.path("3").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item3);
        response = content.path("4").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item4);

        // delete item 3
        response = content.path("3").delete(ClientResponse.class);
        
        //search the container for all items containing the word "king"
        URI searchUri = content.getBuilder().queryParam("search", "king").build();
        Container container = c.resource(searchUri).accept(MediaType.APPLICATION_XML).get(Container.class);
        int numberOfItems = (container.getItem() == null)?0:container.getItem().size();
        int expectedNumber = 0;
        assertEquals("Expected: " + expectedNumber + " items which pass the search criterion, Seeing: " + numberOfItems,
                expectedNumber, numberOfItems);        
    }

    /**
     * Test DELETEs the container "quotes" and sees that a 404 error is seen
     * on subsequent requests for the container.
     */
    public void testDeleteContainerQuotes() {
        // Create a child WebResource for the container "quotes"
        WebResource content = r.path("containers").path("quotes");

        //PUT the container "quotes"
        ClientResponse response = content.put(ClientResponse.class);
        int responseStatusCode = response.getStatus();
        assertTrue("Expected HTTP response code not seen. Seeing: " + responseStatusCode,
                (responseStatusCode - SUCCESS_STATUS_CODE) >= 0 && (responseStatusCode - SUCCESS_STATUS_CODE) <= 4);

        // the items to be added to the container
        String item1 = "Something is rotten in the state of Denmark";
        String item2 = "I could be bounded in a nutshell";
        String item3 = "catch the conscience of the king";
        String item4 = "Get thee to a nunnery";
        //PUT the items in the container
        response = content.path("1").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item1);
        response = content.path("2").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item2);
        response = content.path("3").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item3);
        response = content.path("4").type(MediaType.TEXT_PLAIN).put(ClientResponse.class, item4);

        // delete thc container
        content.delete(ClientResponse.class);
        response = content.get(ClientResponse.class);
        assertEquals("404 error not seen on trying to access deleted container",
                404, response.getStatus());
    }
    
}