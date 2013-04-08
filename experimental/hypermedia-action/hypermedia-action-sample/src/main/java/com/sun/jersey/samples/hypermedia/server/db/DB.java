/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.samples.hypermedia.server.db;

import com.sun.jersey.samples.hypermedia.server.model.*;
import java.util.HashMap;
import java.util.Map;

public class DB {

    static public Map<String, Order> orders = new HashMap<String, Order>();
    static public Map<String, Customer> customers = new HashMap<String, Customer>();
    static public Map<String, Product> products = new HashMap<String, Product>();

    static {
        Address address = new Address();
        address.setId("1");
        address.setNumber("1");
        address.setStreet("Network Drive");
        address.setCity("Burlington");
        address.setState("MA");
        address.setCity("USA");

        Customer customer = new Customer();
        customer.setId("21");
        customer.setName("John");
        customer.getAddresses().add(address);
        address.setCustomer(customer);      // for context
        customer.setCardNumber("12345678");
        customer.setStatus(Customer.Status.SUSPENDED);
        customers.put(customer.getId(), customer);

        Product product = new Product();
        product.setId("3345");
        product.setDescription("Cold Air Intake");
        product.setQuantity(5);
        product.setStatus(Product.Status.IN_STOCK);
        products.put(product.getId(), product);

        Order order = new Order();
        order.setId("1");
        order.setCustomer(customer);
        order.getOrderItems().add(
                new Order.OrderItem(product, 1));
        order.setShippingAddress(address);
        order.setStatus(Order.Status.RECEIVED);
        orders.put(order.getId(), order);

        order = new Order();
        order.setId("2");
        order.setCustomer(customer);
        order.getOrderItems().add(
                new Order.OrderItem(product, 2));
        order.setShippingAddress(address);
        order.setStatus(Order.Status.PAYED);
        orders.put(order.getId(), order);
    }
}
