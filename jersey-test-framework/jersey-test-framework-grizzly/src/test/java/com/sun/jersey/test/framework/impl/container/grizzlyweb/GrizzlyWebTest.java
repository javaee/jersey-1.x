package com.sun.jersey.test.framework.impl.container.grizzlyweb;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author paulsandoz
 */
public class GrizzlyWebTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Path("root")
    public static class TestResource {
        @GET
        public String get() {
            return "GET";
        }

        @Path("sub")
        @GET
        public String getSub() {
            return "sub";
        }
    }    

    public GrizzlyWebTest() {
        super("com.sun.jersey.test.framework.impl.container.grizzlyweb");
    }

    @Test
    public void testGet() {
        WebResource r = resource().path("root");

        String s = r.get(String.class);
        Assert.assertEquals("GET", s);
    }

    @Test
    public void testGetSub() {
        WebResource r = resource().path("root/sub");

        String s = r.get(String.class);
        Assert.assertEquals("sub", s);
    }
}