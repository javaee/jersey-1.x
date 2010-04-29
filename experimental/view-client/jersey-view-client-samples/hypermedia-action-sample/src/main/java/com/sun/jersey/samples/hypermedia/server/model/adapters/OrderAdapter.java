
package com.sun.jersey.samples.hypermedia.server.model.adapters;

import com.sun.jersey.samples.hypermedia.Main;
import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.Order;
import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * OrderAdapter class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class OrderAdapter extends XmlAdapter<URI, Order> {

    @Override
    public URI marshal(Order or) throws Exception {
        return new URI(Main.BASE_URI.toString() 
                + "orders/" + or.getId());
    }

    @Override
    public Order unmarshal(URI vt) throws Exception {
        String s = vt.toString();
        int k = s.lastIndexOf('/');
        assert k > 0;
        return DB.orders.get(s.substring(k + 1));
    }
}
