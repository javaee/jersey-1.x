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
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:pavel.bucek@sun.com">Pavel Bucek</a>
 */
public class InjectionTest {
    @Test
    public void testCarResource() throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();

        System.out.println("**** CarResource Via @MatrixParam ***");
        HttpGet get = new HttpGet("http://localhost:9095/cars/matrix/mercedes/e55;color=black/2006");
        HttpResponse response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }

        System.out.println("**** CarResource Via PathSegment ***");
        get = new HttpGet("http://localhost:9095/cars/segment/mercedes/e55;color=black/2006");
        response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }

        System.out.println("**** CarResource Via PathSegments ***");
        get = new HttpGet("http://localhost:9095/cars/segments/mercedes/e55/amg/year/2006");
        response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }

        System.out.println("**** CarResource Via PathSegment ***");
        get = new HttpGet("http://localhost:9095/cars/uriinfo/mercedes/e55;color=black/2006");
        response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        System.out.println();
        System.out.println();

    }

    @Test
    public void testCustomerResource() throws Exception {
        DefaultHttpClient client = new DefaultHttpClient();

        System.out.println("**** CustomerResource No Query params ***");
        HttpGet get = new HttpGet("http://localhost:9095/customers");
        HttpResponse response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }

        System.out.println("**** CustomerResource With Query params ***");
        get = new HttpGet("http://localhost:9095/customers?start=1&size=3");
        response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }

        System.out.println("**** CustomerResource With UriInfo and Query params ***");
        get = new HttpGet("http://localhost:9095/customers/uriinfo?start=2&size=2");
        response = client.execute(get);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        reader = new BufferedReader(new
                InputStreamReader(response.getEntity().getContent()));

        line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
    }

    @Test
    public void testCarResourceJersey() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/cars");

        System.out.println("**** CarResource Via @MatrixParam ***");
        ClientResponse response = wr.path("matrix/mercedes/e55;color=black/2006").get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus()); // 200 = ok

        System.out.println(response.getEntity(String.class));

        System.out.println("**** CarResource Via PathSegment ***");
        response = wr.path("segment/mercedes/e55;color=black/2006").get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus()); // 200 = ok

        System.out.println(response.getEntity(String.class));


        System.out.println("**** CarResource Via PathSegments ***");
        response = wr.path("segments/mercedes/e55/amg/year/2006").get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus()); // 200 = ok

        System.out.println(response.getEntity(String.class));


        System.out.println("**** CarResource Via PathSegment ***");
        response = wr.path("uriinfo/mercedes/e55;color=black/2006").get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus()); // 200 = ok

        System.out.println(response.getEntity(String.class));
    }

    @Test
    public void testCustomerResourceJersey() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers");

        System.out.println("**** CustomerResource No Query params ***");
        ClientResponse response = wr.get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus()); // 200 = ok

        System.out.println(response.getEntity(String.class));

        System.out.println("**** CustomerResource With Query params ***");
        response = wr.queryParam("start", "1").queryParam("size", "3").get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus()); // 200 = ok

        System.out.println(response.getEntity(String.class));

        System.out.println("**** CustomerResource With UriInfo and Query params ***");
        // TODO there is probably some better way how to construct that url..
        response = c.resource("http://localhost:9095/customers/uriinfo?start=2&size=2").get(ClientResponse.class);
        Assert.assertEquals(200, response.getStatus());

        System.out.println(response.getEntity(String.class));
    }
}
