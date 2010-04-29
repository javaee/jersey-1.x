package com.sun.jersey.samples.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.api.client.WebResourceLinkHeaders;
import com.sun.jersey.client.view.annotation.Status;
import com.sun.jersey.samples.hypermedia.client.model.Address;
import com.sun.jersey.samples.hypermedia.client.model.Order;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;

public class OrderView {

    private Order order;

    private Client c;

    private ViewResource r;

    private WebResourceLinkHeaders links;

    @GET
    @Status(200)
    @Consumes("*/*")
    public void build(Order order, 
            @Context Client c,
            @Context ViewResource r,
            @Context WebResourceLinkHeaders links) {
        this.order = order;
        this.c = c;
        this.r = r;
        this.links = links;
    }

    
    public Order getOrder() {
        return order;
    }

    // operations

    public OrderView update(Order o) {
        return r.put(new Update<OrderView>(OrderView.class, r), o).
                view();
    }

    // relation
    
    public CustomerView getCustomer() {
        return c.view(getOrder().getCustomer(), CustomerView.class);
    }
    
    // actions

    public OrderView review(String notes) {
        return links.viewResource("review").
                header("notes", notes).
                post(new Refresh<OrderView>(OrderView.class)).
                view();
    }

    public OrderView pay(String newCardNumber) {
        return links.viewResource("pay").
                queryParam("newCardNumber", newCardNumber).
                post(new Refresh<OrderView>(OrderView.class)).
                view();
    }

    public OrderView ship(Address newShippingAddress) {
        return links.viewResource("ship").
                put(new Refresh<OrderView>(OrderView.class), newShippingAddress).
                view();
    }

    public OrderView cancel(String notes) {
        return links.viewResource("cancel").
                queryParam("notes", notes).
                post(new Refresh<OrderView>(OrderView.class)).
                view();
    }
}