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

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Cookie;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.non.blocking.config.NonBlockingClientConfig;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.util.ReaderWriter;

import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A root handler with Ning Async HTTP client acting as a backend.
 * <p>
 * Client operations are thread safe, the HTTP connection may
 * be shared between different threads.
 * <p>
 * If a response entity is obtained that is an instance of {@link Closeable}
 * then the instance MUST be closed after processing the entity to release
 * connection-based resources.
 * <p>
 * If a {@link ClientResponse} is obtained and an entity is not read from the
 * response then {@link ClientResponse#close() } MUST be called after processing
 * the response to release connection-based resources.
 * <p>
 * The following methods are currently supported: HEAD, GET, POST, PUT, DELETE
 * and OPTIONS.
 * <p>
 * Chunked transfer encoding can be enabled or disabled but configuration of
 * the chunked encoding size is not possible. If the 
 * {@link ClientConfig#PROPERTY_CHUNKED_ENCODING_SIZE} property is set
 * to a non-null value then chunked transfer encoding is enabled.
 *
 * @author jorgeluisw@mac.com
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public final class NonBlockingClientHandler extends TerminatingClientHandler {

    private final AsyncHttpClient client;
    private final ExecutorService executorService;
    private final ClientConfig clientConfig;

    private final ThreadLocal<Collection<Cookie>> cookieStore = new ThreadLocal<Collection<Cookie>>() {
        @Override
        protected Collection<Cookie> initialValue() {
            return new HashSet<Cookie>();
        }
    };

    public NonBlockingClientHandler(final ClientConfig cc) {

        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();

        if(cc != null) {
            Object o = cc.getProperty(NonBlockingClientConfig.PROPERTY_EXECUTOR_SERVICE);
            if(o != null && (o instanceof ExecutorService)) {
                builder = builder.setExecutorService((ExecutorService)o);
                this.executorService = (ExecutorService)o;
            } else {
                final Object threadpoolSize = cc.getProperties().get(ClientConfig.PROPERTY_THREADPOOL_SIZE);

                if(threadpoolSize != null && threadpoolSize instanceof Integer && (Integer)threadpoolSize > 0) {
                    this.executorService = Executors.newFixedThreadPool((Integer) threadpoolSize);
                } else {
                    this.executorService = Executors.newCachedThreadPool();
                }
                builder = builder.setExecutorService(this.executorService);
            }

            Integer timeout = (Integer)cc.getProperties().get(ClientConfig.PROPERTY_CONNECT_TIMEOUT);
            if(timeout != null)
                builder = builder.setConnectionTimeoutInMs(timeout);

            Object username = cc.getProperties().get(NonBlockingClientConfig.PROPERTY_AUTH_USERNAME);
            Object password = cc.getProperties().get(NonBlockingClientConfig.PROPERTY_AUTH_PASSWORD);

            if((username != null) && (password != null)) {
                boolean preemptiveAuth = cc.getPropertyAsFeature(NonBlockingClientConfig.PROPERTY_AUTH_PREEMPTIVE);
                Object scheme = cc.getProperties().get(NonBlockingClientConfig.PROPERTY_AUTH_SCHEME);

                Realm.AuthScheme authScheme = Realm.AuthScheme.NONE;
                if(scheme != null) {
                    if(scheme.equals("DIGEST")) {
                        authScheme = Realm.AuthScheme.DIGEST;
                    } else if(scheme.equals("KERBEROS")) {
                        authScheme = Realm.AuthScheme.KERBEROS;
                    } else if(scheme.equals("NTLM")) {
                        authScheme = Realm.AuthScheme.NTLM;
                    } else if(scheme.equals("SPNEGO")) {
                        authScheme = Realm.AuthScheme.SPNEGO;
                    }
                } else {
                    authScheme = Realm.AuthScheme.BASIC;
                }

                Realm realm = new Realm.RealmBuilder()
                        .setPrincipal(username.toString())
                        .setPassword(password.toString())
                        .setUsePreemptiveAuth(preemptiveAuth)
                        .setScheme(authScheme).build();

                builder = builder.setRealm(realm);
            }
        } else {
            this.executorService = Executors.newCachedThreadPool();
            builder.setExecutorService(this.executorService);
        }

        this.client =new AsyncHttpClient(builder.build());
        this.clientConfig = cc;
    }

    public AsyncHttpClient getHttpClient() {
        return client;
    }

    public final ExecutorService getExecutorService() {
        return executorService;
    }

    public ClientResponse handle(final ClientRequest cr)
            throws ClientHandlerException {

        final Request request = getRequest(cr);

        try {
            Future<Response> response;

            response = getHttpClient().executeRequest(request);

            return getClientResponse(response.get());
        } catch (Exception e) {
            throw new ClientHandlerException(e);
        }
    }

    ClientResponse getClientResponse(Response response) {

        if(this.clientConfig != null && (!this.clientConfig.getPropertyAsFeature(NonBlockingClientConfig.PROPERTY_DISABLE_COOKIES)))
            cookieStore.get().addAll(response.getCookies());

        try {
            ClientResponse r = new ClientResponse(response.getStatusCode(),
                    getInBoundHeaders(response),
                    new HttpClientResponseInputStream(response),
                    getMessageBodyWorkers());
            if (!r.hasEntity()) {
                r.bufferEntity();
                r.close();
            }

            return r;
        } catch (Exception e) {
            throw new ClientHandlerException(e);
        }
    }

    Request getRequest(final ClientRequest cr) {
        final String strMethod = cr.getMethod();
        final URI uri = cr.getURI();

        RequestBuilder builder = new RequestBuilder(strMethod).setUrl(uri.toString());

        final Request.EntityWriter entity = getHttpEntity(cr);

        if(entity != null) {
            builder = builder.setBody(entity);
        }

        ProxyServer proxyServer = createProxyServer(cr);
        if(proxyServer != null)
            builder.setProxyServer(proxyServer);

        if(this.clientConfig != null) {
            if(!this.clientConfig.getPropertyAsFeature(NonBlockingClientConfig.PROPERTY_DISABLE_COOKIES)) {
                for(Cookie cookie : cookieStore.get())
                    builder = builder.addCookie(cookie);
                cookieStore.remove();
            }

            if(this.clientConfig.getPropertyAsFeature(ClientConfig.PROPERTY_FOLLOW_REDIRECTS))
                builder = builder.setFollowRedirects(true);
            else
                builder = builder.setFollowRedirects(false);
        }

        Request request = builder.build();

//        /* extremely ugly, unnecessary and inefficient. Unfortunately it is needed
//           because client side providers which modifies header values when entity is
//           being written. Ning Client writes is too late, so header is ClientRequest is
//           not yet changed and not propagated into Nings Request.
//           Question is - should we support this case? Yes - for now.
//        */
//        try {
//            if(request.getEntityWriter() != null)
//                request.getEntityWriter().writeEntity(new OutputStream() {
//                    @Override
//                    public void write(int i) throws IOException {
//                    }
//                });
//        } catch (IOException ignored) {
//        }

        writeOutBoundHeaders(cr.getHeaders(), request);

        return request;
    }

    private ProxyServer createProxyServer(ClientRequest cr) {
        Object proxyHost = cr.getProperties().get(NonBlockingClientConfig.PROPERTY_PROXY_HOST);
        if(proxyHost != null) {
            ProxyServerBuilder proxyBuilder = new ProxyServerBuilder((String) proxyHost);

            Integer proxyPort = (Integer)cr.getProperties().get(NonBlockingClientConfig.PROPERTY_PROXY_PORT);
            if(proxyPort != null)
                proxyBuilder.setPort(proxyPort);

            String proxyUser = (String)cr.getProperties().get(NonBlockingClientConfig.PROPERTY_PROXY_USERNAME);
            if(proxyUser != null) {
                String proxyPass = (String)cr.getProperties().get(NonBlockingClientConfig.PROPERTY_PROXY_PASSWORD);
                if(proxyPass != null) {
                    proxyBuilder.setUsername(proxyUser);
                    proxyBuilder.setPassword(proxyPass);
                }
            }

            String proxyProto = (String)cr.getProperties().get(NonBlockingClientConfig.PROPERTY_PROXY_PROTOCOL);
            if(proxyProto != null) {
                if(proxyProto.equals("HTTP")) {
                    proxyBuilder.setProtocol(ProxyServer.Protocol.HTTP);
                } else if(proxyProto.equals("HTTPS")) {
                    proxyBuilder.setProtocol(ProxyServer.Protocol.HTTPS);
                } else if(proxyProto.equals("NTLM")) {
                    proxyBuilder.setProtocol(ProxyServer.Protocol.NTLM);
                } else if(proxyProto.equals("SPNEGO")) {
                    proxyBuilder.setProtocol(ProxyServer.Protocol.SPNEGO);
                } else if(proxyProto.equals("KERBEROS")) {
                    proxyBuilder.setProtocol(ProxyServer.Protocol.KERBEROS);
                }
            }
            return proxyBuilder.build();
        } else {
            return null;
        }
    }

    private Request.EntityWriter getHttpEntity(final ClientRequest cr) {
        final Object entity = cr.getEntity();

        if(entity == null)
            return null;

        final RequestEntityWriter requestEntityWriter = getRequestEntityWriter(cr);

        return new Request.EntityWriter() {
            @Override
            public void writeEntity(OutputStream out) throws IOException {
                requestEntityWriter.writeRequestEntity(out);
            }
        };
    }

    protected final static void writeOutBoundHeaders(final MultivaluedMap<String, Object> headers, final Request request) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                request.getHeaders().add(e.getKey(), ClientRequest.getHeaderValue(vs.get(0)));
            } else {
                StringBuilder b = new StringBuilder();
                for (Object v : e.getValue()) {
                    if (b.length() > 0) {
                        b.append(',');
                    }
                    b.append(ClientRequest.getHeaderValue(v));
                }
                request.getHeaders().add(e.getKey(), b.toString());
            }
        }
    }

    private InBoundHeaders getInBoundHeaders(final Response response) throws ExecutionException, InterruptedException {
        final InBoundHeaders headers = new InBoundHeaders();
        FluentCaseInsensitiveStringsMap responseHeaders = response.getHeaders();

        for (FluentCaseInsensitiveStringsMap.Entry<String, List<String>> header : responseHeaders) {
            headers.put(header.getKey(), header.getValue());
        }

        return headers;
    }

    static final class HttpClientResponseInputStream extends FilterInputStream {

        HttpClientResponseInputStream(Response response) throws IOException, ExecutionException, InterruptedException {
            super(getInputStream(response));
        }

        @Override
        public void close()
                throws IOException {
            super.close();
        }
    }

    private static InputStream getInputStream(final Response response) throws IOException, ExecutionException, InterruptedException {
        if(!response.hasResponseBody()) {
            return new ByteArrayInputStream(new byte[0]);
        } else {
            final InputStream i = response.getResponseBodyAsStream();
            if(i.markSupported())
                return i;
            return new BufferedInputStream(i, ReaderWriter.BUFFER_SIZE);
        }
    }

    private static class ProxyServerBuilder {
        private String host = null;
        private Integer port = null;
        private String username = null;
        private String password = null;
        private ProxyServer.Protocol protocol = null;

        public ProxyServerBuilder(String host) {
            this.host = host;
        };

        public ProxyServerBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public ProxyServerBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public ProxyServerBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public ProxyServerBuilder setProtocol(ProxyServer.Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public ProxyServer build() {
            return new ProxyServer(protocol, host, port, username, password);
        }
    }
}
