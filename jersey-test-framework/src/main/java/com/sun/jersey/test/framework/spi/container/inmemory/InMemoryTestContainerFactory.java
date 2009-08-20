/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.test.framework.spi.container.inmemory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.WebApplicationFactory;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.impl.container.inmemory.TestResourceClientHandler;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import java.net.URI;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;

/**
 * The test container factory implementation responsible for creating an In-memory
 * container.
 * @author Srinivas.Bhimisetty@Sun.COM
 */
public class InMemoryTestContainerFactory implements TestContainerFactory {

    public Class<LowLevelAppDescriptor> supports() {
        return LowLevelAppDescriptor.class;
    }

    /**
     * Creates an instance of the In-memory or In-process test container.
     * @param The baseUri of the application
     * @param An instance of {@link AppDescriptor}
     * @return An instance of {@link InMemoryTestContainer}
     */
    public TestContainer create(URI baseUri, AppDescriptor ad) {
        if (!(ad instanceof LowLevelAppDescriptor))
            throw new IllegalArgumentException(
                    "The application descriptor must be an instance of LowLevelAppDescriptor");

        return new InMemoryTestContainer(baseUri, (LowLevelAppDescriptor)ad);
    }

    /**
     * The class defines methods for starting/stopping an in-memory test container,
     * and for running tests on the container.
     */
    private static class InMemoryTestContainer implements TestContainer {

        private static final Logger LOGGER =
                Logger.getLogger(InMemoryTestContainer.class.getName());

        final URI baseUri;

        final ResourceConfig resourceConfig;

        final WebApplication webApp;

        /**
         * Creates an instance of {@link InMemoryTestContainer}
         * @param Base URI of the application
         * @param An instance of {@link LowLevelAppDescriptor}
         */
        private InMemoryTestContainer(URI baseUri, LowLevelAppDescriptor ad) {
            this.baseUri = UriBuilder.fromUri(baseUri).build();

            LOGGER.info("Creating low level InMemory test container configured at the base URI "
                    + this.baseUri);

            this.resourceConfig = ad.getResourceConfig();
            this.resourceConfig.getProperties()
                    .put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, LoggingFilter.class.getName());
            this.resourceConfig.getProperties()
                    .put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, LoggingFilter.class.getName());
            this.webApp = initiateWebApplication(resourceConfig);
        }

        /**
         * Creates a Client instance
         * @return A {@link Client} instance
         */
        public Client getClient() {
            ClientConfig clientConfig = null;
            Set<Object> providerSingletons = resourceConfig.getProviderSingletons();

            if (providerSingletons.size() > 0) {
                clientConfig = new DefaultClientConfig();
                for(Object providerSingleton : providerSingletons) {
                    clientConfig.getSingletons().add(providerSingleton);
                }
            }

            Client client = (clientConfig == null) ?
                new Client(new TestResourceClientHandler(baseUri, webApp)) :
                new Client(new TestResourceClientHandler(baseUri, webApp), clientConfig);
            
            return client;
        }

        /**
         * Returns base URI of the application
         * @return Base URI of the application
         */
        public URI getBaseUri() {
            return baseUri;
        }

        /**
         * Starts the in-memory test container
         */
        public void start() {
            if (!webApp.isInitiated()) {
                LOGGER.info("Starting low level InMemory test container");

                webApp.initiate(resourceConfig);
            }
        }

        /**
         * Stops the in-memory test container
         */
        public void stop() {
            if (webApp.isInitiated()) {
                LOGGER.info("Stopping low level InMemory test container");

                webApp.destroy();
            }
        }

        private WebApplication initiateWebApplication(ResourceConfig rc) {
            WebApplication webapp = WebApplicationFactory.createWebApplication();
            return webapp;
        }

    }

}