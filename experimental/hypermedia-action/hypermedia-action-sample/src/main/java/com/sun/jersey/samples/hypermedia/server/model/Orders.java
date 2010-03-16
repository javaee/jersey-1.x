
package com.sun.jersey.samples.hypermedia.server.model;

import com.sun.jersey.samples.hypermedia.server.model.adapters.OrderAdapter;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Orders class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
public class Orders {

    private List<Order> orders;

    @XmlJavaTypeAdapter(OrderAdapter.class)
    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

}
