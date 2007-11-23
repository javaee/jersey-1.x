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

package com.sun.ws.rest.spi.container;

import com.sun.ws.rest.api.uri.UriComponent;
import com.sun.ws.rest.impl.MultivaluedMapImpl;
import com.sun.ws.rest.impl.RequestHttpHeadersImpl;
import com.sun.ws.rest.impl.ResponseBuilderImpl;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.model.HttpHelper;
import com.sun.ws.rest.impl.response.Responses;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ProviderFactory;

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
    
    private final String method;
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
    
    /**
     * The absolute URI of a request that is equivalent to the complete URI
     * minus the query and fragment components.
     * <p>
     * The absolute URI must be equivalent to the following:
     *
     *   UriBuilder.fromUri(completeUri).
     *       replaceQuery(null).fragment(null).build();
     *
     *   UriBuilder.fromUri(baseUri).encode(false).
     *       append(encodedPath).build();
     */
    private URI absoluteUri;
    
    /**
     * The percent-encoded path component.
     *
     * The path is relative to the path component of the base URI. The path
     * must not start with a '/'.
     */
    private String encodedPath;
    
    /**
     * The decoded path component.
     */
    private String decodedPath;
    
    private List<PathSegment> decodedPathSegments;
    private List<PathSegment> encodedPathSegments;
    
    private MultivaluedMap<String, String> decodedQueryParameters;
    private MultivaluedMap<String, String> encodedQueryParameters;
    
    private MultivaluedMap<String, String> encodedTemplateValues;
    private MultivaluedMap<String, String> decodedTemplateValues;
    
    
    private MultivaluedMap<String, String> headers;
    private MediaType contentType;
    private List<MediaType> accept;
    private List<Cookie> cookies;
    
    /**
     *
     * @param method the HTTP method
     * @param entity the InputStream of the request entity
     */
    protected AbstractContainerRequest(String method, InputStream entity) {
        this.method = method;
        this.headers = new RequestHttpHeadersImpl();
        this.encodedTemplateValues = new MultivaluedMapImpl();
        this.entity = entity;
    }
    
    // ContainerRequest
    
    public void addTemplateValues(List<String> names, List<String> values) {
        int i = 0;
        for (String name : names) {
            final String value = values.get(i++);
            encodedTemplateValues.putSingle(name, value);
            
            if (decodedTemplateValues != null) {
                decodedTemplateValues.putSingle(
                        UriComponent.decode(name, UriComponent.Type.PATH_SEGMENT),
                        UriComponent.decode(value, UriComponent.Type.PATH));                
            }
        }
    }
    
    // HttpRequestContext
    
    public <T> T getEntity(Class<T> type) {
        try {
            MediaType mediaType = getMediaType();
            return ProviderFactory.getInstance().createMessageBodyReader(type, mediaType).
                    readFrom(type, mediaType, headers, entity);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
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
    
    // UriInfo
    
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
        
        return encodedPath  = completeUri.getRawPath().substring(
                baseUri.getRawPath().length());
    }
    
    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }
    
    public List<PathSegment> getPathSegments(boolean decode) {
        if (decode) {
            if (decodedPathSegments != null)
                return decodedPathSegments;
            
            return decodedPathSegments = extractPathSegments(getPath(false), true);
        } else {
            if (encodedPathSegments != null)
                return encodedPathSegments;
            
            return encodedPathSegments = extractPathSegments(getPath(false), false);
        }
    }
    
    public URI getBase() {
        return baseUri;
    }
    
    public UriBuilder getBaseBuilder() {
        return UriBuilder.fromUri(getBase());
    }
    
    public URI getAbsolute() {
        if (absoluteUri != null) return absoluteUri;
        
        return absoluteUri = getCompleteBuilder().encode(false).
                replaceQueryParams("").fragment("").
                build();
    }
    
    public UriBuilder getBuilder() {
        return UriBuilder.fromUri(getAbsolute());
    }
    
    public URI getComplete() {
        return completeUri;
    }
    
    public UriBuilder getCompleteBuilder() {
        return UriBuilder.fromUri(getComplete());
    }
    
    public MultivaluedMap<String, String> getTemplateParameters() {
        return getTemplateParameters(true);
    }
    
    public MultivaluedMap<String, String> getTemplateParameters(boolean decode) {
        if (decode) {
            if (decodedTemplateValues != null)
                return decodedTemplateValues;
            
            decodedTemplateValues = new MultivaluedMapImpl();
            for (Map.Entry<String, List<String>> e : encodedTemplateValues.entrySet()) {
                List<String> l = new ArrayList<String>();
                for (String v : e.getValue()) {
                    l.add(UriComponent.decode(v, UriComponent.Type.PATH));
                }
                decodedTemplateValues.put(
                        UriComponent.decode(e.getKey(), UriComponent.Type.PATH_SEGMENT),
                        l);
            }
            
            return decodedTemplateValues;
        } else {
            return encodedTemplateValues;
        }
    }
    
    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }
    
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        if (decode) {
            if (decodedQueryParameters != null)
                return decodedQueryParameters;
            
            return decodedQueryParameters = extractQueryParameters(
                    getComplete().getRawQuery(), true);
        } else {
            if (encodedQueryParameters != null)
                return encodedQueryParameters;
            
            return encodedQueryParameters = extractQueryParameters(
                    getComplete().getRawQuery(), false);
        }
    }
    
    // HttpHeaders
    
    public MultivaluedMap<String, String> getRequestHeaders() {
        return headers;
    }
    
    public List<MediaType> getAcceptableMediaTypes() {
        if (accept == null)
            accept = HttpHelper.getAccept(this);
        
        return accept;
    }
    
    public MediaType getMediaType() {
        if (contentType == null)
            contentType = HttpHelper.getContentType(this);
        
        return contentType;
    }
    
    public String getLanguage() {
        return this.getRequestHeaders().getFirst("Langauge");
    }
    
    public List<Cookie> getCookies() {
        if (cookies == null)
            cookies = new ArrayList<Cookie>();
        
        return cookies;
    }
    
    
    private static final class PathSegmentImpl implements PathSegment {
        private String path;
        
        private MultivaluedMap<String, String> matrixParameters;
        
        PathSegmentImpl(String path, MultivaluedMap<String, String> matrixParameters) {
            this.path = path;
            this.matrixParameters = matrixParameters;
        }
        
        public String getPath() {
            return path;
        }
        
        public MultivaluedMap<String, String> getMatrixParameters() {
            return matrixParameters;
        }
    }
    
    /**
     * Extract the path segments from the path
     * TODO: This is not very efficient
     */
    private List<PathSegment> extractPathSegments(String path, boolean decode) {
        List<PathSegment> pathSegments = new LinkedList<PathSegment>();
        
        if (path == null)
            return pathSegments;
        
        // TODO the extraction algorithm requires an absolute path
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        String[] subPaths = path.split("/");
        if (subPaths.length == 0) {
            PathSegment pathSegment = new PathSegmentImpl("", new MultivaluedMapImpl());
            pathSegments.add(pathSegment);
            return pathSegments;
        }
        
        for (String subPath : subPaths) {
            if (subPath.length() == 0)
                continue;
            
            MultivaluedMap<String, String> matrixMap = new MultivaluedMapImpl();
            int colon = subPath.indexOf(';');
            if (colon != -1) {
                String matrixParameters = subPath.substring(colon + 1);
                subPath = (colon == 0) ? "" : subPath.substring(0, colon);
                extractPathParameters(matrixParameters, ";", matrixMap, decode);
            }
            
            if (decode)
                subPath = UriComponent.decode(subPath, UriComponent.Type.PATH_SEGMENT);
            
            PathSegment pathSegment = new PathSegmentImpl(subPath, matrixMap);
            pathSegments.add(pathSegment);
        }
        
        return pathSegments;
    }
    
    /**
     * TODO: This is not very efficient
     */
    private void extractPathParameters(String parameters, String deliminator,
            MultivaluedMap<String, String> map, boolean decode) {
        for (String s : parameters.split(deliminator)) {
            if (s.length() == 0)
                continue;
            
            String[] keyVal = s.split("=");
            String key = (decode)
            ? UriComponent.decode(keyVal[0], UriComponent.Type.PATH_SEGMENT)
            : keyVal[0];
            if (key.length() == 0)
                continue;
            
            // parameter may not have a value, if so default to "";
            String val = (keyVal.length == 2) ?
                (decode) ? UriComponent.decode(keyVal[1], UriComponent.Type.PATH_SEGMENT) : keyVal[1] : "";
            
            List<String> list = map.get(key);
            if (map.get(key) == null) {
                list = new LinkedList<String>();
                map.put(key, list);
            }
            list.add(val);
        }
    }
    
    /**
     * Extract the query parameters from a string and add
     * them to the query parameters map.
     * TODO: This is not very efficient
     */
    private MultivaluedMap<String, String> extractQueryParameters(String queryString, boolean decode) {
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        
        if (queryString == null || queryString.length() == 0)
            return queryParameters;
        
        extractQueryParameters(queryString, "&", queryParameters, decode);
        return queryParameters;
    }
    
    /**
     * TODO: This is not very efficient
     */
    private void extractQueryParameters(String parameters, String deliminator,
            MultivaluedMap<String, String> map, boolean decode) {
        for (String s : parameters.split(deliminator)) {
            if (s.length() == 0)
                continue;
            
            String[] keyVal = s.split("=");
            try {
                String key = (decode) ? URLDecoder.decode(keyVal[0], "UTF-8") : keyVal[0];
                if (key.length() == 0)
                    continue;
                
                // Query parameter may not have a value, if so default to "";
                String val = (keyVal.length == 2) ?
                    (decode) ? URLDecoder.decode(keyVal[1], "UTF-8") : keyVal[1] : "";
                
                List<String> list = map.get(key);
                if (map.get(key) == null) {
                    list = new LinkedList<String>();
                    map.put(key, list);
                }
                list.add(val);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    
    // PreconditionEvaluator
    
    public Response evaluate(EntityTag eTag) {
        Response r = evaluateIfMatch(eTag);
        if (r == null)
            r = evaluateIfNoneMatch(eTag);
        
        return r;
    }
    
    public Response evaluate(Date lastModified) {
        long lastModifiedTime = lastModified.getTime();
        Response r = evaluateIfUnmodifiedSince(lastModifiedTime);
        if (r == null)
            r = evaluateIfModifiedSince(lastModifiedTime);
        
        return r;
    }
    
    public Response evaluate(Date lastModified, EntityTag eTag) {
        Response r = evaluateIfMatch(eTag);
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
    
    private Response evaluateIfMatch(EntityTag eTag) {
        String ifMatchHeader = getRequestHeaders().getFirst("If-Match");
        // TODO require support for eTag types
        // Strong comparison of entity tags is required
        if (ifMatchHeader != null &&
                !ifMatchHeader.trim().equals("*") &&
                !ifMatchHeader.contains(eTag.getValue())) {
            // 412 Precondition Failed
            return Responses.PRECONDITION_FAILED;
        }
        
        return null;
    }
    
    private Response evaluateIfNoneMatch(EntityTag eTag) {
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
                    return ResponseBuilderImpl.notModified(eTag).build();
                } else {
                    // 412 Precondition Failed
                    return Responses.PRECONDITION_FAILED;
                }
            }
        }
        
        return null;
    }
    
    private Response evaluateIfUnmodifiedSince(long lastModified) {
        String ifUnmodifiedSinceHeader = getRequestHeaders().getFirst("If-Unmodified-Since");
        if (ifUnmodifiedSinceHeader != null) {
            try {
                long ifUnmodifiedSince = HttpHeaderReader.readDate(ifUnmodifiedSinceHeader).getTime() + 1000;
                if (lastModified > ifUnmodifiedSince) {
                    // 412 Precondition Failed
                    return Responses.PRECONDITION_FAILED;
                }
            } catch (ParseException ex) {
                // Ignore the header if parsing error
            }
        }
        
        return null;
    }
    
    private Response evaluateIfModifiedSince(long lastModified) {
        String ifModifiedSinceHeader = getRequestHeaders().getFirst("If-Modified-Since");
        if (ifModifiedSinceHeader != null) {
            try {
                // TODO round up if modified since or round down last modified
                long ifModifiedSince = HttpHeaderReader.readDate(ifModifiedSinceHeader).getTime() + 1000;
                if (ifModifiedSince  > lastModified) {
                    // 304 Not modified
                    return Responses.NOT_MODIFIED;
                }
            } catch (ParseException ex) {
                // Ignore the header if parsing error
            }
        }
        
        return null;
    }
}