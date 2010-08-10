package com.sun.jersey.guice;

import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class GuiceBoundTest extends AbstractGuiceGrizzlyTest {

    @Path("bound/perrequest")
    @RequestScoped
    public static class BoundPerRequestResource {

        @Context UriInfo ui;
        
        @QueryParam("x") String x;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/perrequest", ui.getPath());
            assertEquals("x", x);

            return "OK";
        }
    }

    @Path("bound/noscope")
    public static class BoundNoScopeResource {

        @Context UriInfo ui;

        @QueryParam("x") String x;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/noscope", ui.getPath());
            assertEquals("x", x);

            return "OK";
        }
    }

    @Path("bound/singleton")
    @Singleton
    public static class BoundSingletonResource {

        @Context UriInfo ui;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/singleton", ui.getPath());
            String x = ui.getQueryParameters().getFirst("x");
            assertEquals("x", x);

            return "OK";
        }
    }

    public static class TestServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().path("*").
                    bindClass(BoundPerRequestResource.class).
                    bindClass(BoundNoScopeResource.class).
                    bindClass(BoundSingletonResource.class);
        }
    }

    public void testBoundPerRequestResource() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/bound/perrequest").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }


    public void testBoundNoScopeResource() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/bound/noscope").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }

    public void testBoundSingletonResourcee() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/bound/singleton").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }


    @Path("bound")
    public static class BoundSubResource {
        @Context ResourceContext rc;

        @Path("perrequest")
        public BoundPerRequestResource getPerRequest() {
            return rc.getResource(BoundPerRequestResource.class);
        }

        @Path("noscope")
        public BoundNoScopeResource getNoScope() {
            return rc.getResource(BoundNoScopeResource.class);
        }

        @Path("singleton")
        public BoundSingletonResource getSingleton() {
            return rc.getResource(BoundSingletonResource.class);
        }
    }

    public static class SubResourceServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().path("*").
                    bindClass(BoundSubResource.class);
        }
    }

    public void testBoundSubResource() {
        startServer(SubResourceServletConfig.class);

        WebResource r = resource().path("/bound/perrequest").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");

        r = resource().path("/bound/noscope").queryParam("x", "x");
        s = r.get(String.class);
        assertEquals(s, "OK");

        r = resource().path("/bound/singleton").queryParam("x", "x");
        s = r.get(String.class);
        assertEquals(s, "OK");
    }


    @Path("inject")
    @RequestScoped
    public static class InjectResource {

        @Context UriInfo ui;

        @QueryParam("x") String x;

        @GET
        @Produces("text/plain")
        public String getIt(@InjectParam GuiceManagedClass gmc) {
            assertEquals("inject", ui.getPath());
            assertEquals("x", x);

            return gmc.toString();
        }
    }

    public static class GuiceManagedClass {
        public String toString() {
            return "GuiceManagedClass";
        }
    }

    public static class InjectServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().path("*").
                    bindClass(InjectResource.class).
                    bindClass(GuiceManagedClass.class);
        }
    }

    public void testInject() {
        startServer(InjectServletConfig.class);

        WebResource r = resource().path("/inject").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "GuiceManagedClass");
    }
}
