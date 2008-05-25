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

package com.sun.jersey.spi.container;

import com.sun.jersey.api.Responses;
import com.sun.jersey.impl.ResponseHttpHeadersImpl;
import com.sun.jersey.impl.ResponseImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

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
            = new MediaType("application", "octet-stream");
    
    private final MessageBodyContext bodyContext;
    
    private final ContainerRequest request;
    
    private boolean responseSet;
    
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
                commitStatusAndHeaders(-1);
            }
        }
        
        private void commitClose() throws IOException {
            if (!isCommitted) {
                isCommitted = true;
                commitStatusAndHeaders(-1);
            }
        }
    };
    
    /**
     *
     * @param bodyContext the message body context.
     * @param request the container request associated with this response.
     */
    protected AbstractContainerResponse(MessageBodyContext bodyContext, ContainerRequest request) {
        this.bodyContext = bodyContext;
        this.request = request;
        this.status = Responses.NO_CONTENT;
    }
    
    /**
     * Get the OutputStream provided by the underlying container response.
     *
     * @return the OutputStream of the underlying container response.
     * @throws java.io.IOException if there is an error obtaining the 
     *         underlying OutputStream.
     */
    abstract protected OutputStream getUnderlyingOutputStream() throws IOException;
    
    /**
     * Commit the status code and headers (if any) to the underlying
     * container response.
     * 
     * @param contentLength the length, bytes, of the entity to be written,
     *        otherwise -1 if the length is unknown.
     * @throws java.io.IOException if an error commiting status and headers 
     *         occurs.
     */
    abstract protected void commitStatusAndHeaders(long contentLength) throws IOException;
    
    
    // HttpResponseContext
    
    public final void setResponse(Response response) {
        setResponse(response, APPLICATION_OCTET_STREAM);
    }
    
    public final void setResponse(Response response, MediaType contentType) {
        responseSet = true;
        
        if (contentType == null)
            contentType = APPLICATION_OCTET_STREAM;
        
        response = (response != null) ? response : Responses.noContent().build();
        
        this.status = response.getStatus();
        this.entity = response.getEntity();
        
        // If HTTP method is HEAD then there should be no entity
        if (request.getHttpMethod().equals("HEAD"))
            this.entity = null;
        // Otherwise if there is no entity then there should be no content type
        else if (this.entity == null) {
            contentType = null;
        }        
        if (response instanceof ResponseImpl) {
            this.headers = setResponseOptimal((ResponseImpl)response, contentType);
        } else {
            this.headers = setResponseNonOptimal(response, contentType);
        }
    }
    
    public final boolean isResponseSet() {
        return responseSet;
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
     * Write the entity (if any) to the underlying output stream.
     * <p>
     * The status and headers will be committed by calling the method
     * {@link #commitStatusAndHeaders}. The output stream will be obtained
     * by calling the method {@link #getUnderlyingOutputStream}
     * <p>
     * If a {@link MessageBodyReader} cannot be found for the entity
     * then a 406 (Not Acceptable) response is returned.
     * 
     * @throws java.io.IOException if there is an error writing the entity
     */
    @SuppressWarnings("unchecked")
    protected final void writeEntity() throws IOException {
        if (isCommitted)
            return;
        
        if (entity == null) {
            commitStatusAndHeaders(-1);
            return;
        }
        
        final MediaType contentType = getContentType();
        final MessageBodyWriter p = bodyContext.getMessageBodyWriter(
                entity.getClass(), null, 
                null, contentType);
        // If there is no message body writer return a Not Acceptable response
        if (p == null) {
            setResponse(Responses.notAcceptable().build());
            commitStatusAndHeaders(-1);
            return;
        }
        
        commitStatusAndHeaders(p.getSize(entity));
        p.writeTo(entity, entity.getClass(), null, null, 
                contentType, getHttpHeaders(), getUnderlyingOutputStream());
    }
    
    private MessageBodyWriter getMessageBodyWriter(Object entity) {
        return bodyContext.getMessageBodyWriter(
                entity.getClass(), null, 
                null, getContentType());        
    }
    
    private MediaType getContentType() {        
        final Object mediaTypeHeader = getHttpHeaders().getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            return (MediaType)mediaTypeHeader;
        } else {
            if (mediaTypeHeader != null) {
                return MediaType.valueOf(mediaTypeHeader.toString());
            } else {
                return APPLICATION_OCTET_STREAM;
            }
        }
    }
    
    private void checkStatusAndEntity() {
        if (status == 204 && entity != null) status = 200;
        else if (status == 200 && entity == null) status = 204;
    }
    
    private MultivaluedMap<String, Object> setResponseOptimal(ResponseImpl r, MediaType contentType) {
        return r.getMetadataOptimal(request, contentType);
    }
    
    private MultivaluedMap<String, Object> setResponseNonOptimal(Response r, MediaType contentType) {
        MultivaluedMap<String, Object> headers = r.getMetadata();
        
        if (headers.getFirst("Content-Type") == null && contentType != null) {
            headers.putSingle("Content-Type", contentType);
        }
        
        Object location = headers.getFirst("Location");
        if (location != null) {
            if (location instanceof URI) {
                URI absoluteLocation = request.getBaseUri().resolve((URI)location);
                headers.putSingle("Location", absoluteLocation);
            }
        }
        
        return headers;
    }
    
    @SuppressWarnings("unchecked")
    public String getHeaderValue(Object headerValue) {
        // TODO: performance, this is very slow
        HeaderDelegate hp = RuntimeDelegate.getInstance().
                createHeaderDelegate(headerValue.getClass());
        return hp.toString(headerValue);
    }
}