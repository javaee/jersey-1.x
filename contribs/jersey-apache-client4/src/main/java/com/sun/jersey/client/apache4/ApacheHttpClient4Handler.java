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
package com.sun.jersey.client.apache4;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.util.ReaderWriter;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.BasicHttpContext;

import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A root handler with Jakarta Commons HttpClient acting as a backend.
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
public final class ApacheHttpClient4Handler extends TerminatingClientHandler {

//    private static final DefaultCredentialsProvider DEFAULT_CREDENTIALS_PROVIDER =
//            new DefaultCredentialsProvider();

    private final HttpClient client;
    private final CookieStore cookieStore;
    private final boolean preemptiveBasicAuth;

    /**
     * Create a new root handler with an {@link HttpClient}.
     *
     * @param client the {@link HttpClient}.
     * @param cookieStore {@link CookieStore} instance
     * @param preemptiveBasicAuth turns on preemptive basic authentication
     */
    public ApacheHttpClient4Handler(final HttpClient client,
                                    final CookieStore cookieStore,
                                    final boolean preemptiveBasicAuth) {
        this.client = client;
        this.cookieStore = cookieStore;
        this.preemptiveBasicAuth = preemptiveBasicAuth;
    }

    /**
     * Get the {@link HttpClient}.
     *
     * @return the {@link HttpClient}.
     */
    public HttpClient getHttpClient() {
        return client;
    }

    /**
     * Get the {@link CookieStore}.
     *
     * @return the {@link CookieStore} instance or null when
     * ApacheHttpClient4Config.PROPERTY_DISABLE_COOKIES set to true.
     */
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public ClientResponse handle(final ClientRequest cr)
            throws ClientHandlerException {

        final HttpUriRequest request = getUriHttpRequest(cr);

        writeOutBoundHeaders(cr.getHeaders(), request);

        try {

            HttpResponse response;

            if(preemptiveBasicAuth) {
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicScheme = new BasicScheme();
                authCache.put(getHost(request), basicScheme);
                BasicHttpContext localContext = new BasicHttpContext();
                localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);

                response = getHttpClient().execute(getHost(request), request, localContext);
            } else {
                response = getHttpClient().execute(getHost(request), request);
            }

            ClientResponse r = new ClientResponse(response.getStatusLine().getStatusCode(),
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

    private HttpHost getHost(final HttpUriRequest request) {
        return new HttpHost(request.getURI().getHost(), request.getURI().getPort());
    }

    private HttpUriRequest getUriHttpRequest(final ClientRequest cr) {
        final String strMethod = cr.getMethod();
        final URI uri = cr.getURI();

        final HttpEntity entity = getHttpEntity(cr);
        final HttpUriRequest request;

        if (strMethod.equals("GET")) {
            request = new HttpGet(uri);
        } else if (strMethod.equals("POST")) {
            request = new HttpPost(uri);
        } else if (strMethod.equals("PUT")) {
            request = new HttpPut(uri);
        } else if (strMethod.equals("DELETE")) {
            request = new HttpDelete(uri);
        } else if (strMethod.equals("HEAD")) {
            request = new HttpHead(uri);
        } else if (strMethod.equals("OPTIONS")) {
            request = new HttpOptions(uri);
        } else {
            request = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return strMethod;
                }

                @Override
                public URI getURI() {
                    return uri;
                }
            };
        }

        if(entity != null && request instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        } else if (entity != null) {
            throw new ClientHandlerException("Adding entity to http method " + cr.getMethod() + " is not supported.");
        }

        return request;
    }

    private HttpEntity getHttpEntity(final ClientRequest cr) {
        final Object entity = cr.getEntity();

        if(entity == null)
            return null;

        final RequestEntityWriter requestEntityWriter = getRequestEntityWriter(cr);

        try {
            HttpEntity httpEntity = new AbstractHttpEntity() {
                    @Override
                    public boolean isRepeatable() {
                        return false;
                    }

                    @Override
                    public long getContentLength() {
                        return requestEntityWriter.getSize();
                    }

                    @Override
                    public InputStream getContent() throws IOException, IllegalStateException {
                        return null;
                    }

                    @Override
                    public void writeTo(OutputStream outputStream) throws IOException {
                        requestEntityWriter.writeRequestEntity(outputStream);
                    }

                    @Override
                    public boolean isStreaming() {
                        return false;
                    }
                };

            if(cr.getProperties().get(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE) != null) {
                // TODO return InputStreamEntity
                return httpEntity;
            } else {
                return new BufferedHttpEntity(httpEntity);
            }
        } catch (Exception ex) {
            // TODO warning/error?
        }

        return null;
    }

    private void writeOutBoundHeaders(final MultivaluedMap<String, Object> headers, final HttpUriRequest request) {
        for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                request.addHeader(e.getKey(), headerValueToString(vs.get(0)));
            } else {
                StringBuilder b = new StringBuilder();
                for (Object v : e.getValue()) {
                    if (b.length() > 0) {
                        b.append(',');
                    }
                    b.append(headerValueToString(v));
                }
                request.addHeader(e.getKey(), b.toString());
            }
        }
    }

    private InBoundHeaders getInBoundHeaders(final HttpResponse response) {
        final InBoundHeaders headers = new InBoundHeaders();
        final Header[] respHeaders = response.getAllHeaders();
        for (Header header : respHeaders) {
            List<String> list = headers.get(header.getName());
            if (list == null) {
                list = new ArrayList<String>();
            }
            list.add(header.getValue());
            headers.put(header.getName(), list);
        }
        return headers;
    }

    private static final class HttpClientResponseInputStream extends FilterInputStream {

        HttpClientResponseInputStream(final HttpResponse response) throws IOException {
            super(getInputStream(response));
        }

        @Override
        public void close()
                throws IOException {
            super.close();
        }
    }

    private static InputStream getInputStream(final HttpResponse response) throws IOException {

        if(response.getEntity() == null) {
            return new ByteArrayInputStream(new byte[0]);
        } else {
            final InputStream i = response.getEntity().getContent();
            if(i.markSupported())
                return i;
            return new BufferedInputStream(i, ReaderWriter.BUFFER_SIZE);
        }
    }
}
