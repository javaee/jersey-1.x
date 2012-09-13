/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.guice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.servlet.ServletModule;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @author Charlie Groves
 */
public class GuiceWebAppExceptionTest extends AbstractGuiceGrizzlyTest {
    public static class GuiceManagedClass {
        public GuiceManagedClass () {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        public String toString() {
            return "GuiceManagedClass";
        }
    }

    @Path("badinjection")
    public static class GuiceManagedResource {
        private final GuiceManagedClass gmc;

        @Inject
        private GuiceManagedResource(GuiceManagedClass gmc) {
            this.gmc = gmc;
        }

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertNotNull(gmc);

            return gmc.toString();
        }
    }

    public static class TestServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().
                    path("*").
                    initParam(ServletContainer.APPLICATION_CONFIG_CLASS, ClassNamesResourceConfig.class.getName()).
                    initParam(ClassNamesResourceConfig.PROPERTY_CLASSNAMES, GuiceManagedResource.class.getName()).
                    bindClass(GuiceManagedClass.class);
        }
    }

    public void testBadInjection() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/badinjection");
        try {
            r.get(String.class);
            fail("The bad injection should throw an exception!");
        } catch (UniformInterfaceException uie) {
            assertEquals(ClientResponse.Status.BAD_REQUEST, uie.getResponse().getClientResponseStatus());
        }
    }
}
