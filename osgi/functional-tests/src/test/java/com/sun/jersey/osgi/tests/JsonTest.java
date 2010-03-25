package com.sun.jersey.osgi.tests;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.net.URI;
import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;


@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class JsonTest {
    private static final Logger LOGGER = Logger.getLogger(JsonTest.class.getName());

    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/jersey";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();
    
    @Test
    public void testSimpleResource() throws Exception {

        GrizzlyWebServer gws = new GrizzlyWebServer(port);

        // Jersey web resources
        ServletAdapter jerseyAdapter = new ServletAdapter();
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.classnames",
                "com.sun.jersey.osgi.tests.JsonResource");
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.resourceConfigClass",
                "com.sun.jersey.api.core.ClassNamesResourceConfig");
        jerseyAdapter.setContextPath("/jersey");
        jerseyAdapter.setServletInstance(new ServletContainer());

        gws.addGrizzlyAdapter(jerseyAdapter, new String[] {"/jersey"});

        gws.start();

        WebResource r = resource().path("/json");
        JsonResource.NameBean result = r.accept(MediaType.APPLICATION_JSON).get(JsonResource.NameBean.class);

        System.out.println("RESULT.name = " + result.name);
        assertEquals("Jim", result.name);

        gws.stop();


        assertEquals("one", "one");
    }

    public WebResource resource() {
        final Client c = Client.create();
        final WebResource rootResource = c.resource(baseUri);
        return rootResource;
    }


    public static int getEnvVariable(final String varName, int defaultValue) {
        if (null == varName) {
            return defaultValue;
        }
        String varValue = System.getenv(varName);
        if (null != varValue) {
            try {
                return Integer.parseInt(varValue);
            }catch (NumberFormatException e) {
                // will return default value bellow
            }
        }
        return defaultValue;
    }
}
