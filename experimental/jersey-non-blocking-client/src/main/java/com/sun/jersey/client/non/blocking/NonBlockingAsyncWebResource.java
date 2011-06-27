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

package com.sun.jersey.client.non.blocking;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.async.FutureListener;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@inheritDoc}
 *
 * @author pavel.bucek@oracle.com
 */
public class NonBlockingAsyncWebResource extends AsyncWebResource {
    private final NonBlockingClient client;

    protected NonBlockingAsyncWebResource(NonBlockingClient c, CopyOnWriteHashMap<String, Object> properties, URI u) {
        super(c, properties, u);

        this.client = c;
    }

    private NonBlockingAsyncWebResource(NonBlockingAsyncWebResource that, UriBuilder ub) {
        super(that, ub);
        this.client = that.client;
    }

    @Override
    public AsyncWebResource queryParams(MultivaluedMap<String, String> params) {
        UriBuilder b = getUriBuilder();
        for (Map.Entry<String, List<String>> e : params.entrySet()) {
            for (String value : e.getValue())
                b.queryParam(e.getKey(), value);
        }
        return new NonBlockingAsyncWebResource(this, b);
    }

    @Override
    public AsyncWebResource queryParam(String key, String value) {
        UriBuilder b = getUriBuilder();
        b.queryParam(key, value);
        return new NonBlockingAsyncWebResource(this, b);
    }

    @Override
    public AsyncWebResource uri(URI uri) {
        UriBuilder b = getUriBuilder();
        String path = uri.getRawPath();
        if (path != null && path.length() > 0) {
            if (path.startsWith("/")) {
                b.replacePath(path);
            } else {
                b.path(path);
            }
        }
        String query = uri.getRawQuery();
        if (query != null && query.length() > 0) {
            b.replaceQuery(query);
        }
        return new NonBlockingAsyncWebResource(this, b);
    }

    @Override
    public AsyncWebResource path(String path) {
        return new NonBlockingAsyncWebResource(this, getUriBuilder().path(path));
    }

    @Override
    public Future<ClientResponse> handle(ClientRequest request, final FutureListener<ClientResponse> l) {
        Request r = client.getRequest(request);

        try {
            final ListenableFuture<Response> listenableFuture = this.client.getClientHandlerNing().getHttpClient().executeRequest(r);
            final Future<ClientResponse> jerseyFuture = new Future<ClientResponse>() {
                @Override
                public boolean cancel(boolean b) {
                    return listenableFuture.cancel(b);
                }

                @Override
                public boolean isCancelled() {
                    return listenableFuture.isCancelled();
                }

                @Override
                public boolean isDone() {
                    return listenableFuture.isDone();
                }

                @Override
                public ClientResponse get() throws InterruptedException, ExecutionException {
                    final Response response = listenableFuture.get();
                    if(response != null)
                        return client.getClientResponse(response);
                    return null;
                }

                @Override
                public ClientResponse get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                    final Response response = listenableFuture.get(l, timeUnit);
                    if(response != null)
                        return client.getClientResponse(response);
                    return null;
                }
            };

            listenableFuture.addListener(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                l.onComplete(jerseyFuture);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Executor() {
                        @Override
                        public void execute(Runnable runnable) {
                            runnable.run();
                        }
                    });

            return jerseyFuture;

        } catch (IOException e) {
            throw new ClientHandlerException(e);
        }
    }
}
