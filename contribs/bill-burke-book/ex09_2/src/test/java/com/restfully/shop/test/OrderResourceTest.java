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

import com.restfully.shop.domain.Customer;
import com.restfully.shop.domain.LineItem;
import com.restfully.shop.domain.Link;
import com.restfully.shop.domain.Order;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class OrderResourceTest {
    protected Map<String, Link> processLinkHeaders(ClientResponse response) {
        List<String> linkHeaders = (List<String>) response.getHeaders().get("Link");
        Map<String, Link> links = new HashMap<String, Link>();
        for (String header : linkHeaders) {
            Link link = Link.valueOf(header);
            links.put(link.getRelationship(), link);
        }
        return links;
    }

    @Test
    public void testCreateCancelPurge() throws Exception {
        Client c = new Client();
        String url = "http://localhost:9095/shop";

        WebResource wr = c.resource(url);
        ClientResponse response = wr.head();
        Map<String, Link> shoppingLinks = processLinkHeaders(response);

        Link customers = shoppingLinks.get("customers");
        System.out.println("** Create a customer through this URL: " + customers.getHref());

        Customer customer = new Customer();
        customer.setFirstName("Bill");
        customer.setLastName("Burke");
        customer.setStreet("10 Somewhere Street");
        customer.setCity("Westford");
        customer.setState("MA");
        customer.setZip("01711");
        customer.setCountry("USA");

        wr = c.resource(customers.getHref());
        response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, customer);
        Assert.assertEquals(201, response.getStatus());

        Link orders = shoppingLinks.get("orders");

        Order order = new Order();
        order.setTotal("$199.99");
        order.setCustomer(customer);
        order.setDate(new Date().toString());
        LineItem item = new LineItem();
        item.setCost("$199.99");
        item.setProduct("iPhone");
        order.setLineItems(new ArrayList<LineItem>());
        order.getLineItems().add(item);

        System.out.println();
        System.out.println("** Create an order through this URL: " + orders.getHref());
        wr = c.resource(orders.getHref());
        response = wr.type(MediaType.APPLICATION_XML).post(ClientResponse.class, order);
        Assert.assertEquals(201, response.getStatus());
        String createdOrderUrl = (String) response.getHeaders().getFirst("Location");

        System.out.println();
        System.out.println("** New list of orders");
        wr = c.resource(orders.getHref());
        response = wr.get(ClientResponse.class);
        System.out.println(response.getEntity(String.class));
        Map<String, Link> ordersLinks = processLinkHeaders(response);

        wr = c.resource(createdOrderUrl);
        response = wr.head();
        Map<String, Link> orderLinks = processLinkHeaders(response);

        Link cancel = orderLinks.get("cancel");
        if (cancel != null) {
            System.out.println("** Canceling the order at URL: " + cancel.getHref());
            wr = c.resource(cancel.getHref());
            response = wr.post(ClientResponse.class);
            Assert.assertEquals(204, response.getStatus());
        }

        System.out.println();
        System.out.println("** New list of orders after cancel: ");
        wr = c.resource(orders.getHref());
        response = wr.get(ClientResponse.class);
        System.out.println(response.getEntity(String.class));

        System.out.println();
        Link purge = ordersLinks.get("purge");
        System.out.println("** Purge cancelled orders at URL: " + purge.getHref());
        wr = c.resource(purge.getHref());
        response = wr.post(ClientResponse.class);
        Assert.assertEquals(204, response.getStatus());

        System.out.println();
        System.out.println("** New list of orders after purge: ");
        wr = c.resource(orders.getHref());
        response = wr.get(ClientResponse.class);
        System.out.println(response.getEntity(String.class));
    }
}
