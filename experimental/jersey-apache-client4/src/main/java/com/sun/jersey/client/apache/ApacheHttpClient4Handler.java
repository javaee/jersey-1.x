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
import org.apache.http.entity.AbstractHttpEntity;

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

    /**
     * Create a new root handler with an {@link HttpClient}.
     *
     * @param client the {@link HttpClient}.
     */
    public ApacheHttpClient4Handler(HttpClient client, CookieStore cookieStore) {
        this.client = client;
        this.cookieStore = cookieStore;
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

        final Map<String, Object> props = cr.getProperties();

        final HttpUriRequest request = getUriHttpRequest(cr);

//        request.setDoAuthentication(true);

//        final HttpMethodParams methodParams = request.getParams();

//        // Set the handle cookies property
//        if (!cr.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_HANDLE_COOKIES)) {
//            methodParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
//        }
//
//        // Set the interactive and credential provider properties
//        if (cr.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_INTERACTIVE)) {
//            CredentialsProvider provider = (CredentialsProvider)props.get(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER);
//            if (provider == null) {
//                provider = DEFAULT_CREDENTIALS_PROVIDER;
//            }
//            methodParams.setParameter(CredentialsProvider.PROVIDER, provider);
//        } else {
//            methodParams.setParameter(CredentialsProvider.PROVIDER, null);
//        }

//        // Set the read timeout
//        final Integer readTimeout = (Integer)props.get(ApacheHttpClient4Config.PROPERTY_READ_TIMEOUT);
//        if (readTimeout != null) {
//            methodParams.setSoTimeout(readTimeout);
//        }

//        if (request instanceof EntityEnclosingMethod) {
//            final EntityEnclosingMethod entMethod = (EntityEnclosingMethod) request;
//
//            if (cr.getEntity() != null) {
//                final RequestEntityWriter re = getRequestEntityWriter(cr);
//                final Integer chunkedEncodingSize = (Integer)props.get(ApacheHttpClient4Config.PROPERTY_CHUNKED_ENCODING_SIZE);
//                if (chunkedEncodingSize != null) {
//                    // There doesn't seems to be a way to set the chunk size.
//                    entMethod.setContentChunked(true);
//
//                    // It is not possible for a MessageBodyWriter to modify
//                    // the set of headers before writing out any bytes to
//                    // the OutputStream
//                    // This makes it impossible to use the multipart
//                    // writer that modifies the content type to add a boundary
//                    // parameter
//                    writeOutBoundHeaders(cr.getHeaders(), request);
//
//                    // Do not buffer the request entity when chunked encoding is
//                    // set
//                    entMethod.setRequestEntity(new RequestEntity() {
//                        public boolean isRepeatable() {
//                            return false;
//                        }
//
//                        public void writeRequest(OutputStream out) throws IOException {
//                            re.writeRequestEntity(out);
//                        }
//
//                        public long getContentLength() {
//                            return re.getSize();
//                        }
//
//                        public String getContentType() {
//                            return re.getMediaType().toString();
//                        }
//
//                    });
//
//                } else {
//                    entMethod.setContentChunked(false);
//
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    try {
//                        re.writeRequestEntity(new CommittingOutputStream(baos) {
//                            @Override
//                            protected void commit() throws IOException {
//                                writeOutBoundHeaders(cr.getMetadata(), request);
//                            }
//                        });
//                    } catch (IOException ex) {
//                        throw new ClientHandlerException(ex);
//                    }
//
//                    final byte[] content = baos.toByteArray();
//                    entMethod.setRequestEntity(new RequestEntity() {
//                        public boolean isRepeatable() {
//                            return true;
//                        }
//
//                        public void writeRequest(OutputStream out) throws IOException {
//                            out.write(content);
//                        }
//
//                        public long getContentLength() {
//                            return content.length;
//                        }
//
//                        public String getContentType() {
//                            return re.getMediaType().toString();
//                        }
//
//                    });
//                }
//
//            }
//        } else {
//            writeOutBoundHeaders(cr.getHeaders(), request);
//
//            // Follow redirects
//            request.setFollowRedirects(cr.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_FOLLOW_REDIRECTS));
//        }

        writeOutBoundHeaders(cr.getHeaders(), request);

//        ManagedClientConnection conn = null;
//
//        try {
//            ClientConnectionRequest connRequest = client.getConnectionManager().requestConnection(new HttpRoute(getHost(request)), null);
//            conn = connRequest.getConnection(10, TimeUnit.SECONDS);
//
//            conn.sendRequestHeader(request);
//            final HttpResponse response = conn.receiveResponseHeader();
//            conn.receiveResponseEntity(response);
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                BasicManagedEntity managedEntity = new BasicManagedEntity(entity, conn, false);
//                // Replace entity
//                response.setEntity(managedEntity);
//            }
//
//
//            ClientResponse r = new ClientResponse(response.getStatusLine().getStatusCode(),
//                    getInBoundHeaders(response),
//                    new HttpClientResponseInputStream(response),
//                    getMessageBodyWorkers());
//            if (!r.hasEntity()) {
//                r.bufferEntity();
//                r.close();
//            }
//
//            conn.releaseConnection();
//
//            return r;
//
//        } catch (IOException ex) {
//            // Abort connection upon an I/O error.
//            try {
//                conn.abortConnection();
//            } catch (IOException e) {
//                throw new ClientHandlerException(e);
//            }
//            throw new ClientHandlerException(ex);
//        } catch (Exception e) {
//            try {
//                conn.abortConnection();
//            } catch (IOException ex) {
//                throw new ClientHandlerException(ex);
//            }
//            throw new ClientHandlerException(e);
//        }

        try {

            final HttpResponse response = client.execute(getHost(request), request);

            ClientResponse r = new ClientResponse(response.getStatusLine().getStatusCode(),
                    getInBoundHeaders(response),
                    new HttpClientResponseInputStream(response, client),
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

    private HttpHost getHost(HttpUriRequest request) {
        return new HttpHost(request.getURI().getHost(), request.getURI().getPort());
    }

    private HttpUriRequest getUriHttpRequest(ClientRequest cr) {
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

    private HttpEntity getHttpEntity(ClientRequest cr) {
        final Object entity = cr.getEntity();


        if(entity == null)
            return null;


        final RequestEntityWriter requestEntityWriter = getRequestEntityWriter(cr);

//        if(entity instanceof ...)
//            return new ...

        try {
            return new AbstractHttpEntity() {
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
        } catch (Exception ignored) {}

        return null;
    }

//    private static class CustomMethod extends EntityEnclosingMethod {
//        private String method;
//
//        CustomMethod(String method, String uri) {
//            super(uri);
//
//            this.method = method;
//        }
//
//        @Override
//        public String getName() {
//            return method;
//        }
//    }
//
//    private HostConfiguration getHostConfiguration(HttpClient client, Map<String, Object> props) {
//        Object proxy = props.get(ApacheHttpClient4Config.PROPERTY_PROXY_URI);
//        if (proxy != null) {
//            URI proxyUri = getProxyUri(proxy);
//
//            String proxyHost = proxyUri.getHost();
//            if (proxyHost == null) {
//                proxyHost = "localhost";
//            }
//
//            int proxyPort = proxyUri.getPort();
//            if (proxyPort == -1) {
//                proxyPort = 8080;
//            }
//
//            HostConfiguration hostConfig = new HostConfiguration(client.getHostConfiguration());
//            String setHost = hostConfig.getProxyHost();
//            int setPort = hostConfig.getProxyPort();
//
//            if ((setHost == null) ||
//                    (!setHost.equals(proxyHost)) ||
//                    (setPort == -1) ||
//                    (setPort != proxyPort)) {
//                hostConfig.setProxyHost(new ProxyHost(proxyHost, proxyPort));
//            }
//            return hostConfig;
//        } else {
//            return null;
//        }
//    }
//
//    private HttpState getHttpState(Map<String, Object> props) {
//        ApacheHttpClient4State httpState = (ApacheHttpClient4State) props.get(DefaultApacheHttpClient4Config.PROPERTY_HTTP_STATE);
//        if (httpState != null) {
//            return httpState.getHttpState();
//        } else {
//            return null;
//        }
//    }

    private URI getProxyUri(Object proxy) {
        if (proxy instanceof URI) {
            return (URI) proxy;
        } else if (proxy instanceof String) {
            return URI.create((String) proxy);
        } else {
            throw new ClientHandlerException("The proxy URI property MUST be an instance of String or URI");
        }
    }

    private void writeOutBoundHeaders(MultivaluedMap<String, Object> headers, HttpUriRequest request) {
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

    private InBoundHeaders getInBoundHeaders(HttpResponse response) {
        InBoundHeaders headers = new InBoundHeaders();
        Header[] respHeaders = response.getAllHeaders();
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

        private final HttpClient client;
        private final HttpResponse response;

        HttpClientResponseInputStream(HttpResponse response, HttpClient client) throws IOException {
            super(getInputStream(response));
            this.client = client;
            this.response = response;
        }

        @Override
        public void close()
                throws IOException {
            super.close();
//            client.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS); // .shutdown(); // !!! TODO XXX UGLY
        }
    }

    private static InputStream getInputStream(HttpResponse response) throws IOException {

        if(response.getEntity() == null) {
            return new ByteArrayInputStream(new byte[0]);
        } else {
            InputStream i = response.getEntity().getContent();
            if(i.markSupported())
                return i;
            return new BufferedInputStream(i, ReaderWriter.BUFFER_SIZE);
        }
    }
}
