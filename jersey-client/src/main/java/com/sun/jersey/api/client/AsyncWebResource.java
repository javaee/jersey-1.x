/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.async.AsyncClientHandler;
import com.sun.jersey.api.client.async.FutureListener;
import com.sun.jersey.api.client.async.ITypeListener;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.impl.ClientRequestImpl;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import com.sun.jersey.client.impl.async.FutureClientResponseListener;

/**
 * An encapsulation of an asynchronous Web resource capable of building requests
 * to send to the Web resource and processing responses returned from the Web
 * resource.
 * <p>
 * An AsyncWebResource instance is obtained from the {@link Client}.
 * <p>
 * The Web resource implements the {@link AsyncUniformInterface} to invoke the HTTP
 * method on the Web resource. A client request may be built before invocation
 * on the uniform interface.
 * <p>
 * Methods to create a request and return a response are thread-safe. Methods
 * that modify filters are not guaranteed to be thread-safe.
 *
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.client
 */
public class AsyncWebResource extends Filterable implements
        AsyncClientHandler,
        RequestBuilder<AsyncWebResource.Builder>,
        AsyncUniformInterface {
    private static final Logger LOGGER = Logger.getLogger(AsyncWebResource.class.getName());

    private final ExecutorService executorService;

    private final URI u;

    private CopyOnWriteHashMap<String, Object> properties;

    protected AsyncWebResource(Client c,  CopyOnWriteHashMap<String, Object> properties, URI u) {
        super((ClientHandler)c);
        this.executorService = c.getExecutorService();
        this.u = u;
        this.properties = properties.clone();
    }

    protected AsyncWebResource(AsyncWebResource that, UriBuilder ub) {
        super(that);
        this.executorService = that.executorService;
        this.u = ub.build();
        this.properties = that.properties.clone();
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
     * @deprecated
     */
    @Deprecated
    public UriBuilder getBuilder() {
        return UriBuilder.fromUri(u);
    }

    /**
     * Get the URI builder to the resource.
     *
     * @return the URI builder.
     */
    public UriBuilder getUriBuilder() {
        return UriBuilder.fromUri(u);
    }

    /**
     * Get the ClientRequest builder.
     *
     * @return the ClientRequest builder.
     */
    public Builder getRequestBuilder() {
        return new Builder();
    }

    /**
     * @return the URI as a String instance
     */
    @Override
    public String toString() {
        return u.toString();
    }

    /**
     * Returns a hash code for this <code>WebResource</code>.
     * <p>
     * The hash code is the hash code of URI of this
     * <code>WebResource</code>.
     *
     * @return a hash code for this <code>WebResource</code>.
     */
    @Override
    public int hashCode() {
        return u.hashCode();
    }

    /**
     * Compares this resource to the specified object.
     * <p>
     * The result is true if and only if the argument is not null and is a
     * <code>AsyncWebResource</code> object whose URI is equal to the URI of this
     * <code>AsyncWebResource</code>.
     *
     * @param obj the object to compare this <code>AsyncWebResource</code> against.
     * @return true if the <code>AsyncWebResource</code> are equal; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof AsyncWebResource) {
            final AsyncWebResource that = (AsyncWebResource)obj;
            return that.u.equals(this.u);
        }
        return false;
    }

    // AsyncUniformInterface

    public Future<ClientResponse> head() {
        return handle(ClientResponse.class, new ClientRequestImpl(getURI(), "HEAD"));
    }

    public Future<ClientResponse> head(ITypeListener<ClientResponse> l) {
        return handle(l, new ClientRequestImpl(getURI(), "HEAD"));
    }

    public <T> Future<T> options(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "OPTIONS"));
    }

    public <T> Future<T> options(GenericType<T> gt) {
        return handle(gt, new ClientRequestImpl(getURI(), "OPTIONS"));
    }

    public <T> Future<T> options(ITypeListener<T> l) {
        return handle(l, new ClientRequestImpl(getURI(), "OPTIONS"));
    }

    public <T> Future<T> get(Class<T> c) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), "GET"));
    }

    public <T> Future<T> get(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "GET"));
    }

    public <T> Future<T> get(ITypeListener<T> l) {
        return handle(l, new ClientRequestImpl(getURI(), "GET"));
    }

    public Future<?> put() throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), "PUT", null));
    }

    public Future<?> put(Object requestEntity) throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }

    public <T> Future<T> put(Class<T> c) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), "PUT"));
    }

    public <T> Future<T> put(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "PUT"));
    }

    public <T> Future<T> put(ITypeListener<T> l) {
        return handle(l, new ClientRequestImpl(getURI(), "PUT"));
    }

    public <T> Future<T> put(Class<T> c, Object requestEntity) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }

    public <T> Future<T> put(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }

    public <T> Future<T> put(ITypeListener<T> l, Object requestEntity) {
        return handle(l, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }

    public Future<?> post() throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), "POST"));
    }

    public Future<?> post(Object requestEntity) throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), "POST", requestEntity));
    }

    public <T> Future<T> post(Class<T> c) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), "POST"));
    }

    public <T> Future<T> post(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "POST"));
    }

    public <T> Future<T> post(ITypeListener<T> l) {
        return handle(l, new ClientRequestImpl(getURI(), "POST"));
    }

    public <T> Future<T> post(Class<T> c, Object requestEntity) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }

    public <T> Future<T> post(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }

    public <T> Future<T> post(ITypeListener<T> l, Object requestEntity) {
        return handle(l, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }

    public Future<?> delete() throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), "DELETE"));
    }

    public Future<?> delete(Object requestEntity) throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }

    public <T> Future<T> delete(Class<T> c) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE"));
    }

    public <T> Future<T> delete(GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "DELETE"));
    }

    public <T> Future<T> delete(ITypeListener<T> l) {
        return handle(l, new ClientRequestImpl(getURI(), "DELETE"));
    }

    public <T> Future<T> delete(Class<T> c, Object requestEntity) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }

    public <T> Future<T> delete(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }

    public <T> Future<T> delete(ITypeListener<T> l, Object requestEntity) {
        return handle(l, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }

    public Future<?> method(String method) throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), method));
    }

    public Future<?> method(String method, Object requestEntity) throws UniformInterfaceException {
        return voidHandle(new ClientRequestImpl(getURI(), method, requestEntity));
    }

    public <T> Future<T> method(String method, Class<T> c) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), method));
    }

    public <T> Future<T> method(String method, GenericType<T> gt) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), method));
    }

    public <T> Future<T> method(String method, ITypeListener<T> l) {
        return handle(l, new ClientRequestImpl(getURI(), method));
    }

    public <T> Future<T> method(String method, Class<T> c, Object requestEntity) throws UniformInterfaceException {
        return handle(c, new ClientRequestImpl(getURI(), method, requestEntity));
    }

    public <T> Future<T> method(String method, GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
        return handle(gt, new ClientRequestImpl(getURI(), method, requestEntity));
    }

    public <T> Future<T> method(String method, ITypeListener<T> l, Object requestEntity) {
        return handle(l, new ClientRequestImpl(getURI(), method, requestEntity));
    }


    // RequestBuilder<WebResource.Builder>

    public Builder entity(Object entity) {
        return getRequestBuilder().entity(entity);
    }

    public Builder entity(Object entity, MediaType type) {
        return getRequestBuilder().entity(entity, type);
    }

    public Builder entity(Object entity, String type) {
        return getRequestBuilder().entity(entity, type);
    }

    public Builder type(MediaType type) {
        return getRequestBuilder().type(type);
    }

    public Builder type(String type) {
        return getRequestBuilder().type(type);
    }

    public Builder accept(MediaType... types) {
        return getRequestBuilder().accept(types);
    }

    public Builder accept(String... types) {
        return getRequestBuilder().accept(types);
    }

    public Builder acceptLanguage(Locale... locales) {
        return getRequestBuilder().acceptLanguage(locales);
    }

    public Builder acceptLanguage(String... locales) {
        return getRequestBuilder().acceptLanguage(locales);
    }

    public Builder cookie(Cookie cookie) {
        return getRequestBuilder().cookie(cookie);
    }

    public Builder header(String name, Object value) {
        return getRequestBuilder().header(name, value);
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
        return new AsyncWebResource(this, getUriBuilder().path(path));
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
        UriBuilder b = getUriBuilder();
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

    /**
     * Create a new WebResource from this web resource with an additional
     * query parameter added to the URI of this web resource.
     *
     * @param key the query parameter name
     * @param value the query parameter value
     * @return the new web resource.
     */
    public AsyncWebResource queryParam(String key, String value) {
        UriBuilder b = getUriBuilder();
        b.queryParam(key, value);
        return new AsyncWebResource(this, b);
    }

    /**
     * Create a new WebResource from this web resource with additional
     * query parameters added to the URI of this web resource.
     *
     * @param params the query parameters.
     * @return the new web resource.
     */
    public AsyncWebResource queryParams(MultivaluedMap<String, String> params) {
        UriBuilder b = getUriBuilder();
        for (Map.Entry<String, List<String>> e : params.entrySet()) {
            for (String value : e.getValue())
                b.queryParam(e.getKey(), value);
        }
        return new AsyncWebResource(this, b);
    }

    // Builder that builds client request and handles it

    /**
     * The builder for building a {@link ClientRequest} instance and
     * handling the request using the {@link UniformInterface}. The methods
     * of the {@link UniformInterface} are the build methods of the builder.
     */
    public class Builder extends PartialRequestBuilder<Builder>
            implements AsyncUniformInterface {

        private Builder() {
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

        public Future<ClientResponse> head(ITypeListener<ClientResponse> l) {
            return handle(l, build("HEAD"));
        }

        public <T> Future<T> options(Class<T> c) {
            return handle(c, build("OPTIONS"));
        }

        public <T> Future<T> options(GenericType<T> gt) {
            return handle(gt, build("OPTIONS"));
        }

        public <T> Future<T> options(ITypeListener<T> l) {
            return handle(l, build("OPTIONS"));
        }

        public <T> Future<T> get(Class<T> c) {
            return handle(c, build("GET"));
        }

        public <T> Future<T> get(GenericType<T> gt) {
            return handle(gt, build("GET"));
        }

        public <T> Future<T> get(ITypeListener<T> l) {
            return handle(l, build("GET"));
        }

        public Future<?> put() throws UniformInterfaceException {
            return voidHandle(build("PUT"));
        }

        public Future<?> put(Object requestEntity) throws UniformInterfaceException {
            return voidHandle(build("PUT", requestEntity));
        }

        public <T> Future<T> put(Class<T> c) throws UniformInterfaceException {
            return handle(c, build("PUT"));
        }

        public <T> Future<T> put(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("PUT"));
        }

        public <T> Future<T> put(ITypeListener<T> l) {
            return handle(l, build("PUT"));
        }

        public <T> Future<T> put(Class<T> c, Object requestEntity) throws UniformInterfaceException {
            return handle(c, build("PUT", requestEntity));
        }

        public <T> Future<T> put(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build("PUT", requestEntity));
        }

        public <T> Future<T> put(ITypeListener<T> l, Object requestEntity) {
            return handle(l, build("PUT", requestEntity));
        }

        public Future<?> post() throws UniformInterfaceException {
            return voidHandle(build("POST"));
        }

        public Future<?> post(Object requestEntity) throws UniformInterfaceException {
            return voidHandle(build("POST", requestEntity));
        }

        public <T> Future<T> post(Class<T> c) throws UniformInterfaceException {
            return handle(c, build("POST"));
        }

        public <T> Future<T> post(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("POST"));
        }

        public <T> Future<T> post(ITypeListener<T> l) {
            return handle(l, build("POST"));
        }

        public <T> Future<T> post(Class<T> c, Object requestEntity) throws UniformInterfaceException {
            return handle(c, build("POST", requestEntity));
        }

        public <T> Future<T> post(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build("POST", requestEntity));
        }

        public <T> Future<T> post(ITypeListener<T> l, Object requestEntity) {
            return handle(l, build("POST", requestEntity));
        }

        public Future<?> delete() throws UniformInterfaceException {
            return voidHandle(build("DELETE"));
        }

        public Future<?> delete(Object requestEntity) throws UniformInterfaceException {
            return voidHandle(build("DELETE", requestEntity));
        }

        public <T> Future<T> delete(Class<T> c) throws UniformInterfaceException {
            return handle(c, build("DELETE"));
        }

        public <T> Future<T> delete(GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build("DELETE"));
        }

        public <T> Future<T> delete(ITypeListener<T> l) {
            return handle(l, build("DELETE"));
        }

        public <T> Future<T> delete(Class<T> c, Object requestEntity) throws UniformInterfaceException {
            return handle(c, build("DELETE", requestEntity));
        }

        public <T> Future<T> delete(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build("DELETE", requestEntity));
        }

        public <T> Future<T> delete(ITypeListener<T> l, Object requestEntity) {
            return handle(l, build("DELETE", requestEntity));
        }

        public Future<?> method(String method) throws UniformInterfaceException {
            return voidHandle(build(method));
        }

        public Future<?> method(String method, Object requestEntity) throws UniformInterfaceException {
            return voidHandle(build(method, requestEntity));
        }

        public <T> Future<T> method(String method, Class<T> c) throws UniformInterfaceException {
            return handle(c, build(method));
        }

        public <T> Future<T> method(String method, GenericType<T> gt) throws UniformInterfaceException {
            return handle(gt, build(method));
        }

        public <T> Future<T> method(String method, ITypeListener<T> l) {
            return handle(l, build(method));
        }

        public <T> Future<T> method(String method, Class<T> c, Object requestEntity) throws UniformInterfaceException {
            return handle(c, build(method, requestEntity));
        }

        public <T> Future<T> method(String method, GenericType<T> gt, Object requestEntity) throws UniformInterfaceException {
            return handle(gt, build(method, requestEntity));
        }

        public <T> Future<T> method(String method, ITypeListener<T> l, Object requestEntity) {
            return handle(l, build(method, requestEntity));
        }
    }

    /**
     * Sets WebResource related property.
     *
     * @param property property identifier.
     * @param value value of given property.
     */
    public void setProperty(String property, Object value) {
        this.getProperties().put(property, value);
    }

    /**
     * Gets WebResource related properties.
     *
     * <p>Properties are inherited, so setting propeties on "parent" WebResource
     * instance, creating child (for example via WebResource.path("subpath"))
     * will set parents properties on it. However changing child properties
     * won't cause change in parent's properties.
     *
     * <p>Methods entrySet(), keySet() and values() are returning read-only
     * results (via Collection.unmodifiableMap).
     *
     * @return map containg all properties.
     */
    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new CopyOnWriteHashMap<String, Object>();
        }
        return properties;
    }

    private void setProperties(ClientRequest ro) {
        if (properties != null) {
            ro.setProperties(properties);
        }
    }

    private <T> Future<T> handle(final Class<T> c, final ClientRequest request) {
        setProperties(request);
        final FutureClientResponseListener<T> ftw = new FutureClientResponseListener<T>() {
            protected T get(ClientResponse response) {
                if (c == ClientResponse.class) return c.cast(response);

                if (response.getStatus() < 300) return response.getEntity(c);

                throw new UniformInterfaceException(response,
                        request.getPropertyAsFeature(ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
            }
        };

        ftw.setCancelableFuture(handle(request, ftw));

        return ftw;
    }

    private <T> Future<T> handle(final GenericType<T> gt, final ClientRequest request) {
        setProperties(request);
        final FutureClientResponseListener<T> ftw = new FutureClientResponseListener<T>() {
            protected T get(ClientResponse response) {
                if (gt.getRawClass() == ClientResponse.class) return gt.getRawClass().cast(response);

                if (response.getStatus() < 300) return response.getEntity(gt);

                throw new UniformInterfaceException(response,
                        request.getPropertyAsFeature(ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
            }
        };

        ftw.setCancelableFuture(handle(request, ftw));

        return ftw;
    }

    private <T> Future<T> handle(final ITypeListener<T> l, final ClientRequest request) {
        setProperties(request);
        final FutureClientResponseListener<T> ftw = new FutureClientResponseListener<T>() {
            @Override
            protected void done() {
                try {
                    l.onComplete(this);
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE,
                            "Throwable caught on call to ITypeListener.onComplete",
                            t);
                }
            }

            protected T get(ClientResponse response) {
                if (l.getType() == ClientResponse.class) return (T)response;

                if (response.getStatus() < 300) {
                    if (l.getGenericType() == null) {
                        return response.getEntity(l.getType());
                    } else {
                        return response.getEntity(l.getGenericType());
                    }
                }
                throw new UniformInterfaceException(response,
                        request.getPropertyAsFeature(ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
            }
        };

        ftw.setCancelableFuture(handle(request, ftw));

        return ftw;
    }

    private Future<?> voidHandle(final ClientRequest request) {
        setProperties(request);
        final FutureClientResponseListener<?> ftw = new FutureClientResponseListener() {
            protected Object get(ClientResponse response) {
                if (response.getStatus() >= 300)
                    throw new UniformInterfaceException(response,
                            request.getPropertyAsFeature(ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
                response.close();
                return null;
            }
        };

        ftw.setCancelableFuture(handle(request, ftw));

        return ftw;
    }

    // AsyncClientHandler

    public Future<ClientResponse> handle(final ClientRequest request, final FutureListener<ClientResponse> l) {
        setProperties(request);
        Callable<ClientResponse> c = new Callable<ClientResponse>() {
            public ClientResponse call() throws Exception {
                return getHeadHandler().handle(request);
            }
        };
        FutureTask<ClientResponse> ft = new FutureTask<ClientResponse>(c) {
            @Override
            protected void done() {
                try {
                    l.onComplete(this);
                } catch (Throwable t) {
                    LOGGER.log(Level.SEVERE,
                            "Throwable caught on call to ClientResponseListener.onComplete",
                            t);
                }
            }
        };

        executorService.submit(ft);
        return ft;
    }
}
