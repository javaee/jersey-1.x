package com.sun.jersey.guice;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.HashMap;
import java.util.Map;
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
public class GuiceUnBoundTest extends AbstractGuiceGrizzlyTest {

    @Path("unbound/perrequest")
    public static class UnBoundPerRequestResource {

        @Context UriInfo ui;
        
        @QueryParam("x") String x;

        @Inject
        public UnBoundPerRequestResource() {}

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("unbound/perrequest", ui.getPath());
            assertEquals("x", x);

            return "OK";
        }
    }

    public static class GuiceServletConfig extends GuiceServletContextListener {

        @Override
        protected Injector getInjector() {
            return Guice.createInjector(new ServletModule() {

                @Override
                protected void configureServlets() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(ServletContainer.APPLICATION_CONFIG_CLASS, ClassNamesResourceConfig.class.getName());
                    params.put(ClassNamesResourceConfig.PROPERTY_CLASSNAMES, UnBoundPerRequestResource.class.getName());

                    // For some reason "/*" does not work with Grizzly
                    // "/*" works fine for Web container deployments with GF
                    serve("*").with(GuiceContainer.class, params);
                }
            });
        }
    }

    public void testBoundPerRequestResource() {
        startServer(GuiceServletConfig.class);

        WebResource r = resource().path("/unbound/perrequest").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }
}
