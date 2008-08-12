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
package com.sun.jersey.spi.container;

import com.sun.jersey.api.Responses;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.impl.MultivaluedMapImpl;
import com.sun.jersey.impl.VariantSelector;
import com.sun.jersey.impl.http.header.AcceptableLanguageTag;
import com.sun.jersey.impl.http.header.HttpHeaderFactory;
import com.sun.jersey.impl.http.header.reader.HttpHeaderReader;
import com.sun.jersey.impl.model.HttpHelper;
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
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * Containers instantiate, or inherit, and provide an instance to the 
 * {@link WebApplication}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContainerRequest implements HttpRequestContext {
    private static final Logger LOGGER = Logger.getLogger(ContainerRequest.class.getName());
    
    private static final Annotation[] EMTPTY_ANNOTATIONS = new Annotation[0];
    
    private final MessageBodyWorkers bodyContext;
    
    private String method;
    
    private InputStream entity;
    
    private URI baseUri;
    
    private URI requestUri;
    
    private URI absolutePathUri;
    
    private InBoundHeaders headers;

    private int headersModCount;
    
    private MediaType contentType;
    
    private List<MediaType> accept;
    
    private List<Locale> acceptLanguages;
    
    private Map<String, Cookie> cookies;
    
    private MultivaluedMap<String, String> cookieNames;
    
    /**
     * Create a new container request.
     * <p>
     * The base URI and the request URI must contain the same scheme, user info, 
     * host and port components. 
     * 
     * The base URI must not contain the query and fragment components. The 
     * encoded path component of the request URI must start with the encoded 
     * path component of the base URI. The encoded path component of the base 
     * URI must end in a '/' character.
     * 
     * @param wa the web application
     * @param method the HTTP method
     * @param baseUri the base URI of the request
     * @param requestUri the request URI
     * @param headers the request headers
     * @param entity the InputStream of the request entity
     */
    public ContainerRequest(
            WebApplication wa,
            String method,
            URI baseUri,
            URI requestUri,
            InBoundHeaders headers,
            InputStream entity) {        
        this.bodyContext = wa.getMessageBodyWorkers();
        this.method = method;
        this.baseUri = baseUri;
        this.requestUri = requestUri;
        this.headers = headers;
        this.headersModCount = headers.getModCount();
        this.entity = entity;        
    }
    
    // ContainerRequest
    
    /**
     * Set the HTTP method.
     * @param method the method.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Set the base and request URI.
     * 
     * @param baseUri the base URI.
     * @param requestUri the (complete) request URI.
     */
    public void setUris(URI baseUri, URI requestUri) {
        this.baseUri = baseUri;
        this.requestUri = requestUri;
        
        // reset state
        absolutePathUri = null;
    }
    
    /**
     * Set the input stream of the entity.
     * @param entity the input stream of the entity.
     */
    public void setEntity(InputStream entity) {
        this.entity = entity;
    }
            
    /**
     * Set the request headers.
     * 
     * @param headers the request headers.
     */
    public void setHeaders(InBoundHeaders headers) {
        this.headers = headers;
        this.headersModCount = headers.getModCount();
        
        // reset state
        contentType = null;
        accept = null;
        cookies = null;
        cookieNames = null;        
    }
        
    // HttpRequestContext
    
    public URI getBaseUri() {
        return baseUri;
    }
       
    public URI getRequestUri() {
        return requestUri;
    }
    
    public URI getAbsolutePath() {
        if (absolutePathUri != null) return absolutePathUri;
        
        return absolutePathUri = UriBuilder.fromUri(requestUri).encode(false).
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
                LOGGER.severe("A message body reader for Java type, " + type + 
                        ", and MIME media type, " + mediaType + ", was not found");    
                
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
    
    public MediaType getAcceptableMediaType(List<MediaType> mediaTypes) {
        if (mediaTypes.isEmpty())
            return getAcceptableMediaTypes().get(0);
        
        for (MediaType a : getAcceptableMediaTypes()) {
            if (a.getType().equals(MediaType.MEDIA_TYPE_WILDCARD))
                return mediaTypes.get(0);
            
            for (MediaType m : mediaTypes)
                if (m.isCompatible(a) && !m.isWildcardType() && !m.isWildcardSubtype()) 
                    return m;
        }        
        return null;
    }

    public MultivaluedMap<String, String> getCookieNameValueMap() {
        if (cookieNames == null || headersModCount != headers.getModCount()) {
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
        return headers.get(name);
    }
    
    public List<MediaType> getAcceptableMediaTypes() {
        if (accept == null || headersModCount != headers.getModCount())
            accept = new ArrayList<MediaType>(HttpHelper.getAccept(this));            
        
        return accept;
    }
    
    public List<Locale> getAcceptableLanguages() {
        if (acceptLanguages == null || headersModCount != headers.getModCount()) {
             List<AcceptableLanguageTag> alts = HttpHelper.getAcceptLangauge(this);
             
             acceptLanguages = new ArrayList<Locale>(alts.size());
             for(AcceptableLanguageTag alt : alts) {
                 acceptLanguages.add(new Locale(alt.getTag()));
             }
        }
        
        return acceptLanguages;
    }
    
    public MediaType getMediaType() {
        if (contentType == null || headersModCount != headers.getModCount())
            contentType = HttpHelper.getContentType(this);
        
        return contentType;
    }
    
    public Locale getLanguage() {
        final String localeString = this.getRequestHeaders().
                getFirst(HttpHeaders.CONTENT_LANGUAGE);
        if (localeString == null)
            return null;
        return new Locale(localeString);
    }
    
    public Map<String, Cookie> getCookies() {
        if (cookies == null || headersModCount != headers.getModCount()) {
            cookies = new HashMap<String, Cookie>();
            
            List<String> cl = getRequestHeaders().get(HttpHeaders.COOKIE);
            if (cl != null) {
                for (String cookie : cl) {
                    if (cookie != null)
                        cookies.putAll(
                                HttpHeaderFactory.createCookies(cookie));
                }
            }            
        }
        
        return cookies;
    }
    
    
    // Request
    
    public String getMethod() {
        return method;
    }

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
                String httpMethod = getMethod();
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

    public MultivaluedMap<String, String> getFormParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}