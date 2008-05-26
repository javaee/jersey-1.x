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

package com.sun.jersey.api.client;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

/**
 * An interface for building HTTP requests. The build methods for constructing
 * the request from the built information are left undefined and 
 * implementations must define such methods.
 * 
 * @param T the type than implements {@link RequestBuilder}.
 * @author Paul.Sandoz@Sun.Com
 */
public interface RequestBuilder<T extends RequestBuilder> {

    /**
     * Set the request entity.
     * 
     * @param entity the request entity
     * @return the builder.
     */
    T entity(Object entity);

    /**
     * Set the request entity it's media type.
     * 
     * @param entity the request entity
     * @param type the media type
     * @return the builder.
     */
    T entity(Object entity, MediaType type);

    /**
     * Set the request entity it's media type.
     * 
     * @param entity the request entity
     * @param type the media type
     * @return the builder.
     */
    T entity(Object entity, String type);
    
    /**
     * Set the media type.
     * 
     * @param type the media type
     * @return the builder.
     */
    T type(MediaType type);
        
    /**
     * Set the media type.
     * 
     * @param type the media type
     * @return the builder.
     */
    T type(String type);
        
    /**
     * Add acceptable media types.
     * 
     * @param types an array of the acceptable media types
     * @return the builder.
     */
    T accept(MediaType... types);
    
    /**
     * Add acceptable media types.
     * 
     * @param types an array of the acceptable media types
     * @return the builder.
     */
    T accept(String... types);
    
    /**
     * Add a cookie to be set.
     * 
     * @param cookie to be set.
     * @return the builder
     */
    T cookie(Cookie cookie);
    
    /**
     * Add an HTTP header and value.
     * 
     * @param name the HTTP header name.
     * @param value the HTTP header value.
     * @return the builder.
     */
    T header(String name, Object value);   
}