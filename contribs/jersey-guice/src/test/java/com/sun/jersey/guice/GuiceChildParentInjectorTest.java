package com.sun.jersey.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
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
public class GuiceChildParentInjectorTest extends AbstractGuiceGrizzlyTest {

    @Path("bound/perrequest")
    @RequestScoped
    public static class BoundPerRequestResource {

        @Context
        UriInfo ui;
        @QueryParam("x")
        String x;

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

        @Context
        UriInfo ui;
        @QueryParam("x")
        String x;

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

        @Context
        UriInfo ui;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/singleton", ui.getPath());
            String x = ui.getQueryParameters().getFirst("x");
            assertEquals("x", x);

            return "OK";
        }
    }

    public static class MyGuiceConfig extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {

            Injector i = Guice.createInjector(new AbstractModule() {

                @Override
                protected void configure() {
                    bind(BoundNoScopeResource.class);
                    bind(BoundSingletonResource.class);
                }
            });

            return i.createChildInjector(new ServletModule() {

                @Override
                protected void configureServlets() {
                    bind(BoundPerRequestResource.class);

                    serve("*").with(GuiceContainer.class);
                }
            });
        }
    }

    public void testBoundNoScopeResource() {
        startServer(MyGuiceConfig.class);

        WebResource r = resource().path("/bound/noscope").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }

    public void testBoundSingletonResourcee() {
        startServer(MyGuiceConfig.class);

        WebResource r = resource().path("/bound/singleton").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }
//
//    public void testBoundPerRequestResource() {
//        startServer(MyGuiceConfig.class);
//
//        WebResource r = resource().path("/bound/perrequest").queryParam("x", "x");
//        String s = r.get(String.class);
//        assertEquals(s, "OK");
//    }

}
