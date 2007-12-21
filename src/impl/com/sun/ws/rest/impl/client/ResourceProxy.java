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
public abstract class ResourceProxy implements ResourceProxyInvoker {
    private final URI u;
    
    private ResourceProxyInvoker head;
    
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
    
    public final void addFilter(ResourceProxyFilter f) {
        f.setNext(head);
        this.head = f;
    }

    public final void removeFilter(ResourceProxyFilter f) {
        if (head == this) return;
        
        if (head == f) head = f.getNext();

        ResourceProxyFilter e = (ResourceProxyFilter)head;
        while (e.getNext() != f) {
            if (e.getNext() == this) return;
            
            e = (ResourceProxyFilter)e.getNext();
        }
        
        e.setNext(f.getNext());
    }
    
    public final void removeAllFilters() {
        this.head = this;
    }
    
    public final ResponseInBound head() {
        return head(new RequestOutBoundImpl());
    }
    
    public final ResponseInBound head(RequestOutBound ro) {
        return _invoke(u, "HEAD", ro);
    }

    public final <T> T options(Class<T> c) {
        return options(c, new RequestOutBoundImpl());
    }
    
    public final <T> T options(Class<T> c, RequestOutBound ro) {
        return invoke("OPTIONS", c, ro);
    }

    
    public final <T> T get(Class<T> c) {
        return get(c, new RequestOutBoundImpl());
    }
    
    public final <T> T get(Class<T> c, RequestOutBound ro) {
        return invoke("GET", c, ro);
    }
    
    
    public final void put() {
        put(new RequestOutBoundImpl());
    }
    
    public final void put(Object requestEntity) {
        put(new RequestOutBoundImpl(requestEntity));
    }
    
    public final void put(RequestOutBound ro) {
        voidInvoke("PUT", ro);
    }
    
    public final <T> T put(Class<T> c) {
        return put(c, new RequestOutBoundImpl());
    }

    public final <T> T put(Class<T> c, Object requestEntity) {
        return put(c, new RequestOutBoundImpl(requestEntity));
    }
    
    public final <T> T put(Class<T> c, RequestOutBound ro) {
        return invoke("PUT", c, ro);
    }
    
    
    public final void post() {
        put(new RequestOutBoundImpl());
    }
    
    public final void post(Object requestEntity) {
        put(new RequestOutBoundImpl(requestEntity));
    }
    
    public final void post(RequestOutBound ro) {
        voidInvoke("POST", ro);
    }
    
    public final <T> T post(Class<T> c) {
        return post(c, new RequestOutBoundImpl());    
    }

    public final <T> T post(Class<T> c, Object requestEntity) {
        return post(c, new RequestOutBoundImpl(requestEntity));
    }
    
    public final <T> T post(Class<T> c, RequestOutBound ro) {
        return invoke("POST", c, ro);
    }    
    
    
    public final void delete() {
        delete(new RequestOutBoundImpl());
    }
    
    public final void delete(Object requestEntity) {
        delete(new RequestOutBoundImpl(requestEntity));
    }
    
    public final void delete(RequestOutBound ro) {
        voidInvoke("DELETE", ro);
    }
    
    public final <T> T delete(Class<T> c) {
        return delete(c, new RequestOutBoundImpl());    
    }

    public final <T> T delete(Class<T> c, Object requestEntity) {
        return delete(c, new RequestOutBoundImpl(requestEntity));
    }
    
    public final <T> T delete(Class<T> c, RequestOutBound ro) {
        return invoke("DELETE", c, ro);
    }    
    
    
    public class Builder {
        private final RequestOutBound.Builder b;
        
        private Builder(RequestOutBound.Builder b) {
            this.b = b;
        }
        
        public Builder entity(Object entity) {
            b.entity(entity);
            return this;
        }
        
        public Builder entity(Object entity, MediaType type) {
            b.entity(entity, type);
            return this;
        }
        
        public Builder entity(Object entity, String type) {
            b.entity(entity, type);            
            return this;
        }

        public Builder accept(MediaType... types) {
            b.accept(types);
            return this;
        }
    
        public Builder accept(String... types) {
            b.accept(types);
            return this;
        }
        
        public Builder header(String name, Object value) {
            b.header(name, value);
            return this;
        }
        
        public final ResponseInBound head() {
            return ResourceProxy.this.head(b.build());
        }

        
        public final <T> T options(Class<T> c) {
            return ResourceProxy.this.options(c, b.build());
        }
        
        
        public final <T> T get(Class<T> c) {
            return ResourceProxy.this.get(c, b.build());
        }
        
        
        public final void put() {
            ResourceProxy.this.put(b.build());
        }

        public final <T> T put(Class<T> c) {
            return ResourceProxy.this.put(c, b.build());
        }
    
        
        public final void post() {
            ResourceProxy.this.post(b.build());
        }

        public final <T> T post(Class<T> c) {
            return ResourceProxy.this.post(c, b.build());
        }
        
        
        public final void delete() {
            ResourceProxy.this.delete(b.build());
        }

        public final <T> T delete(Class<T> c) {
            return ResourceProxy.this.delete(c, b.build());
        }
    }
    
    public final Builder content(Object entity) {
        return new Builder(RequestOutBound.Builder.content(entity));
    }

    public final Builder content(Object entity, MediaType type) {
        return new Builder(RequestOutBound.Builder.content(entity, type));
    }

    public final Builder content(Object entity, String type) {
        return new Builder(RequestOutBound.Builder.content(entity, type));
    }

    public final Builder acceptable(MediaType... types) {
        return new Builder(RequestOutBound.Builder.acceptable(types));
    }

    public final Builder acceptable(String... types) {
        return new Builder(RequestOutBound.Builder.acceptable(types));
    }    
    
    public final Builder request(String name, Object value) {
        return new Builder(RequestOutBound.Builder.request(name, value));    
    }

    
    
    public final <T> T invoke(String method, Class<T> c) {
        return invoke(method, c, new RequestOutBoundImpl());
    }

    public final <T> T invoke(String method, Class<T> c, RequestOutBound ro) {
        ResponseInBound r = _invoke(u, method, ro);
        return (c == ResponseInBound.class) ? c.cast(r) : r.getEntity(c, true);        
    }
    
    private final void voidInvoke(String method, RequestOutBound ro) {
        ResponseInBound r = _invoke(u, method, ro);
        if (r.getStatus() >= 300)
            throw new ResourceProxyException();
    }
    
    private final ResponseInBound _invoke(URI u, String method, RequestOutBound ro) {
        return head.invoke(u, method, ro);
    }
    
    public abstract ResponseInBound invoke(URI u, String method, RequestOutBound ro);
    
    
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
