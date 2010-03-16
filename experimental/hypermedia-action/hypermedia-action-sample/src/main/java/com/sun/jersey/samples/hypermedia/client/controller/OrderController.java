
package com.sun.jersey.samples.hypermedia.client.controller;

import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.core.hypermedia.Name;
import com.sun.jersey.samples.hypermedia.client.model.Address;
import com.sun.jersey.samples.hypermedia.client.model.Order;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * OrderController class.
 * 
 * @author Santiago.PericasGeertsen@sun.com
 */
@HypermediaController(
    model=Order.class,
    linkType=LinkType.LINK_HEADERS
    )
public interface OrderController {

    /**
     * Returns client copy of model from local cache.
     */
    public Order getModel();

    /**
     * Refreshes client copy of model in local cache. Uses
     * @Produces to select a specific representation.
     */
    @Action("refresh")
    @Produces("application/xml")
    public Order refresh();

    /**
     * Updates server copy of model using local cache. Unannotated
     * parameter <code>newOrder</code> becomes the request's entity.
     * Uses @Consumes to indicate how to serialize order.
     */
    @Action("update")
    @Consumes("application/xml")
    public void update(Order newOrder);

    /**
     * Sets status to REVIEWED and adds notes. Parameter
     * <code>notes</code> is mapped dynamically using WADL.
     */
    @Action("review")
    public void review(@Name("notes") String notes);

    /**
     * Set status to PAID and optionally use new card number.
     * Parameter <code>newCardNumber</code> is mapped statically
     * using @QueryParam. HTTP method is statically set to @POST
     * and must match the HTTP method in the link header.
     */
    @POST
    @Action("pay")
    public void pay(@QueryParam("newCardNumber") String newCardNumber);

    /**
     * Sets status to shipped and optionally specify new address.
     * Unannotated parameter <code>newShippingAddress</code>
     * becomes the request's entity.
     */
    @Action("ship")
    public Order ship(Address newShippingAddress);

    /**
     * Cancels an order and adds notes. Parameter
     * <code>notes</code> is mapped dynamically using WADL.
     */
    @Action("cancel")
    public void cancel(@Name("notes") String notes);

}
