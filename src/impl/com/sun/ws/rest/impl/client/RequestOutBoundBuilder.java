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

import com.sun.ws.rest.impl.client.RequestOutBound.Builder;
import com.sun.ws.rest.impl.ResponseHttpHeadersImpl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RequestOutBoundBuilder extends RequestOutBound.Builder {
    
    private Object entity;
    
    private MultivaluedMap<String, Object> metadata;
    
    RequestOutBoundBuilder() {
        metadata = new ResponseHttpHeadersImpl();
    }
    
    public RequestOutBound build() {
        return new RequestOutBoundImpl(entity, metadata);
    }

    public Builder entity(Object entity) {
        this.entity = entity;
        return this;
    }

    public Builder entity(Object entity, MediaType type) {
        entity(entity);
        metadata.putSingle("Content-Type", type);
        return this;
    }

    public Builder entity(Object entity, String type) {
        entity(entity);
        metadata.putSingle("Content-Type", type);
        return this;
    }
    
    public Builder accept(MediaType... types) {
        for (MediaType type : types)
            metadata.add("Accept", type);
        return this;
    }
    
    public Builder accept(String... types) {
        for (String type : types)
            metadata.add("Accept", type);
        return this;
    }
}
