
package com.sun.jersey.samples.hypermedia.server.controller;

import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.ContextualActionSet;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.*;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import static com.sun.jersey.samples.hypermedia.server.model.Order.Status.*;

/**
 * OrderResource class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@Path("/orders/{id}")
@HypermediaController(
    model=Order.class,
    linkType=LinkType.LINK_HEADERS
    )
public class OrderResource {

    private Order order;

    public OrderResource(@PathParam("id") String id) {
        order = DB.orders.get(id);
        if (order == null) {
            throw new WebApplicationException(404);     // not found
        }
    }

    @GET
    @Produces("application/xml")
    public Order getOrder(@PathParam("id") String id) {      
        return order;
    }

    @PUT
    @Consumes("application/xml")
    public void putOrder(@PathParam("id") String id, Order order) {
        assert id.equals(order.getId());
        this.order = order;
        DB.orders.put(id, order);
    }

    // -- Actions and ActionSets ------------------------------------
    //
    // Set an order's state as REVIEWED, PAYED, SHIPPED or CANCELED.
    // For simplicity, these actions are implemented by updating the
    // order's status. Note that this could be done also using
    // @PUT. In general, these actions may involve several steps (a
    // workflow) that cannot be easily translated into a single @PUT
    // action by the client.
    //

    @GET
    @Action("refresh") @Path("refresh")
    @Produces("application/xml")
    public Order refresh(@PathParam("id") String id) {
        return getOrder(id);
    }

    @PUT
    @Action("update") @Path("update")
    @Consumes("application/xml")
    public void update(@PathParam("id") String id, Order o) {
        putOrder(id, o);
    }

    @POST
    @Action("review") @Path("review")
    public void review(@HeaderParam("notes") String notes) {
        // Do something with notes
        order.setStatus(REVIEWED);
    }

    @POST
    @Action("pay") @Path("pay")
    @Consumes("text/plain")
    public void pay(@QueryParam("newCardNumber") String newCardNumber) {
        // Do something with newCardNumber
        order.setStatus(PAYED);
    }

    @PUT
    @Action("ship") @Path("ship")
    @Produces("application/xml")
    @Consumes("application/xml")
    public Order ship(Address newShippingAddress) {
        // Do something with newShippingAddress
        order.setStatus(SHIPPED);
        return order;
    }

    @POST
    @Action("cancel") @Path("cancel")
    public void cancel(@QueryParam("notes") String notes) {
        // Do something with notes
        order.setStatus(CANCELED);
    }

    /**
     * Order Version 1.
     */
    @ContextualActionSet
    public Set<String> getContextualActionSet() {
        Set<String> result = new HashSet<String>();
        result.add("refresh");
        switch (order.getStatus()) {
            case RECEIVED:
                result.add("review");       // @Action's value
                result.add("cancel");       // @Action's value
                result.add("update");       // @Action's value
                break;
            case REVIEWED:
                result.add("cancel");       // @Action's value
                result.add("pay");          // @Action's value
                result.add("update");       // @Action's value
                break;
            case CANCELED:
                break;
            case PAYED:
                result.add("ship");         // @Action's value
                result.add("update");       // @Action's value
                break;
            case SHIPPED:
                break;
        }
        return result;
    }
}
