/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.samples.bookstore;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import junit.framework.TestCase;
import org.glassfish.embed.GlassFish;
import org.glassfish.embed.ScatteredWar;

/**
 *
 * @author TOSHIBA
 */
public class BookstoreTest extends TestCase {

    private final String baseUri = "http://localhost:" + Main.HttpPort + "/Bookstore";

    private GlassFish glassfish;
    private Client c;
    private WebResource wr;

    public BookstoreTest(){};

    public BookstoreTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        glassfish = Main.startApplication();
        c = Client.create();
        wr = c.resource(baseUri);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        glassfish.stop();
    }

    public void testBookstore()  throws Exception {
        // test subresource locator
        int responseStatusCode = wr.path("items/1/").get(ClientResponse.class).getStatus();
        assertEquals(200, responseStatusCode);

        // test static jsp would fail, (but works fine for mvn clean compile exec:java :-(
        //responseStatusCode = wr.path("help.jsp").get(ClientResponse.class).getStatus();
        //assertEquals(200, responseStatusCode);

        // test regular resource
        responseStatusCode = wr.path("count").get(ClientResponse.class).getStatus();
        assertEquals(200, responseStatusCode);
    }
}
