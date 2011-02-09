/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.client.view.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.async.AsyncClientHandler;
import com.sun.jersey.client.impl.async.FutureClientResponseListener;
import com.sun.jersey.client.proxy.ViewProxy;
import com.sun.jersey.client.proxy.ViewProxyProvider;
import com.sun.jersey.client.view.exception.ClientErrorException;
import com.sun.jersey.client.view.exception.ClientRuntimeException;
import com.sun.jersey.client.view.exception.ResponseNotHandledByViewException;
import com.sun.jersey.client.view.exception.ServerErrorException;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import javax.ws.rs.core.Response;

/**
 * Hypermedia View Proxy Provider.
 * 
 * @author algermissen@acm.org
 * @author Paul.Sandoz@sun.com
 */
public class ViewImplProxyProvider implements ViewProxyProvider {

    public <T> ViewProxy<T> proxy(final Client client, final Class<T> ctlr) {
        // TODO return null if not possible to proxy

        return new ViewProxy<T>() {

            private void invoke(Method m, Object o, Object[] params) {
                try {
                    m.invoke(o, params);
                } catch (InvocationTargetException e) {
                    Throwable target = e.getTargetException();

                    if (target instanceof RuntimeException) {
                        // rethrow
                        throw (RuntimeException) target;
                    } else {
                        // wrap checked exception in a runtime exception
                        // not sure the following is the right one to use
                        throw new ClientRuntimeException(target);
                    }
                } catch (Exception e) {
                    throw new ClientRuntimeException(e);
                } finally {
                    // close cr if cr not injected.
                }
            }

            private T getInstance(Class<T> type) {
                try {
                    Constructor<T> c = type.getConstructor(new Class<?>[]{});
                    return c.newInstance(new Object[]{});
                } catch (Exception e) {
                    throw new ClientRuntimeException(e);
                }
            }

            private void processRequest(ViewModel viewModel, ClientRequest request) {
                String[] consumes = viewModel.getConsumesFor(request.getMethod());
                // overwriting any previous accept() call
                if (consumes.length > 0) {
                    request.getHeaders().remove("Accept");
                    for (String consume : consumes) {
                        request.getHeaders().add("Accept", consume);

                    }
                }
            }

            private T processResponse(ViewModel viewModel, T v, ClientRequest request, ClientResponse response) {
                AnnotatedMethod m = ViewModelMatcher.findMethod(viewModel, request.getMethod(),
                        response);
                if (m == null) {
                    if (response.getClientResponseStatus().getFamily() == Response.Status.Family.CLIENT_ERROR) {
                        throw new ClientErrorException(response);
                    } else if (response.getClientResponseStatus().getFamily() == Response.Status.Family.SERVER_ERROR) {
                        throw new ServerErrorException(response);
                    } else {
                        throw new ResponseNotHandledByViewException(response);
                    }
                }

                // During this call, a fag should be set that indicates
                // whether response or output stream has been injected.
                // If not injected, we can close the response in the
                // finally.
                Object[] args = ArgumentInjector.makeArgs(client, m, response,
                        request.getMethod().equals("GET") ? request.getURI() : null);

                invoke(m.getMethod(), v, args);
                return v;
            }

            public T view(Class<T> type, ClientRequest request, ClientHandler handler) {
                return view(getInstance(type), request, handler);
            }

            public T view(T v, ClientRequest request, ClientHandler handler) {
                ViewModel viewModel = ViewModeller.createViewModel(v.getClass());

                processRequest(viewModel, request);

                ClientResponse response = handler.handle(request);

                return processResponse(viewModel, v, request, response);
            }

            
            public Future<T> asyncView(Class<T> type, ClientRequest request, AsyncClientHandler handler) {
                return asyncView(getInstance(type), request, handler);
            }

            public Future<T> asyncView(final T v, final ClientRequest request, AsyncClientHandler handler) {
                final ViewModel viewModel = ViewModeller.createViewModel(v.getClass());

                processRequest(viewModel, request);
                
                final FutureClientResponseListener<T> ftw = new FutureClientResponseListener<T>() {
                    protected T get(ClientResponse response) {
                        return processResponse(viewModel, v, request, response);
                    }
                };

                ftw.setCancelableFuture(handler.handle(request, ftw));
                return ftw;
            }


            public T view(Class<T> type, ClientResponse response) {
                return view(getInstance(type), response);
            }

            public T view(T v, ClientResponse cr) {
                ViewModel viewModel = ViewModeller.createViewModel(v.getClass());

                AnnotatedMethod m = ViewModelMatcher.findMethod(viewModel, "GET",
                        cr);
                if (m == null) {
                    if (cr.getClientResponseStatus().getFamily() == Response.Status.Family.CLIENT_ERROR) {
                        throw new ClientErrorException(cr);
                    } else if (cr.getClientResponseStatus().getFamily() == Response.Status.Family.SERVER_ERROR) {
                        throw new ServerErrorException(cr);
                    } else {
                        throw new ResponseNotHandledByViewException(cr);
                    }
                }

                // During this call, a fag should be set that indicates
                // whether response or output stream has been injected.
                // If not injected, we can close the response in the
                // finally.
                Object[] args = ArgumentInjector.makeArgs(client, m, cr, null);

                invoke(m.getMethod(), v, args);
                return v;
            }

        };
    }
}
