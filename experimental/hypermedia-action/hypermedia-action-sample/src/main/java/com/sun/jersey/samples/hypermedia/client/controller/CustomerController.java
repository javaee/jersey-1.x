
package com.sun.jersey.samples.hypermedia.client.controller;

import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.samples.hypermedia.client.model.Customer;

/**
 * CustomerController class.
 * 
 * @author Santiago.PericasGeertsen@sun.com
 */
@HypermediaController(
    model=Customer.class,
    linkType=LinkType.LINK_HEADERS
    )
public interface CustomerController {

    // Returns client copy of model
    public Customer getModel();

    // Refreshes client copy of model
    @Action("refresh")
    public Customer refresh();

    // Updates server copy of model
    @Action("update")
    public void update(Customer customer);

    // Activate customer
    @Action("activate")
    public void activate();

    // Suspend customer
    @Action("suspend")
    public void suspend();

}
