/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
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
 * A low-level test container factory for creating test container instances
 * using Grizzly.
 *
 * @author Paul.Sandoz@Sun.COM
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

        /**
         * Creates an instance of {@link SelectorThread}
         * @param The application base URI
         * @param An instance of {@link Adapter}
         * @return A {@link SelectorThread} instance
         * @throws IOException
         * @throws IllegalArgumentException
         */
        private static SelectorThread create(URI u, Adapter adapter)
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