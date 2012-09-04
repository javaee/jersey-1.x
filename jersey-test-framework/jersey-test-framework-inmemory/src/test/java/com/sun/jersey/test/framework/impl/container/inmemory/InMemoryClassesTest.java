package com.sun.jersey.test.framework.impl.container.inmemory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.inmemory.InMemoryTestContainerFactory;

/**
 *
 * @author Paul Sandoz (paul.sandoz at oracle.com)
 */
public class InMemoryClassesTest extends JerseyTest {

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

    public static class MyFilter implements ContainerRequestFilter, ContainerResponseFilter {
        public boolean requestFilterCalled = false;
        public boolean responseFilterCalled = false;

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            requestFilterCalled = true;
            return request;
        }

        @Override
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            responseFilterCalled = true;
            return response;
        }
    }

    static MyFilter myFilter;


    @Override
    protected AppDescriptor configure() {
        myFilter = new MyFilter();

        ResourceConfig rc = new DefaultResourceConfig();
        rc.getClasses().add(TestResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, myFilter);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, myFilter);

        return new LowLevelAppDescriptor.Builder(rc).
                contextPath("context").
                build();
    }

    @Test
    public void testGet() {
        WebResource r = resource().path("root");

        String s = r.get(String.class);
        assertEquals("GET", s);

        assertTrue(myFilter.requestFilterCalled);
        assertTrue(myFilter.responseFilterCalled);
    }

    @Test
    public void testGetSub() {
        WebResource r = resource().path("root/sub");

        String s = r.get(String.class);
        Assert.assertEquals("sub", s);
    }
}