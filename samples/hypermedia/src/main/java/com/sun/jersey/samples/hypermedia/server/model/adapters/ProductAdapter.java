
package com.sun.jersey.samples.hypermedia.server.model.adapters;

import com.sun.jersey.samples.hypermedia.Main;
import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.Product;
import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * ProductAdapter class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class ProductAdapter extends XmlAdapter<URI, Product> {

    @Override
    public URI marshal(Product bt) throws Exception {
        return new URI(Main.BASE_URI.toString() + "products/" + bt.getId());
    }

    @Override
    public Product unmarshal(URI vt) throws Exception {
        String s = vt.toString();
        int k = s.lastIndexOf('/');
        assert k > 0;
        return DB.products.get(s.substring(k + 1));
    }
}
