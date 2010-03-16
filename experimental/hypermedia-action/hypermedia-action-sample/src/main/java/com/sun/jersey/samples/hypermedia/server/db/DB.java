
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
