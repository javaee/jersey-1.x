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

package com.sun.ws.rest.spi.container;

import com.sun.ws.rest.impl.ResponseHttpHeadersImpl;
import com.sun.ws.rest.impl.ResponseImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 * An abstract implementation of {@link ContainerResponse}.
 * <p>
 * Specific containers may extend this class and instances may be passed to
 * the runtime using the method {@link WebApplication#handleRequest}.
 * <p>
 * When the call to the method {@link WebApplication#handleRequest} returns
 * a container must commit the response, if the response has not already been
 * committed, by committing the status and headers, and writing the entity
 * to the underlying output stream, for example:
 * <pre>
 *   if (!isCommitted()) {
 *       commitStatusAndHeaders();
 *       writeEntity(getUnderlyingOutputStream());
 *   }
 * </pre>
 * <p>
 * The runtime may call the method {@link #getUnderlyingOutputStream} and
 * before any bytes are written to this stream call the method
 * {@link #commitStatusAndHeaders}. When one or more bytes are written to the
 * stream the response is marked as committed. Such behaviour arises when a
 * resource chooses to write an entity directly to an output stream obtained
 * from the method {@link #getOutputStream}.
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractContainerResponse implements ContainerResponse {
    private static final MediaType APPLICATION_OCTET_STREAM
            = new MediaType("application/octet-stream");
    
    public static final Response EMPTY_RESPONSE
            = Response.Builder.noContent().build();
    
    private final ContainerRequest request;
    
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
                commitStatusAndHeaders();
            }
        }
        
        private void commitClose() throws IOException {
            if (!isCommitted) {
                isCommitted = true;
                commitStatusAndHeaders();
            }
        }
    };
    
    /**
     *
     * @param request the container request associated with this response.
     */
    protected AbstractContainerResponse(ContainerRequest request) {
        this.request = request;
        this.status = EMPTY_RESPONSE.getStatus();
    }
    
    /**
     * Get the OutputStream provided by the underlying container response.
     *
     * @return the OutputStream of the underlying container response.
     */
    abstract protected OutputStream getUnderlyingOutputStream() throws IOException;
    
    /**
     * Commit the status code and headers (if any) to the underlying
     * container response.
     */
    abstract protected void commitStatusAndHeaders() throws IOException;
    
    
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
        if (request.getHttpMethod().equals("HEAD"))
            this.entity = null;
        // Otherwise if there is no entity then there should be no content type
        else if (this.entity == null) {
            contentType = null;
            if (status == 200) status = 204;
        } else
            if (status == 204) status = 200;
        
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
        checkStatusAndEntity();
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
    
    /**
     * Write the entity to the output stream
     */
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
        MediaType mediaType = null;
        
        final Object mediaTypeHeader = getHttpHeaders().getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            mediaType = (MediaType)mediaTypeHeader;
        } else {
            if (mediaTypeHeader != null) {
                mediaType = new MediaType(mediaTypeHeader.toString());
            } else {
                mediaType = new MediaType("application", "octet-stream");
            }
        }
        
        final MessageBodyWriter p = ProviderFactory.getInstance().createMessageBodyWriter(entity.getClass(), mediaType);
        p.writeTo(entity, mediaType, getHttpHeaders(), out);
    }
    
    private void checkStatusAndEntity() {
        if (status == 204 && entity != null) status = 200;
        else if (status == 200 && entity == null) status = 204;
    }
    
    private void setResponseOptimal(ResponseImpl r, MediaType contentType) {
        r.addMetadataOptimal(headers, request, contentType);
    }
    
    private void setResponseNonOptimal(Response r, MediaType contentType) {
        if (headers.getFirst("Content-Type") == null && contentType != null) {
            headers.putSingle("Content-Type", contentType);
        }
        
        Object location = headers.getFirst("Location");
        if (location != null) {
            if (location instanceof URI) {
                URI absoluteLocation = request.getBase().resolve((URI)location);
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