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
package com.sun.jersey.client.apache;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.CommittingOutputStream;
import com.sun.jersey.api.client.RequestWriter;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.ApacheHttpClientState;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultCredentialsProvider;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * A default implementation of an Apache HTTP method executor.
 *
 * @author imyousuf@smartitengineering.com
 * @author Paul Sandoz
 */
public class DefaultApacheHttpMethodExecutor extends RequestWriter implements ApacheHttpMethodExecutor {

    private static final DefaultCredentialsProvider DEFAULT_CREDENTIALS_PROVIDER =
            new DefaultCredentialsProvider();

    private final HttpClient httpClient;

    public DefaultApacheHttpMethodExecutor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void executeMethod(final HttpMethod method, final ClientRequest cr) {
        final Map<String, Object> props = cr.getProperties();

        method.setDoAuthentication(true);

        final HttpMethodParams methodParams = method.getParams();

        // Set the handle cookies property
        if (!cr.getPropertyAsFeature(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES)) {
            methodParams.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        }

        // Set the interactive and credential provider properties
        if (cr.getPropertyAsFeature(ApacheHttpClientConfig.PROPERTY_INTERACTIVE)) {
            CredentialsProvider provider = (CredentialsProvider) props.get(
                    ApacheHttpClientConfig.PROPERTY_CREDENTIALS_PROVIDER);
            if (provider == null) {
                provider = DEFAULT_CREDENTIALS_PROVIDER;
            }
            methodParams.setParameter(CredentialsProvider.PROVIDER, provider);
        } else {
            methodParams.setParameter(CredentialsProvider.PROVIDER, null);
        }

        // Set the read timeout
        final Integer readTimeout = (Integer) props.get(ApacheHttpClientConfig.PROPERTY_READ_TIMEOUT);
        if (readTimeout != null) {
            methodParams.setSoTimeout(readTimeout);
        }

        if (method instanceof EntityEnclosingMethod) {
            final EntityEnclosingMethod entMethod = (EntityEnclosingMethod) method;

            if (cr.getEntity() != null) {
                final RequestEntityWriter re = getRequestEntityWriter(cr);
                final Integer chunkedEncodingSize = (Integer) props.get(ApacheHttpClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE);
                if (chunkedEncodingSize != null) {
                    // There doesn't seems to be a way to set the chunk size.
                    entMethod.setContentChunked(true);

                    // It is not possible for a MessageBodyWriter to modify
                    // the set of headers before writing out any bytes to
                    // the OutputStream
                    // This makes it impossible to use the multipart
                    // writer that modifies the content type to add a boundary
                    // parameter
                    writeOutBoundHeaders(cr.getHeaders(), method);

                    // Do not buffer the request entity when chunked encoding is
                    // set
                    entMethod.setRequestEntity(new RequestEntity() {

                        @Override
                        public boolean isRepeatable() {
                            return false;
                        }

                        @Override
                        public void writeRequest(OutputStream out) throws IOException {
                            re.writeRequestEntity(out);
                        }

                        @Override
                        public long getContentLength() {
                            return re.getSize();
                        }

                        @Override
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

                        @Override
                        public boolean isRepeatable() {
                            return true;
                        }

                        @Override
                        public void writeRequest(OutputStream out) throws IOException {
                            out.write(content);
                        }

                        @Override
                        public long getContentLength() {
                            return content.length;
                        }

                        @Override
                        public String getContentType() {
                            return re.getMediaType().toString();
                        }
                    });
                }
            } else {
                writeOutBoundHeaders(cr.getHeaders(), method);
            }
        } else {
            writeOutBoundHeaders(cr.getHeaders(), method);

            // Follow redirects
            method.setFollowRedirects(cr.getPropertyAsFeature(ApacheHttpClientConfig.PROPERTY_FOLLOW_REDIRECTS));
        }
        try {
            httpClient.executeMethod(getHostConfiguration(httpClient, props), method, getHttpState(props));
        } catch (Exception e) {
            method.releaseConnection();
            throw new ClientHandlerException(e);
        }
    }

    private HttpState getHttpState(Map<String, Object> props) {
        ApacheHttpClientState httpState = (ApacheHttpClientState) props.get(
                DefaultApacheHttpClientConfig.PROPERTY_HTTP_STATE);
        if (httpState != null) {
            return httpState.getHttpState();
        } else {
            return null;
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

            if ((setHost == null)
                    || (!setHost.equals(proxyHost))
                    || (setPort == -1)
                    || (setPort != proxyPort)) {
                hostConfig.setProxyHost(new ProxyHost(proxyHost, proxyPort));
            }
            return hostConfig;
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
}
