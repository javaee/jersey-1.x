/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.client.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.async.AsyncClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.impl.async.FutureClientResponseListener;
import com.sun.jersey.client.proxy.ViewProxy;
import com.sun.jersey.client.proxy.ViewProxyProvider;
import com.sun.jersey.core.hypermedia.HypermediaController;
import java.lang.reflect.Proxy;
import java.util.concurrent.Future;

/**
 * Hypermedia View Proxy Provider.
 * 
 * @author Santiago.PericasGeertsen@sun.com
 * @author Paul.Sandoz@sun.com
 */
public class HypermediaViewProxyProvider implements ViewProxyProvider {

    public <T> ViewProxy<T> proxy(final Client client, final Class<T> ctlr) {
        if (!ctlr.isAnnotationPresent(HypermediaController.class))
            return null;

        return new ViewProxy<T>() {

            private T process(ClientRequest request, ClientResponse response) {
                HypermediaController ctrl = ctlr.getAnnotation(HypermediaController.class);
                Class<?> c = ctrl.model();
                Object instance;
                if (c == ClientResponse.class) {
                    instance = c.cast(response);
                } else if (response.getStatus() < 300) {
                    instance = response.getEntity(c);
                } else {
                    throw new UniformInterfaceException(response,
                        request.getPropertyAsFeature(
                            ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
                }

                return (T) Proxy.newProxyInstance(ctlr.getClassLoader(),
                        new Class[] { ctlr },
                        new ControllerInvocationHandler(client, instance, response, ctlr));
            }

            public T view(Class<T> type, ClientRequest request, ClientHandler handler) {                
                ClientResponse response = handler.handle(request);
                return process(request, response);
            }

            public T view(T v, ClientRequest request, ClientHandler handler) {
                throw new UnsupportedOperationException();
            }

            public Future<T> asyncView(Class<T> type, final ClientRequest request, AsyncClientHandler handler) {

                final FutureClientResponseListener<T> ftw = new FutureClientResponseListener<T>() {
                    protected T get(ClientResponse response) {
                        return process(request, response);
                    }
                };

                ftw.setCancelableFuture(handler.handle(request, ftw));
                return ftw;
            }

            public Future<T> asyncView(T v, ClientRequest request, AsyncClientHandler handler) {
                throw new UnsupportedOperationException();
            }

            public T view(Class<T> type, ClientResponse response) {
                throw new UnsupportedOperationException();
            }

            public T view(T v, ClientResponse cr) {
                throw new UnsupportedOperationException();
            }

        };
    }
}
