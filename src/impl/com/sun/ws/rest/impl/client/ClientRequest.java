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

package com.sun.ws.rest.impl.client;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ClientRequest {
    
    public abstract URI getURI();
    
    public abstract String getMethod();
    
    public abstract Object getEntity();
    
    public abstract MultivaluedMap<String, Object> getMetadata();

    @Override
    public abstract ClientRequest clone();

    
    public static final ClientRequest.Builder entity(Object entity) {
        return new Builder().entity(entity);
    }

    public static final ClientRequest.Builder entity(Object entity, MediaType type) {
        return new Builder().entity(entity, type);
    }

    public static final Builder entity(Object entity, String type) {
        return new Builder().entity(entity, type);
    }

    public static final Builder type(MediaType type) {
        return new Builder().type(type);
    }
        
    public static final Builder type(String type) {
        return new Builder().type(type);
    }
    
    public static final Builder accept(MediaType... types) {
        return new Builder().accept(types);
    }

    public static final Builder accept(String... types) {
        return new Builder().accept(types);            
    }

    public static final Builder header(String name, Object value) {
        return new Builder().header(name, value);
    }
        
    public static final class Builder extends BaseClientRequestBuilder<Builder> {
        public ClientRequest build(URI uri, String method) {
            ClientRequest ro = new ClientRequestImpl(uri, method, entity, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
    }
}
