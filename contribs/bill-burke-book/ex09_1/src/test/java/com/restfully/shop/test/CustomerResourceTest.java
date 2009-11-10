package com.restfully.shop.test;

import com.restfully.shop.domain.Customers;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.Test;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CustomerResourceTest {
    @Test
    public void testQueryCustomers() throws Exception {
        Client c = new Client();

        String url = "http://localhost:9095/customers";
        while (url != null) {

            WebResource wr = c.resource(url);

            String output = wr.get(String.class);

            System.out.println("** XML from " + url);
            System.out.println(output);

            Customers customers = wr.get(Customers.class);
            url = customers.getNext();
        }
    }
}
