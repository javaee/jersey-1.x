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
package com.sun.jersey.test.framework.spi.container.grizzly.web;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import java.io.IOException;
import java.net.URI;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.ws.rs.core.UriBuilder;

/**
 * A Web-based test container factory for creating test container instances
 * using Grizzly.
 *
 * @author Srinivas.Bhimisetty@Sun.COM
 */
public class GrizzlyWebTestContainerFactory implements TestContainerFactory {

    public Class<WebAppDescriptor> supports() {
        return WebAppDescriptor.class;
    }

    public TestContainer create(URI baseUri, AppDescriptor ad) {
        if (!(ad instanceof WebAppDescriptor))
            throw new IllegalArgumentException(
                    "The application descriptor must be an instance of WebAppDescriptor");

        return new GrizzlyWebTestContainer(baseUri, (WebAppDescriptor)ad);
    }

    /**
     * This class has methods for instantiating, starting and stopping the Grizzly Web
     * Server.
     */
    private static class GrizzlyWebTestContainer implements TestContainer {

        private static final Logger LOGGER =
                Logger.getLogger(GrizzlyWebTestContainer.class.getName());

        final URI baseUri;

        final String contextPath;

        final String servletPath;

        final Class servletClass;

        final Class filterClass;

        final List<Class<? extends EventListener>> eventListeners;

        final Map<String, String> initParams;

        final Map<String, String> contextParams;

        private GrizzlyWebServer webServer;

        /**
         * Creates an instance of {@link GrizzlyWebTestContainer}
         * @param Base URI of the application
         * @param An instance of {@link WebAppDescriptor}
         */
        private GrizzlyWebTestContainer(URI baseUri, WebAppDescriptor ad) {
            this.baseUri = UriBuilder.fromUri(baseUri)
                    .path(ad.getContextPath())
                    .path(ad.getServletPath())
                    .build();
            
            LOGGER.info("Creating Grizzly Web Container configured at the base URI " + this.baseUri);
            this.contextPath = ad.getContextPath();
            this.servletPath = ad.getServletPath();
            this.servletClass = ad.getServletClass();
            this.filterClass = ad.getFilterClass();
            this.initParams = ad.getInitParams();
            this.contextParams = ad.getContextParams();
            this.eventListeners = ad.getListeners();

            instantiateGrizzlyWebServer();

        }

        public Client getClient() {
            return null;
        }

        public URI getBaseUri() {
            return baseUri;
        }

        public void start() {
            LOGGER.info("Starting the Grizzly Web Container...");
            
            try {
                webServer.start();                
            } catch (IOException ex) {
                throw new TestContainerException(ex);
            }
             
        }

        public void stop() {
            LOGGER.info("Stopping the Grizzly Web Container...");
            webServer.stop();
            webServer.getSelectorThread().stopEndpoint();
        }

        /**
         * Instantiates the Grizzly Web Server
         */
        private void instantiateGrizzlyWebServer() {
            webServer = new GrizzlyWebServer(baseUri.getPort());
            ServletAdapter sa = new ServletAdapter();
            Servlet servletInstance;
            if( servletClass != null) {
                try {
                    servletInstance = (Servlet) servletClass.newInstance();
                } catch (InstantiationException ex) {
                    throw new TestContainerException(ex);
                } catch (IllegalAccessException ex) {
                    throw new TestContainerException(ex);
                }
                sa.setServletInstance(servletInstance);
            }

            if (eventListeners != null) {
                for(Class<? extends EventListener> eventListener : eventListeners) {
                    sa.addServletListener(eventListener.getName());
                }
            }

            // Filter support
            if ( filterClass!=null ) {
                try {
                    sa.addFilter((Filter) filterClass.newInstance(), "jerseyfilter", initParams);                    
                } catch (InstantiationException ex) {                    
                    throw new TestContainerException(ex);
                } catch (IllegalAccessException ex) {                    
                    throw new TestContainerException(ex);
                }
            }
            
            for(String contextParamName : contextParams.keySet()) {
                sa.addContextParameter(contextParamName, contextParams.get(contextParamName));
            }

            for(String initParamName : initParams.keySet()) {
                sa.addInitParameter(initParamName, initParams.get(initParamName));                
            }
            
            if(contextPath != null && contextPath.length() > 0) {
                if( !contextPath.startsWith("/") ) {
                    sa.setContextPath("/" + contextPath);
                } else {
                    sa.setContextPath(contextPath);
                }
            }

            if(servletPath != null && servletPath.length() > 0) {
                if( !servletPath.startsWith("/") ) {
                    sa.setServletPath("/" + servletPath);
                } else {
                    sa.setServletPath(servletPath);
                }
            }

            String[] mapping = null;
            webServer.addGrizzlyAdapter(sa, mapping);

        }

    }

}