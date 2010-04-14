/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.client.apache;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.CommittingOutputStream;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.ApacheHttpClientState;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultCredentialsProvider;
import com.sun.jersey.core.header.InBoundHeaders;

import com.sun.jersey.core.util.ReaderWriter;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

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
public final class ApacheHttpClientHandler extends TerminatingClientHandler {

    private static final DefaultCredentialsProvider DEFAULT_CREDENTIALS_PROVIDER =
            new DefaultCredentialsProvider();
    
    private final HttpClient client;

    /**
     * Create a new root handler with an {@link HttpClient}.
     *
     * @param client the {@link HttpClient}.
     */
    public ApacheHttpClientHandler(HttpClient client) {
        this.client = client;
    }

    /**
     * Get the {@link HttpClient}.
     *
     * @return the {@link HttpClient}.
     */
    public HttpClient getHttpClient() {
        return client;
    }
    
    public ClientResponse handle(final ClientRequest cr)
            throws ClientHandlerException {

        final Map<String, Object> props = cr.getProperties();

        final HttpMethod method = getHttpMethod(cr);

        method.setDoAuthentication(true);

        final HttpMethodParams methodParams = method.getParams();

        // Set the handle cookies property
        if (!cr.getPropertyAsFeature(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES)) {
            methodParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        }

        // Set the interactive and credential provider properties
        if (cr.getPropertyAsFeature(ApacheHttpClientConfig.PROPERTY_INTERACTIVE)) {
            CredentialsProvider provider = (CredentialsProvider)props.get(ApacheHttpClientConfig.PROPERTY_CREDENTIALS_PROVIDER);
            if (provider == null) {
                provider = DEFAULT_CREDENTIALS_PROVIDER;
            }
            methodParams.setParameter(CredentialsProvider.PROVIDER, provider);
        } else {
            methodParams.setParameter(CredentialsProvider.PROVIDER, null);
        }

        // Set the read timeout
        final Integer readTimeout = (Integer)props.get(ApacheHttpClientConfig.PROPERTY_READ_TIMEOUT);
        if (readTimeout != null) {
            methodParams.setSoTimeout(readTimeout);
        }

        if (method instanceof EntityEnclosingMethod) {
            final EntityEnclosingMethod entMethod = (EntityEnclosingMethod) method;

            if (cr.getEntity() != null) {
                final RequestEntityWriter re = getRequestEntityWriter(cr);
                final Integer chunkedEncodingSize = (Integer)props.get(ApacheHttpClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE);
                if (chunkedEncodingSize != null) {
                    // There doesn't seems to be a way to set the chunk size.
                    entMethod.setContentChunked(true);

                    // It is not possible for a MessageBodyWriter to modify
                    // the set of headers before writing out any bytes to
                    // the OutputStream
                    // This makes it impossible to use the multipart
                    // writer that modifies the content type to add a boundary
                    // parameter
                    writeOutBoundHeaders(cr.getMetadata(), method);

                    // Do not buffer the request entity when chunked encoding is
                    // set
                    entMethod.setRequestEntity(new RequestEntity() {
                        public boolean isRepeatable() {
                            return false;
                        }

                        public void writeRequest(OutputStream out) throws IOException {
                            re.writeRequestEntity(out);
                        }

                        public long getContentLength() {
                            return re.getSize();
                        }

                        public String getContentType() {
                            return re.getMediaType().toString();
                        }

                    });

                } else {
                    entMethod.setContentChunked(false);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        re.writeRequestEntity(new CommittingOutputStream(baos) {
                            @Override
                            protected void commit() throws IOException {
                                writeOutBoundHeaders(cr.getMetadata(), method);
                            }
                        });
                    } catch (IOException ex) {
                        throw new ClientHandlerException(ex);
                    }

                    final byte[] content = baos.toByteArray();
                    entMethod.setRequestEntity(new RequestEntity() {
                        public boolean isRepeatable() {
                            return true;
                        }

                        public void writeRequest(OutputStream out) throws IOException {
                            out.write(content);
                        }

                        public long getContentLength() {
                            return content.length;
                        }

                        public String getContentType() {
                            return re.getMediaType().toString();
                        }

                    });
                }

            }
        } else {
            writeOutBoundHeaders(cr.getMetadata(), method);
        
            // Follow redirects
            method.setFollowRedirects(cr.getPropertyAsFeature(ApacheHttpClientConfig.PROPERTY_FOLLOW_REDIRECTS));
        }

        try {
            client.executeMethod(getHostConfiguration(client, props), method, getHttpState(props));

            ClientResponse r = new ClientResponse(method.getStatusCode(),
                    getInBoundHeaders(method),
                    new HttpClientResponseInputStream(method),
                    getMessageBodyWorkers());
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

    private HostConfiguration getHostConfiguration(HttpClient client, Map<String, Object> props) {
        Object proxy = props.get(ApacheHttpClientConfig.PROPERTY_PROXY_URI);
        if (proxy != null) {
            URI proxyUri = getProxyUri(proxy);

            String proxyHost = proxyUri.getHost();
            if (proxyHost == null) {
                proxyHost = "localhost";
            }

            int proxyPort = proxyUri.getPort();
            if (proxyPort == -1) {
                proxyPort = 8080;
            }

            HostConfiguration hostConfig = new HostConfiguration(client.getHostConfiguration());
            String setHost = hostConfig.getProxyHost();
            int setPort = hostConfig.getProxyPort();

            if ((setHost == null) ||
                    (!setHost.equals(proxyHost)) ||
                    (setPort == -1) ||
                    (setPort != proxyPort)) {
                hostConfig.setProxyHost(new ProxyHost(proxyHost, proxyPort));
            }
            return hostConfig;
        } else {
            return null;
        }
    }

    private HttpState getHttpState(Map<String, Object> props) {
        ApacheHttpClientState httpState = (ApacheHttpClientState) props.get(DefaultApacheHttpClientConfig.PROPERTY_HTTP_STATE);
        if (httpState != null) {
            return httpState.getHttpState();
        } else {
            return null;
        }
    }

    private URI getProxyUri(Object proxy) {
        if (proxy instanceof URI) {
            return (URI) proxy;
        } else if (proxy instanceof String) {
            return URI.create((String) proxy);
        } else {
            throw new ClientHandlerException("The proxy URI property MUST be an instance of String or URI");
        }
    }

    private void writeOutBoundHeaders(MultivaluedMap<String, Object> metadata, HttpMethod method) {
        for (Map.Entry<String, List<Object>> e : metadata.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                method.setRequestHeader(e.getKey(), headerValueToString(vs.get(0)));
            } else {
                StringBuilder b = new StringBuilder();
                for (Object v : e.getValue()) {
                    if (b.length() > 0) {
                        b.append(',');
                    }
                    b.append(headerValueToString(v));
                }
                method.setRequestHeader(e.getKey(), b.toString());
            }
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
}
