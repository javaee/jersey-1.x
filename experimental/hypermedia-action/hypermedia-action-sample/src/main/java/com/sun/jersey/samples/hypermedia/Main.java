package com.sun.jersey.samples.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.samples.hypermedia.client.controller.CustomerController;
import com.sun.jersey.samples.hypermedia.client.controller.OrderController;
import com.sun.jersey.samples.hypermedia.client.model.Address;
import com.sun.jersey.samples.hypermedia.client.model.Order;
import com.sun.jersey.server.hypermedia.filter.HypermediaFilterFactory;
import org.glassfish.grizzly.http.server.HttpServer;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 * @author Santiago.PericasGeertsen@sun.com
 */
public class Main {

    public static final URI BASE_URI = UriBuilder.fromUri("http://localhost/").port(9998).build();

    public static HttpServer startServer() throws IOException {

        ResourceConfig rc = new PackagesResourceConfig(
                "com.sun.jersey.samples.hypermedia.server.controller");

        rc.getFeatures().put(ResourceConfig.FEATURE_FORMATTED, Boolean.TRUE);

        // Register HypermediaFilterFactory 
        rc.getResourceFilterFactories().add(HypermediaFilterFactory.class);

        System.out.println("Starting grizzly...");
        return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
    }

    public static void main(String[] args) throws Exception {
        // Grizzly 2 initialization
        HttpServer httpServer = startServer();

        try {
            // Create Client and configure it
            Client client = new Client();
            client.addFilter(new LoggingFilter());

            // Create proxy for order 1
            OrderController orderCtrl = client.view(
                    "http://localhost:9998/orders/1",
                    OrderController.class);

            // Approve order
            orderCtrl.review("approve");

            // Refresh order
            orderCtrl.refresh();

            // Create proxy for customer in order 1
            CustomerController customerCtrl = client.view(
                    orderCtrl.getModel().getCustomer(),
                    CustomerController.class);

            // Activate customer in order 1 (which was suspended)
            customerCtrl.activate();

            // Pay order
            orderCtrl.pay("123456789");

            // Ship order (returns new order)
            Address newAddress = new Address();
            newAddress.setCity("Springfield");
            newAddress.setStreet("Main");
            orderCtrl.ship(newAddress);
        }
        finally {
            httpServer.stop();
        }
    }

    private static void printOrder(Order order) {
        System.out.flush();
        System.out.println("### Order id: " + order.getId());
        System.out.println("### Order status: " + order.getStatus().name());
    }
}
