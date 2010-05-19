package com.sun.jersey.test.framework.impl.container.grizzlyweb;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 * @author pavel.bucek@sun.com
 */
public class GrizzlyWebInjectionTest extends JerseyTest {

    @Path("GrizzlyWebInjectionTest")
    public static class TestResource {

        @Context ServletConfig servletConfig;
        @Context ServletContext servletContext;

        @GET
        public String get() {

            if(
                    servletConfig != null &&
                    servletContext != null &&
                    servletConfig.getInitParameter(
                            PackagesResourceConfig.PROPERTY_PACKAGES).equals("com.sun.jersey.test.framework.impl.container.grizzlyweb")
                    )

                return "SUCCESS";
            else
                return "FAIL";
        }
    }

    public GrizzlyWebInjectionTest() {
        super(new WebAppDescriptor.Builder()
                .initParam(PackagesResourceConfig.PROPERTY_PACKAGES, "com.sun.jersey.test.framework.impl.container.grizzlyweb").build());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    public void testGet() {
        WebResource r = resource().path("GrizzlyWebInjectionTest");

        String s = r.get(String.class);
        Assert.assertEquals("SUCCESS", s);
    }
}
