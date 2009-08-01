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
 *
 * @author paulsandoz
 */
public class GrizzlyTestContainerFactory implements TestContainerFactory {

    public Class<LowLevelAppDescriptor> supports() {
        return LowLevelAppDescriptor.class;
    }

    public TestContainer create(URI baseUri, AppDescriptor ad) {
        if (!(ad instanceof LowLevelAppDescriptor))
            throw new IllegalArgumentException(
                    "The application descriptor must be an instance of LowLevelAppDescriptor");
        
        return new GrizzlyTestContainer(baseUri, (LowLevelAppDescriptor)ad);
    }

    private static class GrizzlyTestContainer implements TestContainer {
        private static final Logger LOGGER =
                Logger.getLogger(GrizzlyTestContainer.class.getName());
        
        final SelectorThread selectorThread;

        final URI baseUri;

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

        public Client getClient() {
            return null;
        }

        public URI getBaseUri() {
            return baseUri;
        }
        
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

        public void stop() {
            if (selectorThread.isRunning()) {
                LOGGER.info("Stopping low level grizzly container");
                selectorThread.stopEndpoint();
            }
        }

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