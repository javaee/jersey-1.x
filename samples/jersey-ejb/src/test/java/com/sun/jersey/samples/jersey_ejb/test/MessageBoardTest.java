/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.samples.jersey_ejb.test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.util.ApplicationDescriptor;
import java.net.URI;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author pavel.bucek@sun.com
 */
public class MessageBoardTest extends JerseyTest {

    public MessageBoardTest() throws Exception {
        super();
        ApplicationDescriptor appDescriptor = new ApplicationDescriptor();
                appDescriptor.setContextPath("/jersey-ejb");
                appDescriptor.setRootResourcePackageName("com.sun.jersey.samples.jersey_ejb.resources");
        super.setupTestEnvironment(appDescriptor);
    }

    @Test public void testDeployed() {
        String s = webResource.get(String.class);
        assertFalse(s.length() == 0);
    }

    @Test public void testAddMessage() {
        ClientResponse response = webResource.path("app/messages").post(ClientResponse.class, "hello world!");

        assertTrue(response.getClientResponseStatus() == ClientResponse.Status.CREATED);

        jerseyClient.resource(response.getLocation()).delete(); // remove added message
    }

    @Test public void testDeleteMessage() {
        URI u = webResource.getURI(); // just placeholder

        ClientResponse response = webResource.path("app/messages").post(ClientResponse.class, "toDelete");
        if(response.getClientResponseStatus() == ClientResponse.Status.CREATED) {
            u = response.getLocation();
        } else {
            assertTrue(false);
        }

        String s = jerseyClient.resource(u).get(String.class);

        assertTrue(s.contains("toDelete"));

        jerseyClient.resource(u).delete();

        boolean caught = false;

        try {
            s = jerseyClient.resource(u).get(String.class);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 404) {
                caught = true;
            }
        }

        assertTrue(caught);
    }
}

