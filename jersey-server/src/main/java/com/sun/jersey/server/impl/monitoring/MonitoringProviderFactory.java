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
package com.sun.jersey.server.impl.monitoring;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.monitoring.DispatchingListener;
import com.sun.jersey.spi.monitoring.DispatchingListenerAdapter;
import com.sun.jersey.spi.monitoring.RequestListener;
import com.sun.jersey.spi.monitoring.RequestListenerAdapter;
import com.sun.jersey.spi.monitoring.ResponseListener;
import com.sun.jersey.spi.monitoring.ResponseListenerAdapter;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author Jakub.Podlesak@Oracle.Com
 */
public final class MonitoringProviderFactory {

    private MonitoringProviderFactory() {}

    private static class EmptyListener implements RequestListener, ResponseListener, DispatchingListener {
        @Override
        public void onSubResource(long id, Class subResource) {
        }

        @Override
        public void onSubResourceLocator(long id, AbstractSubResourceLocator locator) {
        }

        @Override
        public void onResourceMethod(long id, AbstractResourceMethod method) {
        }

        @Override
        public void onRequest(long id, ContainerRequest request) {
        }

        @Override
        public void onError(long id, Throwable ex) {
        }

        @Override
        public void onResponse(long id, ContainerResponse response) {
        }

        @Override
        public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
        }
    }

    private static class AggregatedRequestListener implements RequestListener {
        
        private final Set<RequestListener> listeners;

        private AggregatedRequestListener(Set<RequestListener> listeners) {
            this.listeners = Collections.unmodifiableSet(listeners);
        }

        @Override
        public void onRequest(long id, ContainerRequest request) {
            for (RequestListener requestListener : listeners) {
                requestListener.onRequest(id, request);
            }
        }
    }

    private static class AggregatedResponseListener implements ResponseListener {

        private final Set<ResponseListener> listeners;

        private AggregatedResponseListener(Set<ResponseListener> listeners) {
            this.listeners = Collections.unmodifiableSet(listeners);
        }

        @Override
        public void onError(long id, Throwable ex) {
            for (ResponseListener responseListener : listeners) {
                responseListener.onError(id, ex);
            }
        }

        @Override
        public void onResponse(long id, ContainerResponse response) {
            for (ResponseListener responseListener : listeners) {
                responseListener.onResponse(id, response);
            }
        }

        @Override
        public void onMappedException(long id, Throwable exception, ExceptionMapper mapper) {
            for (ResponseListener responseListener : listeners) {
                responseListener.onMappedException(id, exception, mapper);
            }
        }
    }


    private static class AggregatedDispatchingListener implements DispatchingListener {

        private final Set<DispatchingListener> listeners;

        private AggregatedDispatchingListener(Set<DispatchingListener> listeners) {
            this.listeners = Collections.unmodifiableSet(listeners);
        }

        @Override
        public void onSubResource(long id, Class subResource) {
            for (DispatchingListener dispatchingListener : listeners) {
                dispatchingListener.onSubResource(id, subResource);
            }
        }

        @Override
        public void onSubResourceLocator(long id, AbstractSubResourceLocator locator) {
            for (DispatchingListener dispatchingListener : listeners) {
                dispatchingListener.onSubResourceLocator(id, locator);
            }
        }

        @Override
        public void onResourceMethod(long id, AbstractResourceMethod method) {
            for (DispatchingListener dispatchingListener : listeners) {
                dispatchingListener.onResourceMethod(id, method);
            }
        }
    }


    private final static EmptyListener EMPTY_LISTENER = new EmptyListener();

    public static RequestListener createRequestListener(ProviderServices providerServices) {

        final Set<RequestListener> listeners = providerServices.getProvidersAndServices(RequestListener.class);
        RequestListener requestListener = listeners.isEmpty() ? EMPTY_LISTENER : new AggregatedRequestListener(listeners);

        for(RequestListenerAdapter a : providerServices.getProvidersAndServices(RequestListenerAdapter.class)) {
            requestListener = a.adapt(requestListener);
        }

        return requestListener;
    }

    public static DispatchingListener createDispatchingListener(ProviderServices providerServices) {

        final Set<DispatchingListener> listeners = providerServices.getProvidersAndServices(DispatchingListener.class);
        DispatchingListener dispatchingListener = listeners.isEmpty() ? EMPTY_LISTENER : new AggregatedDispatchingListener(listeners);

        for(DispatchingListenerAdapter a : providerServices.getProvidersAndServices(DispatchingListenerAdapter.class)) {
            dispatchingListener = a.adapt(dispatchingListener);
        }

        return dispatchingListener;
    }

    public static ResponseListener createResponseListener(ProviderServices providerServices) {

        final Set<ResponseListener> listeners = providerServices.getProvidersAndServices(ResponseListener.class);
        ResponseListener responseListener = listeners.isEmpty() ? EMPTY_LISTENER : new AggregatedResponseListener(listeners);

        for(ResponseListenerAdapter a : providerServices.getProvidersAndServices(ResponseListenerAdapter.class)) {
            responseListener = a.adapt(responseListener);
        }

        return responseListener;
    }
}
