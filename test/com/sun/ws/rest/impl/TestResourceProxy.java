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

package com.sun.ws.rest.impl;

import com.sun.ws.rest.impl.client.RequestOutBound;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.client.ResourceProxyException;
import com.sun.ws.rest.impl.client.ResponseInBound;
import com.sun.ws.rest.impl.client.ResponseInBoundImpl;
import com.sun.ws.rest.spi.container.AbstractContainerRequest;
import com.sun.ws.rest.spi.container.AbstractContainerResponse;
import com.sun.ws.rest.spi.container.WebApplication;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TestResourceProxy extends ResourceProxy {
    public static final URI base = URI.create("/base/");
    
    private final WebApplication w;
    
    public TestResourceProxy(String path, WebApplication w) {
        super(UriBuilder.fromUri(base).path(path).build());
        this.w = w;
    }
    
    private final static class Response extends ResponseInBoundImpl {
        private final AbstractContainerResponse response;
        private final MultivaluedMap<String, String> metadata;
        
       Response(AbstractContainerResponse response) {
            this.response = response;
            this.metadata = new RequestHttpHeadersImpl();
            
            writeHeaders(response.getHttpHeaders(), metadata);
        }
        
        public int getStatus() {
            return response.getStatus();
        }

        public MultivaluedMap<String, String> getMetadata() {
            return metadata;
        }

        public boolean hasEntity() {
            return response.getEntity() != null;
        }
        
        public <T> T getEntity(Class<T> c) {
            return getEntity(c, false);
        }
        
        public <T> T getEntity(Class<T> c, boolean successful) {
            if (response.getEntity() == null) return null;
            
            try {
                return ProviderFactory.getInstance().createEntityProvider(c).
                        readFrom(c, getContentType(), null, getInputStream(successful));
            } catch (IOException ex) {
                throw new ResourceProxyException(ex);
            }
        }

        private InputStream getInputStream(boolean successful) throws IOException {
            return writeEntity(response.getHttpHeaders(), response.getEntity()); 
        }
    }

    public ResponseInBound invoke(URI u, String method, RequestOutBound ro) {
        final AbstractContainerRequest request = new TestHttpRequestContext(
                method, writeEntity(ro.getMetadata(), ro.getEntity()),
                u, base);
        
        writeHeaders(ro.getMetadata(), request.getRequestHeaders());

        final AbstractContainerResponse response = new TestHttpResponseContext(request);
        
        w.handleRequest(request, response);
        
        return new Response(response);
    }

    private static void writeHeaders(MultivaluedMap<String, Object> metadata, 
            MultivaluedMap<String, String> request) {
        for (Map.Entry<String, List<Object>> e : metadata.entrySet()) {
            for (Object v : e.getValue()) {
                request.add(e.getKey(), writeHeaderValue(v));
            }
        }        
    }
    
    @SuppressWarnings("unchecked")
    private static String writeHeaderValue(Object headerValue) {
        HeaderProvider hp = ProviderFactory.getInstance().createHeaderProvider(headerValue.getClass());
        return hp.toString(headerValue);
    }
    
    private static InputStream writeEntity(MultivaluedMap<String, Object> metadata, Object entity) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeEntity(metadata, entity, baos);
        return new ByteArrayInputStream(baos.toByteArray());        
    }
    
    @SuppressWarnings("unchecked")
    private static void writeEntity(MultivaluedMap<String, Object> metadata, Object entity, 
            OutputStream out) {
        if (entity == null) return;
        
        final EntityProvider p = ProviderFactory.getInstance().createEntityProvider(entity.getClass());
        
        try {
            final Object mediaType = metadata.getFirst("Content-Type");
            if (mediaType instanceof MediaType) {
                p.writeTo(entity, (MediaType)mediaType, null, out);
            } else {
                if (mediaType != null) {
                    p.writeTo(entity, new MediaType(mediaType.toString()), null, out);
                } else {
                    p.writeTo(entity, null, null, out);
                }
            }
            out.flush();
            out.close();
        } catch (IOException ex) {
            throw new ResourceProxyException(ex);
        }
    }
}
