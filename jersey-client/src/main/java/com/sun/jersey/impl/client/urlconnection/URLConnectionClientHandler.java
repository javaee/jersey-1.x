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
package com.sun.jersey.impl.client.urlconnection;

import com.sun.jersey.spi.container.InBoundHeaders;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.spi.container.MessageBodyWorkers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class URLConnectionClientHandler implements ClientHandler {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    
    private final class URLConnectionResponse extends ClientResponse {
        private Map<String, Object> properties;
        private final String method;
        private int status;
        private final HttpURLConnection uc;
        private final MultivaluedMap<String, String> metadata;
        private InputStream in;

        URLConnectionResponse(String method, int status, HttpURLConnection uc) {
            this.method = method;
            this.status = status;
            this.uc = uc;
            
            this.metadata = new InBoundHeaders();            
            for (Map.Entry<String, List<String>> e : uc.getHeaderFields().entrySet()) {
                if (e.getKey() != null)
                    metadata.put(e.getKey(), e.getValue());
            }
            
            try {
                this.in = getInputStream();
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        
        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Response.Status getResponseStatus() {
            return Response.Status.fromStatusCode(status);
        }
    
        public void setResponseStatus(Response.Status status) {
            setStatus(status.getStatusCode());            
        }

        public MultivaluedMap<String, String> getMetadata() {
            return metadata;
        }

        public boolean hasEntity() {
            if (method.equals("HEAD") || in == null)
                return false;

            int l = uc.getContentLength();
            return l > 0 || l == -1;
        }
        
        public InputStream getEntityInputStream() {
            return in;
        }

        public void setEntityInputStream(InputStream in) {
            this.in = in;
        }
        
        public <T> T getEntity(Class<T> c) {
            return getEntity(c, c);
        }

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
        
        public Map<String, Object> getProperties() {
            if (properties != null) return properties;

            return properties = new HashMap<String, Object>();
        }
        
        private InputStream getInputStream() throws IOException {
            if (status < 300) {
                return uc.getInputStream();
            } else {
                InputStream ein = uc.getErrorStream();
                return (ein != null)
                        ? ein : new ByteArrayInputStream(new byte[0]);
            }
        }

    }

    @Context private MessageBodyWorkers bodyContext;
    
    // ClientHandler
    
    public ClientResponse handle(ClientRequest ro) {
        try {
            return _invoke(ro);
        } catch (Exception ex) {
            throw new ClientHandlerException(ex);
        }
    }

    private ClientResponse _invoke(ClientRequest ro) 
            throws ProtocolException, IOException {
        HttpURLConnection uc = (HttpURLConnection)ro.getURI().toURL().openConnection();
        
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
        
        // Set the request method
        uc.setRequestMethod(ro.getMethod());

        // Write the request headers
        writeHeaders(ro.getMetadata(), uc);
        
        if (ro.getMethod().equalsIgnoreCase("GET")) {
            if (ro.getMetadata().getFirst("Content-Type") != null) {
                System.err.println("CONTENT-TYPE: " + ro.getMetadata().getFirst("Content-Type"));
            }
        }
        
        // Write the entity (if any)
        Object entity = ro.getEntity();
        if (entity != null) {
            uc.setDoOutput(true);
            writeEntity(uc, ro, entity);
        }
        
        // Return the in-bound response
        return new URLConnectionResponse(ro.getMethod(), uc.getResponseCode(), uc);        
    }
    
    private void writeHeaders(MultivaluedMap<String, Object> metadata, HttpURLConnection uc) {
        for (Map.Entry<String, List<Object>> e : metadata.entrySet()) {
            List<Object> vs = e.getValue();
            if (vs.size() == 1) {
                uc.setRequestProperty(e.getKey(), getHeaderValue(vs.get(0)));
            } else {
                StringBuilder b = new StringBuilder();
                boolean add = false;
                for (Object v : e.getValue()) {
                    if (add) b.append(',');
                    add = true;
                    b.append(getHeaderValue(v));
                }
                uc.setRequestProperty(e.getKey(), b.toString());
            }

        }        
    }
    
    @SuppressWarnings("unchecked")
    private String getHeaderValue(Object headerValue) {
        HeaderDelegate hp = RuntimeDelegate.getInstance().
                createHeaderDelegate(headerValue.getClass());
        return hp.toString(headerValue);
    }
    
    @SuppressWarnings("unchecked")
    private void writeEntity(HttpURLConnection uc, 
            ClientRequest ro, Object entity) throws IOException {
        MultivaluedMap<String, Object> metadata = ro.getMetadata();
        MediaType mediaType = null;
        final Object mediaTypeHeader = metadata.getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            mediaType = (MediaType)mediaTypeHeader;
        } else {
            if (mediaTypeHeader != null) {
                mediaType = MediaType.valueOf(mediaTypeHeader.toString());
            } else {
                mediaType = new MediaType("application", "octet-stream");
            }
        }
            
        Type entityType = null;
        if (entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity)entity;
            entityType = ge.getType();                
            entity = ge.getEntity();            
        } else {
            entityType = entity.getClass();
        }
        final Class entityClass = entity.getClass();
        
        final MessageBodyWriter bw = bodyContext.getMessageBodyWriter(
                entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType);
        if (bw == null) {
            throw new ClientHandlerException(
                    "A message body writer for Java type, " + entity.getClass() + 
                    ", and MIME media type, " + mediaType + ", was not found");
        }
        final long size = bw.getSize(
                entity, entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType);
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
        
        final OutputStream out = ro.getAdapter().adapt(ro, uc.getOutputStream());
        bw.writeTo(entity, entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType, metadata, out);
        out.flush();
        out.close();
    }
}