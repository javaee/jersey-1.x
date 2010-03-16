
package com.sun.jersey.samples.hypermedia.server.model.adapters;

import com.sun.jersey.samples.hypermedia.Main;
import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.Customer;
import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * CustomerAdapter class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class CustomerAdapter extends XmlAdapter<URI, Customer> {

    @Override
    public URI marshal(Customer bt) throws Exception {
        return new URI(Main.BASE_URI.toString() + "customers/" + bt.getId());
    }

    @Override
    public Customer unmarshal(URI vt) throws Exception {
        String s = vt.toString();
        int k = s.lastIndexOf('/');
        assert k > 0;
        return DB.customers.get(s.substring(k + 1));
    }
}
