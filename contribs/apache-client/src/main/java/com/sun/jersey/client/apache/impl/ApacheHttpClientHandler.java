/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package com.sun.jersey.client.apache.impl;

import com.sun.jersey.client.apache.config.DefaultCredentialsProvider;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import com.sun.jersey.core.header.InBoundHeaders;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import com.sun.jersey.client.apache.config.ApacheHttpClientState;
import com.sun.jersey.spi.MessageBodyWorkers;

import java.net.URI;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.auth.CredentialsProvider;

/**
 * A root handler with Jakarta Commons HttpClient acting as a backend.
 * <p>
 *  <strong>Please Note:</strong>
 *  <ul>
 *     <li>There is a single HTTPClient per HttpClientHandler. It may
 *     be worth while to use it as a root for multiple Clients to
 *     save on resources. There should typically be a single
 *     HttpClientHandler per application.</li>
 *     <li>Client operations are thread safe, the HTTP connection may
 *     be shared between different threads. If you retrive a
 *     response entity input stream you <strong>must</strong> call
 *     close() on the stream when you're done with it in order to
 *     release the connection to other threads.</li>
 *     <li>In this initial implementation, only standard methods
 *     (GET, POST, DELETE, OPTIONS, HEAD, and PUT) are
 *     supported.</li>
 *     <li>Chunk encoding is a true/false operation in HTTPClient
 *      there's no way of specifying a chunk size.  If you set
 *      PROPERTY_CHUNKED_ENCODING_SIZE to anything other than null it
 *      will be set to true.</li>
 *     <li>In this initial implementation, chunk encoding probably
 *     doesn't matter since we write entities to a byte array before
 *     we transmit them</li>
 *  </ul>
 * </p>
 *
 * @author jorgew
 */
public final class ApacheHttpClientHandler implements ClientHandler {

    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    private static final DefaultCredentialsProvider DEFAULT_CREDENTIALS_PROVIDER =
            new DefaultCredentialsProvider();

    @Context
    private MessageBodyWorkers bodyContext;
    
