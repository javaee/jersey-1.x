package com.sun.jersey.osgi.tests;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import static org.junit.Assert.assertEquals;

import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class BasicOsgiIntegrationTest {
    private static final Logger LOGGER = Logger.getLogger(BasicOsgiIntegrationTest.class.getName());

    private static final int port = getEnvVariable("JERSEY_HTTP_PORT", 8765);
    private static final String CONTEXT = "/jersey";
    private static final URI baseUri = UriBuilder.fromUri("http://localhost").port(port).path(CONTEXT).build();

    @Inject private BundleContext bundleContext;


    @Test
    public void testSimpleResource() throws Exception {

        for (Bundle b : bundleContext.getBundles()) {
            System.out.println(String.format("bid:%s, bname:%s, bstate:%s",
                    b.getBundleId(),
                    b.getSymbolicName(),
                    bundleStateName(b.getState())));
        }

        WebResource r = resource().path("/simple");
        String result = r.get(String.class);

        System.out.println("RESULT = " + result);
        assertEquals("OK", result);
    }

    private static String bundleStateName(int state) {
        switch(state) {
            case Bundle.ACTIVE : return "ACTIVE";
            case Bundle.INSTALLED : return "INSTALLED";
            case Bundle.RESOLVED : return "RESOLVED";
            case Bundle.STARTING : return "STARTING";
            case Bundle.STOPPING : return "STOPPING";
            case Bundle.UNINSTALLED : return "UNINSTALLED";
            default : return "NOT_DEFINED";
        }
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
