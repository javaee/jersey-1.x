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

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;

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
        System.out.println("*** Create a new Customer ***");
        // Create a new customer
        String newCustomer = "<customer>"
                + "<first-name>Bill</first-name>"
                + "<last-name>Burke</last-name>"
                + "<street>256 Clarendon Street</street>"
                + "<city>Boston</city>"
                + "<state>MA</state>"
                + "<zip>02115</zip>"
                + "<country>USA</country>"
                + "</customer>";

        URL postUrl = new URL("http://localhost:" + getJettyPort() + "/customers");
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");
        OutputStream os = connection.getOutputStream();
        os.write(newCustomer.getBytes());
        os.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());
        System.out.println("Location: " + connection.getHeaderField("Location"));
        connection.disconnect();


        // Get the new customer
        System.out.println("*** GET Created Customer **");
        URL getUrl = new URL("http://localhost:" + getJettyPort() + "/customers/1");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        System.out.println("Content-Type: " + connection.getContentType());

        BufferedReader reader = new BufferedReader(new
                InputStreamReader(connection.getInputStream()));

        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

        // Update the new customer.  Change Bill's name to William
        String updateCustomer = "<customer>"
                + "<first-name>William</first-name>"
                + "<last-name>Burke</last-name>"
                + "<street>256 Clarendon Street</street>"
                + "<city>Boston</city>"
                + "<state>MA</state>"
                + "<zip>02115</zip>"
                + "<country>USA</country>"
                + "</customer>";
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/xml");
        os = connection.getOutputStream();
        os.write(updateCustomer.getBytes());
        os.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        connection.disconnect();

        // Show the update
        System.out.println("**** After Update ***");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");

        System.out.println("Content-Type: " + connection.getContentType());
        reader = new BufferedReader(new
                InputStreamReader(connection.getInputStream()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();
    }

    @Test
    public void testCustomerResourceJersey() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:" + getJettyPort() + "/customers");

        System.out.println("*** Create a new Customer ***");
        // Create a new customer
        String newCustomer = "<customer>"
                + "<first-name>Pavel</first-name>"
                + "<last-name>Bucek</last-name>"
                + "<street>Top Secret 123</street>"
                + "<city>Prague</city>"
                + "<state>N/A</state>"
                + "<zip>12000</zip>"
                + "<country>Czech Republic</country>"
                + "</customer>";

        ClientResponse response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, newCustomer);

        Assert.assertEquals(201, response.getStatus()); // 201 = created
        System.out.println("Location: " + response.getHeaders().get("Location"));

        // Get the new customer
        System.out.println("*** GET Created Customer **");
        wr = wr.path("2"); // second customer
        response = wr.get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok

        // Update the new customer.  Change Bill's name to William
        String updateCustomer = "<customer>"
                + "<first-name>Petr</first-name>"
                + "<last-name>Bucek</last-name>"
                + "<street>Top Secret 123</street>"
                + "<city>Prague</city>"
                + "<state>N/A</state>"
                + "<zip>12000</zip>"
                + "<country>Czech Republic</country>"
                + "</customer>";

        response = wr.type(MediaType.APPLICATION_XML).put(ClientResponse.class, updateCustomer);

        Assert.assertEquals(204, response.getStatus()); // 204 = no content

        // Show the update
        System.out.println("**** After Update ***");
        response = wr.get(ClientResponse.class);
        System.out.println(response.getEntity(String.class));
        Assert.assertEquals(200, response.getStatus()); // 200 = ok
    }
}
