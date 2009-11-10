package com.restfully.shop.test;

import com.restfully.shop.domain.Customer;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:pavel.bucek@sun.com">Pavel Bucek</a>
 */
public class CustomerResourceTest {
    @Test
    public void testCustomerResource() throws Exception {
        System.out.println("*** Create a new Customer ***");
        Customer cust = new Customer();
        cust.setFirstName("Bill");
        cust.setLastName("Burke");
        cust.setStreet("256 Clarendon Street");
        cust.setCity("Boston");
        cust.setState("MA");
        cust.setZip("02115");
        cust.setCountry("USA");

        URL postUrl = new URL("http://localhost:9095/customers");
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
        OutputStream os = connection.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(cust);
        oos.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());
        System.out.println("Location: " + connection.getHeaderField("Location"));
        connection.disconnect();


        // Get the new customer
        System.out.println("*** GET Created Customer **");
        URL getUrl = new URL("http://localhost:9095/customers/1");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");
        System.out.println("Content-Type: " + connection.getContentType());

        ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
        cust = (Customer) ois.readObject();
        System.out.println(cust);
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        connection.disconnect();

        // Update the new customer.  Change Bill's name to William
        cust.setFirstName("William");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/x-java-serialized-object");
        os = connection.getOutputStream();
        oos = new ObjectOutputStream(os);
        oos.writeObject(cust);
        oos.flush();
        Assert.assertEquals(HttpURLConnection.HTTP_NO_CONTENT, connection.getResponseCode());
        connection.disconnect();

        // Show the update
        System.out.println("**** After Update ***");
        connection = (HttpURLConnection) getUrl.openConnection();
        connection.setRequestMethod("GET");

        System.out.println("Content-Type: " + connection.getContentType());
        Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        ois = new ObjectInputStream(connection.getInputStream());
        cust = (Customer) ois.readObject();
        System.out.println(cust);
        connection.disconnect();
    }

    @Test
    public void testCustomerResourceJersey() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers");

        System.out.println("*** Create a new Customer ***");
        Customer cust = new Customer();
        cust.setFirstName("Bill");
        cust.setLastName("Burke");
        cust.setStreet("256 Clarendon Street");
        cust.setCity("Boston");
        cust.setState("MA");
        cust.setZip("02115");
        cust.setCountry("USA");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cust);
        oos.flush();

        ClientResponse response = wr.type("application/x-java-serialized-object").post(ClientResponse.class, baos.toByteArray());
        Assert.assertEquals(201, response.getStatus()); // 201 = created
        System.out.println("Location: " + response.getHeaders().get("Location"));


        // Get the new customer
        System.out.println("*** GET Created Customer **");
        wr = wr.path("2"); // second customer
        response = wr.get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        ObjectInputStream ois = new ObjectInputStream(response.getEntityInputStream());
        cust = (Customer) ois.readObject();

        Assert.assertEquals(200, response.getStatus()); // 200 = ok


        // Update the new customer.  Change Pavel's name to Petr
        cust.setFirstName("Petr");
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(cust);
        oos.flush();

        response = wr.type("application/x-java-serialized-object").put(ClientResponse.class, baos.toByteArray());

        Assert.assertEquals(204, response.getStatus()); // 204 = no content

        // Show the update
        System.out.println("**** After Update ***");
        response = wr.get(ClientResponse.class);
        ois = new ObjectInputStream(response.getEntityInputStream());
        cust = (Customer) ois.readObject();

        Assert.assertEquals(200, response.getStatus()); // 200 = ok
        System.out.println(cust);
    }
}
