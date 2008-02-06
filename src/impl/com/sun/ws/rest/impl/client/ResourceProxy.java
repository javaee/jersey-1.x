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
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceProxy extends Filterable implements 
        RequestBuilder<ResourceProxy.Builder>,
        UniformInterface {    
    private final URI u;
    
    /* package */ ResourceProxy(ClientHandler c, URI u) {
        super(c);
        this.u = u;
    }
    
    public URI getURI() {
        return u;
    }
    
    public UriBuilder getBuilder() {
        return UriBuilder.fromUri(u);
    }
        
    // UniformInterface
    
    public ClientResponse head() {
        return getHeadHandler().handle(new ClientRequestImpl(u, "HEAD"));
    }
        
    public <T> T options(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "OPTIONS"));
    }
        
    public <T> T get(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "GET"));
    }
            
    public void put() {
        voidHandle(new ClientRequestImpl(u, "PUT", null));
    }
    
    public void put(Object requestEntity) {
        voidHandle(new ClientRequestImpl(u, "PUT", requestEntity));
    }
    
    public <T> T put(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "PUT"));
    }

    public <T> T put(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "PUT", requestEntity));
    }
            
    public void post() {
        voidHandle(new ClientRequestImpl(u, "POST"));
    }
    
    public void post(Object requestEntity) {
        voidHandle(new ClientRequestImpl(u, "POST", requestEntity));
    }
    
    public <T> T post(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "POST"));
    }

    public <T> T post(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "POST", requestEntity));
    }
            
    public void delete() {
        voidHandle(new ClientRequestImpl(u, "DELETE"));
    }
    
    public void delete(Object requestEntity) {
        voidHandle(new ClientRequestImpl(u, "DELETE", requestEntity));
    }
    
    public <T> T delete(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "DELETE"));    
    }

    public <T> T delete(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "DELETE", requestEntity));
    }
        
    // RequestBuilder<ResourceProxy.Builder>
    
    public Builder entity(Object entity) {
        return new Builder(u).entity(entity);
    }

    public Builder entity(Object entity, MediaType type) {
        return new Builder(u).entity(entity, type);
    }

    public Builder entity(Object entity, String type) {
        return new Builder(u).entity(entity, type);
    }

    public Builder type(MediaType type) {
        return new Builder(u).type(type);
    }
        
    public Builder type(String type) {
        return new Builder(u).type(type);
    }
    
    public Builder accept(MediaType... types) {
        return new Builder(u).accept(types);
    }

    public Builder accept(String... types) {
        return new Builder(u).accept(types);
    }    
    
    public Builder header(String name, Object value) {
        return new Builder(u).header(name, value);
    }

    // URI specific building
    
    public Builder path(String path) {
        return new Builder(UriBuilder.fromUri(u).path(path).build());
    }
    
    public Builder uri(URI uri) {
        UriBuilder b = UriBuilder.fromUri(u).encode(false);
        String path = uri.getRawPath();
        if (path != null && path.length() > 0) {
            if (path.startsWith("/")) {
                b.replacePath(path);
            } else {
                b.path(path);
            }
        }
        String query = uri.getRawQuery();
        if (query != null && query.length() > 0) {
            b.replaceQueryParams(query);        
        }
        return new Builder(b.build());
    }
    
    // Builder that builds client request and handles it
    
    public final class Builder extends ClientRequestBuilder<Builder> 
            implements UniformInterface {  
        
        private final URI u;
        
        private Builder(URI u) {
            this.u = u;
        }
        
        private ClientRequest build(String method) {
            ClientRequest ro = new ClientRequestImpl(u, method, entity, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
        
        private ClientRequest build(String method, Object e) {
            ClientRequest ro = new ClientRequestImpl(u, method, e, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
        
        // UniformInterface
        
        public ClientResponse head() {
            return getHeadHandler().handle(build("HEAD"));
        }
        
        public <T> T options(Class<T> c) {
            return handle(c, build("OPTIONS"));
        }
                
        public <T> T get(Class<T> c) {
            return handle(c, build("GET"));
        }
                
        public void put() {
            voidHandle(build("PUT"));
        }

        public void put(Object requestEntity) {
            voidHandle(build("PUT", requestEntity));
        }
        
        public <T> T put(Class<T> c) {
            return handle(c, build("PUT"));
        }
        
        public <T> T put(Class<T> c, Object requestEntity) {
            return handle(c, build("PUT", requestEntity));
        }
        
        public void post() {
            voidHandle(build("POST"));
        }

        public void post(Object requestEntity) {
            voidHandle(build("POST", requestEntity));
        }

        public <T> T post(Class<T> c) {
            return handle(c, build("POST"));
        }
                
        public <T> T post(Class<T> c, Object requestEntity) {
            return handle(c, build("POST", requestEntity));
        }
        
        public void delete() {
            voidHandle(build("DELETE"));
        }

        public void delete(Object requestEntity) {
            voidHandle(build("DELETE", requestEntity));
        }
        
        public <T> T delete(Class<T> c) {
            return handle(c, build("DELETE"));
        }
        
        public <T> T delete(Class<T> c, Object requestEntity) {
            return handle(c, build("DELETE", requestEntity));
        }
    }
    
    
    private <T> T handle(Class<T> c, ClientRequest ro) {
        ClientResponse r = getHeadHandler().handle(ro);
        
        if (c == ClientResponse.class) return c.cast(r);
        
        if (r.getStatus() < 300) return r.getEntity(c);
        
        throw new ResourceProxyException(r);
    }
    
    private void voidHandle(ClientRequest ro) {
        ClientResponse r = getHeadHandler().handle(ro);
        
        if (r.getStatus() >= 300) throw new ResourceProxyException(r);
    }
}