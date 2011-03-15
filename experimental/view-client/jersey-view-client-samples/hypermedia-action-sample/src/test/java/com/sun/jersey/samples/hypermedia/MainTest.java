
package com.sun.jersey.samples.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;
import junit.framework.TestCase;
import org.glassfish.grizzly.http.server.HttpServer;

public class MainTest extends TestCase {

    private HttpServer httpServer;
    
    private WebResource r;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        httpServer = Main.startServer();

        Client c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        httpServer.stop();
    }

    public void testOrder() {
        String responseMsg = r.path("orders/1").get(String.class);
        System.out.println(responseMsg);
    }

    public void testOrderWadl() {
        String responseMsg = r.path("orders/1/review").options(String.class);
        System.out.println(responseMsg);
    }

    public void testCustomer() {
        String responseMsg = r.path("customers/21").get(String.class);
        System.out.println(responseMsg);
    }

    public void testCustomerAddress() {
        String responseMsg = r.path("customers/21/address/1").get(String.class);
        System.out.println(responseMsg);
    }

    public void testApplicationWadl1() {
        String serviceWadl = 
                r.path("orders").path("1").path("ship")
                .accept(MediaTypes.WADL).options(String.class);
        System.out.println("-------------");
        System.out.println(serviceWadl);
        System.out.println("-------------");
    }

}
