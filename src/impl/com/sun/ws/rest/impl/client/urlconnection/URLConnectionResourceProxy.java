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
import com.sun.ws.rest.impl.client.RequestOutBound;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResourceProxyException;
import com.sun.ws.rest.impl.client.ResponseInBound;
import com.sun.ws.rest.impl.client.ResponseInBoundImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class URLConnectionResourceProxy extends ResourceProxy {
    private final static class URLConnectionResponse extends ResponseInBoundImpl {
        private final int status;
        private final HttpURLConnection uc;
        private final MultivaluedMap<String, String> metadata;
        
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
            return getEntity(c, false);
        }
        
        public <T> T getEntity(Class<T> c, boolean successful) {
            try {
                MediaType mediaType = getContentType();
                return ProviderFactory.getInstance().createMessageBodyReader(c, mediaType).
                        readFrom(c, mediaType, metadata, getInputStream(successful));
            } catch (IOException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        private InputStream getInputStream(boolean successful) throws IOException {
            if (successful) {
                return uc.getInputStream();
            } else if (status < 300) {
                return uc.getInputStream();
            } else {
                return uc.getErrorStream();
            }
        }
    }
    
    public URLConnectionResourceProxy(URI u) throws MalformedURLException, IOException {
        super(u);
    }
    
    public ResponseInBound invoke(URI u, String method, RequestOutBound ro) {
        try {
            return _invoke(u.toURL(), method, ro);
        } catch (Exception ex) {
            throw new ResourceProxyException(ex);
        }
    }

    private ResponseInBound _invoke(URL u, String method, RequestOutBound ro) 
            throws ProtocolException, IOException {
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();

        uc.setReadTimeout(5000);
        
        // Set the request method
        uc.setRequestMethod(method);

        // Write the request headers
        writeHeaders(ro.getMetadata(), uc);
        
        // Write the entity (if any)
        Object entity = ro.getEntity();
        if (entity != null) {
            uc.setDoOutput(true);
            writeEntity(ro.getMetadata(), entity, uc.getOutputStream());
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
        HeaderProvider hp = ProviderFactory.getInstance().createHeaderProvider(headerValue.getClass());
        return hp.toString(headerValue);
    }
    
    @SuppressWarnings("unchecked")
    private void writeEntity(MultivaluedMap<String, Object> metadata, Object entity, 
            OutputStream out) throws IOException {
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
                
        final MessageBodyWriter p = ProviderFactory.getInstance().createMessageBodyWriter(entity.getClass(), mediaType);
        p.writeTo(entity, mediaType, metadata, out);
        
        out.flush();
        out.close();
    }
}
