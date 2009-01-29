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
import java.io.OutputStream;


import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.header.InBoundHeaders;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.WriteRequestEntityListener;
import com.sun.jersey.client.apache.config.ApacheHttpClientState;

import java.net.URI;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
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
import org.apache.commons.httpclient.params.HttpMethodParams;

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
public final class ApacheHttpClientHandler extends TerminatingClientHandler {

    private static final DefaultCredentialsProvider DEFAULT_CREDENTIALS_PROVIDER =
            new DefaultCredentialsProvider();
    private HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

    public ClientResponse handle(ClientRequest cr)
            throws ClientHandlerException {

        Map<String, Object> props = cr.getProperties();

        // Seems that this property can only be set on the client
        client.getParams().setAuthenticationPreemptive(cr.getBooleanProperty(ApacheHttpClientConfig.PROPERTY_PREEMPTIVE_AUTHENTICATION));


        // This can be set on HttpConnectionParams
        Integer connectTimeout = (Integer)props.get(ApacheHttpClientConfig.PROPERTY_CONNECT_TIMEOUT);
        if (connectTimeout != null) {
            client.getHttpConnectionManager().getParams().setConnectionTimeout(connectTimeout);
        }


        final HttpMethod method = getHttpMethod(cr);

        method.setDoAuthentication(true);

        final HttpMethodParams methodParams = method.getParams();

        // Set the handle cookies property
        if (!cr.getBooleanProperty(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES)) {
            methodParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        }

        // Set the interactive and credential provider properties
        if (cr.getBooleanProperty(ApacheHttpClientConfig.PROPERTY_INTERACTIVE)) {
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

        writeOutBoundHeaders(cr.getMetadata(), method);

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
                final ByteArrayOutputStream bout = new ByteArrayOutputStream();

                try {
                    writeRequestEntity(cr, new WriteRequestEntityListener() {

                        public void onRequestEntitySize(long size) {
                        }

                        public OutputStream onGetOutputStream() throws IOException {
                            return bout;
                        }
                    });
                } catch (IOException e) {
                    throw new ClientHandlerException(e);
                }
                entMethod.setRequestEntity(new ByteArrayRequestEntity(bout.toByteArray(),
                        cr.getMetadata().getFirst("Content-Type").toString()));
            }
        } else {
            // Follow redirects
            method.setFollowRedirects(cr.getBooleanProperty(ApacheHttpClientConfig.PROPERTY_FOLLOW_REDIRECTS));
        }

        try {

            /*
            {
                HttpConnection connection = client.getHttpConnectionManager().
                        getConnection(getHostConfiguration(client, props));

                final Integer readTimeout = (Integer)props.get(ApacheHttpClientConfig.PROPERTY_READ_TIMEOUT);
                if (readTimeout != null) {
                    connection.getParams().setSoTimeout(readTimeout);
                }

                Integer connectTimeout = (Integer)props.get(ApacheHttpClientConfig.PROPERTY_CONNECT_TIMEOUT);
                if (connectTimeout != null) {
                    connection.getParams().setConnectionTimeout(connectTimeout);
                }

                method.execute(getHttpState(client, props), connection);
            }
            */

            client.executeMethod(getHostConfiguration(client, props), method, getHttpState(client, props));
            return new HttpClientResponse(method);
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
            return new DeleteMethod(uri);
        } else if (strMethod.equals("HEAD")) {
            return new HeadMethod(uri);
        } else if (strMethod.equals("OPTIONS")) {
            return new OptionsMethod(uri);
        } else {
            throw new ClientHandlerException("Method " + strMethod + " is not supported.");
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
            return client.getHostConfiguration();
        }
    }

    private HttpState getHttpState(HttpClient client, Map<String, Object> props) {
        ApacheHttpClientState httpState = (ApacheHttpClientState) props.get(DefaultApacheHttpClientConfig.PROPERTY_HTTP_STATE);
        if (httpState != null) {
            return httpState.getHttpState();
        } else {
            return client.getState();
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

        HttpClientResponse(HttpMethod method) throws IOException {
            super(method.getStatusCode(), getInBoundHeaders(method),
                    new HttpClientResponseInputStream(method), getMessageBodyWorkers());
            this.method = method;
        }

        @Override
        public boolean hasEntity() {
            if (method instanceof HeadMethod) {
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
    }
}
