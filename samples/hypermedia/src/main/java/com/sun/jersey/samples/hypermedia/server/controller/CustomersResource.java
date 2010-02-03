
package com.sun.jersey.samples.hypermedia.server.controller;

import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.*;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * CustomersResource class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@Path("/customers")
public class CustomersResource {

    @GET
    public Customers getCustomers() {
        ArrayList<Customer> l = new ArrayList<Customer>();
        for (Customer customer : DB.customers.values()) {
            l.add(customer);
        }
        // JAXB bean wrapper to use adapters
        Customers customers = new Customers();
        customers.setCustomers(l);
        return customers;
    }
    
}
