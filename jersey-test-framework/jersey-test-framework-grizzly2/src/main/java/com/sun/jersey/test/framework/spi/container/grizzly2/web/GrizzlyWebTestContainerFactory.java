/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.jersey.test.framework.spi.container.grizzly2.web;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainer;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;

import javax.servlet.Servlet;
import javax.ws.rs.core.UriBuilder;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A Web-based test container factory for creating test container instances
 * using Grizzly 2.
 *
 * @author Srinivas.Bhimisetty@Sun.COM
 * @author pavel.bucek@oracle.com
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
     * This class has methods for instantiating, starting and stopping the Grizzly 2 Web
     * Server.
     */
    private static class GrizzlyWebTestContainer implements TestContainer {

        private static final Logger LOGGER =
                Logger.getLogger(GrizzlyWebTestContainer.class.getName());

        final URI baseUri;

        final String contextPath;

        final String servletPath;

        final Class<? extends Servlet> servletClass;

        List<WebAppDescriptor.FilterDescriptor> filters = null;

        final List<Class<? extends EventListener>> eventListeners;

        final Map<String, String> initParams;

        final Map<String, String> contextParams;

        private HttpServer httpServer;

        /**
         * Creates an instance of {@link GrizzlyWebTestContainer}
         * @param baseUri URI of the application
         * @param ad An instance of {@link WebAppDescriptor}
         */
        private GrizzlyWebTestContainer(URI baseUri, WebAppDescriptor ad) {
            this.baseUri = UriBuilder.fromUri(baseUri)
                    .path(ad.getContextPath())
                    .path(ad.getServletPath())
                    .build();
            
            LOGGER.info("Creating Grizzly2 Web Container configured at the base URI " + this.baseUri);
            this.contextPath = ad.getContextPath();
            this.servletPath = ad.getServletPath();
            this.servletClass = ad.getServletClass();
            this.filters = ad.getFilters();
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
            LOGGER.info("Starting the Grizzly2 Web Container...");
            
            try {
                httpServer.start();
            } catch (IOException ex) {
                throw new TestContainerException(ex);
            }
             
        }

        public void stop() {
            LOGGER.info("Stopping the Grizzly2 Web Container...");
            httpServer.stop();
            // webServer.getSelectorThread().stopEndpoint(); TODO
        }

        /**
         * Instantiates the Grizzly 2 Web Server
         */
        private void instantiateGrizzlyWebServer() {

            String contextPathLocal;
            if (contextPath != null && contextPath.length() > 0) {
                if (!contextPath.startsWith("/")) {
                    contextPathLocal = "/" + contextPath;
                } else {
                    contextPathLocal = contextPath;
                }
            } else {
                contextPathLocal = "";
            }
            String servletPathLocal;
            if(servletPath != null && servletPath.length() > 0) {
                if( !servletPath.startsWith("/") ) {
                    servletPathLocal = "/" + servletPath;
                } else {
                    servletPathLocal = servletPath;
                }
                if (servletPathLocal.endsWith("/")) {
                    servletPathLocal += "*";
                } else {
                    servletPathLocal +="/*";
                }
            } else {
               servletPathLocal = "/*";
            }

            WebappContext context = 
                    new WebappContext("TestContext", contextPathLocal);

            if (servletClass != null) {
                ServletRegistration registration =
                    context.addServlet(servletClass.getName(), servletClass);
                for(String initParamName : initParams.keySet()) {
                    registration.setInitParameter(initParamName, initParams.get(initParamName));
                }

            
                registration.addMapping(servletPathLocal);
            } else {
                ServletRegistration registration = 
                   context.addServlet("default", new HttpServlet() {
                       public void service() throws ServletException {
                       }
                   });
                registration.addMapping("");
            }

            for(Class<? extends EventListener> eventListener : eventListeners) {
                context.addListener(eventListener);
            }

            for(String contextParamName : contextParams.keySet()) {
                context.addContextInitParameter(contextParamName, contextParams.get(contextParamName));
            }


            // Filter support
            if ( filters!=null ) {
                for (WebAppDescriptor.FilterDescriptor d : this.filters) {
                    FilterRegistration freg = context.addFilter(d.getFilterName(), d.getFilterClass());
                    freg.setInitParameters(d.getInitParams());
                    freg.addMappingForUrlPatterns(null, servletPathLocal);
                }
            }

            try {
                httpServer = GrizzlyServerFactory.createHttpServer(baseUri, (HttpHandler) null);
                context.deploy(httpServer);
            } catch(IOException ioe) {
                throw new TestContainerException(ioe);
            }
        }
    }
}
