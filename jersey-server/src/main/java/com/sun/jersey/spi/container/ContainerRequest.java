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

import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.TraceInformation;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.server.impl.VariantSelector;
import com.sun.jersey.core.header.AcceptableLanguageTag;
import com.sun.jersey.core.header.MatchingEntityTag;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.server.impl.model.HttpHelper;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * An in-bound HTTP request to be processed by the web application.
 * <p>
 * Containers instantiate, or inherit, and provide an instance to the
 * {@link WebApplication}.
 * <p>
 * By default the implementation of {@link SecurityContext} will throw
 * {@link UnsupportedOperationException} if the methods are invoked.
 * Containers SHOULD use the method {@link #setSecurityContext(javax.ws.rs.core.SecurityContext) }
 * to define security context behaviour rather than extending from this class
 * and overriding the methods.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ContainerRequest implements HttpRequestContext {
    private static final Logger LOGGER = Logger.getLogger(ContainerRequest.class.getName());
    
    private static final Annotation[] EMTPTY_ANNOTATIONS = new Annotation[0];

    private final WebApplication wa;

    private final boolean isTraceEnabled;
    
    private Map<String, Object> properties;
    
    private String method;
    
    private InputStream entity;
    
    private URI baseUri;
    
    private URI requestUri;
    
    private URI absolutePathUri;
    
    private String encodedPath;
    
    private String decodedPath;

    private List<PathSegment> decodedPathSegments;

    private List<PathSegment> encodedPathSegments;
    
    private MultivaluedMap<String, String> decodedQueryParameters;

    private MultivaluedMap<String, String> encodedQueryParameters;
    
    private InBoundHeaders headers;

    private int headersModCount;
    
    private MediaType contentType;
    
    private List<MediaType> accept;
    
    private List<Locale> acceptLanguages;
    
    private Map<String, Cookie> cookies;
    
    private MultivaluedMap<String, String> cookieNames;

    private SecurityContext securityContext;
    
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
        this.wa = wa;
        this.isTraceEnabled = wa.isTracingEnabled();
        this.method = method;
        this.baseUri = baseUri;
        this.requestUri = requestUri;
        this.headers = headers;
        this.headersModCount = headers.getModCount();
        this.entity = entity;        
    }

    /* package */ ContainerRequest(ContainerRequest r) {
        this.wa = r.wa;
        this.isTraceEnabled = r.isTraceEnabled;
    }

    // ContainerRequest
    
    /**
     * Get the mutable properties.
     * <p>
     * Care should be taken not to clear the properties or remove properties
     * that are unknown otherwise unspecified behaviour may result.
     *
     * @return the properties.
     */
    public Map<String, Object> getProperties() {
        if (properties != null) {
            return properties;
        }

        return properties = new HashMap<String, Object>();
    }

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
        
        encodedPath = decodedPath = null;

        decodedPathSegments = encodedPathSegments = null;
        
        decodedQueryParameters = encodedQueryParameters = null;
    }
    
    /**
     * Get the input stream of the entity.
     *
     * @return the input stream of the entity.
     */
    public InputStream getEntityInputStream() {
        return entity;
    }
    
    /**
     * Set the input stream of the entity.
     * 
     * @param entity the input stream of the entity.
     */
    public void setEntityInputStream(InputStream entity) {
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

    /**
     * Set the security context.
     *
     * @param securityContext the security context.
     */
    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }
    
    /**
     * Get the message body workers.
     * 
     * @return the message body workers.
     */
    public MessageBodyWorkers getMessageBodyWorkers() {
        return wa.getMessageBodyWorkers();
    }

    // Traceable

    public boolean isTracingEnabled() {
        return isTraceEnabled;
    }

    public void trace(String message) {
        if (!isTracingEnabled())
            return;

        if (wa.getFeaturesAndProperties().getFeature(ResourceConfig.FEATURE_TRACE_PER_REQUEST) &&
                !getRequestHeaders().containsKey("X-Jersey-Trace-Accept"))
            return;

        TraceInformation ti = (TraceInformation)getProperties().
                get(TraceInformation.class.getName());
        ti.trace(message);
    }


    // HttpRequestContext
    
    public URI getBaseUri() {
        return baseUri;
    }
       
    public UriBuilder getBaseUriBuilder() {
        return UriBuilder.fromUri(getBaseUri());
    }
    
    public URI getRequestUri() {
        return requestUri;
    }
    
    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(getRequestUri());
    }
    
    public URI getAbsolutePath() {
        if (absolutePathUri != null) return absolutePathUri;
        
        return absolutePathUri = UriBuilder.fromUri(requestUri).
                replaceQuery("").fragment("").
                build();        
    }
    
    public UriBuilder getAbsolutePathBuilder() {
        return UriBuilder.fromUri(getAbsolutePath());
    }

    public String getPath() {
        return getPath(true);
    }
    
    public String getPath(boolean decode) {
        if (decode) {
            if (decodedPath != null) return decodedPath;
            
            return decodedPath = UriComponent.decode(
                    getEncodedPath(),
                    UriComponent.Type.PATH);
        } else {
            return getEncodedPath();
        }
    }
    
    private String getEncodedPath() {
        if (encodedPath != null) return encodedPath;
        
        return encodedPath  = getRequestUri().getRawPath().substring(
                getBaseUri().getRawPath().length());
    }
    
    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }
    
    public List<PathSegment> getPathSegments(boolean decode) {
        if (decode) {
            if (decodedPathSegments != null)
                return decodedPathSegments;
            
            return decodedPathSegments = UriComponent.decodePath(getPath(false), true);
        } else {
            if (encodedPathSegments != null)
                return encodedPathSegments;
            
            return encodedPathSegments = UriComponent.decodePath(getPath(false), false);
        }
    }
    
    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }
    
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        if (decode) {
            if (decodedQueryParameters != null)
                return decodedQueryParameters;
            
            return decodedQueryParameters = UriComponent.decodeQuery(
                    getRequestUri(), true);
        } else {
            if (encodedQueryParameters != null)
                return encodedQueryParameters;
            
            return encodedQueryParameters = UriComponent.decodeQuery(
                    getRequestUri(), false);
        }
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
        MediaType mediaType = getMediaType();
        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }

        MessageBodyReader<T> bw = getMessageBodyWorkers().getMessageBodyReader(
                type, genericType,
                as, mediaType);
        if (bw == null) {
            LOGGER.severe("A message body reader for Java type, " + type +
                    ", and MIME media type, " + mediaType + ", was not found");

            throw new WebApplicationException(
                    Responses.unsupportedMediaType().build());
        }

        if (isTracingEnabled()) {
            trace(String.format("matched message body reader: %s, \"%s\" -> %s",
                    genericType,
                    mediaType,
                    ReflectionHelper.objectToString(bw)));
        }

        try {
            return bw.readFrom(type, genericType, as, mediaType, headers, entity);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception e) {
            throw new MappableContainerException(e);
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

    public List<MediaType> getAcceptableMediaTypes(List<QualitySourceMediaType> priorityMediaTypes) {
        return new ArrayList<MediaType>(HttpHelper.getAccept(this, priorityMediaTypes));
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
    
    public Form getFormParameters() {
        if (MediaTypes.typeEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, getMediaType())) {
            InputStream in = getEntityInputStream();
            if (in.getClass() != ByteArrayInputStream.class) {
                // Buffer input
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ReaderWriter.writeTo(in, baos);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }

                in = new ByteArrayInputStream(baos.toByteArray());
                setEntityInputStream(in);
            }

            ByteArrayInputStream bais = (ByteArrayInputStream)in;
            Form f = getEntity(Form.class);
            bais.reset();
            return f;
        } else {
            return new Form();
        }
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
                 acceptLanguages.add(alt.getAsLocale());
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
        return HttpHelper.getContentLanguageAsLocale(this);
    }
    
    public Map<String, Cookie> getCookies() {
        if (cookies == null || headersModCount != headers.getModCount()) {
            cookies = new HashMap<String, Cookie>();
            
            List<String> cl = getRequestHeaders().get(HttpHeaders.COOKIE);
            if (cl != null) {
                for (String cookie : cl) {
                    if (cookie != null)
                        cookies.putAll(
                                HttpHeaderReader.readCookies(cookie));
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

    public ResponseBuilder evaluatePreconditions() {
        Set<MatchingEntityTag> matchingTags = HttpHelper.getIfMatch(this);
        if (matchingTags == null) {
            return null;
        }

        // Since the resource does not exist the method must not be
        // perform and 412 Precondition Failed is returned
        return Responses.preconditionFailed();
    }

    public ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        ResponseBuilder r = evaluateIfMatch(eTag);
        if (r != null)
            return r;

        return evaluateIfNoneMatch(eTag);
    }

    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        final long lastModifiedTime = lastModified.getTime();
        ResponseBuilder r = evaluateIfUnmodifiedSince(lastModifiedTime);
        if (r != null)
            return r;

        return evaluateIfModifiedSince(lastModifiedTime);
    }
    
    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        ResponseBuilder r = evaluateIfMatch(eTag);
        if (r != null)
            return r;

        final long lastModifiedTime = lastModified.getTime();
        r = evaluateIfUnmodifiedSince(lastModifiedTime);
        if (r != null)
            return r;

        final boolean isGetOrHead = getMethod().equals("GET") || getMethod().equals("HEAD");
        final Set<MatchingEntityTag> matchingTags = HttpHelper.getIfNoneMatch(this);
        if (matchingTags != null) {
            r = evaluateIfNoneMatch(eTag, matchingTags, isGetOrHead);
            // If the If-None-Match header is present and there is no
            // match then the If-Modified-Since header must be ignored
            if (r == null)
                return r;

            // Otherwise if the If-None-Match header is present and there
            // is a match then the If-Modified-Since header must be checked
            // for consistency
        }

        final String ifModifiedSinceHeader = getRequestHeaders().getFirst("If-Modified-Since");
        if (ifModifiedSinceHeader != null && isGetOrHead) {
            r = evaluateIfModifiedSince(lastModifiedTime, ifModifiedSinceHeader);
            if (r != null)
                r.tag(eTag);
        }
        
        return r;        
    }
        
    private ResponseBuilder evaluateIfMatch(EntityTag eTag) {        
        Set<MatchingEntityTag> matchingTags = HttpHelper.getIfMatch(this);
        if (matchingTags == null) {
            return null;
        }

        // The strong comparison function must be used to compare the entity
        // tags. Thus if the entity tag of the entity is weak then matching
        // of entity tags in the If-Match header should fail.
        if (eTag.isWeak()) {
            return Responses.preconditionFailed();
        }

        if (matchingTags != MatchingEntityTag.ANY_MATCH &&
            !matchingTags.contains(eTag)) {
            // 412 Precondition Failed
            return Responses.preconditionFailed();
        }
        
        return null;
    }
    
    private ResponseBuilder evaluateIfNoneMatch(EntityTag eTag) {
        Set<MatchingEntityTag> matchingTags = HttpHelper.getIfNoneMatch(this);
        if (matchingTags == null)
            return null;
        
        final String httpMethod = getMethod();
        return evaluateIfNoneMatch(
                eTag,
                matchingTags,
                httpMethod.equals("GET") || httpMethod.equals("HEAD"));
    }

    private ResponseBuilder evaluateIfNoneMatch(
            EntityTag eTag,
            Set<MatchingEntityTag> matchingTags,
            boolean isGetOrHead) {
        if (isGetOrHead) {
            if (matchingTags == MatchingEntityTag.ANY_MATCH) {
                // 304 Not modified
                return Response.notModified(eTag);
            }

            // The weak comparison function may be used to compare entity tags
            if (matchingTags.contains(eTag) || matchingTags.contains(new EntityTag(eTag.getValue(), !eTag.isWeak()))) {
                // 304 Not modified
                return Response.notModified(eTag);

            }
        } else {
            // The strong comparison function must be used to compare the entity
            // tags. Thus if the entity tag of the entity is weak then matching
            // of entity tags in the If-None-Match header should fail if the
            // HTTP method is not GET or not HEAD.
            if (eTag.isWeak()) {
                return null;
            }

            if (matchingTags == MatchingEntityTag.ANY_MATCH || matchingTags.contains(eTag)) {
                // 412 Precondition Failed
                return Responses.preconditionFailed();
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
        if (ifModifiedSinceHeader == null)
            return null;

        final String httpMethod = getMethod();
        if (httpMethod.equals("GET") || httpMethod.equals("HEAD")) {
            return evaluateIfModifiedSince(
                    lastModified,
                    ifModifiedSinceHeader);
        } else {
            return null;
        }
    }

    private ResponseBuilder evaluateIfModifiedSince(
            long lastModified,
            String ifModifiedSinceHeader) {
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
        
        return null;
    }
    
    // SecurityContext
    
    public Principal getUserPrincipal() {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.getUserPrincipal();
    }
    
    public boolean isUserInRole(String role) {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.isUserInRole(role);
    }
    
    public boolean isSecure() {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.isSecure();
    }
    
    public String getAuthenticationScheme() {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.getAuthenticationScheme();
    }
}