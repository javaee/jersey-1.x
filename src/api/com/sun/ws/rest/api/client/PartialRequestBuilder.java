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

package com.sun.ws.rest.api.client;

import com.sun.ws.rest.impl.ResponseHttpHeadersImpl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A partial implementation of {@link RequestBuilder} that implementations
 * the addition of the HTTP request entity and HTTP request headers
 * but leaves undefined the build methods for constructing the request.
 * 
 * @param T the type than implements {@link RequestBuilder}.
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class PartialRequestBuilder<T extends RequestBuilder> 
        implements RequestBuilder<T> {

    protected Object entity;
    
    protected MultivaluedMap<String, Object> metadata;
    
    protected PartialRequestBuilder() {
        metadata = new ResponseHttpHeadersImpl();
    }
    
    public T entity(Object entity) {
        this.entity = entity;
        return (T)this;
    }

    public T entity(Object entity, MediaType type) {
        entity(entity);
        type(type);
        return (T)this;
    }

    public T entity(Object entity, String type) {
        entity(entity);
        type(type);
        return (T)this;
    }
    
    public T type(MediaType type) {
        getMetadata().putSingle("Content-Type", type);        
        return (T)this;
    }
        
    public T type(String type) {
        getMetadata().putSingle("Content-Type", type);        
        return (T)this;
    }
        
    public T accept(MediaType... types) {
        for (MediaType type : types)
            getMetadata().add("Accept", type);
        return (T)this;
    }
    
    public T accept(String... types) {
        for (String type : types)
            getMetadata().add("Accept", type);
        return (T)this;
    }
    
    public T header(String name, Object value) {
        getMetadata().add(name, value);
        return (T)this;
    }
    
    private MultivaluedMap<String, Object> getMetadata() {
        if (metadata != null) return metadata;
        
        return metadata = new ResponseHttpHeadersImpl();
    }
}