/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.Path;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ExplicitRootResourceTest extends AbstractResourceTester {

    public ExplicitRootResourceTest(String testName) {
        super(testName);
    }

    public static class ExplicitResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    public void testExplicitResource() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getExplicitRootResources().put("/one", ExplicitResource.class);
        rc.getExplicitRootResources().put("/two", ExplicitResource.class);
        initiateWebApplication(rc);

        assertEquals("GET", resource("/one").get(String.class));
        assertEquals("GET", resource("/two").get(String.class));
    }


    @Singleton
    @Path("singleton")
    public static class SingletonExplicitResource {
        int i = 1;

        @GET
        public String get() {
            return "GET" + i++;
        }
    }

    @Singleton
    @Path("singleton-custom-constructor")
    public static class SingletonExplicitCustomConstructorResource {

        private int i;

        public SingletonExplicitCustomConstructorResource(final int i) {
            this.i = i;
        }

        @GET
        public String get() {
            return "GET" + i++;
        }
    }

    public void testSingleton() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getExplicitRootResources().put("/one", SingletonExplicitResource.class);
        rc.getExplicitRootResources().put("/two", SingletonExplicitResource.class);
        initiateWebApplication(rc);

        assertEquals("GET1", resource("/one").get(String.class));
        assertEquals("GET2", resource("/two").get(String.class));
    }

    public void testSingletonInstance() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig();
        SingletonExplicitResource r = new SingletonExplicitResource();
        rc.getExplicitRootResources().put("/one", r);
        rc.getExplicitRootResources().put("/two", r);
        initiateWebApplication(rc);

        assertEquals("GET1", resource("/one").get(String.class));
        assertEquals("GET2", resource("/two").get(String.class));
    }

    public void testSingletonAndSingletonInstance() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig();
        SingletonExplicitResource r = new SingletonExplicitResource();
        rc.getExplicitRootResources().put("/one", r);
        rc.getExplicitRootResources().put("/two", r);
        rc.getExplicitRootResources().put("/three", SingletonExplicitResource.class);
        rc.getExplicitRootResources().put("/four", SingletonExplicitResource.class);
        initiateWebApplication(rc);

        assertEquals("GET1", resource("/one").get(String.class));
        assertEquals("GET2", resource("/two").get(String.class));
        assertEquals("GET1", resource("/three").get(String.class));
        assertEquals("GET2", resource("/four").get(String.class));
    }

    public void testSingletonCustomConstructorInstance() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig();
        SingletonExplicitCustomConstructorResource r = new SingletonExplicitCustomConstructorResource(1);
        rc.getExplicitRootResources().put("/one", r);
        initiateWebApplication(rc);

        assertEquals("GET1", resource("/one").get(String.class));
    }

    public void testSingletonResourceSingletonAndSingletonInstance() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getClasses().add(SingletonExplicitResource.class);
        SingletonExplicitResource r = new SingletonExplicitResource();
        rc.getExplicitRootResources().put("/one", r);
        rc.getExplicitRootResources().put("/two", r);
        rc.getExplicitRootResources().put("/three", SingletonExplicitResource.class);
        rc.getExplicitRootResources().put("/four", SingletonExplicitResource.class);
        initiateWebApplication(rc);

        assertEquals("GET1", resource("/one").get(String.class));
        assertEquals("GET2", resource("/two").get(String.class));
        assertEquals("GET1", resource("/three").get(String.class));
        assertEquals("GET2", resource("/four").get(String.class));
        assertEquals("GET3", resource("/singleton").get(String.class));
        assertEquals("GET4", resource("/singleton").get(String.class));
    }

    public static class PathParamExplicitResource {
        @GET
        public String get(@PathParam("one") String one, @PathParam("two") String two) {
            return "" + one + two;
        }
    }

    public void testPathParam() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getExplicitRootResources().put("/{one}", PathParamExplicitResource.class);
        rc.getExplicitRootResources().put("/{one}/{two}", PathParamExplicitResource.class);
        initiateWebApplication(rc);

        assertEquals("onenull", resource("/one").get(String.class));
        assertEquals("onetwo", resource("/one/two").get(String.class));
    }
}
