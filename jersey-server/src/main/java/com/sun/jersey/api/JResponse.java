/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.api;

import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.core.spi.factory.ResponseImpl;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;

/**
 * Defines the contract between a returned instance and the runtime when
 * an application needs to provide metadata to the runtime.
 * <p>
 * JResponse is a type safe alternative to {@link Response} that preserves the
 * type information of response entity thus it is not necessary to utilize
 * {@link GenericEntity}. It provides equivalent functonality to
 * {@link Response}.
 * <p>
 * JResponse may be extended in combination with {@link AJResponseBuilder}
 * specialization when building responses.
 * <p>
 * Several methods have parameters of type URI, {@link UriBuilder} provides
 * convenient methods to create such values as does
 * {@link <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/net/URI.html#create(java.lang.String)">URI.create()</a>}.
 *
 * @param <E> The entity type
 * @see JResponseBuilder
 * @see Response
 * @author Paul.Sandoz@Sun.Com
 */
public class JResponse<E> {
    private final StatusType statusType;

    private final E entity;

    private final OutBoundHeaders headers;

    /**
     * Construct given a status type, entity and metadata.
     *
     * @param statusType the status type
     * @param headers the metadata, it is the callers responsibility to copy
     *        the metadata if necessary.
     * @param entity the entity
     */
    public JResponse(StatusType statusType, OutBoundHeaders headers, E entity) {
        this.statusType = statusType;
        this.entity = entity;
        this.headers = headers;
    }

    /**
     * Construct given a status, entity and metadata.
     * 
     * @param status the status
     * @param headers the metadata, it is the callers responsibility to copy
     *        the metadata if necessary.
     * @param entity the entity
     */
    public JResponse(int status, OutBoundHeaders headers, E entity) {
        this(ResponseImpl.toStatusType(status), headers, entity);
    }

    /**
     * Construct a shallow copy. The metadata map will be copied but not the
     * key/value references.
     *
     * @param that the JResponse to copy from.
     */
    public JResponse(JResponse<E> that) {
        this(that.statusType,
                that.headers != null ? new OutBoundHeaders(that.headers) : null,
                that.entity);
    }

    /**
     * Construct from a {@link AJResponseBuilder}.
     *
     * @param b the builder.
     */
    protected JResponse(AJResponseBuilder<E, ?> b) {
        this.statusType = b.getStatusType();
        this.entity = b.getEntity();
        this.headers = b.getMetadata();
    }

    /**
     * Convert to a {@link Response} compatible instance.
     *
     * @return the {@link Response} compatible instance.
     */
    public JResponseAsResponse toResponse() {
        return new JResponseAsResponse(this);
    }

    /**
     * Convert to a {@link Response} compatible instance.
     *
     * @param type the entity type
     * @return the {@link Response} compatible instance.
     */
    public JResponseAsResponse toResponse(Type type) {
        return new JResponseAsResponse(this, type);
    }

    /**
     * Get the status type associated with the response.
     *
     * @return the response status type.
     */
    public StatusType getStatusType() {
        return statusType;
    }

    /**
     * Get the status code associated with the response.
     * 
     * @return the response status code.
     */
    public int getStatus() {
        return statusType.getStatusCode();
    }

    /**
     * Get metadata associated with the response as a map. The returned map
     * may be subsequently modified by the JAX-RS runtime. Values will be
     * serialized using a {@link javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate}
     * if one is available via
     * {@link javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
     * for the class of the value or using the values {@code toString} method if a
     * header delegate is not available.
     *
     * @return response metadata as a map
     */
    public OutBoundHeaders getMetadata() {
        return headers;
    }
    
    /**
     * Get the response entity. The response will be serialized using a
     * MessageBodyWriter for the class and type the entity <code>E</code>.
     *
     * @return the response entity.
     * @see javax.ws.rs.ext.MessageBodyWriter
     */
    public E getEntity() {
        return entity;
    }

    /**
     * Get the type of the entity.
     *
     * @return the type of the entity.
     */
    public Type getType() {
        return getSuperclassTypeParameter(getClass());
    }
    
