
package com.sun.jersey.samples.hypermedia.client.model;

import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Customers class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
public class Customers {

    private List<URI> customers;

    public List<URI> getCustomers() {
        return customers;
    }

    public void setCustomers(List<URI> customers) {
        this.customers = customers;
    }

}
