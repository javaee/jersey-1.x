package com.sun.jersey.samples.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.api.client.WebResourceLinkHeaders;
import com.sun.jersey.client.view.annotation.Status;
import com.sun.jersey.samples.hypermedia.client.model.Customer;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

public class CustomerView {

    private Customer customer;

    private Client c;

    private ViewResource r;

    private WebResourceLinkHeaders links;

    @GET
    @Status(200)
    @Consumes("*/*")
    public void build(Customer customer,
            @Context Client c,
            @Context ViewResource r,
            @Context WebResourceLinkHeaders links) {
        this.customer = customer;
        this.c = c;
        this.r = r;
        this.links = links;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isActive() {
        return customer.getStatus() == Customer.Status.ACTIVE;
    }
    
    // operations
    
    public CustomerView update(Customer customer) {
        return r.put(new Update<CustomerView>(CustomerView.class, r), customer).
                view();

    }

    // transitions

    public CustomerView activate() {
        return links.viewResource("activate").
                post(new Refresh<CustomerView>(CustomerView.class)).
                view();
    }

    public CustomerView suspend() {
        return links.viewResource("suspend").
                post(new Refresh<CustomerView>(CustomerView.class)).
                view();
    }
}