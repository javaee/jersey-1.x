
package com.sun.jersey.samples.hypermedia.server.model;

import com.sun.jersey.samples.hypermedia.server.model.adapters.AddressAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Customer class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
@XmlType(propOrder={ "id", "name", "addresses", "cardNumber", "status" })
public class Customer {

    @XmlType(name="CustomerState")
    public enum Status { ACTIVE, SUSPENDED };

    private String id;
    private String name;
    private List<Address> addresses;
    private String cardNumber;
    private Status status;

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
    
    @XmlJavaTypeAdapter(AddressAdapter.class)
    public List<Address> getAddresses() {
        if (addresses == null) {
            addresses = new ArrayList<Address>();
        }
        return addresses;
    }

    public Address getAddressById(String id) {
        if (addresses != null) {
            for (Address a : addresses) {
                if (a.getId().equals(id)) return a;
            }
        }
        return null;
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
