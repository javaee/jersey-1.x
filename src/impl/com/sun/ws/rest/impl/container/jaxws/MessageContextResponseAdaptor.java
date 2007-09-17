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

package com.sun.ws.rest.impl.container.jaxws;

import com.sun.ws.rest.spi.container.AbstractContainerResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataSource;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.ProviderFactory;
import javax.xml.ws.handler.MessageContext;

/**
 * Adapts a JAX-WS <code>Endpoint</code> response to provide the methods of HttpResponse
 *
 */
public final class MessageContextResponseAdaptor extends AbstractContainerResponse {
    
    private final HttpServletResponse response; 
    
    private final MessageContext context;
    
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    
    /* package */ MessageContextResponseAdaptor(MessageContext context, 
            MessageContextRequestAdaptor requestContext) {
        super(requestContext);
        this.context = context;
        this.response = (HttpServletResponse)context.get(context.SERVLET_RESPONSE);
    }

    private final class HttpResponseDataSource implements DataSource {
        final private String mediaType;

        public HttpResponseDataSource() {
            this.mediaType = getHeaderValue(getHttpHeaders().getFirst("Content-Type"));
        }

        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            return getClass().toString();
        }

        public InputStream getInputStream() throws IOException {
            writeEntity(out);
            return new ByteArrayInputStream(out.toByteArray());
        }

        public String getContentType() {
            return mediaType;
        }
    } 

    /* package */ DataSource getResultDataSource() throws IOException {
        if (!isCommitted())
            commitStatusAndHeaders();
        
        return new HttpResponseDataSource();
    }

    
    // HttpResponseContextImpl

    protected OutputStream getUnderlyingOutputStream() throws IOException {
        return out;
    }
    
    protected void commitStatusAndHeaders() throws IOException {
        // If JAX-WS is deployed using servlet
        if (response != null) {
            response.setStatus(getStatus());
            MultivaluedMap<String, Object> headers = this.getHttpHeaders();
            for (Map.Entry<String, List<Object>> e : headers.entrySet()) {
                for (Object v : e.getValue()) {
                    response.addHeader(e.getKey(), getHeaderValue(v));
                }
            }
        // If JAX-WS is deployed using another HTTP container
        } else {
            Map<String, List<String>> map = new HashMap<String, List<String>>();
            MultivaluedMap<String, Object> headers = this.getHttpHeaders();
            
            for (String header: headers.keySet()) {
                List<String> values = new ArrayList<String>();
                for (Object v : headers.get(header))
                    values.add(getHeaderValue(v));
                map.put(header, values);
            }              
            context.put(context.HTTP_RESPONSE_CODE, getStatus());
            context.put(context.HTTP_RESPONSE_HEADERS, map);
        }
    }
}
