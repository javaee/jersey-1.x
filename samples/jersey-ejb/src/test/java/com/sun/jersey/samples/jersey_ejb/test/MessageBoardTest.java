/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.samples.jersey_ejb.test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import java.net.URI;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author pavel.bucek@sun.com
 */
public class MessageBoardTest extends JerseyTest {

    public MessageBoardTest() throws Exception {
        super(new WebAppDescriptor.Builder("com.sun.jersey.samples.jersey_ejb.resources")
                .contextPath("jersey-ejb")
                .build());
    }

    @Test public void testDeployed() {
        WebResource webResource = resource();
        String s = webResource.get(String.class);
        assertFalse(s.length() == 0);
    }

    @Test public void testAddMessage() {
        WebResource webResource = resource();
        ClientResponse response = webResource.path("app/messages").post(ClientResponse.class, "hello world!");

        assertTrue(response.getClientResponseStatus() == ClientResponse.Status.CREATED);



        client().resource(response.getLocation()).delete(); // remove added message
    }

    @Test public void testDeleteMessage() {
        WebResource webResource = resource();
        URI u = webResource.getURI(); // just placeholder

        ClientResponse response = webResource.path("app/messages").post(ClientResponse.class, "toDelete");
        if(response.getClientResponseStatus() == ClientResponse.Status.CREATED) {
            u = response.getLocation();
        } else {
            assertTrue(false);
        }

        String s = client().resource(u).get(String.class);

        assertTrue(s.contains("toDelete"));

        client().resource(u).delete();

        boolean caught = false;

        try {
            s = client().resource(u).get(String.class);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 404) {
                caught = true;
            }
        }
        assertTrue(caught);

        caught = false;

        try {
            client().resource(u).delete();
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() == 404) {
                caught = true;
            }
        }
        assertTrue(caught);
    }
}

