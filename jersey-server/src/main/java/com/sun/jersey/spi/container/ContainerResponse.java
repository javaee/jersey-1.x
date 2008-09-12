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
package com.sun.jersey.spi.container;

import com.sun.jersey.impl.container.OutBoundHeaders;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.impl.ResponseImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * Containers instantiate, or inherit, and provide an instance to the 
 * {@link WebApplication}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContainerResponse implements HttpResponseContext {
    private static final Logger LOGGER = Logger.getLogger(ContainerResponse.class.getName());
    
    private static final RuntimeDelegate rd = RuntimeDelegate.getInstance();
    
    private MessageBodyWorkers bodyContext;
    
    private ContainerRequest request;
    
    private ContainerResponseWriter responseWriter;
    
    private Response response;
    
    private int status;
    
    private MultivaluedMap<String, Object> headers;
    
    private Object entity;
    
    private Type entityType;
    
    private boolean isCommitted;
    
    private CommittingOutputStream out;
    
    private final class CommittingOutputStream extends OutputStream {
        private final long size;
        
        private OutputStream o;

        CommittingOutputStream(long size) {
            this.size = size;
        }
        
        @Override
        public void write(byte b[]) throws IOException {
            commitWrite();
            o.write(b);
        }
        
        @Override
        public void write(byte b[], int off, int len) throws IOException {
            commitWrite();
            o.write(b, off, len);
        }
        
        public void write(int b) throws IOException {
            commitWrite();
            o.write(b);
        }
        
        @Override
        public void flush() throws IOException {
            if (isCommitted)
                o.flush();
        }
        
        @Override
        public void close() throws IOException {
            commitClose();
            o.close();
        }
        
        private void commitWrite() throws IOException {
            if (!isCommitted) {
                if (getStatus() == 204)
                    setStatus(200);
                isCommitted = true;
                o = responseWriter.writeStatusAndHeaders(size, ContainerResponse.this);
            }
        }
        
        private void commitClose() throws IOException {
            if (!isCommitted) {
                isCommitted = true;
                o = responseWriter.writeStatusAndHeaders(-1, ContainerResponse.this);
            }
        }
    };
    
    /**
     * Instantate a new ContainerResponse.
     * 
     * @param wa the web application.
     * @param request the container request associated with this response.
     * @param responseWriter the response writer
     */
    public ContainerResponse(
            WebApplication wa, 
            ContainerRequest request,
            ContainerResponseWriter responseWriter) {
        this.bodyContext = wa.getMessageBodyWorkers();
        this.request = request;
        this.responseWriter = responseWriter;
        this.status = Responses.NO_CONTENT;
    }
    
    // ContainerResponse
        
    /**
     * Convert a header value, represented as a general object, to the 
     * string value.
     * <p>
     * This method defers to {@link RuntimeDelegate#createHeaderDelegate} to
     * obtain a {@link HeaderDelegate} to convert the value to a string.
     * <p>
     * Containers may use this method to convert the header values obtained
     * from the {@link #getHttpHeaders}
     * 
     * @param headerValue the header value as an object
     * @return the string value
     */
    @SuppressWarnings("unchecked")
    public static String getHeaderValue(Object headerValue) {
        HeaderDelegate hp = rd.createHeaderDelegate(headerValue.getClass());
        return hp.toString(headerValue);
    }
    
    /**
     * Write the response.
     * <p>
     * The status and headers will be written by calling the method
     * {@link ContainerResponseWriter#writeStatusAndHeaders} on the provided
     * {@link ContainerResponseWriter} instance. The {@link OutputStream}
     * returned from that method call is used to write the entity (if any)
     * to that {@link OutputStream}. An appropriate {@link MessageBodyWriter}
     * will be found to write the entity.
     * 
     * @throws WebApplicationException if {@link MessageBodyWriter} cannot be 
     *         found for the entity with a 406 (Not Acceptable) response.
     * @throws java.io.IOException if there is an error writing the entity
     */
    @SuppressWarnings("unchecked")
    public void write() throws IOException {
        if (isCommitted)
            return;        
        
        if (entity == null) {
            responseWriter.writeStatusAndHeaders(-1, this);
            return;
        }
        
        MediaType contentType = getContentType();
        if (contentType == null) {
            List<MediaType> mts = bodyContext.getMessageBodyWriterMediaTypes(
                    entity.getClass(), entityType, null);
            contentType = request.getAcceptableMediaType(mts);
            if (contentType == null || 
                    contentType.isWildcardType() || contentType.isWildcardSubtype())
                contentType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            
            getHttpHeaders().putSingle("Content-Type", contentType);
        }
        
        final MessageBodyWriter p = bodyContext.getMessageBodyWriter(
                entity.getClass(), entityType, 
                null, contentType);
        // If there is no message body writer return a Not Acceptable response
        if (p == null) {
            String message = "A message body writer for Java type, " + entity.getClass() + 
                    ", and MIME media type, " + contentType + ", was not found";

            LOGGER.severe(message);            
            if (request.getMethod().equals("HEAD")) {
                responseWriter.writeStatusAndHeaders(-1, this);
                return;                
            } else 
                throw new WebApplicationException(Response.serverError().entity(message).build());
        }

        final long size = p.getSize(entity, entity.getClass(), entityType, null, contentType);
        if (request.getMethod().equals("HEAD")) {
            if (size != -1)
                getHttpHeaders().putSingle("Content-Length", Long.toString(size));
            isCommitted = true;
            responseWriter.writeStatusAndHeaders(0, this);
        } else {
            OutputStream o = new CommittingOutputStream(size);
            p.writeTo(entity, entity.getClass(), entityType, null, 
                    contentType, getHttpHeaders(), o);            
            if (!isCommitted) {
                isCommitted = true;
                responseWriter.writeStatusAndHeaders(-1, this);
            }
        }
        responseWriter.finish();
    }

    /**
     * Reset the response to 204 (No content) with no headers.
     */
    public void reset() {
        setResponse(Responses.noContent().build());
    }
    
    /**
     * 
     * @return the container response writer
     */
    public ContainerResponseWriter getContainerResponseWriter() {
        return responseWriter; 
    }
    
    /**
     * Set the container response writer.
     * @param responseWriter the container response writer
     */
    public void setContainerResponseWriter(ContainerResponseWriter responseWriter) {
        this.responseWriter = responseWriter; 
    }
    
    // HttpResponseContext
    
    public Response getResponse() {
        return response;
    }
    
    public void setResponse(Response response) {
        setResponse(response, null);
    }
    
    public void setResponse(Response r, MediaType contentType) {
        this.isCommitted = false;
        this.out = null;
        this.response = r = (r != null) ? r : Responses.noContent().build();
        
        this.status = r.getStatus();
        
        if (r instanceof ResponseImpl) {
            this.headers = setResponseOptimal((ResponseImpl)r, contentType);
        } else {
            this.headers = setResponseNonOptimal(r, contentType);
        }
    }
    
    public boolean isResponseSet() {
        return response != null;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public Object getEntity() {
        return entity;
    }
    
    public Type getEntityType() {
        return entityType;
    }
    
    public void setEntity(Object entity) {
        this.entity = entity;        
        if (this.entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity)this.entity;
            this.entityType = ge.getType();                
            this.entity = ge.getEntity();            
        } else {
            if (entity != null)
                this.entityType = this.entity.getClass();
        }        
        
        checkStatusAndEntity();
    }
    
    public MultivaluedMap<String, Object> getHttpHeaders() {
        if (headers == null)
            headers = new OutBoundHeaders();
        return headers;
    }
    
    public OutputStream getOutputStream() throws IOException {
        if (out == null)
            out = new CommittingOutputStream(-1);
        
        return out;
    }
    
    public boolean isCommitted() {
        return isCommitted;
    }
    
    
    //
    
    
    private MediaType getContentType() {        
        final Object mediaTypeHeader = getHttpHeaders().getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            return (MediaType)mediaTypeHeader;
        } else if (mediaTypeHeader != null) {
            return MediaType.valueOf(mediaTypeHeader.toString());
        }
    
        return null;
    }
    
    private void checkStatusAndEntity() {
        if (status == 204 && entity != null) status = 200;
        else if (status == 200 && entity == null) status = 204;
    }
    
    private MultivaluedMap<String, Object> setResponseOptimal(ResponseImpl r, MediaType contentType) {
        this.entityType = r.getEntityType();
        this.entity = r.getEntity();        
        if (entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity)this.entity;
            this.entityType = ge.getType();                
            this.entity = ge.getEntity();
        }
        
        return r.getMetadataOptimal(request, contentType);
    }
    
    private MultivaluedMap<String, Object> setResponseNonOptimal(Response r, MediaType contentType) {
        setEntity(r.getEntity());
        
        MultivaluedMap<String, Object> _headers = r.getMetadata();
        
        if (_headers.getFirst("Content-Type") == null && contentType != null) {
            _headers.putSingle("Content-Type", contentType);
        }
        
        Object location = _headers.getFirst("Location");
        if (location != null) {
            if (location instanceof URI) {
                URI absoluteLocation = request.getBaseUri().resolve((URI)location);
                _headers.putSingle("Location", absoluteLocation);
            }
        }
        
        return _headers;
    }
}