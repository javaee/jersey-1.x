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

import com.sun.ws.rest.impl.ResponseHttpHeadersImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

final class RequestOutBoundImpl implements RequestOutBound {
    private final Object entity;
    
    private final MultivaluedMap<String, Object> metadata;

    /* package */ RequestOutBoundImpl() {
        this(null, null);
    }
    
    /* package */ RequestOutBoundImpl(Object entity) {
        this(entity, null);
    }
    
    /* package */ RequestOutBoundImpl(Object entity, MultivaluedMap<String, Object> metadata) {
        this.entity = entity;
        this.metadata = (metadata != null) ? metadata : new ResponseHttpHeadersImpl();
    }

    public Object getEntity() {
        return entity;
    }

    public MultivaluedMap<String, Object> getMetadata() {
        return metadata;
    }
    
    @Override
    public RequestOutBound clone() {
        return new RequestOutBoundImpl(this.entity, clone(this.metadata));
    }
    
    private static MultivaluedMap<String, Object> clone(MultivaluedMap<String, Object> md) {
        MultivaluedMap<String, Object> clone = new ResponseHttpHeadersImpl();
        for (Map.Entry<String, List<Object>> e : md.entrySet()) {
            clone.put(e.getKey(), new ArrayList<Object>(e.getValue()));
        }
        return clone;
    }
}
