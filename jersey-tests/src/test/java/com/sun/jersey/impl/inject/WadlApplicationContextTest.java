/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.inject;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResourceModelContext;
import com.sun.jersey.api.model.AbstractResourceModelListener;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.server.wadl.WadlApplicationContext;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.monitoring.DispatchingListener;
import com.sun.jersey.spi.monitoring.DispatchingListenerAdapter;
import com.sun.jersey.spi.monitoring.RequestListener;
import com.sun.jersey.spi.monitoring.RequestListenerAdapter;
import com.sun.jersey.spi.monitoring.ResponseListener;
import com.sun.jersey.spi.monitoring.ResponseListenerAdapter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author pavel.bucek@oracle.com
 */
public class WadlApplicationContextTest extends AbstractResourceTester {

    public WadlApplicationContextTest(String testName) {
        super(testName);
    }

    @Path("/randomUniqueDumbResource123456")
    public static class A {
        @GET
        public String get() {
            return "A";
        }

    }

    @Provider
    public static class ProviderAdapter implements RequestListenerAdapter,
            ResponseListenerAdapter,
            DispatchingListenerAdapter,
            AbstractResourceModelListener,
            ResponseListener {

        private int requestCount = 0;

        @Context
        private WadlApplicationContext wadlApplicationContext;

        @Override
        public void onLoaded(AbstractResourceModelContext modelContext) {
            checkWadlApplicationContext();
        }

        @Override
        public DispatchingListener adapt(DispatchingListener dispatchingListener) {
            checkWadlApplicationContext();
            return dispatchingListener;
        }

        @Override
        public RequestListener adapt(RequestListener requestListener) {
            checkWadlApplicationContext();
            return requestListener;
        }

        @Override
        public ResponseListener adapt(ResponseListener responseListener) {
            checkWadlApplicationContext();
            return responseListener;
        }

        private void checkWadlApplicationContext() {
            assertTrue(wadlApplicationContext != null);
        }

        @Override
        public void onError(long id, Throwable ex) {
        }

        @Override
        public void onResponse(long id, ContainerResponse response) {
            requestCount++;

            if((requestCount % 2) == 0)
                wadlApplicationContext.setWadlGenerationEnabled(!wadlApplicationContext.isWadlGenerationEnabled());
        }

        @Override
        public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
        }
    }

    public void testInject() {
        initiateWebApplication(A.class, ProviderAdapter.class);

        resource("randomUniqueDumbResource123456").get(String.class);
    }

    public void testInjectSingleton() {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getClasses().add(A.class);
        rc.getSingletons().add(new ProviderAdapter());

        initiateWebApplication(rc);

        resource("randomUniqueDumbResource123456").get(String.class);
    }

    public void testEnableDisableRuntime() {
        initiateWebApplication(A.class, ProviderAdapter.class);
        WebResource r = resource("/", false);
        r.addFilter(new LoggingFilter());

        ClientResponse response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);

        response = r.path("randomUniqueDumbResource123456").options(ClientResponse.class);
        assertTrue(response.getStatus() == 200);

        response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 404);

        response = r.path("randomUniqueDumbResource123456").options(ClientResponse.class);
        assertTrue(response.getStatus() == 204);

        response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
    }

    public void testEnableDisableRuntimeSingleton() {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getClasses().add(A.class);
        rc.getSingletons().add(new ProviderAdapter());

        initiateWebApplication(rc);

        WebResource r = resource("/", false);
        r.addFilter(new LoggingFilter());

        ClientResponse response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);

        response = r.path("randomUniqueDumbResource123456").options(ClientResponse.class);
        assertTrue(response.getStatus() == 200);

        response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 404);

        response = r.path("randomUniqueDumbResource123456").options(ClientResponse.class);
        assertTrue(response.getStatus() == 204);

        response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
    }
}

