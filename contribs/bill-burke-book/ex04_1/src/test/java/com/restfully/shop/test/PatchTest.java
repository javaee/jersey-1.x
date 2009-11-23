/**
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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.client.apache.ApacheHttpClient;

import javax.ws.rs.core.MediaType;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:pavel.bucek@sun.com">Pavel Bucek</a>
 */
public class PatchTest {
    private static class HttpPatch extends HttpPost {
        public HttpPatch(String s) {
            super(s);
        }

        public String getMethod() {
            return "PATCH";
        }
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

        DefaultHttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost("http://localhost:9095/customers");
        StringEntity entity = new StringEntity(newCustomer);
        entity.setContentType("application/xml");
        post.setEntity(entity);
        HttpClientParams.setRedirecting(post.getParams(), false);
        HttpResponse response = client.execute(post);

        Assert.assertEquals(201, response.getStatusLine().getStatusCode());
        System.out.println("Location: " + response.getLastHeader("Location"));

        HttpPatch patch = new HttpPatch("http://localhost:9095/customers/1");

        // Update the new customer.  Change Bill's name to William
        String patchCustomer = "<customer>"
                + "<first-name>William</first-name>"
                + "</customer>";
        entity = new StringEntity(patchCustomer);
        entity.setContentType("application/xml");
        patch.setEntity(entity);
        response = client.execute(patch);

        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

        // Show the update
        System.out.println("**** After Update ***");
        HttpGet get = new HttpGet("http://localhost:9095/customers/1");
        response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        System.out.println("Content-Type: " + response.getEntity().getContentType());
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
    }

    @Test
    public void testCustomerResourceJersey() throws Exception {
        Client c = ApacheHttpClient.create();
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

        response = wr.type(MediaType.APPLICATION_XML).method("PATCH", ClientResponse.class, updateCustomer);

        Assert.assertEquals(204, response.getStatus()); // 204 = no content

        // Show the update
        System.out.println("**** After Update ***");
        response = wr.get(ClientResponse.class);
        System.out.println(response.getEntity(String.class));
        Assert.assertEquals(200, response.getStatus()); // 200 = ok
    }
}
