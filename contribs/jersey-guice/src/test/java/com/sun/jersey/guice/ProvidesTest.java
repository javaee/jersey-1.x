package com.sun.jersey.guice;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.container.ExceptionMapperContext;
import com.sun.jersey.spi.container.WebApplication;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ProvidesTest extends AbstractGuiceGrizzlyTest {

    @Path("/")
    @RequestScoped
    public static class InjectResource {

        @Inject WebApplication wa;
        @Inject Providers p;
        @Inject FeaturesAndProperties fp;
        @Inject MessageBodyWorkers mbws;
        @Inject ExceptionMapperContext emc;
        @Inject HttpContext hc;
        @Inject UriInfo ui;
        @Inject ExtendedUriInfo eui;
        @Inject HttpRequestContext hrequestc;
        @Inject HttpHeaders h;
        @Inject Request r;
        @Inject SecurityContext sc;
        @Inject HttpResponseContext hresponsec;

        @GET
        @Produces("text/plain")
        public String getIt() {
            return "OK";
        }
    }

    public static class InjectServletConfig extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            return Guice.createInjector(
                Stage.PRODUCTION,
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        bind(InjectResource.class);

                        serve("*").with(GuiceContainer.class);
                    }
                }
            );
        }
    }

    public void testInjectResource() {
        startServer(InjectServletConfig.class);

        String s = resource().path("/").get(String.class);
        assertEquals(s, "OK");
    }


    @Path("bound/perrequest")
    @RequestScoped
    public static class BoundPerRequestResource {

        @Inject UriInfo ui;
        
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

        @Inject UriInfo ui;

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

        @Inject Provider<UriInfo> ui;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/singleton", ui.get().getPath());
            String x = ui.get().getQueryParameters().getFirst("x");
            assertEquals("x", x);

            return "OK";
        }
    }


    public static class ResourceServletConfig extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            return Guice.createInjector(
                Stage.PRODUCTION,
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        bind(BoundPerRequestResource.class);
                        bind(BoundNoScopeResource.class);
                        bind(BoundSingletonResource.class);

                        serve("*").with(GuiceContainer.class);
                    }
                }
            );
        }
    }

    public void testBoundPerRequestResource() {
        startServer(ResourceServletConfig.class);

        WebResource r = resource().path("/bound/perrequest").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }


    public void testBoundNoScopeResource() {
        startServer(ResourceServletConfig.class);

        WebResource r = resource().path("/bound/noscope").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }

    public void testBoundSingletonResourcee() {
        startServer(ResourceServletConfig.class);

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

    public static class SubResourceServletConfig extends GuiceServletContextListener {
        @Override
        protected Injector getInjector() {
            return Guice.createInjector(
                Stage.PRODUCTION,
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        bind(BoundSubResource.class);
                        serve("*").with(GuiceContainer.class);
                    }
                }
            );
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
}
