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

package com.sun.ws.rest.api.core;

import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Encapsulates the response to a HTTP request.
 * <p>
 * The default state of a response is a HTTP response with a status code of 204 
 * (OK) with no HTTP response headers and no entity.
 */
public interface HttpResponseContext {
    
    /**
     * Set the response state from a Response instance. This replaces a 
     * pre-existing response state.
     * <p>
     * If an entity is set but there is no MIME media type declared for the 
     * Content-Type response header then the MIME media type will be set to 
     * "application/octet-stream".
     *
     * @param response the response.
     */
    void setResponse(Response response);
    
    /**
     * Set the response state from a Response instance. This replaces a 
     * pre-existing response state.
     *
     * @param response the response.
     * @param contentType the MIME media type to use fot the Content-Type response
     *        header if the header is not set by the response. If null then
     *        "application/octet-stream" will be used.
     */
    void setResponse(Response response, MediaType contentType);

    /**
     * Check if the response has been set using the setReponse methods.
     * 
     * @return true if the response has been set.
     */
    boolean isResponseSet();
    
    /**
     * Get the status of the response.
     */
    int getStatus();
    
    /**
     * Set the status of the response.
     */
    void setStatus(int status);
    
    /**
     * Get the entity of the response
     */
    Object getEntity();
    
    /**
     * Set the entity of the response
     */
    void setEntity(Object entity);
    
    /**
     * Get the HTTP response headers. The returned map is case-insensitive wrt
     * keys. Note that <code>setHttpResponse</code> can change the HTTP response
     * headers and may overwrite headers set previously.
     *
     * @return a mutable map of HTTP header names and values that will be
     * included in the response. Any headers explicitly set will override
     * automatically generated values.
     */
    MultivaluedMap<String, Object> getHttpHeaders();
    
    /**
     * Get an OutputStream to which an entity may be written.
     * <p>
     * The first byte written will cause the status code and headers 
     * (if any) to be committed to the underlying container.
     *
     * @return the output stream
     * @throws java.io.IOException if an IO error occurs
     */
    OutputStream getOutputStream() throws IOException;
    
    /**
     * Ascertain if a response has been committed to an underlying container.
     * <p>
     * A response is committed if the status code, headers (if any) have been
     * committed to the underlying container.
     *  
     * @return true if the response has been committed.
     */
    boolean isCommitted();
}
