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
 */
public interface HttpResponseContext {
    
    /**
     * Set the response object. If not set an empty '204 OK' will be
     * sent.
     * @param response the response.
     */
    void setResponse(Response response);
    
    /**
     * Set the response object. If not set an empty '204 OK' will be
     * sent.
     * @param response the response.
     * @param contentType the content type to use if content type 
     *                    is not set in the respose, if null then 
     *                    "application/octet-stream" will be used.
     */
    void setResponse(Response response, MediaType contentType);

    /**
     * Get the status of the response.
     */
    int getStatus();
    
    /**
     * Set the status of the response.
     */
    void getStatus(int status);
    
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
     * @return a mutable map of HTTP header names and values that will be
     * included in the response. Any headers explicitly set will override
     * automatically generated values.
     */
    MultivaluedMap<String, Object> getHttpHeaders();
    
    /**
     * Get an OutputStream to which a representation may be written. The first
     * byte written will cause any headers currently set to be flushed.
     * @return the output stream
     * @throws java.io.IOException if an IO error occurs
     */
    OutputStream getOutputStream() throws IOException;
    
}
