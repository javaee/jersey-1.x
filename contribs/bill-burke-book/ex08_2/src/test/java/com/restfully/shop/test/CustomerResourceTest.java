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

import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;


/**
 * @author <a href="mailto:pavel.bucek@sun.com">Pavel Bucek</a>
 */
public class CustomerResourceTest {

    @Test
    public void testCustomerResource() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers/");

        // Get customer
        System.out.println("*** GET Customer (default) **");
        wr = wr.path("1"); // second customer
        ClientResponse response = wr.get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok
    }

    @Test
    public void testCustomerResourceMediaTypeMappingsTXT() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers/");

        // Get customer
        System.out.println("*** GET Customer as TXT **");
        wr = wr.path("1.txt"); // second customer
        ClientResponse response = wr.accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok
    }

    @Test
    public void testCustomerResourceMediaTypeMappingsHTML() throws Exception {
        Client c = new Client();
        WebResource wr = c.resource("http://localhost:9095/customers/");

        // Get customer
        System.out.println("*** GET Customer as TXT **");
        wr = wr.path("1.html"); // second customer
        ClientResponse response = wr.accept(MediaType.TEXT_HTML).get(ClientResponse.class);
        System.out.println("Content-Type: " + response.getHeaders().get("Content-Type"));

        System.out.println(response.getEntity(String.class));

        Assert.assertEquals(200, response.getStatus()); // 200 = ok
    }
}
