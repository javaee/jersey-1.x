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

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.impl.ClientRequestImpl;
import com.sun.jersey.client.impl.CopyOnWriteHashMap;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An encapsulation of a Web resource capable of building requests
 * to send to the Web resource and processing responses returned from the Web
 * resource.
 * <p>
 * A WebResource instance is obtained from the {@link Client}.
 * <p>
 * The Web resource implements the {@link UniformInterface} to invoke the HTTP 
 * method on the Web resource. A client request may be built before invocation
 * on the uniform interface.
 * <p>
 * Methods to create a request and return a response are thread-safe. Methods
 * that modify filters are not guaranteed to be thread-safe.
 * 
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.client
 */
public class WebResource extends Filterable implements 
        RequestBuilder<WebResource.Builder>,
        UniformInterface {    
    private final URI u;
    private CopyOnWriteHashMap<String, Object> properties;

    /* package */ WebResource(ClientHandler c, CopyOnWriteHashMap<String, Object> properties, URI u) {
        super(c);
        this.u = u;
        this.properties = properties.clone();
    }
    
    private WebResource(WebResource that, UriBuilder ub) {
        super(that);
        this.u = ub.build();
        properties = (that.properties == null ? null : that.properties.clone());
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
     * <code>WebResource</code> object whose URI is equal to the URI of this
     * <code>WebResource</code>.
     *
     * @param obj the object to compare this <code>WebResource</code> against.
     * @return true if the <code>WebResource</code> are equal; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof WebResource) {
            final WebResource that = (WebResource) obj;
            return that.u.equals(this.u);
        }
        return false;
    }

    // UniformInterface
    
    @Override
    public ClientResponse head() throws ClientHandlerException{
        ClientRequestImpl ro = new ClientRequestImpl(getURI(), "HEAD");
        setProperties(ro);
        return getHeadHandler().handle(ro);
    }
        
    @Override
    public <T> T options(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "OPTIONS"));
    }
        
    @Override
    public <T> T options(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "OPTIONS"));            
    }
        
    @Override
    public <T> T get(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "GET"));
    }
            
    @Override
    public <T> T get(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "GET"));            
    }
        
    @Override
    public void put() throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), "PUT", null));
    }
    
    @Override
    public void put(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }
    
    @Override
    public <T> T put(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "PUT"));
    }

    @Override
    public <T> T put(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "PUT"));
    }

    @Override
    public <T> T put(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }
            
    @Override
    public <T> T put(GenericType<T> gt, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }

    @Override
    public void post() throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), "POST"));
    }
    
    @Override
    public void post(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), "POST", requestEntity));
    }
    
    @Override
    public <T> T post(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "POST"));
    }

    @Override
    public <T> T post(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "POST"));
    }
    
    @Override
    public <T> T post(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }
            
    @Override
    public <T> T post(GenericType<T> gt, Object requestEntity)
            throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }
    
    @Override
    public void delete() throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), "DELETE"));
    }
    
    @Override
    public void delete(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }
    
    @Override
    public <T> T delete(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE"));    
    }

    @Override
    public <T> T delete(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "DELETE"));
    }
    
    @Override
    public <T> T delete(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }
      
    @Override
    public <T> T delete(GenericType<T> gt, Object requestEntity)
            throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }
    
    @Override
    public void method(String method) throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), method));        
    }
    
    @Override
    public void method(String method, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
        voidHandle(new ClientRequestImpl(getURI(), method, requestEntity));        
    }
    
    @Override
    public <T> T method(String method, Class<T> c) throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), method));            
    }
    
    @Override
    public <T> T method(String method, GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), method));            
    }
    
    @Override
    public <T> T method(String method, Class<T> c, Object requestEntity)
            throws UniformInterfaceException, ClientHandlerException {
        return handle(c, new ClientRequestImpl(getURI(), method, requestEntity));        
    }
    
    @Override
    public <T> T method(String method, GenericType<T> gt, Object requestEntity)
            throws UniformInterfaceException, ClientHandlerException {
        return handle(gt, new ClientRequestImpl(getURI(), method, requestEntity));        
    }
    
    // RequestBuilder<WebResource.Builder>
    
    @Override
    public Builder entity(Object entity) {
        return getRequestBuilder().entity(entity);
    }

    @Override
    public Builder entity(Object entity, MediaType type) {
        return getRequestBuilder().entity(entity, type);
    }

    @Override
    public Builder entity(Object entity, String type) {
        return getRequestBuilder().entity(entity, type);
    }

    @Override
    public Builder type(MediaType type) {
        return getRequestBuilder().type(type);
    }
        
    @Override
    public Builder type(String type) {
        return getRequestBuilder().type(type);
    }
    
    @Override
    public Builder accept(MediaType... types) {
        return getRequestBuilder().accept(types);
    }

    @Override
    public Builder accept(String... types) {
        return getRequestBuilder().accept(types);
    }

    @Override
    public Builder acceptLanguage(Locale... locales) {
        return getRequestBuilder().acceptLanguage(locales);
    }

    @Override
    public Builder acceptLanguage(String... locales) {
        return getRequestBuilder().acceptLanguage(locales);
    }

    @Override
    public Builder cookie(Cookie cookie) {
        return getRequestBuilder().cookie(cookie);
    }
    
    @Override
    public Builder header(String name, Object value) {
        return getRequestBuilder().header(name, value);
    }

    // URI specific building
    
    /**
     * Create a new WebResource from this web resource with an additional path
     * added to the URI of this web resource.
     * <p>
     * Any filters on this web resource are inherited. Removal of filters
     * may cause undefined behaviour.
     *
     * @param path the additional path.
     * 
     * @return the new web resource.
     */
    public WebResource path(String path) {
        return new WebResource(this, getUriBuilder().path(path));
    }

    /**
     * Create a new WebResource from this web resource.
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
    public WebResource uri(URI uri) {
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
        return new WebResource(this, b);
    }

    /**
     * Create a new WebResource from this web resource with an additional
     * query parameter added to the URI of this web resource.
     *
     * @param key the query parameter name
     * @param value the query parameter value
     * @return the new web resource.
     */
    public WebResource queryParam(String key, String value) {
        UriBuilder b = getUriBuilder();
        b.queryParam(key, value);
        return new WebResource(this, b);
    }

    /**
     * Create a new WebResource from this web resource with additional
     * query parameters added to the URI of this web resource.
     *
     * @param params the query parameters.
     * @return the new web resource.
     */
    public WebResource queryParams(MultivaluedMap<String, String> params) {
        UriBuilder b = getUriBuilder();
        for (Map.Entry<String, List<String>> e : params.entrySet()) {
            for (String value : e.getValue())
                b.queryParam(e.getKey(), value);
        }
        return new WebResource(this, b);
    }

    // Builder that builds client request and handles it
    
    /**
     * The builder for building a {@link ClientRequest} instance and 
     * handling the request using the {@link UniformInterface}. The methods
     * of the {@link UniformInterface} are the build methods of the builder.
     */
    public class Builder extends PartialRequestBuilder<Builder>
            implements UniformInterface {  


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
        
        @Override
        public ClientResponse head() throws ClientHandlerException {
            return getHeadHandler().handle(build("HEAD"));
        }
        
        @Override
        public <T> T options(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("OPTIONS"));
        }
                
        @Override
        public <T> T options(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("OPTIONS"));            
        }
        
        @Override
        public <T> T get(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("GET"));
        }
                
        @Override
        public <T> T get(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("GET"));            
        }
        
        @Override
        public void put() throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build("PUT"));
        }

        @Override
        public void put(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build("PUT", requestEntity));
        }
        
        @Override
        public <T> T put(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("PUT"));
        }
        
        @Override
        public <T> T put(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("PUT"));
        }

        @Override
        public <T> T put(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("PUT", requestEntity));
        }
        
        @Override
        public <T> T put(GenericType<T> gt, Object requestEntity)
                throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("PUT", requestEntity));
        }

        @Override
        public void post() throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build("POST"));
        }

        @Override
        public void post(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build("POST", requestEntity));
        }

        @Override
        public <T> T post(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("POST"));
        }
                
        @Override
        public <T> T post(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("POST"));
        }

        @Override
        public <T> T post(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("POST", requestEntity));
        }
        
        @Override
        public <T> T post(GenericType<T> gt, Object requestEntity)
                throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("POST", requestEntity));
        }
    
        @Override
        public void delete() throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build("DELETE"));
        }

        @Override
        public void delete(Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build("DELETE", requestEntity));
        }
        
        @Override
        public <T> T delete(Class<T> c) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("DELETE"));
        }
        
        @Override
        public <T> T delete(GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("DELETE"));
        }

        @Override
        public <T> T delete(Class<T> c, Object requestEntity) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build("DELETE", requestEntity));
        }
        
        @Override
        public <T> T delete(GenericType<T> gt, Object requestEntity)
                throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build("DELETE", requestEntity));
        }
    
        @Override
        public void method(String method) throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build(method));
        }

        @Override
        public void method(String method, Object requestEntity)
                throws UniformInterfaceException, ClientHandlerException {
            voidHandle(build(method, requestEntity));
        }

        @Override
        public <T> T method(String method, Class<T> c) throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build(method));
        }

        @Override
        public <T> T method(String method, GenericType<T> gt) throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build(method));
        }

        @Override
        public <T> T method(String method, Class<T> c, Object requestEntity)
                throws UniformInterfaceException, ClientHandlerException {
            return handle(c, build(method, requestEntity));
        }        
        
        @Override
        public <T> T method(String method, GenericType<T> gt, Object requestEntity)
                throws UniformInterfaceException, ClientHandlerException {
            return handle(gt, build(method, requestEntity));
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
     * <p>Properties are inherited, so setting properties on "parent" WebResource
     * instance, creating child (for example via WebResource.path("subpath"))
     * will set parents properties on it. However changing child properties
     * won't cause change in parent's properties.
     *
     * <p>Methods entrySet(), keySet() and values() are returning read-only
     * results (via Collection.unmodifiableMap).
     *
     * @return map containing all properties.
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
    
    private <T> T handle(Class<T> c, ClientRequest ro) throws UniformInterfaceException, ClientHandlerException {
        setProperties(ro);
        ClientResponse r = getHeadHandler().handle(ro);
        
        if (c == ClientResponse.class) return c.cast(r);
        
        if (r.getStatus() < 300) return r.getEntity(c);
        
        throw new UniformInterfaceException(r,
                ro.getPropertyAsFeature(ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
    }

    private <T> T handle(GenericType<T> gt, ClientRequest ro) throws UniformInterfaceException, ClientHandlerException {
        setProperties(ro);
        ClientResponse r = getHeadHandler().handle(ro);
        
        if (gt.getRawClass() == ClientResponse.class) return gt.getRawClass().cast(r);
        
        if (r.getStatus() < 300) return r.getEntity(gt);
        
        throw new UniformInterfaceException(r,
                ro.getPropertyAsFeature(ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
    }
    
    private void voidHandle(ClientRequest ro) throws UniformInterfaceException, ClientHandlerException {
        setProperties(ro);
        ClientResponse r = getHeadHandler().handle(ro);
        
        if (r.getStatus() >= 300) 
            throw new UniformInterfaceException(r,
                    ro.getPropertyAsFeature(ClientConfig.PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION, true));
        
        r.close();
    }
}
