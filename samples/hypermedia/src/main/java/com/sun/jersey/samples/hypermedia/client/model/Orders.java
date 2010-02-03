
package com.sun.jersey.samples.hypermedia.client.model;

import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Orders class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
public class Orders {

    private List<URI> orders;

    public List<URI> getOrders() {
        return orders;
    }

    public void setOrders(List<URI> orders) {
        this.orders = orders;
    }

}
