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

package com.sun.jersey.spi.container;

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
import javax.ws.rs.ext.MessageBodyWriter;

import com.sun.jersey.api.MessageException;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.TraceInformation;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.header.AcceptableLanguageTag;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.header.MatchingEntityTag;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.server.impl.VariantSelector;
import com.sun.jersey.server.impl.model.HttpHelper;
import com.sun.jersey.spi.MessageBodyWorkers;

/**
 * An in-bound HTTP request to be processed by the web application.
 * <p/>
 * Containers instantiate, or inherit, and provide an instance to the
 * {@link WebApplication}.
 * <p/>
 * By default the implementation of {@link SecurityContext} will throw
 * {@link UnsupportedOperationException} if the methods are invoked.
 * Containers SHOULD use the method {@link #setSecurityContext(javax.ws.rs.core.SecurityContext) }
 * to define security context behaviour rather than extending from this class
 * and overriding the methods.
 *
 * @author Paul.Sandoz@Sun.Com
 * @author pavel.bucek@oracle.com
 */
public class ContainerRequest implements HttpRequestContext {
    private static final Logger LOGGER = Logger.getLogger(ContainerRequest.class.getName());

    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    public static final String VARY_HEADER = "Vary";

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
     * <p/>
     * The base URI and the request URI must contain the same scheme, user info,
     * host and port components.
     * <p/>
     * The base URI must not contain the query and fragment components. The
     * encoded path component of the request URI must start with the encoded
     * path component of the base URI. The encoded path component of the base
     * URI must end in a '/' character.
     *
     * @param wa         the web application
     * @param method     the HTTP method
     * @param baseUri    the base URI of the request
     * @param requestUri the request URI
     * @param headers    the request headers
     * @param entity     the InputStream of the request entity
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
     * <p/>
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
     *
     * @param method the method.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Set the base and request URI.
     *
     * @param baseUri    the base URI.
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
     * Get the security context.
     *
     * @return the security context.
     */
    public SecurityContext getSecurityContext() {
        return securityContext;
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
    @Override
    public boolean isTracingEnabled() {
        return isTraceEnabled;
    }

    @Override
    public void trace(String message) {
        if (!isTracingEnabled())
            return;

        if (wa.getFeaturesAndProperties().getFeature(ResourceConfig.FEATURE_TRACE_PER_REQUEST) &&
                !getRequestHeaders().containsKey("X-Jersey-Trace-Accept"))
            return;

        TraceInformation ti = (TraceInformation) getProperties().
                get(TraceInformation.class.getName());
        ti.trace(message);
    }


    // HttpRequestContext
    @Override
    public URI getBaseUri() {
        return baseUri;
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return UriBuilder.fromUri(getBaseUri());
    }

    @Override
    public URI getRequestUri() {
        return requestUri;
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(getRequestUri());
    }

    @Override
    public URI getAbsolutePath() {
        if (absolutePathUri != null) return absolutePathUri;

        return absolutePathUri = UriBuilder.fromUri(requestUri).
                replaceQuery("").fragment("").
                build();
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return UriBuilder.fromUri(getAbsolutePath());
    }

    @Override
    public String getPath() {
        return getPath(true);
    }

    @Override
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

        final int length = getBaseUri().getRawPath().length();
        if(length < getRequestUri().getRawPath().length()) {
            return encodedPath = getRequestUri().getRawPath().substring(length);
        } else {
            return "";
        }
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    @Override
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

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    @Override
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

    @Override
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

    @Override
    public <T> T getEntity(Class<T> type, Type genericType, Annotation[] as) {
        MediaType mediaType = getMediaType();
        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }

        MessageBodyReader<T> bw = getMessageBodyWorkers().getMessageBodyReader(
                type, genericType,
                as, mediaType);
        if (bw == null) {
            String message = "A message body reader for Java class " + type.getName() +
                    ", and Java type " + genericType +
                    ", and MIME media type " + mediaType + " was not found.\n";

            Map<MediaType, List<MessageBodyReader>> m = getMessageBodyWorkers().
                    getReaders(mediaType);
            LOGGER.severe(message + "The registered message body readers compatible with the MIME media type are:\n" +
                    getMessageBodyWorkers().readersToString(m));

            throw new WebApplicationException(
                    new MessageException(message),
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

    /**
     * Set the request entity.
     *
     * @param type        the class of object that is to be written.
     * @param genericType the type of object to be written, obtained either
     *                    by reflection of a resource method return type or by inspection
     *                    of the returned instance. {@link javax.ws.rs.core.GenericEntity}
     *                    provides a way to specify this information at runtime.
     * @param annotations an array of the annotations on the resource
     *                    method that returns the object.
     * @param mediaType   the media type of the HTTP entity.
     * @param httpHeaders a mutable map of the HTTP response headers.
     * @param entity      the entity instance to write.
     * @throws MappableContainerException encapsulates exceptions thrown while
     *                    serializing the entity.
     */
    public <T> void setEntity(final Class<T> type, final Type genericType,
                              final Annotation annotations[], final MediaType mediaType,
                              final MultivaluedMap<String, Object> httpHeaders, final T entity) {

        final MessageBodyWriter<T> writer = getMessageBodyWorkers().getMessageBodyWriter(type, genericType, annotations, mediaType);

        if (writer == null) {
            String message = "A message body writer for Java class " + type.getName() +
                    ", and Java type " + genericType +
                    ", and MIME media type " + mediaType + " was not found.\n";

            Map<MediaType, List<MessageBodyReader>> m = getMessageBodyWorkers().
                    getReaders(mediaType);
            LOGGER.severe(message + "The registered message body readers compatible with the MIME media type are:\n" +
                    getMessageBodyWorkers().readersToString(m));

            throw new WebApplicationException(
                    new MessageException(message),
                    Responses.unsupportedMediaType().build());
        }

        if (isTracingEnabled()) {
            trace(String.format("matched message body writer: %s, \"%s\" -> %s",
                    genericType,
                    mediaType,
                    ReflectionHelper.objectToString(writer)));
        }

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            writer.writeTo(entity, type, genericType, annotations, mediaType, httpHeaders, byteArrayOutputStream);
        } catch (IOException e) {
            throw new MappableContainerException(e);
        }

        this.entity = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    @Override
    public <T> T getEntity(Class<T> type) {
        return getEntity(type, type, EMPTY_ANNOTATIONS);
    }

    @Override
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

    @Override
    public List<MediaType> getAcceptableMediaTypes(List<QualitySourceMediaType> priorityMediaTypes) {
        return new ArrayList<MediaType>(HttpHelper.getAccept(this, priorityMediaTypes));
    }

    @Override
    public MultivaluedMap<String, String> getCookieNameValueMap() {
        if (cookieNames == null || headersModCount != headers.getModCount()) {
            cookieNames = new MultivaluedMapImpl();
            for (Map.Entry<String, Cookie> e : getCookies().entrySet()) {
                cookieNames.putSingle(e.getKey(), e.getValue().getValue());
            }
        }

        return cookieNames;
    }

    @Override
    public Form getFormParameters() {
        if (MediaTypes.typeEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, getMediaType())) {
            InputStream in = getEntityInputStream();
            if (in.getClass() != ByteArrayInputStream.class) {
                // Buffer input
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    ReaderWriter.writeTo(in, byteArrayOutputStream);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }

                in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                setEntityInputStream(in);
            }

            ByteArrayInputStream byteArrayInputStream = (ByteArrayInputStream) in;
            Form f = getEntity(Form.class);
            byteArrayInputStream.reset();
            return f;
        } else {
            return new Form();
        }
    }


    // HttpHeaders
    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return headers;
    }

