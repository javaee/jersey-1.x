/**
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 */

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
