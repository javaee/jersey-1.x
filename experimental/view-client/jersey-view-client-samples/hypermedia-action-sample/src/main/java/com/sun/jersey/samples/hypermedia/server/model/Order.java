
package com.sun.jersey.samples.hypermedia.server.model;

import com.sun.jersey.samples.hypermedia.server.model.adapters.AddressAdapter;
import com.sun.jersey.samples.hypermedia.server.model.adapters.CustomerAdapter;
import com.sun.jersey.samples.hypermedia.server.model.adapters.ProductAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Order class.
 * 
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
@XmlType(propOrder={"id", "customer", "shippingAddress",
                    "orderItems", "notes", "status" })
public class Order {

    @XmlType(name="OrderState")
    public enum Status { RECEIVED, REVIEWED, CANCELED, PAYED, SHIPPED };

    public static class OrderItem {
        Product product;
        int quantity;

        public OrderItem() {
        }

        public OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        @XmlJavaTypeAdapter(ProductAdapter.class)
        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    private String id;
    private Customer customer;
    private Address shippingAddress;
    private List<OrderItem> orderItems;
    private String notes;
    private Status status;

    @XmlJavaTypeAdapter(CustomerAdapter.class)
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    public List<OrderItem> getOrderItems() {
        if (orderItems == null) {
            orderItems = new ArrayList<OrderItem>();
        }
        return orderItems;
    }

    @XmlJavaTypeAdapter(AddressAdapter.class)
    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
