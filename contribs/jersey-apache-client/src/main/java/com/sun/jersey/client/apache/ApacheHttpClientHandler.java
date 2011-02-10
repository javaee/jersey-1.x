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
package com.sun.jersey.client.apache;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.header.InBoundHeaders;

import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Context;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

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
 */
public final class ApacheHttpClientHandler implements ClientHandler {

    private final HttpClient client;

    private final ClientConfig config;

    private final ApacheHttpMethodExecutor methodExecutor;

    @Context
    private MessageBodyWorkers workers;

    /**
     * Create a new root handler with an {@link HttpClient}.
     *
     * @param client the {@link HttpClient}.
     */
    public ApacheHttpClientHandler(HttpClient client) {
        this(client, new DefaultApacheHttpClientConfig(), new DefaultApacheHttpMethodExecutor(client));
    }

    /**
     * Create a new root handler with an {@link HttpClient}.
     *
     * @param client the {@link HttpClient}.
     * @param config the client configuration.
     */
    public ApacheHttpClientHandler(HttpClient client, ClientConfig config) {
        this(client, config, new DefaultApacheHttpMethodExecutor(client));
    }

    /**
     * Create a new root handler with an {@link HttpClient}.
     *
     * @param client the {@link HttpClient}.
     * @param config the client configuration.
     * @param methodExecutor the method executor.
     */
    public ApacheHttpClientHandler(HttpClient client, ClientConfig config,
            ApacheHttpMethodExecutor methodExecutor) {
        this.client = client;
        this.config = config;
        
        client.getParams().setAuthenticationPreemptive(config.getPropertyAsFeature(
                ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION));
        final Integer connectTimeout = (Integer)config.getProperty(
                ApacheHttpClientConfig.PROPERTY_CONNECT_TIMEOUT);
        if (connectTimeout != null) {
            client.getHttpConnectionManager().getParams().setConnectionTimeout(connectTimeout);
        }

        this.methodExecutor = methodExecutor;
    }

    /**
     * Get the client config.
     * 
     * @return the client config.
     */
    public ClientConfig getConfig() {
        return config;
    }

    /**
     * Get the {@link HttpClient}.
     *
     * @return the {@link HttpClient}.
     */
    public HttpClient getHttpClient() {
        return client;
    }

    @Override
    public ClientResponse handle(final ClientRequest cr)
            throws ClientHandlerException {

        final HttpMethod method = getHttpMethod(cr);

        methodExecutor.executeMethod(method, cr);

        try {
            ClientResponse r = new ClientResponse(method.getStatusCode(),
                    getInBoundHeaders(method),
                    new HttpClientResponseInputStream(method),
                    workers);
            if (!r.hasEntity()) {
                r.bufferEntity();
                r.close();
            }
            return r;
        } catch (Exception e) {
            method.releaseConnection();
            throw new ClientHandlerException(e);
        }
    }

    private HttpMethod getHttpMethod(ClientRequest cr) {
        final String strMethod = cr.getMethod();
        final String uri = cr.getURI().toString();

        if (strMethod.equals("GET")) {
            return new GetMethod(uri);
        } else if (strMethod.equals("POST")) {
            return new PostMethod(uri);
        } else if (strMethod.equals("PUT")) {
            return new PutMethod(uri);
        } else if (strMethod.equals("DELETE")) {
            return new CustomMethod("DELETE", uri);
        } else if (strMethod.equals("HEAD")) {
            return new HeadMethod(uri);
        } else if (strMethod.equals("OPTIONS")) {
            return new OptionsMethod(uri);
        } else {
            return new CustomMethod(strMethod, uri);
        }
    }

    private static class CustomMethod extends EntityEnclosingMethod {

        private String method;

        CustomMethod(String method, String uri) {
            super(uri);

            this.method = method;
        }

        @Override
        public String getName() {
            return method;
        }
    }

    private InBoundHeaders getInBoundHeaders(HttpMethod method) {
        InBoundHeaders headers = new InBoundHeaders();
        Header[] respHeaders = method.getResponseHeaders();
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

        private final HttpMethod method;

        HttpClientResponseInputStream(HttpMethod method) throws IOException {
            super(getInputStream(method));
            this.method = method;
        }

        @Override
        public void close()
                throws IOException {
            super.close();
            method.releaseConnection();
        }
    }

    private static InputStream getInputStream(HttpMethod method) throws IOException {
        InputStream i = method.getResponseBodyAsStream();

        if (i == null) {
            return new ByteArrayInputStream(new byte[0]);
        } else if (i.markSupported()) {
            return i;
        } else {
            return new BufferedInputStream(i, ReaderWriter.BUFFER_SIZE);
        }
    }

    /* package */ ApacheHttpMethodExecutor getMethodProcessor() {
        return methodExecutor;
    }
}