    private HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {
        HttpMethod method = null;
        try {

            Map<String, Object> props = cr.getProperties();
            HttpClientParams clientParams = client.getParams();
            HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();

            Boolean handleCookies = (Boolean) props.get(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES);
            if ((handleCookies == null) || (!handleCookies)) {
                clientParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            }

            Object proxy = props.get(ApacheHttpClientConfig.PROPERTY_PROXY_URI);
            if (proxy != null) {
                URI proxyUri = getProxyUri(proxy);

                String proxyHost = proxyUri.getHost();
                if (proxyHost == null)
                    proxyHost = "localhost";
                
                int proxyPort = proxyUri.getPort();
                if (proxyPort == -1)
                    proxyPort = 8080;
                
                HostConfiguration hostConfig = client.getHostConfiguration();
                String setHost = hostConfig.getProxyHost();
                int setPort = hostConfig.getProxyPort();

                if ((setHost == null) ||
                        (!setHost.equals(proxyHost)) ||
                        (setPort == -1) ||
                        (setPort != proxyPort)) {
                    hostConfig.setProxyHost(new ProxyHost(proxyHost, proxyPort));
                }
            } else {
                client.setHostConfiguration(HostConfiguration.ANY_HOST_CONFIGURATION);
            }

            ApacheHttpClientState httpState = (ApacheHttpClientState) props.get(DefaultApacheHttpClientConfig.PROPERTY_HTTP_STATE);
            if (httpState != null) {
                client.setState(httpState.getHttpState());
            }

            Boolean preemptiveAuth = (Boolean) props.get(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION);
            if (preemptiveAuth != null) {
                clientParams.setAuthenticationPreemptive(preemptiveAuth);
            } else {
                clientParams.setAuthenticationPreemptive(false);
            }

            Boolean interactiveAuth = (Boolean) props.get(ApacheHttpClientConfig.PROPERTY_INTERACTIVE);
            if ((interactiveAuth != null) || (interactiveAuth)) {
                CredentialsProvider provider = (CredentialsProvider) props.get(ApacheHttpClientConfig.PROPERTY_CREDENTIALS_PROVIDER);
                if (provider == null) {
                    provider = DEFAULT_CREDENTIALS_PROVIDER;
                }
                clientParams.setParameter(CredentialsProvider.PROVIDER, provider);
            } else {
                clientParams.setParameter(CredentialsProvider.PROVIDER, null);
            }

            Integer readTimeout = (Integer) props.get(ApacheHttpClientConfig.PROPERTY_READ_TIMEOUT);
            if (readTimeout != null) {
                params.setSoTimeout(readTimeout);
            }

            Integer connectTimeout = (Integer) props.get(ApacheHttpClientConfig.PROPERTY_CONNECT_TIMEOUT);
            if (connectTimeout != null) {
                params.setConnectionTimeout(connectTimeout);
            }

            String strMethod = cr.getMethod();
            String uri = cr.getURI().toString();

            if (strMethod.equals("GET")) {
                method = new GetMethod(uri);
            } else if (strMethod.equals("POST")) {
                method = new PostMethod(uri);
            } else if (strMethod.equals("PUT")) {
                method = new PutMethod(uri);
            } else if (strMethod.equals("DELETE")) {
                method = new DeleteMethod(uri);
            } else if (strMethod.equals("HEAD")) {
                method = new HeadMethod(uri);
            } else if (strMethod.equals("OPTIONS")) {
                method = new OptionsMethod(uri);
            } else {
                throw new ClientHandlerException("Method " + method + " is not supported.");
            }

            method.setDoAuthentication(true);

            writeHeaders(cr.getMetadata(), method);

            if (method instanceof EntityEnclosingMethod) {
                EntityEnclosingMethod entMethod = (EntityEnclosingMethod) method;
                Integer chunkedEncodingSize = (Integer) props.get(ApacheHttpClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE);
                if (chunkedEncodingSize != null) {
                    //
                    //  There doesn't seems to be a way to set the
                    //  chunk size.
                    //
                    entMethod.setContentChunked(true);
                } else {
                    entMethod.setContentChunked(false);
                }

                Object entity = cr.getEntity();
                if (entity != null) {
                    writeEntity(entMethod, cr, entity);
                }
            } else {
                Boolean followRedirects = (Boolean) props.get(ApacheHttpClientConfig.PROPERTY_FOLLOW_REDIRECTS);
                if (followRedirects != null) {
                    method.setFollowRedirects(followRedirects);
                } else {
                    method.setFollowRedirects(true);
                }
            }

            client.executeMethod(method);

            return new HttpClientResponse(method);

        } catch (Exception e) {
            if (method != null) {
                method.releaseConnection();
            }
            throw new ClientHandlerException(e);
        }
    }

    private URI getProxyUri(Object proxy) {
        if (proxy instanceof URI) {
            return (URI)proxy;
        } else if (proxy instanceof String) {
            return URI.create((String)proxy);
        } else {
            throw new ClientHandlerException("The proxy URI property MUST be an instance of String or URI");
        }
    }
    
    private void writeEntity(EntityEnclosingMethod entMethod, ClientRequest cr, Object entity)
            throws IOException {

        MultivaluedMap<String, Object> metadata = cr.getMetadata();
        MediaType mediaType = null;
        final Object mediaTypeHeader = metadata.getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            mediaType = (MediaType) mediaTypeHeader;
        } else {
            if (mediaTypeHeader != null) {
                mediaType = MediaType.valueOf(mediaTypeHeader.toString());
            } else {
                mediaType = new MediaType("application", "octet-stream");
            }
        }

