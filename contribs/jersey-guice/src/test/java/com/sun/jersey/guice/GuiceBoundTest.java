/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.core.ResourceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class GuiceBoundTest extends AbstractGuiceGrizzlyTest {

    @Path("bound/perrequest")
    @RequestScoped
    public static class BoundPerRequestResource {

        @Context UriInfo ui;
        
        @QueryParam("x") String x;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/perrequest", ui.getPath());
            assertEquals("x", x);

            return "OK";
        }
    }

    @Path("bound/noscope")
    public static class BoundNoScopeResource {

        @Context UriInfo ui;

        @QueryParam("x") String x;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/noscope", ui.getPath());
            assertEquals("x", x);

            return "OK";
        }
    }

    @Path("bound/singleton")
    @Singleton
    public static class BoundSingletonResource {

        @Context UriInfo ui;

        @GET
        @Produces("text/plain")
        public String getIt() {
            assertEquals("bound/singleton", ui.getPath());
            String x = ui.getQueryParameters().getFirst("x");
            assertEquals("x", x);

            return "OK";
        }
    }

    public static class TestServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().path("*").
                    bindClass(BoundPerRequestResource.class).
                    bindClass(BoundNoScopeResource.class).
                    bindClass(BoundSingletonResource.class);
        }
    }

    public void testBoundPerRequestResource() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/bound/perrequest").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }


    public void testBoundNoScopeResource() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/bound/noscope").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }

    public void testBoundSingletonResourcee() {
        startServer(TestServletConfig.class);

        WebResource r = resource().path("/bound/singleton").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");
    }


    @Path("bound")
    public static class BoundSubResource {
        @Context ResourceContext rc;

        @Path("perrequest")
        public BoundPerRequestResource getPerRequest() {
            return rc.getResource(BoundPerRequestResource.class);
        }

        @Path("noscope")
        public BoundNoScopeResource getNoScope() {
            return rc.getResource(BoundNoScopeResource.class);
        }

        @Path("singleton")
        public BoundSingletonResource getSingleton() {
            return rc.getResource(BoundSingletonResource.class);
        }
    }

    public static class SubResourceServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().path("*").
                    bindClass(BoundSubResource.class);
        }
    }

    public void testBoundSubResource() {
        startServer(SubResourceServletConfig.class);

        WebResource r = resource().path("/bound/perrequest").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "OK");

        r = resource().path("/bound/noscope").queryParam("x", "x");
        s = r.get(String.class);
        assertEquals(s, "OK");

        r = resource().path("/bound/singleton").queryParam("x", "x");
        s = r.get(String.class);
        assertEquals(s, "OK");
    }


    @Path("inject")
    @RequestScoped
    public static class InjectResource {

        @Context UriInfo ui;

        @QueryParam("x") String x;

        @GET
        @Produces("text/plain")
        public String getIt(@InjectParam GuiceManagedClass gmc) {
            assertEquals("inject", ui.getPath());
            assertEquals("x", x);

            return gmc.toString();
        }
    }

    public static class GuiceManagedClass {
        public String toString() {
            return "GuiceManagedClass";
        }
    }

    public static class InjectServletConfig extends JerseyTestGuiceServletContextListener {
        @Override
        protected ServletModule configure() {
            return new JerseyTestServletModule().path("*").
                    bindClass(InjectResource.class).
                    bindClass(GuiceManagedClass.class);
        }
    }

    public void testInject() {
        startServer(InjectServletConfig.class);

        WebResource r = resource().path("/inject").queryParam("x", "x");
        String s = r.get(String.class);
        assertEquals(s, "GuiceManagedClass");
    }
}
