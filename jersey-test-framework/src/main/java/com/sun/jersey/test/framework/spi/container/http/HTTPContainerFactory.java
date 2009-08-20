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
package com.sun.jersey.test.framework.spi.container.http;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.container.ContainerFactory;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;

/**
 * The test container factory implementation responsible for creating a HTTPServer
 * instance.
 * @author Srinivas.Bhimisetty@Sun.COM
 */
public class HTTPContainerFactory implements TestContainerFactory {

    public Class<LowLevelAppDescriptor> supports() {
        return LowLevelAppDescriptor.class;
    }

    /**
     * Creates an instance of the HTTPServer test container
     * @param Base URI of the application
     * @param An instance of {@link LowLevelAppDescriptor}
     * @return An instance of {@link TestContainer}
     */
    public TestContainer create(URI baseUri, AppDescriptor ad) {

        if (!(ad instanceof LowLevelAppDescriptor))
            throw new IllegalArgumentException(
                    "The application descriptor must be an instance of LowLevelAppDescriptor");

        return new HTTPTestContainer(baseUri, (LowLevelAppDescriptor)ad);
    }

    /**
     * The class provides methods for starting/stopping the HTTPServer test container,
     * and running tests on the HTTPTestContainer.
     */
    private static class HTTPTestContainer implements TestContainer {

        private static final Logger LOGGER =
                Logger.getLogger(HTTPTestContainer.class.getName());

        final URI baseUri;

        final ResourceConfig resourceConfig;

        final HttpHandler httpHandler;

        final HttpServer httpServer;

        /**
         * Creates an instance of the HTTPTestContainer
         * @param Base URI of the application
         * @param A {@link LowLevelAppDescriptor} instance
         */
        HTTPTestContainer(URI baseUri, LowLevelAppDescriptor ad) {
            this.baseUri = UriBuilder.fromUri(baseUri).path(ad.getContextPath()).build();
            LOGGER.info("Creating low level http container configured at the base URI "
                    + this.baseUri);
            this.resourceConfig = ad.getResourceConfig();
            this.httpHandler = ContainerFactory.createContainer( HttpHandler.class,
                    resourceConfig );
            try {
                this.httpServer = HttpServerFactory.create(this.baseUri, httpHandler);
            } catch (Exception ex) {
                throw new TestContainerException(ex);
            }

        }

        /**
         * Creates a Client instance
         * @return An instance of {@link Client}
         */
        public Client getClient() {
            return null;
        }

        /**
         * Returns base URI of the application
         * @return Base URI of the application
         */
        public URI getBaseUri() {
            return baseUri;
        }

        /**
         * Starts the HTTPServer test container.
         */
        public void start() {
            LOGGER.info("Starting low level HTTPServer container");
            httpServer.start();
        }

        /**
         * Stops the HTTPServer test container.
         */
        public void stop() {
            LOGGER.info("Stopping low level HTTPServer container");
            httpServer.stop(0);
        }

    }

}
