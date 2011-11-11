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
package com.sun.jersey.client.non.blocking.impl;

import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.RequestFilter;
import com.ning.http.client.filter.ResponseFilter;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.non.blocking.NonBlockingClient;
import com.sun.jersey.client.non.blocking.config.DefaultNonBlockingClientConfig;
import com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FilterTest extends AbstractGrizzlyServerTester {

    @Path("one")
    public static class OneResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    public FilterTest(String name) {
        super(name);
    }

    public static class CustomFilter extends ClientFilter {
        public boolean used = false;

        @Override
        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
            this.used = true;
            return getNext().handle(cr);
        }
    }

    @Test
    public void testSyncFilter() {
        startServer(OneResource.class);

        NonBlockingClient c = new NonBlockingClient();
        CustomFilter cf = new CustomFilter();
        c.addFilter(cf);
        c.resource(getUri().path("one").build()).get(String.class);

        assertTrue(cf.used == true);

        stopServer();
    }

    // @Test
    // doesn't work and most likely never will
    public void _testAsyncFilter() {
        startServer(OneResource.class);

        NonBlockingClient c = new NonBlockingClient();
        CustomFilter cf = new CustomFilter();
        c.addFilter(cf);
        AsyncWebResource asyncWebResource = c.asyncResource(getUri().path("one").build());
        Future<String> stringFuture = asyncWebResource.get(String.class);
        try {
            stringFuture.get();
        } catch (InterruptedException e) {
            assertTrue(false);
        } catch (ExecutionException e) {
            assertTrue(false);
        }

        assertTrue(cf.used == true);

        stopServer();
    }

    public static class CustomRequestFilter implements RequestFilter {
        public boolean used = false;

        @Override
        public FilterContext filter(FilterContext filterContext) throws FilterException {
            used = true;
            return filterContext;
        }
    }

    public static class CustomResponseFilter implements ResponseFilter {
        public boolean used = false;

        @Override
        public FilterContext filter(FilterContext filterContext) throws FilterException {
            this.used = true;
            return filterContext;
        }
    }


    @Test
    public void testRequestFilter() {
        startServer(OneResource.class);

        CustomRequestFilter rf = new CustomRequestFilter();

        NonBlockingClientConfig nbcc = new DefaultNonBlockingClientConfig();
        nbcc.getProperties().put(NonBlockingClientConfig.PROPERTY_REQUEST_FILTERS, rf);

        NonBlockingClient c = NonBlockingClient.create(nbcc);
        CustomFilter cf = new CustomFilter();
        AsyncWebResource asyncWebResource = c.asyncResource(getUri().path("one").build());
        Future<String> stringFuture = asyncWebResource.get(String.class);
        try {
            stringFuture.get();
        } catch (InterruptedException e) {
            assertTrue(false);
        } catch (ExecutionException e) {
            assertTrue(false);
        }

        assertTrue(rf.used == true);

        stopServer();
    }

    @Test
    public void testResponseFilter() {
        startServer(OneResource.class);

        CustomResponseFilter rf = new CustomResponseFilter();

        NonBlockingClientConfig nbcc = new DefaultNonBlockingClientConfig();
        nbcc.getProperties().put(NonBlockingClientConfig.PROPERTY_RESPONSE_FILTERS, rf);

        NonBlockingClient c = NonBlockingClient.create(nbcc);
        CustomFilter cf = new CustomFilter();
        AsyncWebResource asyncWebResource = c.asyncResource(getUri().path("one").build());
        Future<String> stringFuture = asyncWebResource.get(String.class);
        try {
            stringFuture.get();
        } catch (InterruptedException e) {
            assertTrue(false);
        } catch (ExecutionException e) {
            assertTrue(false);
        }

        assertTrue(rf.used == true);

        stopServer();
    }

    @Test
    public void testRequestFilterList() {
        startServer(OneResource.class);

        ArrayList<CustomRequestFilter> requestFilters = new ArrayList<CustomRequestFilter>();

        requestFilters.add(new CustomRequestFilter());

        NonBlockingClientConfig nbcc = new DefaultNonBlockingClientConfig();
        nbcc.getProperties().put(NonBlockingClientConfig.PROPERTY_REQUEST_FILTERS, requestFilters);

        NonBlockingClient c = NonBlockingClient.create(nbcc);
        CustomFilter cf = new CustomFilter();
        AsyncWebResource asyncWebResource = c.asyncResource(getUri().path("one").build());
        Future<String> stringFuture = asyncWebResource.get(String.class);
        try {
            stringFuture.get();
        } catch (InterruptedException e) {
            assertTrue(false);
        } catch (ExecutionException e) {
            assertTrue(false);
        }

        for(CustomRequestFilter requestFilter : requestFilters) {
            assertTrue(requestFilter.used == true);
        }

        stopServer();
    }

    @Test
    public void testResponseFilterList() {
        startServer(OneResource.class);

        ArrayList<CustomResponseFilter> responseFilters = new ArrayList<CustomResponseFilter>();

        responseFilters.add(new CustomResponseFilter());

        NonBlockingClientConfig nbcc = new DefaultNonBlockingClientConfig();
        nbcc.getProperties().put(NonBlockingClientConfig.PROPERTY_RESPONSE_FILTERS, responseFilters);

        NonBlockingClient c = NonBlockingClient.create(nbcc);
        CustomFilter cf = new CustomFilter();
        AsyncWebResource asyncWebResource = c.asyncResource(getUri().path("one").build());
        Future<String> stringFuture = asyncWebResource.get(String.class);
        try {
            stringFuture.get();
        } catch (InterruptedException e) {
            assertTrue(false);
        } catch (ExecutionException e) {
            assertTrue(false);
        }

        for(CustomResponseFilter responseFilter : responseFilters) {
            assertTrue(responseFilter.used == true);
        }

        stopServer();
    }

}
