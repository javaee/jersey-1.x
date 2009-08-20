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
package com.sun.jersey.test.framework.spi.container.external;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 * The test container factory implementation responsible for running tests on an
 * external container.
 * @author Srinivas.Bhimisetty@Sun.COM
 */
public class ExternalTestContainerFactory implements TestContainerFactory {

    public Class<WebAppDescriptor> supports() {
        return WebAppDescriptor.class;
    }

    /**
     * Creates an instance of {@link ExternalTestContainer}
     * @param Base URI of the application
     * @param An instance of {@link WebAppDescriptor}
     * @return An instance of {@link ExternalTestContainer}
     */
    public TestContainer create(URI baseUri, AppDescriptor ad) {
        if (!(ad instanceof WebAppDescriptor))
            throw new IllegalArgumentException(
                    "The application descriptor must be an instance of WebAppDescriptor");

        return new ExternalTestContainer(baseUri, (WebAppDescriptor)ad);
    }

    /**
     * Class which helps running tests on an external container. It assumes that
     * the container is started/stopped explicitly and also that the application is
     * pre-deployed.
     */
    private static class ExternalTestContainer implements TestContainer {

        final URI baseUri;

        private ExternalTestContainer(URI baseUri, WebAppDescriptor ad) {
            this.baseUri = UriBuilder.fromUri(baseUri)
                    .path(ad.getContextPath())
                    .path(ad.getServletPath())
                    .build();
        }

        public Client getClient() {
            return null;
        }

        public URI getBaseUri() {
            return baseUri;
        }

        /**
         * An empty implementation which does nothing
         */
        public void start() {
            // do nothing
        }

        /**
         * An empty implementation which does nothing
         */
        public void stop() {
            // do nothing
        }

    }

}
