
package com.sun.jersey.samples.hypermedia.server.model.adapters;

import com.sun.jersey.samples.hypermedia.Main;
import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.Address;
import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * AddressAdapter class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class AddressAdapter extends XmlAdapter<URI, Address> {

    @Override
    public URI marshal(Address bt) throws Exception {
        return new URI(Main.BASE_URI.toString() 
                + "customers/" + bt.getCustomer().getId()
                + "/address/" + bt.getId());
    }

    /**
     * URI is http://.../customers/{id}/address/{id}
     */
    @Override
    public Address unmarshal(URI vt) throws Exception {
        String s = vt.toString();
        int last = s.length();
        int first = s.lastIndexOf('/');
        String addressId = s.substring(first, last);
        last = first;
        first = s.lastIndexOf('/', last);       // skip 'address'
        last = first;
        first = s.lastIndexOf('/', last);
        String customerId = s.substring(first, last);
        return DB.customers.get(customerId).getAddressById(addressId);
    }
}
