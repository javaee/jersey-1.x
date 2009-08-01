package com.sun.jersey.test.framework.impl.container.grizzlyweb.contextpath;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
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
public class GrizzlyWebContextPathTest extends JerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Path("contextroot")
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

    public GrizzlyWebContextPathTest() {
        super(new WebAppDescriptor.Builder("com.sun.jersey.test.framework.impl.container.grizzlyweb.contextpath")
                .contextPath("context")
                .build());
    }

    @Test
    public void testGet() {
        WebResource r = resource().path("contextroot");

        String s = r.get(String.class);
        Assert.assertEquals("GET", s);
    }

    @Test
    public void testGetSub() {
        WebResource r = resource().path("contextroot/sub");

        String s = r.get(String.class);
        Assert.assertEquals("sub", s);
    }
}