        Type entityType = null;
        if (entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity) entity;
            entityType = ge.getType();
            entity = ge.getEntity();
        } else {
            entityType = entity.getClass();
        }
        final Class entityClass = entity.getClass();

        final MessageBodyWriter bw = bodyContext.getMessageBodyWriter(entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType);
        if (bw == null) {
            throw new ClientHandlerException(
                    "A message body writer for Java type, " + entity.getClass() +
                    ", and MIME media type, " + mediaType + ", was not found");
        }

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final OutputStream out = cr.getAdapter().adapt(cr, bout);
        bw.writeTo(entity, entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType, metadata, out);
        out.flush();
        out.close();

        entMethod.setRequestEntity(new ByteArrayRequestEntity(bout.toByteArray(), mediaType.toString()));
    }

    private void writeHeaders(MultivaluedMap<String, Object> metadata, HttpMethod method) {
        for (Map.Entry<String, List<Object>> e : metadata.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                method.setRequestHeader(e.getKey(), getHeaderValue(vs.get(0)));
            } else {
                StringBuilder b = new StringBuilder();
                boolean add = false;
                for (Object v : e.getValue()) {
                    if (add) {
                        b.append(',');
                    }
                    add = true;
                    b.append(getHeaderValue(v));
                }
                method.setRequestHeader(e.getKey(), b.toString());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String getHeaderValue(Object headerValue) {
        HeaderDelegate hp = RuntimeDelegate.getInstance().
                createHeaderDelegate(headerValue.getClass());
        return hp.toString(headerValue);
    }

    private final class HttpClientResponseInputStream extends FilterInputStream {

        private final HttpMethod method;

        HttpClientResponseInputStream(HttpMethod method)
                throws IOException {
            super(method.getResponseBodyAsStream());
            this.method = method;
        }

        @Override
        public void close()
                throws IOException {
            super.close();
            method.releaseConnection();
        }
    }

    private final class HttpClientResponse extends ClientResponse {

        private final HttpMethod method;
        private final MultivaluedMap<String, String> metadata;
        private Map<String, Object> properties;
        private InputStream in;
        private int status;

        HttpClientResponse(HttpMethod method) {
            this.method = method;
            this.status = method.getStatusCode();

            this.metadata = new InBoundHeaders();
            Header[] respHeaders = method.getResponseHeaders();
            for (Header header : respHeaders) {
                List<String> list = metadata.get(header.getName());
                if (list == null) {
                    list = new ArrayList<String>();
                }
                list.add(header.getValue());
                metadata.put(header.getName(), list);
            }

            try {
                in = new HttpClientResponseInputStream(method);
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public Response.Status getResponseStatus() {
            return Response.Status.fromStatusCode(status);
        }

        @Override
        public void setResponseStatus(Response.Status status) {
            setStatus(status.getStatusCode());
        }

        @Override
        public MultivaluedMap<String, String> getMetadata() {
            return metadata;
        }

        @Override
        public boolean hasEntity() {
            if ((method instanceof HeadMethod) ||
                    (in == null)) {
                return false;
            }

            Header contentLength = method.getResponseHeader("Content-Length");
            if (contentLength != null) {
                try {
                    int len = Integer.parseInt(contentLength.getValue());
                    return len > 0 || len == -1;
                } catch (NumberFormatException nfe) {
                }
            }

            return false;
        }

        @Override
        public InputStream getEntityInputStream() {
            return in;
        }

        @Override
        public void setEntityInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public <T> T getEntity(Class<T> c) {
            return getEntity(c, c);
        }

        @Override
        public <T> T getEntity(GenericType<T> gt) {
            return getEntity(gt.getRawClass(), gt.getType());
        }

        private <T> T getEntity(Class<T> c, Type type) {
            try {
                MediaType mediaType = getType();
                final MessageBodyReader<T> br = bodyContext.getMessageBodyReader(
                        c, type,
                        EMPTY_ANNOTATIONS, mediaType);
                if (br == null) {
                    throw new ClientHandlerException(
                            "A message body reader for Java type, " + c +
                            ", and MIME media type, " + mediaType + ", was not found");
                }
                T t = br.readFrom(c, type, EMPTY_ANNOTATIONS, mediaType, metadata, in);
                if (!(t instanceof InputStream)) {
                    in.close();
                }
                in = null;
                return t;
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        @Override
        public Map<String, Object> getProperties() {
            if (properties != null) {
                return properties;
            }

            return properties = new HashMap<String, Object>();
        }
    }
}
