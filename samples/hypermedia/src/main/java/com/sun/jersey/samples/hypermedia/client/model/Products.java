
package com.sun.jersey.samples.hypermedia.client.model;

import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Products class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@XmlRootElement
public class Products {

    private List<URI> products;

    public List<URI> getProducts() {
        return products;
    }

    public void setProducts(List<URI> products) {
        this.products = products;
    }

}
