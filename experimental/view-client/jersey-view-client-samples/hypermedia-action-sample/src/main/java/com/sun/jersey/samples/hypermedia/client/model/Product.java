
package com.sun.jersey.samples.hypermedia.client.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Product class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
@XmlType(propOrder={ "id", "description", "quantity", "status" })
public class Product {

    @XmlType(name="ProductState")
    public enum Status { IN_STOCK, OUT_OF_STOCK, BACKORDERED, DISCONTINUED };

    private String id;
    private String description;
    private int quantity;
    private Status status;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        status = (quantity > 0) ? Status.IN_STOCK : Status.OUT_OF_STOCK;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == Status.IN_STOCK) {
            assert quantity > 0;
        } else if (status == Status.OUT_OF_STOCK) {
            assert quantity == 0;
        }
        this.status = status;
    }

}

