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
package com.sun.jersey.client.urlconnection;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.CommittingOutputStream;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.header.InBoundHeaders;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * A terminating client handler that uses {@link HttpURLConnection} or
 * {@link HttpsURLConnection} to make HTTP requests and receive HTTP responses.
 * <p>
 * By default a {@link HttpURLConnection} or {@link HttpsURLConnection}
 * instance is obtained using {@link URL#openConnection() }. This behaviour
 * may be overridden by registering an {@link HttpURLConnectionFactory}
 * instance when constructing this class.
 * <p>
 * For SSL configuration of HTTPS the {@link HTTPSProperties} may be used
 * and an instance added as a property of the {@link Client} or
 * {@link ClientRequest}.
 * 
 * @author Paul.Sandoz@Sun.Com
 * @see HttpURLConnectionFactory
 */
public final class URLConnectionClientHandler extends TerminatingClientHandler {

    private final class URLConnectionResponse extends ClientResponse {
        private final String method;
        private final HttpURLConnection uc;

        URLConnectionResponse(int status, InBoundHeaders headers, InputStream entity, String method, HttpURLConnection uc) {
            super(status, headers, entity, getMessageBodyWorkers());
            this.method = method;
            this.uc = uc;
        }
        
        @Override
        public boolean hasEntity() {
            if (method.equals("HEAD") || getEntityInputStream() == null)
                return false;

            int l = uc.getContentLength();
            return l > 0 || l == -1;
        }

        @Override
        public String toString() {
            return uc.getRequestMethod() + " " + uc.getURL() + " returned a response status of " + this.getStatus();
        }
    }

    private HttpURLConnectionFactory httpURLConnectionFactory = null;

    /**
     * Construct a new instance with an HTTP URL connection factory.
     *
     * @param httpURLConnectionFactory the HTTP URL connection factory.
     */
    public URLConnectionClientHandler(HttpURLConnectionFactory httpURLConnectionFactory) {
        this.httpURLConnectionFactory = httpURLConnectionFactory;
    }

    public URLConnectionClientHandler() {
        this(null);
    }


    /**
     * ClientRequest handler.
     *
     * @param ro ClientRequest
     * @return Server response represented as ClientResponse
     */
    public ClientResponse handle(ClientRequest ro) {
        try {
            return _invoke(ro);
        } catch (Exception ex) {
            throw new ClientHandlerException(ex);
        }
    }

    private ClientResponse _invoke(final ClientRequest ro) throws IOException {
        final HttpURLConnection uc;

        if(this.httpURLConnectionFactory == null) {
            uc = (HttpURLConnection)ro.getURI().toURL().openConnection();
        } else {
            uc = this.httpURLConnectionFactory.getHttpURLConnection(ro.getURI().toURL());
        }

        Integer readTimeout = (Integer)ro.getProperties().get(
                ClientConfig.PROPERTY_READ_TIMEOUT);
        if (readTimeout != null) {
            uc.setReadTimeout(readTimeout);
        }
        
        Integer connectTimeout = (Integer)ro.getProperties().get(
                ClientConfig.PROPERTY_CONNECT_TIMEOUT);
        if (connectTimeout != null) {
            uc.setConnectTimeout(connectTimeout);
        }
        
        Boolean followRedirects = (Boolean)ro.getProperties().get(
                ClientConfig.PROPERTY_FOLLOW_REDIRECTS);
        if (followRedirects != null) {
            uc.setInstanceFollowRedirects(followRedirects);
        }

        if (uc instanceof HttpsURLConnection) {
            HTTPSProperties httpsProperties = (HTTPSProperties) ro.getProperties().get(
                    HTTPSProperties.PROPERTY_HTTPS_PROPERTIES);
            if (httpsProperties != null) {
                httpsProperties.setConnection((HttpsURLConnection)uc);
            }
        }

        // Set the request method
        uc.setRequestMethod(ro.getMethod());

        // Write the request headers
        writeOutBoundHeaders(ro.getHeaders(), uc);

        // Write the entity (if any)
        Object entity = ro.getEntity();
        if (entity != null) {
            uc.setDoOutput(true);

            writeRequestEntity(ro, new RequestEntityWriterListener() {
                public void onRequestEntitySize(long size) {
                    if (size != -1 && size < Integer.MAX_VALUE) {
                        // HttpURLConnection uses the int type for content length
                        uc.setFixedLengthStreamingMode((int)size);
                    } else {
                        // TODO it appears HttpURLConnection has some bugs in
                        // chunked encoding
                        // uc.setChunkedStreamingMode(0);
                        Integer chunkedEncodingSize = (Integer)ro.getProperties().get(
                                ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE);
                        if (chunkedEncodingSize != null) {
                            uc.setChunkedStreamingMode(chunkedEncodingSize);
                        }
                    }
                }

                public OutputStream onGetOutputStream() throws IOException {
                    return new CommittingOutputStream() {
                        @Override
                        protected OutputStream getOutputStream() throws IOException {
                            return uc.getOutputStream();
                        }

                        @Override
                        public void commit() throws IOException {
                            writeOutBoundHeaders(ro.getHeaders(), uc);
                        }
                    };
                }
            });
        } else {
            writeOutBoundHeaders(ro.getHeaders(), uc);
        }
        
        // Return the in-bound response
        return new URLConnectionResponse(
                uc.getResponseCode(),
                getInBoundHeaders(uc),
                getInputStream(uc),
                ro.getMethod(),
                uc);
    }
    
    private void writeOutBoundHeaders(MultivaluedMap<String, Object> metadata, HttpURLConnection uc) {
        for (Map.Entry<String, List<Object>> e : metadata.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                uc.setRequestProperty(e.getKey(), headerValueToString(vs.get(0)));
            } else {
                StringBuilder b = new StringBuilder();
                boolean add = false;
                for (Object v : e.getValue()) {
                    if (add) b.append(',');
                    add = true;
                    b.append(headerValueToString(v));
                }
                uc.setRequestProperty(e.getKey(), b.toString());
            }

        }
    }

    private InBoundHeaders getInBoundHeaders(HttpURLConnection uc) {
        InBoundHeaders headers = new InBoundHeaders();
        for (Map.Entry<String, List<String>> e : uc.getHeaderFields().entrySet()) {
            if (e.getKey() != null)
                headers.put(e.getKey(), e.getValue());
        }
        return headers;
    }

    private InputStream getInputStream(HttpURLConnection uc) throws IOException {
        if (uc.getResponseCode() < 300) {
            return uc.getInputStream();
        } else {
            InputStream ein = uc.getErrorStream();
            return (ein != null)
                    ? ein : new ByteArrayInputStream(new byte[0]);
        }
    }
}