    private static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            return Object.class;
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }


    /**
     * Create a new {@link JResponseBuilder} by performing a shallow copy of an
     * existing {@link Response}. The returned builder has its own metadata map but
     * entries are simply references to the keys and values contained in the
     * supplied Response metadata map.
     *
     * @param <E> The entity type
     * @param response a Response from which the status code, entity and metadata
     * will be copied
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> fromResponse(Response response) {
        JResponseBuilder b = status(response.getStatus());
        b.entity(response.getEntity());
        for (String headerName: response.getMetadata().keySet()) {
            List<Object> headerValues = response.getMetadata().get(headerName);
            for (Object headerValue: headerValues) {
                b.header(headerName, headerValue);
            }
        }
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} by performing a shallow copy of an
     * existing {@link JResponse}. The returned builder has its own metadata map but
     * entries are simply references to the keys and values contained in the
     * supplied Response metadata map.
     *
     * @param <E> The entity type
     * @param response a JResponse from which the status code, entity and metadata
     * will be copied
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> fromResponse(JResponse<E> response) {
        JResponseBuilder<E> b = status(response.getStatus());
        b.entity(response.getEntity());
        for (String headerName: response.getMetadata().keySet()) {
            List<Object> headerValues = response.getMetadata().get(headerName);
            for (Object headerValue: headerValues) {
                b.header(headerName, headerValue);
            }
        }
        return b;
    }
    
    /**
     * Create a new {@link JResponseBuilder} with the supplied status.
     *
     * @param <E> The entity type
     * @param status the response status
     * @return a new JResponseBuilder
     * @throws IllegalArgumentException if status is null
     */
    public static <E> JResponseBuilder<E> status(StatusType status) {
        JResponseBuilder<E> b = new JResponseBuilder<E>();
        b.status(status);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} with the supplied status.
     *
     * @param <E> The entity type
     * @param status the response status
     * @return a new JResponseBuilder
     * @throws IllegalArgumentException if status is null
     */
    public static <E> JResponseBuilder<E> status(Response.Status status) {
        return status((StatusType)status);
    }

    /**
     * Create a new {@link JResponseBuilder} with the supplied status.
     *
     * @param <E> The entity type
     * @param status the response status
     * @return a new JResponseBuilder
     * @throws IllegalArgumentException if status is less than 100 or greater
     * than 599.
     */
    public static <E> JResponseBuilder<E> status(int status) {
        JResponseBuilder<E> b = new JResponseBuilder<E>();
        b.status(status);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} with an OK status.
     *
     * @param <E> The entity type
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> ok() {
        JResponseBuilder b = status(Status.OK);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} that contains a representation.
     *
     * @param <E> The entity type
     * @param entity the representation entity data
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> ok(E entity) {
        JResponseBuilder<E> b = ok();
        b.entity(entity);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} that contains a representation.
     *
     * @param <E> The entity type
     * @param entity the representation entity data
     * @param type the media type of the entity
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> ok(E entity, MediaType type) {
        JResponseBuilder<E> b = ok();
        b.entity(entity);
        b.type(type);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} that contains a representation.
     *
     * @param <E> The entity type
     * @param entity the representation entity data
     * @param type the media type of the entity
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> ok(E entity, String type) {
        JResponseBuilder<E> b = ok();
        b.entity(entity);
        b.type(type);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} that contains a representation.
     *
     * @param <E> The entity type
     * @param entity the representation entity data
     * @param variant representation metadata
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> ok(E entity, Variant variant) {
        JResponseBuilder<E> b = ok();
        b.entity(entity);
        b.variant(variant);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} with an server error status.
     *
     * @param <E> The entity type
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> serverError() {
        JResponseBuilder<E> b = status(Status.INTERNAL_SERVER_ERROR);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} for a created resource, set the
     * location header using the supplied value.
     *
     * @param <E> The entity type
     * @param location the URI of the new resource. If a relative URI is
     * supplied it will be converted into an absolute URI by resolving it
     * relative to the request URI (see {@link UriInfo#getRequestUri}).
     * @return a new JResponseBuilder
     * @throws java.lang.IllegalArgumentException if location is null
     */
    public static <E> JResponseBuilder<E> created(URI location) {
        JResponseBuilder<E> b = JResponse.<E>status(Status.CREATED).location(location);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} for an empty response.
     *
     * @param <E> The entity type
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> noContent() {
        JResponseBuilder<E> b = status(Status.NO_CONTENT);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} with a not-modified status.
     *
     * @param <E> The entity type
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> notModified() {
        JResponseBuilder<E> b = status(Status.NOT_MODIFIED);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} with a not-modified status.
     *
     * @param <E> The entity type
     * @param tag a tag for the unmodified entity
     * @return a new JResponseBuilder
     * @throws java.lang.IllegalArgumentException if tag is null
     */
    public static <E> JResponseBuilder<E> notModified(EntityTag tag) {
        JResponseBuilder<E> b = notModified();
        b.tag(tag);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} with a not-modified status
     * and a strong entity tag. This is a shortcut
     * for <code>notModified(new EntityTag(<i>value</i>))</code>.
     *
     * @param <E> The entity type
     * @param tag the string content of a strong entity tag. The JAX-RS
     * runtime will quote the supplied value when creating the header.
     * @return a new JResponseBuilder
     * @throws java.lang.IllegalArgumentException if tag is null
     */
    public static <E> JResponseBuilder<E> notModified(String tag) {
        JResponseBuilder b = notModified();
        b.tag(tag);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} for a redirection. Used in the
     * redirect-after-POST (aka POST/redirect/GET) pattern.
     *
     * @param <E> The entity type
     * @param location the redirection URI. If a relative URI is
     * supplied it will be converted into an absolute URI by resolving it
     * relative to the base URI of the application (see
     * {@link UriInfo#getBaseUri}).
     * @return a new JResponseBuilder
     * @throws java.lang.IllegalArgumentException if location is null
     */
    public static <E> JResponseBuilder<E> seeOther(URI location) {
        JResponseBuilder<E> b = JResponse.<E>status(Status.SEE_OTHER).location(location);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} for a temporary redirection.
     *
     * @param <E> The entity type
     * @param location the redirection URI. If a relative URI is
     * supplied it will be converted into an absolute URI by resolving it
     * relative to the base URI of the application (see
     * {@link UriInfo#getBaseUri}).
     * @return a new JResponseBuilder
     * @throws java.lang.IllegalArgumentException if location is null
     */
    public static <E> JResponseBuilder<E> temporaryRedirect(URI location) {
        JResponseBuilder<E> b = JResponse.<E>status(Status.TEMPORARY_REDIRECT).location(location);
        return b;
    }

    /**
     * Create a new {@link JResponseBuilder} for a not acceptable response.
     *
     * @param <E> The entity type
     * @param variants list of variants that were available, a null value is
     * equivalent to an empty list.
     * @return a new JResponseBuilder
     */
    public static <E> JResponseBuilder<E> notAcceptable(List<Variant> variants) {
        JResponseBuilder<E> b = JResponse.<E>status(Status.NOT_ACCEPTABLE).variants(variants);
        return b;
    }

    /**
     * A class used to build {@link JResponse} instances that contain metadata 
     * instead of or in addition to an entity. An initial instance may be
     * obtained via static methods of the {@link JResponse} class, instance
     * methods provide the ability to set metadata. E.g. to create a response
     * that indicates the creation of a new resource:
     * <pre>&#64;POST
     * JResponse addWidget(...) {
     *   Widget w = ...
     *   URI widgetId = UriBuilder.fromResource(Widget.class)...
     *   return JResponse.created(widgetId).build();
     * }</pre>
     * <p>
     * Several methods have parameters of type URI, {@link UriBuilder}
     * provides convenient methods to create such values as does
     * <code>URI.create()</code>.
     * <p>
     * Where multiple variants of the same method are provided, the type of
     * the supplied parameter is retained in the metadata of the built
     * {@link JResponse}.
     *
     * @param <E> The entity type
     */
    public static final class JResponseBuilder<E> extends AJResponseBuilder<E, JResponseBuilder<E>> {
        /**
         * Default constructor.
         */
        public JResponseBuilder() {}

        /**
         * Construct a shallow copy. The metadata map will be copied but not the
         * key/value references.
         *
         * @param that the JResponseBuilder to copy from.
         */
        public JResponseBuilder(JResponseBuilder<E> that) {
            super(that);
        }
        
        /**
         * Create a shallow copy preserving state. The metadata map will be
         * copied but not the key/value references.
         *
         * @return the copy.
         */
        @Override
        public JResponseBuilder<E> clone() {
            return new JResponseBuilder<E>(this);
        }

        /**
         * Create a {@link JResponse} instance from the current JResponseBuilder.
         * The builder is reset to a blank state equivalent to calling
         * {@link JResponse#ok() }.
         *
         * @return a JResponse instance
         */
        public JResponse<E> build() {
            JResponse<E> r = new JResponse<E>(this);
            reset();
            return r;
        }
    }

    /**
     * An abstract response builder that may be utilized to extend
     * response building and the construction of {@link JResponse}
     * instances.
     * 
     * @param <E> The entity type
     * @param <B> The builder type
     */
    public static abstract class AJResponseBuilder<E, B extends AJResponseBuilder> {
        /**
         * The status type.
         */
        protected StatusType statusType = Status.NO_CONTENT;

        /**
         * The response metadata.
         */
        protected OutBoundHeaders headers;

        /**
         * The entity.
         */
        protected E entity;

        /**
         * Default constructor.
         */
        protected AJResponseBuilder() {}

        /**
         * Construct a shallow copy. The metadata map will be copied but not the
         * key/value references.
         *
         * @param that the AJResponseBuilder to copy from.
         */
        protected AJResponseBuilder(AJResponseBuilder<E, ?> that) {
            this.statusType = that.statusType;
            this.entity = that.entity;
            if (that.headers != null) {
                this.headers = new OutBoundHeaders(that.headers);
            } else {
                this.headers = null;
            }
        }

        /**
         * Reset to the default state.
         */
        protected void reset() {
            statusType = Status.NO_CONTENT;
            entity = null;
            headers = null;
        }

        /**
         * Get the status type associated with the response.
         *
         * @return the response status type.
         */
        protected StatusType getStatusType() {
            return statusType;
        }

        /**
         * Get the status code associated with the response.
         *
         * @return the response status code.
         */
        protected int getStatus() {
            return statusType.getStatusCode();
        }

        /**
         * Get the metadata associated with the response.
         *
         * @return response metadata as a map
         */
        protected OutBoundHeaders getMetadata() {
            if (headers == null)
                headers = new OutBoundHeaders();
            return headers;
        }

        /**
         * Get the response entity.
         *
         * @return the response entity.
         */
        protected E getEntity() {
            return entity;
        }

        /**
         * Set the status.
         *
         * @param status the response status
         * @return the updated instance
         * @throws IllegalArgumentException if status is less than 100 or greater
         * than 599.
         */
        public B status(int status) {
            return status(ResponseImpl.toStatusType(status));
        }

        /**
         * Set the status.
         *
         * @param status the response status
         * @return the updated instance.
         * @throws IllegalArgumentException if status is null
         */
        public B status(StatusType status) {
            if (status == null)
                throw new IllegalArgumentException();
            this.statusType = status;
            return (B)this;
        };

        /**
         * Set the status.
         *
         * @param status the response status
         * @return the updated instance.
         * @throws IllegalArgumentException if status is null
         */
        public B status(Status status) {
            return status((StatusType)status);
        };

        /**
         * Set the entity.
         *
         * @param entity the response entity
         * @return the updated instance
         */
        public B entity(E entity) {
            this.entity = entity;
            return (B)this;
        }

        /**
         * Set the response media type.
         *
         * @param type the media type of the response entity, if null any
         * existing value for type will be removed
         * @return the updated instance
         */
        public B type(MediaType type) {
            headerSingle(HttpHeaders.CONTENT_TYPE, type);
            return (B)this;
        }

        /**
         * Set the response media type.
         *
         * @param type the media type of the response entity, if null any
         * existing value for type will be removed
         * @return the updated instance
         * @throws IllegalArgumentException if type cannot be parsed
         */
        public B type(String type) {
            return type(type == null ? null : MediaType.valueOf(type));
        }

        /**
         * Set representation metadata. Equivalent to setting the values of
         * content type, content language, and content encoding separately using
         * the values of the variant properties.
         *
         * @param variant metadata of the response entity, a null value is
         * equivalent to a variant with all null properties.
         * @return the updated instance
         */
        public B variant(Variant variant) {
            if (variant == null) {
                type((MediaType)null);
                language((String)null);
                encoding(null);
                return (B)this;
            }

            type(variant.getMediaType());
            // TODO set charset
            language(variant.getLanguage());
            encoding(variant.getEncoding());

            return (B)this;
        }

        /**
         * Add a Vary header that lists the available variants.
         *
         * @param variants a list of available representation variants, a null
         * value will remove an existing value for vary.
         * @return the updated instance
         */
        public B variants(List<Variant> variants) {
            if (variants == null) {
                header(HttpHeaders.VARY, null);
                return (B)this;
            }

            if (variants.isEmpty())
                return (B)this;

            MediaType accept = variants.get(0).getMediaType();
            boolean vAccept = false;

            Locale acceptLanguage = variants.get(0).getLanguage();
            boolean vAcceptLanguage = false;

            String acceptEncoding = variants.get(0).getEncoding();
            boolean vAcceptEncoding = false;

            for (Variant v : variants) {
                vAccept |= !vAccept && vary(v.getMediaType(), accept);
                vAcceptLanguage |= !vAcceptLanguage && vary(v.getLanguage(), acceptLanguage);
                vAcceptEncoding |= !vAcceptEncoding && vary(v.getEncoding(), acceptEncoding);
            }

            StringBuilder vary = new StringBuilder();
            append(vary, vAccept, HttpHeaders.ACCEPT);
            append(vary, vAcceptLanguage, HttpHeaders.ACCEPT_LANGUAGE);
            append(vary, vAcceptEncoding, HttpHeaders.ACCEPT_ENCODING);

            if (vary.length() > 0)
                header(HttpHeaders.VARY, vary.toString());
            return (B)this;
        }

        private boolean vary(MediaType v, MediaType vary) {
            return v != null && !v.equals(vary);
        }

        private boolean vary(Locale v, Locale vary) {
            return v != null && !v.equals(vary);
        }

        private boolean vary(String v, String vary) {
            return v != null && !v.equalsIgnoreCase(vary);
        }

        private void append(StringBuilder sb, boolean v, String s) {
            if (v) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(s);
            }
        }

        /**
         * Set the language.
         *
         * @param language the language of the response entity, if null any
         * existing value for language will be removed
         * @return the updated instance
         */
        public B language(String language) {
            headerSingle(HttpHeaders.CONTENT_LANGUAGE, language);
            return (B)this;
        }

        /**
         * Set the language.
         *
         * @param language the language of the response entity, if null any
         * existing value for type will be removed
         * @return the updated instance
         */
        public B language(Locale language) {
            headerSingle(HttpHeaders.CONTENT_LANGUAGE, language);
            return (B)this;
        }

        /**
         * Set the location.
         *
         * @param location the location. If a relative URI is
         * supplied it will be converted into an absolute URI by resolving it
         * relative to the base URI of the application (see
         * {@link UriInfo#getBaseUri}). If null any
         * existing value for location will be removed.
         * @return the updated instance.
         */
        public B location(URI location) {
            headerSingle(HttpHeaders.LOCATION, location);
            return (B)this;
        }

        /**
         * Set the content location.
         *
         * @param location the content location. Relative or absolute URIs
         * may be used for the value of content location. If null any
         * existing value for content location will be removed.
         * @return the updated instance
         */
        public B contentLocation(URI location) {
            headerSingle(HttpHeaders.CONTENT_LOCATION, location);
            return (B)this;
        }

        /**
         * Set the content encoding.
         *
         * @param encoding the content encoding of the response entity, if null 
         * any existing value for type will be removed
         * @return the updated instance
         */
        public B encoding(String encoding) {
            headerSingle(HttpHeaders.CONTENT_ENCODING, encoding);
            return (B)this;
        }

        /**
         * Set an entity tag.
         *
         * @param tag the entity tag, if null any
         * existing entity tag value will be removed.
         * @return the updated instance
         */
        public B tag(EntityTag tag) {
            headerSingle(HttpHeaders.ETAG, tag);
            return (B)this;
        }

        /**
         * Set a strong entity tag. This is a shortcut
         * for <code>tag(new EntityTag(<i>value</i>))</code>.
         *
         * @param tag the string content of a strong entity tag. The JAX-RS
         * runtime will quote the supplied value when creating the header. If
         * null any existing entity tag value will be removed.
         * @return the updated instance
         */
        public B tag(String tag) {
            return tag(tag == null ? null : new EntityTag(tag));
        }

        /**
         * Set the last modified date.
         *
         * @param lastModified the last modified date, if null any existing
         * last modified value will be removed.
         * @return the updated instance
         */
        public B lastModified(Date lastModified) {
            headerSingle(HttpHeaders.LAST_MODIFIED, lastModified);
            return (B)this;
        }

        /**
         * Set the cache control.
         *
         * @param cacheControl the cache control directives, if null removes any
         * existing cache control directives.
         * @return the updated instance
         */
        public B cacheControl(CacheControl cacheControl) {
            headerSingle(HttpHeaders.CACHE_CONTROL, cacheControl);
            return (B)this;
        }

        /**
         * Set the expires date.
         *
         * @param expires the expiration date, if null removes any existing
         * expires value.
         * @return the updated instance
         */
        public B expires(Date expires) {
            headerSingle(HttpHeaders.EXPIRES, expires);
            return (B)this;
        }

        /**
         * Add cookies.
         *
         * @param cookies new cookies that will accompany the response. A null
         * value will remove all cookies, including those added via the
         * {@link #header(java.lang.String, java.lang.Object)} method.
         * @return the updated instance
         */
        public B cookie(NewCookie... cookies) {
            if (cookies != null) {
                for (NewCookie cookie : cookies)
                    header(HttpHeaders.SET_COOKIE, cookie);
            } else {
                header(HttpHeaders.SET_COOKIE, null);
            }
            return (B)this;
        }

        /**
         * Add a header.
         *
         * @param name the name of the header
         * @param value the value of the header, the header will be serialized
         * using a {@link javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate} if
         * one is available via
         * {@link javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
         * for the class of {@code value} or using its {@code toString} method if a
         * header delegate is not available. If {@code value} is null then all
         * current headers of the same name will be removed.
         * @return the updated instance.
         */
        public B header(String name, Object value) {
            return header(name, value, false);
        }

        /**
         * Add a header or replace an existing header.
         *
         * @param name the name of the header
         * @param value the value of the header, the header will be serialized
         * using a {@link javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate} if
         * one is available via
         * {@link javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
         * for the class of {@code value} or using its {@code toString} method if a
         * header delegate is not available. If {@code value} is null then all
         * current headers of the same name will be removed.
         * @return the updated instance.
         */
        public B headerSingle(String name, Object value) {
            return header(name, value, true);
        }

        /**
         * Add a header.
         *
         * @param name the name of the header
         * @param value the value of the header, the header will be serialized
         * using a {@link javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate} if
         * one is available via
         * {@link javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
         * for the class of {@code value} or using its {@code toString} method if a
         * header delegate is not available. If {@code value} is null then all
         * current headers of the same name will be removed.
         * @param single if true then replace the header if it exists, otherwise
         * add the header.
         * @return the updated instance.
         */
        public B header(String name, Object value, boolean single) {
            if (value != null) {
                if (single) {
                    getMetadata().putSingle(name, value);
                } else {
                    getMetadata().add(name, value);
                }
            } else {
                getMetadata().remove(name);
            }
            return (B)this;
        }
    }
}