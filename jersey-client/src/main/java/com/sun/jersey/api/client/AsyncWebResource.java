/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.api.client;

import com.sun.jersey.impl.client.ClientRequestImpl;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * An encapsulation of an asynchronous Web resource capable of building requests
 * to send to the Web resource and processing responses returned from the Web
 * resource.
 * <p>
 * The Web resource implements the {@link UniformInterface} to invoke the HTTP 
 * method on the Web resource. A client request may be built before invocation
 * on the uniform interface.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class AsyncWebResource extends Filterable implements 
        RequestBuilder<AsyncWebResource.Builder>,
        AsyncUniformInterface {    
    private final URI u;
    
    /* package */ AsyncWebResource(ClientHandler c, URI u) {
        super(c);
        this.u = u;
    }
    
    /**
     * Get the URI to the resource.
     * 
     * @return the URI.
     */
    public URI getURI() {
        return u;
    }
    
    /**
     * Get the URI builder to the resource.
     * 
     * @return the URI builder.
     */
    public UriBuilder getBuilder() {
        return UriBuilder.fromUri(u);
    }
        
    // UniformInterface
    
    public Future<ClientResponse> head() {
        return handle(ClientResponse.class, new ClientRequestImpl(u, "HEAD"));
    }
        
    public <T> Future<T> options(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "OPTIONS"));
    }
        
    public <T> Future<T> get(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "GET"));
    }
            
    public Future<?> put() {
        return voidHandle(new ClientRequestImpl(u, "PUT", null));
    }
    
    public Future<?> put(Object requestEntity) {
        return voidHandle(new ClientRequestImpl(u, "PUT", requestEntity));
    }
    
    public <T> Future<T> put(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "PUT"));
    }

    public <T> Future<T> put(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "PUT", requestEntity));
    }
            
    public Future<?> post() {
        return voidHandle(new ClientRequestImpl(u, "POST"));
    }
    
    public Future<?> post(Object requestEntity) {
        return voidHandle(new ClientRequestImpl(u, "POST", requestEntity));
    }
    
    public <T> Future<T> post(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "POST"));
    }

    public <T> Future<T> post(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "POST", requestEntity));
    }
            
    public Future<?> delete() {
        return voidHandle(new ClientRequestImpl(u, "DELETE"));
    }
    
    public Future<?> delete(Object requestEntity) {
        return voidHandle(new ClientRequestImpl(u, "DELETE", requestEntity));
    }
    
    public <T> Future<T> delete(Class<T> c) {
        return handle(c, new ClientRequestImpl(u, "DELETE"));    
    }

    public <T> Future<T> delete(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, "DELETE", requestEntity));
    }
      
    public Future<?> method(String method) {
        return voidHandle(new ClientRequestImpl(u, method));        
    }
    
    public Future<?> method(String method, Object requestEntity) {
        return voidHandle(new ClientRequestImpl(u, method, requestEntity));        
    }
    
    public <T> Future<T> method(String method, Class<T> c) {
        return handle(c, new ClientRequestImpl(u, method));            
    }
    
    public <T> Future<T> method(String method, Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(u, method, requestEntity));        
    }
    
    // RequestBuilder<WebResource.Builder>
    
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
    
    public Builder acceptLanguage(Locale... locales) {
        return new Builder(u).acceptLanguage(locales);
    }

    public Builder acceptLanguage(String... locales) {
        return new Builder(u).acceptLanguage(locales);
    }    
    
    public Builder cookie(Cookie cookie) {
        return new Builder(u).cookie(cookie);
    }
    
    public Builder header(String name, Object value) {
        return new Builder(u).header(name, value);
    }

    // URI specific building
    
    /**
     * Start building from an additional path from the URI to the resource
     * 
     * @param path the additional path.
     * 
     * @return the builder.
     */
    public Builder path(String path) {
        return new Builder(UriBuilder.fromUri(u).path(path).build());
    }

    /**
     * Start building from a URI.
     * <p>
     * If the URI contains a path component and the path starts with a '/' then
     * the path of the resource proxy URI is replaced. Otherise the path is 
     * appended to the path of the resource proxy URI.
     * <p>
     * If the URI contains query parameters then those query parameters will
     * replace the query parameters (if any) of the resource proxy URI.
     * 
     * @param uri the URI.
     * @return the builder.
     */
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
    
    /**
     * The builder for building a {@link ClientRequest} instance and 
     * handling the request using the {@link UniformInterface}. The methods
     * of the {@link UniformInterface} are the build methods of the builder.
     */
    public final class Builder extends PartialRequestBuilder<Builder> 
            implements AsyncUniformInterface {  
        
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
        
        public Future<ClientResponse> head() {
            return handle(ClientResponse.class, build("HEAD"));
        }
        
        public <T> Future<T> options(Class<T> c) {
            return handle(c, build("OPTIONS"));
        }
                
        public <T> Future<T> get(Class<T> c) {
            return handle(c, build("GET"));
        }
                
        public Future<?> put() {
            return voidHandle(build("PUT"));
        }

        public Future<?> put(Object requestEntity) {
            return voidHandle(build("PUT", requestEntity));
        }
        
        public <T> Future<T> put(Class<T> c) {
            return handle(c, build("PUT"));
        }
        
        public <T> Future<T> put(Class<T> c, Object requestEntity) {
            return handle(c, build("PUT", requestEntity));
        }
        
        public Future<?> post() {
            return voidHandle(build("POST"));
        }

        public Future<?> post(Object requestEntity) {
            return voidHandle(build("POST", requestEntity));
        }

        public <T> Future<T> post(Class<T> c) {
            return handle(c, build("POST"));
        }
                
        public <T> Future<T> post(Class<T> c, Object requestEntity) {
            return handle(c, build("POST", requestEntity));
        }
        
        public Future<?> delete() {
            return voidHandle(build("DELETE"));
        }

        public Future<?> delete(Object requestEntity) {
            return voidHandle(build("DELETE", requestEntity));
        }
        
        public <T> Future<T> delete(Class<T> c) {
            return handle(c, build("DELETE"));
        }
        
        public <T> Future<T> delete(Class<T> c, Object requestEntity) {
            return handle(c, build("DELETE", requestEntity));
        }
        
        public Future<?> method(String method) {
            return voidHandle(build(method));
        }

        public Future<?> method(String method, Object requestEntity) {
            return voidHandle(build(method, requestEntity));
        }

        public <T> Future<T> method(String method, Class<T> c) {
            return handle(c, build(method));
        }

        public <T> Future<T> method(String method, Class<T> c, Object requestEntity) {
            return handle(c, build(method, requestEntity));
        }        
    }
    
    
    private <T> Future<T> handle(final Class<T> c, final ClientRequest ro) {
        FutureTask<T> ft = new FutureTask<T>(new Callable<T>() {
            public T call() throws Exception {
                ClientResponse r = getHeadHandler().handle(ro);

                if (c == ClientResponse.class) return c.cast(r);

                if (r.getStatus() < 300) return r.getEntity(c);

                throw new UniformInterfaceException(r);
                // throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        new Thread(ft).start();
        return ft;
    }
    
    private Future<?> voidHandle(final ClientRequest ro) {
        FutureTask<?> ft = new FutureTask<Object>(new Callable<Object>() {
            public Object call() throws Exception {
                ClientResponse r = getHeadHandler().handle(ro);

                if (r.getStatus() >= 300) throw new UniformInterfaceException(r);
                return null;
            }
        });
        new Thread(ft).start();
        return ft;        
    }
}