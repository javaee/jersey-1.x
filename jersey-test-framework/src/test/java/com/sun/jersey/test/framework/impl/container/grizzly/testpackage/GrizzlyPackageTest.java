package com.sun.jersey.test.framework.impl.container.grizzly.testpackage;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author paulsandoz
 */
public class GrizzlyPackageTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
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

    public GrizzlyPackageTest() {
        super("com.sun.jersey.test.framework.impl.container.grizzly.testpackage");        
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