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

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.spi.container.ContainerResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.EntityProvider;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class HttpResponseContextImpl implements ContainerResponse {
    private static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application/octet-stream");
        
    public static final Response EMPTY_RESPONSE = Response.Builder.noContent().build();
            
    private final HttpRequestContext requestContext;
    
    private int status;
    
    private MultivaluedMap<String, Object> headers;
    
    private Object entity;
    
    private boolean isCommitted;
    
    private OutputStream out;
    
    private final class CommittingOutputStream extends OutputStream {
        final OutputStream o;
        
        CommittingOutputStream(OutputStream o) {
            this.o = o;
        }
        
        public void write(byte b[]) throws IOException {
            commitWrite();
            o.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            commitWrite();
            o.write(b, off, len);
        }

        public void write(int b) throws IOException {
            commitWrite();
            o.write(b);
        }
        
        public void flush() throws IOException {
            o.flush();
        }

        public void close() throws IOException {
            commitClose();
            o.close();
        }
        
        private void commitWrite() throws IOException {
            if (!isCommitted) {
                if (getStatus() == 204)
                    setStatus(200);
                isCommitted = true;
                HttpResponseContextImpl.this.commit();
            }
        }
        
        private void commitClose() throws IOException {
            if (!isCommitted) {
                isCommitted = true;
                HttpResponseContextImpl.this.commit();
            }
        }
    };
    
    public HttpResponseContextImpl(HttpRequestContext requestContext) {
        this.requestContext = requestContext;
        this.status = EMPTY_RESPONSE.getStatus();
    }

    // HttpResponseContext
    
    public final void setResponse(Response response) {
        setResponse(response, APPLICATION_OCTET_STREAM);
    }
    
    public final void setResponse(Response response, MediaType contentType) {
        if (contentType == null)
            contentType = APPLICATION_OCTET_STREAM;
        
        response = (response != null) ? response : EMPTY_RESPONSE;
        
        this.status = response.getStatus();
        this.entity = response.getEntity();
        
        // If HTTP method is HEAD then there should be no entity
        if (requestContext.getHttpMethod().equals("HEAD"))
            this.entity = null;
        // Otherwise if there is no entity then there should be no content type
        else if (this.entity == null) 
            contentType = null;
        
        this.headers = new ResponseHttpHeadersImpl();
        if (response instanceof ResponseImpl) {
            setResponseOptimal((ResponseImpl)response, contentType);
        } else {
            response.addMetadata(headers);
            setResponseNonOptimal(response, contentType);
        }        
    }
    
    public final int getStatus() {
        return status;
    }
    
    public final void setStatus(int status) {
        this.status = status;
    }
    
    public final Object getEntity() {
        return entity;
    }
    
    public final void setEntity(Object entity) {
        this.entity = entity;
    }
    
    public final MultivaluedMap<String, Object> getHttpHeaders() {
        if (headers == null)
            headers = new ResponseHttpHeadersImpl();
        return headers;
    }    

    
    public final OutputStream getOutputStream() throws IOException {
        if (out == null)
            out = new CommittingOutputStream(getUnderlyingOutputStream());
            
        return out;
    }
    
    public final boolean isCommitted() {
        return isCommitted;
    }

    //
    
    /**
     * Get the OutputStream provided by the underlying container response.
     *
     * @return the OutputStream of the the underlying container response.
     */
    abstract protected OutputStream getUnderlyingOutputStream() throws IOException;

    /**
     * Commit the status code and headers (if any) to the underlying 
     * container response.
     */
    abstract protected void commit() throws IOException;

    /**
     * Write the entity to the output stream
     */
    @SuppressWarnings("unchecked")
    protected final void writeEntity(OutputStream out) throws IOException {
        final Object entity = this.getEntity();
        if (entity != null) {
            writeEntity(entity, out);
        }
        
    }
    
    /**
     * Write the entity to the output stream
     */
    @SuppressWarnings("unchecked")
    protected final void writeEntity(Object entity, OutputStream out) throws IOException {
        final EntityProvider p = ProviderFactory.getInstance().createEntityProvider(entity.getClass());

        final Object mediaType = getHttpHeaders().getFirst("Content-Type");
        if (mediaType instanceof MediaType) {
            p.writeTo(entity, (MediaType)mediaType, getHttpHeaders(), out);
        } else {
            if (mediaType != null) {
                p.writeTo(entity, new MediaType(mediaType.toString()), getHttpHeaders(), out);
            } else {
                p.writeTo(entity, null, getHttpHeaders(), out);
            }
        }
    }
    
    //

    private void setResponseOptimal(ResponseImpl r, MediaType contentType) {
        r.addMetadataOptimal(headers, requestContext, contentType);
    }
    
    private void setResponseNonOptimal(Response r, MediaType contentType) {
        if (headers.getFirst("Content-Type") == null && contentType != null) {
            headers.putSingle("Content-Type", contentType);
        }
        
        Object location = headers.getFirst("Location");
        if (location != null) {
            if (location instanceof URI) {
                URI absoluteLocation = requestContext.getBase().resolve((URI)location);
                headers.putSingle("Location", absoluteLocation);
            }
        }
    }
        
    @SuppressWarnings("unchecked")
    public String getHeaderValue(Object headerValue) {
        // TODO: performance, this is very slow
        HeaderProvider hp = ProviderFactory.getInstance().createHeaderProvider(headerValue.getClass());
        return hp.toString(headerValue);
    }    
}