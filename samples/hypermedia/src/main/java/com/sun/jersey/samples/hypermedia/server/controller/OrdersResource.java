
package com.sun.jersey.samples.hypermedia.server.controller;

import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.*;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * OrdersResource class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@Path("/orders")
public class OrdersResource {

    @GET
    public Orders getOrders() {
        ArrayList<Order> l = new ArrayList<Order>();
        for (Order order : DB.orders.values()) {
            l.add(order);
        }
        // JAXB bean wrapper to use adapters
        Orders orders = new Orders();
        orders.setOrders(l);
        return orders;
    }
    
}
