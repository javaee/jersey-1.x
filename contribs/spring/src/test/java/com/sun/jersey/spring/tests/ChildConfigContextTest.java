/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.spring.tests;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author paulsandoz
 */
@Test
public class ChildConfigContextTest extends AbstractTest {

    @Path("app")
    public static class ApplicationConfigResource {

        @GET
        public String get() {
            return "app";
        }
    }

    @Path("child")
    public static class ChildConfigResource {

        @GET
        public String get() {
            return "child";
        }
    }

    @Test
    public void testApplicationConfig() {
        start();

        WebResource r = resource("app");
        Assert.assertEquals("app", r.get(String.class));

        r = resource("child");

        ClientResponse cr = r.get(ClientResponse.class);
        Assert.assertEquals(404, cr.getStatus());
    }

    @Test
    public void testChildConfig() {
        String clientConfig =  this.getClass().getName();
        clientConfig = clientConfig.replace(".", "/") + "-client-config.xml";

        Map<String, String> m = new HashMap<String, String>();
        m.put(SpringServlet.CONTEXT_CONFIG_LOCATION, "classpath:" + clientConfig);
        start(m);

        WebResource r = resource("app");
        Assert.assertEquals("app", r.get(String.class));

        r = resource("child");
        Assert.assertEquals("child", r.get(String.class));
    }
}
