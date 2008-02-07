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

import com.sun.ws.rest.impl.client.*;
import java.net.URI;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A client (outbound) HTTP request.
 * <p>
 * Instances may be created by using the static method {@link #create} and
 * methods on {@link ClientRequest.Builder}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ClientRequest {
    
    /**
     * Get the URI of the request. The URI shall contain sufficient
     * components to correctly dispatch a request
     * 
     * @return the URI of the request.
     */
    public abstract URI getURI();
    
    /**
     * Get the HTTP method.
     * 
     * @return the HTTP method.
     */
    public abstract String getMethod();
    
    /**
     * Get the entity of the request.
     * 
     * @return the entity of the request.
     */
    public abstract Object getEntity();
    
    /**
     * Get the HTTP headers of the request.
     * 
     * @return the HTTP headers of the request.
     */
    public abstract MultivaluedMap<String, Object> getMetadata();

    /**
     * Clone the request.
     * 
     * @return the cloned request.
     */
    @Override
    public abstract ClientRequest clone();

    /**
     * Create a builder for building a new {@link ClientRequest}instance.
     * 
     * @return the builder.
     */
    public static final ClientRequest.Builder create() {
        return new Builder();
    }
            
    /**
     * The builder for building a {@link ClientRequest} instance.
     */
    public static final class Builder extends PartialRequestBuilder<Builder> {
        /**
         * Build the {@link ClientRequest}instance.
         * 
         * @param uri the URI of the request.
         * @param method the HTTP method.
         * @return the client request.
         */
        public ClientRequest build(URI uri, String method) {
            ClientRequest ro = new ClientRequestImpl(uri, method, entity, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
    }
}