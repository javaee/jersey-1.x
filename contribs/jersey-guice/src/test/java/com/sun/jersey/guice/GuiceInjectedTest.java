package com.sun.jersey.guice;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
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
public class GuiceInjectedTest extends AbstractGuiceGrizzlyTest {

    @Path("unbound/perrequest")
    public static class InjectedPerRequestResource {

        @Context UriInfo ui;
        
        String x;

        @Inject
        GuiceManagedClass gmc;

        public InjectedPerRequestResource(@QueryParam("x") String x) {
            this.x = x;
        }

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("unbound/perrequest", ui.getPath());
            assertEquals("x", x);

            return gmc.toString();
        }
    }

    public static class GuiceManagedClass {
        public String toString() {
            return "GuiceManagedClass";
        }
    }

    public static class TestServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().
                    path("*").
                    initParam(ServletContainer.APPLICATION_CONFIG_CLASS, ClassNamesResourceConfig.class.getName()).
                    initParam(ClassNamesResourceConfig.PROPERTY_CLASSNAMES, InjectedPerRequestResource.class.getName()).
                    bindClass(GuiceManagedClass.class);
        }
    }

    public void testBoundPerRequestResource() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/unbound/perrequest").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "GuiceManagedClass");
    }
}
