
package com.sun.jersey.samples.hypermedia.server.model;

import com.sun.jersey.samples.hypermedia.server.model.adapters.CustomerAdapter;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Customers class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
public class Customers {

    private List<Customer> customers;

    @XmlJavaTypeAdapter(CustomerAdapter.class)
    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

}