    @Override
    public List<String> getRequestHeader(String name) {
        return headers.get(name);
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        if (accept == null || headersModCount != headers.getModCount())
            accept = new ArrayList<MediaType>(HttpHelper.getAccept(this));

        return accept;
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        if (acceptLanguages == null || headersModCount != headers.getModCount()) {
            List<AcceptableLanguageTag> alts = HttpHelper.getAcceptLangauge(this);

            acceptLanguages = new ArrayList<Locale>(alts.size());
            for (AcceptableLanguageTag alt : alts) {
                acceptLanguages.add(alt.getAsLocale());
            }
        }

        return acceptLanguages;
    }

    @Override
    public MediaType getMediaType() {
        if (contentType == null || headersModCount != headers.getModCount())
            contentType = HttpHelper.getContentType(this);

        return contentType;
    }

    @Override
    public Locale getLanguage() {
        return HttpHelper.getContentLanguageAsLocale(this);
    }

    @Override
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
    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Variant selectVariant(List<Variant> variants) {
        if (variants == null || variants.isEmpty())
            throw new IllegalArgumentException("The list of variants is null or empty");

        // TODO mark the Vary header to be added to the response

        return VariantSelector.selectVariant(this, variants);
    }

    @Override
    public ResponseBuilder evaluatePreconditions() {
        Set<MatchingEntityTag> matchingTags = HttpHelper.getIfMatch(this);
        if (matchingTags == null) {
            return null;
        }

        // Since the resource does not exist the method must not be
        // perform and 412 Precondition Failed is returned
        return Responses.preconditionFailed();
    }

    @Override
    public ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        if(eTag == null) {
            throw new IllegalArgumentException("Parameter 'eTag' cannot be null.");
        }

        ResponseBuilder r = evaluateIfMatch(eTag);
        if (r != null)
            return r;

        return evaluateIfNoneMatch(eTag);
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        if(lastModified == null) {
            throw new IllegalArgumentException("Parameter 'lastModified' cannot be null.");
        }

        final long lastModifiedTime = lastModified.getTime();
        ResponseBuilder r = evaluateIfUnmodifiedSince(lastModifiedTime);
        if (r != null)
            return r;

        return evaluateIfModifiedSince(lastModifiedTime);
    }

    @Override
    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        if(lastModified == null || eTag == null) {
            throw new IllegalArgumentException("Parameters 'lastModified' and 'eTag' cannot be null.");
        }

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
                long ifUnmodifiedSince = HttpHeaderReader.readDate(ifUnmodifiedSinceHeader).getTime();
                if (roundDown(lastModified) > ifUnmodifiedSince) {
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
            long ifModifiedSince = HttpHeaderReader.readDate(ifModifiedSinceHeader).getTime();
            if (roundDown(lastModified) <= ifModifiedSince) {
                // 304 Not modified
                return Responses.notModified();
            }
        } catch (ParseException ex) {
            // Ignore the header if parsing error
        }

        return null;
    }

    /**
     * Round down the time to the nearest second.
     *
     * @param time the time to round down.
     * @return the rounded down time.
     */
    private static long roundDown(long time) {
        return time - time % 1000;
    }

    // SecurityContext

    @Override
    public Principal getUserPrincipal() {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.getUserPrincipal();
    }

    @Override
    public boolean isUserInRole(String role) {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.isUserInRole(role);
    }

    @Override
    public boolean isSecure() {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        if (securityContext == null)
            throw new UnsupportedOperationException();
        return securityContext.getAuthenticationScheme();
    }
}
