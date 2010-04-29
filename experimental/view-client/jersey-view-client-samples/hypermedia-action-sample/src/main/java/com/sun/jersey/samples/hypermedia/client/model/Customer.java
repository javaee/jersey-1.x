
package com.sun.jersey.samples.hypermedia.client.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Customer class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
@XmlType(propOrder={ "id", "name", "address", "cardNumber", "status" })
public class Customer {

    @XmlType(name="CustomerState")
    public enum Status { ACTIVE, SUSPENDED };

    private String id;
    private String name;
    private List<URI> address;
    private String cardNumber;
    private Status status;

    public void setAddress(List<URI> address) {
        this.address = address;
    }
    
    public List<URI> getAddress() {
        if (address == null) {
            address = new ArrayList<URI>();
        }
        return address;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
