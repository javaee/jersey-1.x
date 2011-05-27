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

package com.sun.jersey.samples.storageservice;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Date;

/**
 *
 * @author Naresh (srinivas.bhimisetty@sun.com)
 */
public class MainTest {

    private HttpServer httpServer;

    private WebResource r;

    private Client c;

    @Before
    public void setUp() throws Exception {
        
        //start the Grizzly web container and create the client
        httpServer = Main.startServer();

        c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        
        httpServer.stop();
    }

    /**
     * Test checks that an application.wadl file is present for the resource.
     */
    @Test
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
    @Test
    public void testContainersResource() {
        Containers containers = r.path("containers").
                accept(MediaType.APPLICATION_XML).get(Containers.class);
        assertNotNull(containers);
    }

    /**
     * Test checks that containers and items could be added using PUT.
     * It also checks that the number of items in the container is the same
     * as the number which were added by PUT.
     */
    @Test
    public void testPutOnContainerAndItemResource() {
        // Create a child WebResource for the container "quotes"
        WebResource content = r.path("containers").path("quotes");

        // delete thc container
        content.delete(ClientResponse.class);

        // PUT the container "quotes"
        ClientResponse response = content.put(ClientResponse.class);
        assertEquals(Response.Status.CREATED, response.getResponseStatus());

        // PUT the items to be added to the "quotes" container
        response = content.path("1").type(MediaType.TEXT_PLAIN).put(ClientResponse.class,
                "Something is rotten in the state of Denmark");
        assertEquals(Response.Status.CREATED, response.getResponseStatus());

        response = content.path("2").type(MediaType.TEXT_PLAIN).put(ClientResponse.class,
                "I could be bounded in a nutshell");
        assertEquals(Response.Status.CREATED, response.getResponseStatus());

        response = content.path("3").type(MediaType.TEXT_PLAIN).put(ClientResponse.class,
                "catch the conscience of the king");
        assertEquals(Response.Status.CREATED, response.getResponseStatus());

        response = content.path("4").type(MediaType.TEXT_PLAIN).put(ClientResponse.class,
                "Get thee to a nunnery");
        assertEquals(Response.Status.CREATED, response.getResponseStatus());

        // check that there are four items in the container "quotes"
        Container container = content.accept(MediaType.APPLICATION_XML).get(Container.class);
        int numberOfItems = container.getItem().size();
        int expectedNumber = 4;
        assertEquals("Expected: " + expectedNumber + " items, Seeing: " + numberOfItems,
                expectedNumber, numberOfItems);

         //search the container for all items containing the word "king"
        URI searchUri = content.getUriBuilder().queryParam("search", "king").build();
        container = c.resource(searchUri).accept(MediaType.APPLICATION_XML).get(Container.class);
        numberOfItems = (container.getItem() == null) ? 0 : container.getItem().size();
        expectedNumber = 1;
        assertEquals("Expected: " + expectedNumber +
                " items which pass the search criterion, Seeing: " + numberOfItems,
                expectedNumber, numberOfItems);
    }


    @Test
    public void testUpdateItem3() {
        WebResource content = r.path("containers").path("quotes");

        content.path("1").type(MediaType.TEXT_PLAIN).put(
                "Something is rotten in the state of Denmark");
        content.path("2").type(MediaType.TEXT_PLAIN).put(
                "I could be bounded in a nutshell");
        content.path("3").type(MediaType.TEXT_PLAIN).put(
                "catch the conscience of the king");
        content.path("4").type(MediaType.TEXT_PLAIN).put(
                "Get thee to a nunnery");

        // Get the last modified and etag of item 3
        ClientResponse response = content.path("3").get(ClientResponse.class);
        Date lastModified = response.getLastModified();
        EntityTag etag = response.getEntityTag();

        // Check that a Not Modified response is returned
        response = content.path("3").
                header("If-Modified-Since", lastModified).
                header("If-None-Match", etag).
                get(ClientResponse.class);
        assertEquals(Response.Status.NOT_MODIFIED, response.getResponseStatus());

        // Update item 3
        content.path("3").type(MediaType.TEXT_PLAIN).put(
                "The play's the thing Wherein I'll catch the conscience of the king");

        // Check that a OK response is returned
        response = content.path("3").
                header("If-Modified-Since", lastModified).
                header("If-None-Match", etag).
                get(ClientResponse.class);
        assertEquals(Response.Status.OK, response.getResponseStatus());
        assertEquals("The play's the thing Wherein I'll catch the conscience of the king",
                response.getEntity(String.class));
    }

    /**
     * Test deletes the item 3, which is the only one which supposedly has the word "king"
     * and then searches for the word in the other items of the container.
     */
    @Test
    public void testDeleteItem3AndSearchForKing() {
        WebResource content = r.path("containers").path("quotes");

        content.path("1").type(MediaType.TEXT_PLAIN).put(
                "Something is rotten in the state of Denmark");
        content.path("2").type(MediaType.TEXT_PLAIN).put(
                "I could be bounded in a nutshell");
        content.path("3").type(MediaType.TEXT_PLAIN).put(
                "catch the conscience of the king");
        content.path("4").type(MediaType.TEXT_PLAIN).put(
                "Get thee to a nunnery");

        // delete item 3
        content.path("3").delete();

        //search the container for all items containing the word "king"
        URI searchUri = content.getUriBuilder().queryParam("search", "king").build();
        Container container = c.resource(searchUri).accept(MediaType.APPLICATION_XML).get(Container.class);
        int numberOfItems = (container.getItem() == null) ? 0 : container.getItem().size();
        int expectedNumber = 0;
        assertEquals("Expected: " + expectedNumber +
                " items which pass the search criterion, Seeing: " + numberOfItems,
                expectedNumber, numberOfItems);
    }

    /**
     * Test DELETEs the container "quotes" and sees that a 404 error is seen
     * on subsequent requests for the container.
     */
    @Test
    public void testDeleteContainerQuotes() {
        WebResource content = r.path("containers").path("quotes");

        content.put();

        // delete thc container
        content.delete();

        boolean caught = false;
        try {
            // A UniformInterfaceException will be throw because the "quotes"
            // container no longer exists
            content.get(String.class);
        } catch(UniformInterfaceException e) {
            caught = true;
            assertEquals("404 error not seen on trying to access deleted container",
                    Response.Status.NOT_FOUND, e.getResponse().getResponseStatus());
        }
        assertTrue("Expecting a UniformInterfaceException to be thrown", caught);
    }

}