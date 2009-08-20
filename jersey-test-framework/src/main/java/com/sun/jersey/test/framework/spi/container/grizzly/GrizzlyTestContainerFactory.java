package com.sun.jersey.test.framework.spi.container.grizzly;

import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;

/**
 * The test container factory implementation responsible for creating a light-weight
 * Grizzly server instance.
 * @author Paul.Sandoz@Sun.COM
 */
public class GrizzlyTestContainerFactory implements TestContainerFactory {

    public Class<LowLevelAppDescriptor> supports() {
        return LowLevelAppDescriptor.class;
    }

    /**
     * Creates an instance of {@link GrizzlyTestContainer}
     * @param Base URI of the application
     * @param An instance of {@link AppDescriptor}
     * @return An instance of {@link GrizzlyTestContainer}
     */
    public TestContainer create(URI baseUri, AppDescriptor ad) {
        if (!(ad instanceof LowLevelAppDescriptor))
            throw new IllegalArgumentException(
                    "The application descriptor must be an instance of LowLevelAppDescriptor");
        
        return new GrizzlyTestContainer(baseUri, (LowLevelAppDescriptor)ad);
    }

    /**
     * This class has methods for instantiating, starting and stopping the light-weight
     * Grizzly server.
     */
    private static class GrizzlyTestContainer implements TestContainer {
        private static final Logger LOGGER =
                Logger.getLogger(GrizzlyTestContainer.class.getName());
        
        final SelectorThread selectorThread;

        final URI baseUri;

        /**
         * Creates an instance of {@link GrizzlyTestContainer}
         * @param Base URI of the application
         * @param An instance of {@link LowLevelAppDescriptor}
         */
        GrizzlyTestContainer(URI baseUri, LowLevelAppDescriptor ad) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(ad.getContextPath()).build();
            
            LOGGER.info("Creating low level grizzly container configured at the base URI " + this.baseUri);
            try {
                Adapter adapter = ContainerFactory.createContainer(Adapter.class,
                        ad.getResourceConfig());
                this.selectorThread = create(this.baseUri, adapter);
            } catch (Exception ex) {
                throw new TestContainerException(ex);
            }
        }

        /**
         * Creates a {@link Client} instance
         * @return A {@link Client} instance
         */
        public Client getClient() {
            return null;
        }

        /**
         * Returns the base URI of the application
         * @return Application base URI
         */
        public URI getBaseUri() {
            return baseUri;
        }

        /**
         * Starts the test container
         */
        public void start() {
            try {
                LOGGER.info("Starting low level grizzly container");
                selectorThread.listen();
            } catch (InstantiationException ex) {
                throw new TestContainerException(ex);
            } catch (IOException ex) {
                throw new TestContainerException(ex);
            }
        }

        /**
         * Stops the test container
         */
        public void stop() {
            if (selectorThread.isRunning()) {
                LOGGER.info("Stopping low level grizzly container");
                selectorThread.stopEndpoint();
            }
        }

        /**
         * Creates an instance of {@link SelectorThread}
         * @param The application base URI
         * @param An instance of {@link Adapter}
         * @return A {@link SelectorThread} instance
         * @throws IOException
         * @throws IllegalArgumentException
         */
        public static SelectorThread create(URI u, Adapter adapter)
                throws IOException, IllegalArgumentException {
            if (u == null)
                throw new IllegalArgumentException("The URI must not be null");

            // TODO support https
            final String scheme = u.getScheme();
            if (!scheme.equalsIgnoreCase("http"))
                throw new IllegalArgumentException("The URI scheme, of the URI " + u +
                        ", must be equal (ignoring case) to 'http'");

            if (adapter instanceof GrizzlyAdapter) {
                GrizzlyAdapter ga = (GrizzlyAdapter)adapter;
                ga.setResourcesContextPath(u.getRawPath());
            }

            final SelectorThread selectorThread = new SelectorThread();

            selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());

            final int port = (u.getPort() == -1) ? 80 : u.getPort();
            selectorThread.setPort(port);

            selectorThread.setAdapter(adapter);

            return selectorThread;
        }
    }
}