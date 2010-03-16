
package com.sun.jersey.samples.hypermedia.server.controller;

import com.sun.jersey.samples.hypermedia.server.db.DB;
import com.sun.jersey.samples.hypermedia.server.model.*;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * ProductsResource class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
@Path("/products")
public class ProductsResource {

    @GET
    public Products getProducts() {
        ArrayList<Product> l = new ArrayList<Product>();
        for (Product product : DB.products.values()) {
            l.add(product);
        }
        // JAXB bean wrapper to use adapters
        Products products = new Products();
        products.setProducts(l);
        return products;
    }
    
}
