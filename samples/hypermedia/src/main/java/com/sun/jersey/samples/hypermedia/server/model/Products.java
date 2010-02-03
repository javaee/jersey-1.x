
package com.sun.jersey.samples.hypermedia.server.model;

import com.sun.jersey.samples.hypermedia.server.model.adapters.ProductAdapter;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Products class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
public class Products {

    private List<Product> products;

    @XmlJavaTypeAdapter(ProductAdapter.class)
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

}
