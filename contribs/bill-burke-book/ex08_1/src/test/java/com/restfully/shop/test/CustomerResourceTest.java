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

        URL postUrl = new URL("http://localhost:9095/customers");
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


        // Get XML customer
        System.out.println("*** GET Customer as XML **");
        URL getUrl = new URL("http://localhost:9095/customers/1");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/xml");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        Assert.assertEquals("application/xml", connection.getContentType());

        BufferedReader reader = new BufferedReader(new
                InputStreamReader(connection.getInputStream()));

        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        connection.disconnect();


        // Get json
        System.out.println("*** GET Customer as JSON **");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        Assert.assertEquals("application/json", connection.getContentType());

        reader = new BufferedReader(new
                InputStreamReader(connection.getInputStream()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        connection.disconnect();

        // Get plaintext
        System.out.println("*** GET Customer as plain text **");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/plain");
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        Assert.assertEquals("text/plain", connection.getContentType());

        reader = new BufferedReader(new
                InputStreamReader(connection.getInputStream()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        connection.disconnect();
    }


    @Test
    public void testCustomerResourceJersey() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers");

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


        // Get XML customer
        System.out.println("*** GET Customer as XML **");
        wr = wr.path("2"); // second customer
        response = wr.accept(MediaType.APPLICATION_XML).get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok


        // Get json
        System.out.println("*** GET Customer as JSON **");
        response = wr.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok
        

        // Get plaintext
        System.out.println("*** GET Customer as plain text **");
        response = wr.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok


    }
}
