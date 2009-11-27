/*
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 */
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

    private String getJettyPort() {
        String port = System.getenv("JERSEY_HTTP_PORT");
        if(port != null)
            return port;

        else return "9095"; // default
    }
    
    @Test
    public void testCustomerResource() throws Exception {
        // Show the update
        System.out.println("**** Get Unknown Customer ***");
        URL getUrl = new URL("http://localhost:" + getJettyPort() + "/customers/1");
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
        WebResource wr = c.resource("http://localhost:" + getJettyPort() + "/customers/1");

        // Show the update
        System.out.println("**** Get Unknown Customer ***");
        ClientResponse response = wr.get(ClientResponse.class);

        if (response.getStatus() == 404) {
            System.out.println("Customer not found. Returned message: " + response.getEntity(String.class));
        } // else { ..
    }
}
