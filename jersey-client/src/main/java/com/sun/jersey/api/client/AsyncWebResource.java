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
    
    private AsyncWebResource(AsyncWebResource that, UriBuilder ub) {
        super(that);
        this.u = ub.build();
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
        return handle(ClientResponse.class, new ClientRequestImpl(getURI(), "HEAD"));
    }
        
    public <T> Future<T> options(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "OPTIONS"));
    }
        
    public <T> Future<T> options(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "OPTIONS"));
    }

    public <T> Future<T> get(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "GET"));
    }
            
    public <T> Future<T> get(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "GET"));
    }

    public Future<?> put() {
        return voidHandle(new ClientRequestImpl(getURI(), "PUT", null));
    }
    
    public Future<?> put(Object requestEntity) {
        return voidHandle(new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }
    
    public <T> Future<T> put(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "PUT"));
    }

    public <T> Future<T> put(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "PUT"));
    }

    public <T> Future<T> put(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }
            
    public <T> Future<T> put(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }

    public Future<?> post() {
        return voidHandle(new ClientRequestImpl(getURI(), "POST"));
    }
    
    public Future<?> post(Object requestEntity) {
        return voidHandle(new ClientRequestImpl(getURI(), "POST", requestEntity));
    }
    
    public <T> Future<T> post(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "POST"));
    }

    public <T> Future<T> post(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "POST"));
    }

    public <T> Future<T> post(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }
            
    public <T> Future<T> post(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }

    public Future<?> delete() {
        return voidHandle(new ClientRequestImpl(getURI(), "DELETE"));
    }
    
    public Future<?> delete(Object requestEntity) {
        return voidHandle(new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }
    
    public <T> Future<T> delete(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE"));    
    }

    public <T> Future<T> delete(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "DELETE"));    
    }

    public <T> Future<T> delete(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }
      
    public <T> Future<T> delete(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }

    public Future<?> method(String method) {
        return voidHandle(new ClientRequestImpl(getURI(), method));        
    }
    
    public Future<?> method(String method, Object requestEntity) {
        return voidHandle(new ClientRequestImpl(getURI(), method, requestEntity));        
    }
    
    public <T> Future<T> method(String method, Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), method));            
    }
    
    public <T> Future<T> method(String method, GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), method));            
    }

    public <T> Future<T> method(String method, Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), method, requestEntity));        
    }
    
    public <T> Future<T> method(String method, GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), method, requestEntity));        
    }
    
    
    // RequestBuilder<WebResource.Builder>
    
    public Builder entity(Object entity) {
        return new Builder(getURI()).entity(entity);
    }

    public Builder entity(Object entity, MediaType type) {
        return new Builder(getURI()).entity(entity, type);
    }

    public Builder entity(Object entity, String type) {
        return new Builder(getURI()).entity(entity, type);
    }

    public Builder type(MediaType type) {
        return new Builder(getURI()).type(type);
    }
        
    public Builder type(String type) {
        return new Builder(getURI()).type(type);
    }
    
    public Builder accept(MediaType... types) {
        return new Builder(getURI()).accept(types);
    }

    public Builder accept(String... types) {
        return new Builder(getURI()).accept(types);
    }    
    
    public Builder acceptLanguage(Locale... locales) {
        return new Builder(getURI()).acceptLanguage(locales);
    }

    public Builder acceptLanguage(String... locales) {
        return new Builder(getURI()).acceptLanguage(locales);
    }    
    
    public Builder cookie(Cookie cookie) {
        return new Builder(getURI()).cookie(cookie);
    }
    
    public Builder header(String name, Object value) {
        return new Builder(getURI()).header(name, value);
    }

    // URI specific building
    
    /**
     * Create a new AsyncWebResource from this web resource with an additional path
     * added to the URI of this web resource.
     * <p>
     * Any filters on this web resource are inherited. Removal of filters
     * may cause undefined behaviour.
     *
     * @param path the additional path.
     * 
     * @return the new web resource.
     */
    public AsyncWebResource path(String path) {
        return new AsyncWebResource(this, getBuilder().path(path));
    }

    /**
     * Create a new AsyncWebResource from this web resource.
     * <p>
     * If the URI contains a path component and the path starts with a '/' then
     * the path of this web resource URI is replaced. Otherwise the path is
     * appended.
     * <p>
     * If the URI contains query parameters then those query parameters will
     * replace the query parameters (if any) of this web resource.
     * <p>
     * Any filters on this web resource are inherited. Removal of filters
     * may cause undefined behaviour.
     * 
     * @param uri the URI.
     * @return the new web resource.
     */
    public AsyncWebResource uri(URI uri) {
        UriBuilder b = getBuilder();
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
            b.replaceQuery(query);        
        }
        return new AsyncWebResource(this, b);
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
                
        public <T> Future<T> options(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("OPTIONS"));
        }

        public <T> Future<T> get(Class<T> c) {
            return handle(c, build("GET"));
        }
                
        public <T> Future<T> get(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("GET"));
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
        
        public <T> Future<T> put(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("PUT"));
        }

        public <T> Future<T> put(Class<T> c, Object requestEntity) {
            return handle(c, build("PUT", requestEntity));
        }
        
        public <T> Future<T> put(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build("PUT", requestEntity));
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
                
        public <T> Future<T> post(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("POST"));
        }

        public <T> Future<T> post(Class<T> c, Object requestEntity) {
            return handle(c, build("POST", requestEntity));
        }
        
        public <T> Future<T> post(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build("POST", requestEntity));
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
        
        public <T> Future<T> delete(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("DELETE"));
        }

        public <T> Future<T> delete(Class<T> c, Object requestEntity) {
            return handle(c, build("DELETE", requestEntity));
        }
        
        public <T> Future<T> delete(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build("DELETE", requestEntity));
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

        public <T> Future<T> method(String method, GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build(method));
        }

        public <T> Future<T> method(String method, Class<T> c, Object requestEntity) {
            return handle(c, build(method, requestEntity));
        }
        
        public <T> Future<T> method(String method, GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build(method, requestEntity));
        }
    }
    
    
    private <T> Future<T> handle(final Class<T> c, final ClientRequest ro) {
        FutureTask<T> ft = new FutureTask<T>(new Callable<T>() {
            public T call() throws Exception {
                ClientResponse r = getHeadHandler().handle(ro);

                if (c == ClientResponse.class) return c.cast(r);

                if (r.getStatus() < 300) return r.getEntity(c);

                throw new UniformInterfaceException("Status: " + r.getStatus(), r);
            }
        });
        new Thread(ft).start();
        return ft;
    }
    
    private <T> Future<T> handle(final GenericType<T> gt, final ClientRequest ro) {
        FutureTask<T> ft = new FutureTask<T>(new Callable<T>() {
            public T call() throws Exception {
                ClientResponse r = getHeadHandler().handle(ro);

                if (gt.getRawClass() == ClientResponse.class) gt.getRawClass().cast(r);

                if (r.getStatus() < 300) return r.getEntity(gt);

                throw new UniformInterfaceException("Status: " + r.getStatus(), r);
            }
        });
        new Thread(ft).start();
        return ft;
    }
    
    private Future<?> voidHandle(final ClientRequest ro) {
        FutureTask<?> ft = new FutureTask<Object>(new Callable<Object>() {
            public Object call() throws Exception {
                ClientResponse r = getHeadHandler().handle(ro);

                if (r.getStatus() >= 300) 
                    throw new UniformInterfaceException("Status: " + r.getStatus(), r);
                return null;
            }
        });
        new Thread(ft).start();
        return ft;        
    }
}