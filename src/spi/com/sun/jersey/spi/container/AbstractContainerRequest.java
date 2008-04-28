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

package com.sun.jersey.spi.container;

import com.sun.jersey.api.Responses;
import com.sun.ws.rest.impl.MultivaluedMapImpl;
import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import com.sun.ws.rest.impl.VariantSelector;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.model.HttpHelper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * An abstract implementation of {@link ContainerRequest}.
 * <p>
 * Specific containers may extend this class and instances may be passed to
 * the runtime using the method {@link WebApplication#handleRequest}.
 * <p>
 * The following are required by a concrete implementation when constructed
 * or before the instance is passed to the runtime.
 * <ul>
 * <li>The two protected fields baseUri and completeUri must be correctly
 * set.</li>
 * <li>The HTTP headers must be set by calling the method
 * {@link #getRequestHeaders} and copying the container specific headers
 * to the returned {@link MultivaluedMap} instance.</li>
 * </ul>
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractContainerRequest implements ContainerRequest {
    private static final Annotation[] EMTPTY_ANNOTATIONS = new Annotation[0];
    
    private final MessageBodyContext bodyContext;
    
    /**
     * The HTTP method
     */
    private final String method;
    
    /**
     * The input stream of the request entity, if present.
     */
    private final InputStream entity;
    
    /**
     * The base URI of the request.
     * <p>
     * The scheme, user info, host and port components must be equivalent to
     * the same componnents of the complete URI. 
     * 
     * The base URI must not contain the query and fragment components.
     *
     * The encoded path component of the complete URI must start with the 
     * encoded path component of the base URI.
     *
     * The encoded path component must end in a '/' character.
     */
    protected URI baseUri;
    
    /**
     * The complete URI of a request, including the query and fragment
     * components (if any).
     */
    protected URI completeUri;
    
    private URI absolutePathUri;
    
    private MultivaluedMap<String, String> headers;

    private MediaType contentType;
    
    private List<MediaType> accept;
    
    private Map<String, Cookie> cookies;
    
    private MultivaluedMap<String, String> cookieNames;
    
    /**
     *
     * @param bodyContext the message body context
     * @param method the HTTP method
     * @param entity the InputStream of the request entity
     */
    protected AbstractContainerRequest(MessageBodyContext bodyContext,
            String method, InputStream entity) {
        this.bodyContext = bodyContext;
        this.method = method;
        this.headers = new RequestHttpHeadersImpl();
        this.entity = entity;
    }
    
    // HttpRequestContext
    
    public URI getBaseUri() {
        return baseUri;
    }
       
    public URI getRequestUri() {
        return completeUri;
    }
    
    public URI getAbsolutePath() {
        if (absolutePathUri != null) return absolutePathUri;
        
        return absolutePathUri = UriBuilder.fromUri(completeUri).encode(false).
                replaceQueryParams("").fragment("").
                build();        
    }
    
    public String getHeaderValue(String name) {
        final List<String> v = getRequestHeaders().get(name);

        if (v == null) return null;
        
        if (v.isEmpty()) return "";
                
        if (v.size() == 1) return v.get(0);
        
        StringBuilder sb = new StringBuilder(v.get(0));
        for (int i = 1; i < v.size(); i++) {
            final String s = v.get(i);
            if (s.length() > 0)
                sb.append(',').append(s);
        }
        return sb.toString();
    }

    public <T> T getEntity(Class<T> type, Type genericType, Annotation[] as) {
        try {
            MediaType mediaType = getMediaType();
            MessageBodyReader<T> bw = bodyContext.getMessageBodyReader(
                    type, genericType, 
                    as, mediaType);
            if (bw == null) {
                throw new WebApplicationException(
                        Responses.unsupportedMediaType().build());
            }
            return bw.readFrom(type, genericType, as, mediaType, headers, entity);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public <T> T getEntity(Class<T> type) {
        return getEntity(type, type, EMTPTY_ANNOTATIONS);
    }
    
    public String getHttpMethod() {
        return method;
    }
    
    public MediaType getAcceptableMediaType(List<MediaType> mediaTypes) {
        for (MediaType a : getAcceptableMediaTypes()) {
            if (a.getType().equals(MediaType.MEDIA_TYPE_WILDCARD))
                return mediaTypes.get(0);
            
            for (MediaType m : mediaTypes)
                if (m.isCompatible(a)) return m;
        }
        
        return null;
    }

    public MultivaluedMap<String, String> getCookieNameValueMap() {
        if (cookieNames == null) {
            cookieNames = new MultivaluedMapImpl();
            for (Map.Entry<String, Cookie> e : getCookies().entrySet()) {
                cookieNames.putSingle(e.getKey(), e.getValue().getValue());
            }
        }
        
        return cookieNames;
    }
    
    // HttpHeaders
    
    public MultivaluedMap<String, String> getRequestHeaders() {
        return headers;
    }

    public List<String> getRequestHeader(String name) {
        throw new UnsupportedOperationException();        
    }
    
    public List<MediaType> getAcceptableMediaTypes() {
        if (accept == null)
            accept = new ArrayList<MediaType>(HttpHelper.getAccept(this));            
        
        return accept;
    }
    
    public List<String> getAcceptableLanguages() {
        throw new UnsupportedOperationException();
    }
    
    public MediaType getMediaType() {
        if (contentType == null)
            contentType = HttpHelper.getContentType(this);
        
        return contentType;
    }
    
    public String getLanguage() {
        return this.getRequestHeaders().getFirst("Content-Langauge");
    }
    
    public Map<String, Cookie> getCookies() {
        if (cookies == null)
            cookies = new HashMap<String, Cookie>();
        
        return cookies;
    }
    
    
    // Request

    public Variant selectVariant(List<Variant> variants) {
        if (variants == null || variants.isEmpty()) 
            throw new IllegalArgumentException("The list of variants is null or empty");
    
        // TODO mark the Vary header to be added to the response
        
        return VariantSelector.selectVariant(this, variants);
    }
    
    public ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        ResponseBuilder r = evaluateIfMatch(eTag);
        if (r == null)
            r = evaluateIfNoneMatch(eTag);
        
        return r;        
    }

    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        long lastModifiedTime = lastModified.getTime();
        ResponseBuilder r = evaluateIfUnmodifiedSince(lastModifiedTime);
        if (r == null)
            r = evaluateIfModifiedSince(lastModifiedTime);
        
        return r;        
    }
    
    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        ResponseBuilder r = evaluateIfMatch(eTag);
        if (r == null) {
            long lastModifiedTime = lastModified.getTime();
            r = evaluateIfUnmodifiedSince(lastModifiedTime);
            if (r == null)
                r = evaluateIfNoneMatch(eTag);
            if (r == null)
                r = evaluateIfModifiedSince(lastModifiedTime);
        }
        
        return r;        
    }
        
    private ResponseBuilder evaluateIfMatch(EntityTag eTag) {
        String ifMatchHeader = getRequestHeaders().getFirst("If-Match");
        // TODO require support for eTag types
        // Strong comparison of entity tags is required
        if (ifMatchHeader != null &&
                !ifMatchHeader.trim().equals("*") &&
                !ifMatchHeader.contains(eTag.getValue())) {
            // 412 Precondition Failed
            return Responses.preconditionFailed();
        }
        
        return null;
    }
    
    private ResponseBuilder evaluateIfNoneMatch(EntityTag eTag) {
        String ifNoneMatchHeader = getRequestHeaders().getFirst("If-None-Match");
        if (ifNoneMatchHeader != null) {
            // TODO require support for eTag types
            // Weak entity tag comparisons can only be used
            // with GET/HEAD
            if (ifNoneMatchHeader.trim().equals("*") || ifNoneMatchHeader.contains(eTag.getValue())) {
                String httpMethod = getHttpMethod();
                if (httpMethod.equals("GET") || httpMethod.equals("HEAD")) {
                    // 304 Not modified
                    // TODO
                    // Include cache related header fields
                    // such as ETag
                    return Response.notModified(eTag);
                } else {
                    // 412 Precondition Failed
                    return Responses.preconditionFailed();
                }
            }
        }
        
        return null;
    }
    
    private ResponseBuilder evaluateIfUnmodifiedSince(long lastModified) {
        String ifUnmodifiedSinceHeader = getRequestHeaders().getFirst("If-Unmodified-Since");
        if (ifUnmodifiedSinceHeader != null) {
            try {
                long ifUnmodifiedSince = HttpHeaderReader.
                        readDate(ifUnmodifiedSinceHeader).getTime() + 1000;
                if (lastModified > ifUnmodifiedSince) {
                    // 412 Precondition Failed
                    return Responses.preconditionFailed();
                }
            } catch (ParseException ex) {
                // Ignore the header if parsing error
            }
        }
        
        return null;
    }
    
    private ResponseBuilder evaluateIfModifiedSince(long lastModified) {
        String ifModifiedSinceHeader = getRequestHeaders().getFirst("If-Modified-Since");
        if (ifModifiedSinceHeader != null) {
            try {
                // TODO round up if modified since or round down last modified
                long ifModifiedSince = HttpHeaderReader.
                        readDate(ifModifiedSinceHeader).getTime() + 1000;
                if (ifModifiedSince  > lastModified) {
                    // 304 Not modified
                    return Responses.notModified();
                }
            } catch (ParseException ex) {
                // Ignore the header if parsing error
            }
        }
        
        return null;
    }
    
    // SecurityContext
    
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }
    
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }
    
    public String getAuthenticationScheme() {
        throw new UnsupportedOperationException();
    }
}