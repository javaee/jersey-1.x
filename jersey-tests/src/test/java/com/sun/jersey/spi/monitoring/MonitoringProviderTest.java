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

package com.sun.jersey.spi.monitoring;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Jakub.Podlesak@Oracle.Com
 */
public class MonitoringProviderTest extends AbstractResourceTester {


    public MonitoringProviderTest(String testName) {
        super(testName);
    }

    @Provider
    public static class DummyMonitor implements RequestListenerAdapter, DispatchingListenerAdapter, ResponseListenerAdapter {

        public static class DummyMonitoringProvider implements RequestListener, ResponseListener, DispatchingListener {

            public static int reqStart;
            public static int reqEnd;
            public static int resMethodPreDispatch;
            public static int error;

            @Override
            public void onSubResource(long id, Class subResource) {
            }

            @Override
            public void onSubResourceLocator(long id, AbstractSubResourceLocator locator) {
            }

            @Override
            public void onResourceMethod(long id, AbstractResourceMethod method) {
                resMethodPreDispatch++;
            }

            @Override
            public void onRequest(long id, ContainerRequest request) {
                reqStart++;
            }

            @Override
            public void onError(long id, Throwable ex) {
                error++;
            }

            @Override
            public void onResponse(long id, ContainerResponse response) {
                reqEnd++;
            }

            @Override
            public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
            }
        }

        private final DummyMonitoringProvider dummyMonitoringProvider = new DummyMonitoringProvider();

        @Override
        public DispatchingListener adapt(DispatchingListener dispatchingListener) {
            return dummyMonitoringProvider;
        }

        @Override
        public RequestListener adapt(RequestListener requestListener) {
            return dummyMonitoringProvider;
        }

        @Override
        public ResponseListener adapt(ResponseListener responseListener) {
            return dummyMonitoringProvider;
        }
    }

    @Path("dummy")
    public static class DummyResource {
        @GET
        @Produces("plain/text")
        public String getText() {
            return "dummy";
        }
    }

    public void testGet() throws Exception {

        ResourceConfig rc = new DefaultResourceConfig(DummyResource.class);
        final DummyMonitor dummyMonitor = new DummyMonitor();

        rc.getSingletons().add(dummyMonitor);
        initiateWebApplication(rc);

        assertTrue(DummyMonitor.DummyMonitoringProvider.resMethodPreDispatch == 0);


        WebResource r = resource("/dummy");
        r.get(String.class);

        assertTrue(DummyMonitor.DummyMonitoringProvider.resMethodPreDispatch == 1);
    }
}
