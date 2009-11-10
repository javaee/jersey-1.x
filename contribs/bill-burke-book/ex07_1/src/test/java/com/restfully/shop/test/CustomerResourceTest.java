package com.restfully.shop.test;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:pavel.bucek@sun.com">Pavel Bucek</a>
 */
public class CustomerResourceTest {
    @Test
    public void testCustomerResource() throws Exception {
        // Show the update
        System.out.println("**** Get Unknown Customer ***");
        URL getUrl = new URL("http://localhost:9095/customers/1");
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        try {
            int code = connection.getResponseCode();
        }
        catch (FileNotFoundException e) {
            System.out.println("Customer not found.");
        }
        connection.disconnect();
    }

    @Test
    public void testCustomerResourceJersey() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers/1");

        // Show the update
        System.out.println("**** Get Unknown Customer ***");
        ClientResponse response = wr.get(ClientResponse.class);

        if (response.getStatus() == 404) {
            System.out.println("Customer not found. Returned message: " + response.getEntity(String.class));
        } // else { ..
    }
}
