package com.sun.jersey.test.framework.impl.container.grizzlyweb;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;

/**
 * @author pavel.bucek@sun.com
 */
public class GrizzlyWebServletAndFilterTest extends JerseyTest {

    public static class MyServlet extends ServletContainer {

        public static boolean visited = false;

        @Override
        public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.service(req, resp);
            visited = true;
        }
    }

    public static class MyFilter1 implements Filter {

        public static boolean visited = false;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            visited = true;
            filterChain.doFilter(servletRequest, servletResponse);
        }

        @Override
        public void destroy() {
        }
    }

    public static class MyFilter2 implements Filter {

        public static boolean visited = false;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            visited = true;
            filterChain.doFilter(servletRequest, servletResponse);
        }

        @Override
        public void destroy() {
        }
    }

    @Path("GrizzlyWebServletAndFilterTest")
    public static class TestResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    public GrizzlyWebServletAndFilterTest() {
        super(new WebAppDescriptor.Builder().servletClass(MyServlet.class)
                .addFilter(MyFilter1.class, "myFilter", null)
                .addFilter(MyFilter2.class, "myFilter", null)
                .initParam(PackagesResourceConfig.PROPERTY_PACKAGES, "com.sun.jersey.test.framework.impl.container.grizzlyweb").build());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    public void testGet() {
        WebResource r = resource().path("GrizzlyWebServletAndFilterTest");

        String s = r.get(String.class);
        Assert.assertEquals("GET", s);

        Assert.assertTrue(MyServlet.visited);
        Assert.assertTrue(MyFilter1.visited);
        Assert.assertTrue(MyFilter2.visited);
    }
}
