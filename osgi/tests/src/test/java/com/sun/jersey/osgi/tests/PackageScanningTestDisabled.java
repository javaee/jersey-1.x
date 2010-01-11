package com.sun.jersey.osgi.tests;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.net.URI;
import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;


public class PackageScanningTestDisabled {
    private static final Logger LOGGER = Logger.getLogger(PackageScanningTestDisabled.class.getName());

    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8080);
    private static final String CONTEXT = "/jersey";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();
    
    @Test
    public void testSimpleResource() throws Exception {

        GrizzlyWebServer gws = new GrizzlyWebServer(port);

        ServletAdapter jerseyAdapter = new ServletAdapter();
        jerseyAdapter.addInitParameter("com.sun.jersey.config.property.packages",
                "com.sun.jersey.osgi.tests com/sun/jersey com.sun.jersey.osgi com/sun/jersey/osgi");
        //jerseyAdapter.addInitParameter("com.sun.jersey.config.property.resourceConfigClass",
        //        ClassNamesResourceConfig.class.getName());
        jerseyAdapter.setContextPath("/jersey");
        jerseyAdapter.setServletInstance(new ServletContainer());

        gws.addGrizzlyAdapter(jerseyAdapter, new String[] {"/jersey"});

        gws.start();

        WebResource r = resource().path("/simple");
        String result = r.get(String.class);

        System.out.println("RESULT = " + result);
        assertEquals("OK", result);


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
