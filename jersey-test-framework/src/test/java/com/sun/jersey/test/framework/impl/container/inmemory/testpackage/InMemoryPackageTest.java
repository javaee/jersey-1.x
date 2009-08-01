package com.sun.jersey.test.framework.impl.container.inmemory.testpackage;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author paulsandoz
 */
public class InMemoryPackageTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new InMemoryTestContainerFactory();
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

    public InMemoryPackageTest() {
        super("com.sun.jersey.test.framework.impl.container.inmemory.testpackage");
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