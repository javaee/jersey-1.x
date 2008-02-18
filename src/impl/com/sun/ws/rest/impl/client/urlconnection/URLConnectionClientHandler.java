/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.client.urlconnection;

import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import com.sun.ws.rest.api.client.ClientHandler;
import com.sun.ws.rest.api.client.ClientHandlerException;
import com.sun.ws.rest.api.client.ClientRequest;
import com.sun.ws.rest.api.client.ClientResponse;
import com.sun.ws.rest.spi.container.MessageBodyContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class URLConnectionClientHandler implements ClientHandler {
    private final class URLConnectionResponse extends ClientResponse {
        private final int status;
        private final HttpURLConnection uc;
        private final MultivaluedMap<String, String> metadata;
        private Map<String, Object> properties;
        
        URLConnectionResponse(int status, HttpURLConnection uc) {
            this.status = status;
            this.uc = uc;
            this.metadata = new RequestHttpHeadersImpl();
            
            for (Map.Entry<String, List<String>> e : uc.getHeaderFields().entrySet()) {
                if (e.getKey() != null)
                    metadata.put(e.getKey(), e.getValue());
            }
        }
        
        public int getStatus() {
            return status;
        }

        public MultivaluedMap<String, String> getMetadata() {
            return metadata;
        }

        public boolean hasEntity() {
            int l = uc.getContentLength();
            return l > 0 || l == -1;
        }
        
        public <T> T getEntity(Class<T> c) {
            try {
                MediaType mediaType = getType();
                return bodyContext.getMessageBodyReader(c, mediaType).
                        readFrom(c, mediaType, metadata, getInputStream());
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
                return uc.getErrorStream();
            }
        }
    }

    @Context private MessageBodyContext bodyContext;
    
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
        
        // Set the request method
        uc.setRequestMethod(ro.getMethod());

        // Write the request headers
        writeHeaders(ro.getMetadata(), uc);
        
        // Write the entity (if any)
        Object entity = ro.getEntity();
        if (entity != null) {
            uc.setDoOutput(true);
            writeEntity(uc, ro.getMetadata(), entity);
        }
        
        // Return the in-bound response
        return new URLConnectionResponse(uc.getResponseCode(), uc);        
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
            MultivaluedMap<String, Object> metadata, Object entity) throws IOException {
        MediaType mediaType = null;
        final Object mediaTypeHeader = metadata.getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            mediaType = (MediaType)mediaTypeHeader;
        } else {
            if (mediaTypeHeader != null) {
                mediaType = MediaType.parse(mediaTypeHeader.toString());
            } else {
                mediaType = new MediaType("application", "octet-stream");
            }
        }
                
        final MessageBodyWriter p = bodyContext.
                getMessageBodyWriter(entity.getClass(), mediaType);
        
        final long size = p.getSize(entity);
        if (size != -1 && size < Integer.MAX_VALUE) {
            // HttpURLConnection uses the int type for content length
            uc.setFixedLengthStreamingMode((int)size);
        } else {
            // TODO it appears HttpURLConnection has some bugs in
            // chunked encoding
            // uc.setChunkedStreamingMode(0);
        }
        
        final OutputStream out = uc.getOutputStream();
        p.writeTo(entity, mediaType, metadata, out);        
        out.flush();
        out.close();
    }
}
