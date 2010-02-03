
package com.sun.jersey.samples.hypermedia.client.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
        URI product;
        int quantity;

        public OrderItem() {
        }

        public OrderItem(URI product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public URI getProduct() {
            return product;
        }

        public void setProduct(URI product) {
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
    private URI customer;
    private URI shippingAddress;
    private List<OrderItem> orderItems;
    private String notes;
    private Status status;

    public URI getCustomer() {
        return customer;
    }

    public void setCustomer(URI customer) {
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

    public URI getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(URI shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
