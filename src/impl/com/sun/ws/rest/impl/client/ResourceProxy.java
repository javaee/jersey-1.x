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

import com.sun.ws.rest.impl.client.urlconnection.URLConnectionResourceProxy;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceProxy implements ClientHandler, 
        ClientRequestBuilder<ResourceProxy.Builder>,
        UniformMethodProxy {
    private final URI u;
    
    private ClientHandler head;
    
    protected ResourceProxy(URI u) {
        this.u = u;
        this.head = this;
    }
    
    public final URI getURI() {
        return u;
    }
    
    public final UriBuilder getBuilder() {
        return UriBuilder.fromUri(u);
    }
    
    public final void addFilter(ClientFilter f) {
        f.setNext(head);
        this.head = f;
    }

    public final void removeFilter(ClientFilter f) {
        if (head == this) return;
        
        if (head == f) head = f.getNext();

        ClientFilter e = (ClientFilter)head;
        while (e.getNext() != f) {
            if (e.getNext() == this) return;
            
            e = (ClientFilter)e.getNext();
        }
        
        e.setNext(f.getNext());
    }
    
    public final void removeAllFilters() {
        this.head = this;
    }
    
    // UniformMethodProxy
    
    public final ClientResponse head() {
        return _handle(new ClientRequestImpl(u, "HEAD"));
    }
        
    public final <T> T options(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "OPTIONS"));
    }
        
    public final <T> T get(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "GET"));
    }
            
    public final void put() {
        voidHandle(new ClientRequestImpl(u, "PUT", null));
    }
    
    public final void put(Object requestEntity) {
        voidHandle(new ClientRequestImpl(u, "PUT", requestEntity));
    }
    
    public final <T> T put(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "PUT"));
    }

    public final <T> T put(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "PUT", requestEntity));
    }
            
    public final void post() {
        voidHandle(new ClientRequestImpl(u, "POST"));
    }
    
    public final void post(Object requestEntity) {
        voidHandle(new ClientRequestImpl(u, "POST", requestEntity));
    }
    
    public final <T> T post(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "POST"));
    }

    public final <T> T post(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "POST", requestEntity));
    }
            
    public final void delete() {
        voidHandle(new ClientRequestImpl(u, "DELETE"));
    }
    
    public final void delete(Object requestEntity) {
        voidHandle(new ClientRequestImpl(u, "DELETE", requestEntity));
    }
    
    public final <T> T delete(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "DELETE"));    
    }

    public final <T> T delete(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "DELETE", requestEntity));
    }
        
    
    public final class Builder extends BaseClientRequestBuilder<Builder> 
            implements UniformMethodProxy {  
        
        private Builder() {}
        
        private ClientRequest build(URI uri, String method) {
            ClientRequest ro = new ClientRequestImpl(uri, method, entity, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
        
        private ClientRequest build(URI uri, String method, Object e) {
            ClientRequest ro = new ClientRequestImpl(uri, method, e, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
        
        // UniformMethodProxy
        
        public final ClientResponse head() {
            return _handle(build(u, "HEAD"));
        }
        
        public final <T> T options(Class<T> c) {
            return handle(c, build(u, "OPTIONS"));
        }
                
        public final <T> T get(Class<T> c) {
            return handle(c, build(u, "GET"));
        }
                
        public final void put() {
            voidHandle(build(u, "PUT"));
        }

        public final void put(Object requestEntity) {
            voidHandle(build(u, "PUT", requestEntity));
        }
        
        public final <T> T put(Class<T> c) {
            return handle(c, build(u, "PUT"));
        }
        
        public final <T> T put(Class<T> c, Object requestEntity) {
            return handle(c, build(u, "PUT", requestEntity));
        }
        
        public final void post() {
            voidHandle(build(u, "POST"));
        }

        public final void post(Object requestEntity) {
            voidHandle(build(u, "POST", requestEntity));
        }

        public final <T> T post(Class<T> c) {
            return handle(c, build(u, "POST"));
        }
                
        public final <T> T post(Class<T> c, Object requestEntity) {
            return handle(c, build(u, "POST", requestEntity));
        }
        
        public final void delete() {
            voidHandle(build(u, "DELETE"));
        }

        public final void delete(Object requestEntity) {
            voidHandle(build(u, "DELETE", requestEntity));
        }
        
        public final <T> T delete(Class<T> c) {
            return handle(c, build(u, "DELETE"));
        }
        
        public final <T> T delete(Class<T> c, Object requestEntity) {
            return handle(c, build(u, "DELETE", requestEntity));
        }
    }
    
    // ClientRequestBuilder<ResourceProxy.Builder>
    
    public final Builder entity(Object entity) {
        return new Builder().entity(entity);
    }

    public final Builder entity(Object entity, MediaType type) {
        return new Builder().entity(entity, type);
    }

    public final Builder entity(Object entity, String type) {
        return new Builder().entity(entity, type);
    }

    public final Builder type(MediaType type) {
        return new Builder().type(type);
    }
        
    public final Builder type(String type) {
        return new Builder().type(type);
    }
    
    public final Builder accept(MediaType... types) {
        return new Builder().accept(types);
    }

    public final Builder accept(String... types) {
        return new Builder().accept(types);
    }    
    
    public final Builder header(String name, Object value) {
        return new Builder().header(name, value);
    }

    
    
    private final void voidHandle(ClientRequest ro) {
        ClientResponse r = _handle(ro);
        if (r.getStatus() >= 300)
            throw new ResourceProxyException();
    }
    
    private final ClientResponse _handle(ClientRequest ro) {
        return head.handle(ro);
    }
    
    public final <T> T handle(Class<T> c, ClientRequest ro) {
        ClientResponse r = _handle(ro);
        return (c == ClientResponse.class) ? c.cast(r) : r.getEntity(c, true);        
    }
    
    public abstract ClientResponse handle(ClientRequest ro);
    
    
    
    public static ResourceProxy create(String u) {
        return create(URI.create(u));
    }
    
    public static ResourceProxy create(URI u) {
        if (u.getScheme().equals("http") || u.getScheme().equals("https")) {
            try {
                return new URLConnectionResourceProxy(u);
            } catch (Exception ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        
        throw new IllegalArgumentException();
    }
}
