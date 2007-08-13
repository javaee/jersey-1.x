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

package com.sun.ws.rest.impl;

import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.model.HttpHelper;
import com.sun.ws.rest.impl.response.Responses;
import com.sun.ws.rest.spi.container.ContainerRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpRequestContextImpl implements ContainerRequest {

    protected InputStream entity;
    protected String method;

    protected URI uri;
    protected URI baseURI;
    protected String uriPath;
    protected String queryString;
    protected MultivaluedMap<String, String> queryParameters;
    protected MultivaluedMap<String, String> templateValues;
    protected List<PathSegment> pathSegments;
    
    protected MultivaluedMap<String, String> headers;
    protected MediaType contentType;
    protected List<MediaType> accept;
    protected List<Cookie> cookies;

    public HttpRequestContextImpl(String method, InputStream entity) {
        this.method = method;
        this.headers = new RequestHttpHeadersImpl();
        this.templateValues = new MultivaluedMapImpl();
        this.entity = entity;
    }
    
    public HttpRequestContextImpl(String method, String uriPath, URI baseURI, InputStream entity) {
        this(method, entity);
        this.uriPath = uriPath;
        this.baseURI = baseURI;
    }
    
    public HttpRequestContextImpl(String method, String uriPath, String baseURI, InputStream entity)  {
        this(method, entity);
        this.uriPath = uriPath;
        this.baseURI = URI.create(baseURI);
    }
    
    public HttpRequestContextImpl(String method, String uriPath, URI baseURI, String queryString,
            InputStream entity) {
        this(method, uriPath, baseURI, entity);
        this.queryString = queryString;
        this.queryParameters = extractQueryParameters(queryString, true);
    }
    
    public HttpRequestContextImpl(String method, String uriPath, String baseURI, String queryString,
            InputStream entity) {
        this(method, uriPath, baseURI, entity);
        this.queryString = queryString;
        this.queryParameters = extractQueryParameters(queryString, true);
    }
        
    // HttpRequestContext 
    
    public <T> T getEntity(Class<T> type) {
        try {
            String mediaType = headers.getFirst("Content-Type");
            
            return ProviderFactory.getInstance().createEntityProvider(type).
                    readFrom(type, mediaType, headers, entity);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getHttpMethod() {
        return method;
    }
    
    
    // UriInfo
    
    public String getURIPath() {
        return uriPath;
    }
    
    public String getURIPath(boolean decode) {
        if (decode) {
            return uriPath;
        } else {
            return baseURI.relativize(getURI()).getRawPath();
        }
    }
    
    public List<PathSegment> getURIPathSegments() {
        if (pathSegments != null) {
            return pathSegments;
        }
            
        pathSegments = extractPathSegments(uriPath, false);
        return pathSegments;
    }
    
    public List<PathSegment> getURIPathSegments(boolean decode) {
        if (decode) {
            return getURIPathSegments();
        } else {
            return extractPathSegments(getURIPath(false), false);
        }
    }
    
    public URI getBaseURI() {
        return baseURI;
    }
    
    public URI getURI() {
        if (uri == null) {
            try {
                URI u = new URI(null, null, uriPath, null);
                uri = baseURI.resolve(u);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
            
        return uri;
    }

    public MultivaluedMap<String, String> getURIParameters() {
        return templateValues;
    }

    public MultivaluedMap<String, String> getURIParameters(boolean decode) {
        if (decode) {
            return templateValues;
        } else {
            MultivaluedMapImpl encodedTemplateValues = new MultivaluedMapImpl();
            for (Map.Entry<String, List<String>> e : templateValues.entrySet()) {
                List<String> l = new ArrayList<String>();
                for (String v : e.getValue()) {
                    try {
                        l.add(URLEncoder.encode(v, "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        ex.printStackTrace();
                    }
                }
                encodedTemplateValues.put(e.getKey(), l);
            }
            return encodedTemplateValues;
        }
    }
    
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }
    
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        if (decode) {
            return queryParameters;
        } else {
            return extractQueryParameters(queryString, false);
        }
    }
    
    /**
     * Get the base URI given a URI and a relative URI path.
     * @param uri the URI
     * @param path the URI path (decoded)
     */
    protected URI getBaseURI(URI uri, String path) {
        String uriPath = uri.getPath();
        int i = uriPath.lastIndexOf(path);
        String contextPath = uriPath.substring(0, i);
        return uri.resolve(contextPath);
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
    protected List<PathSegment> extractPathSegments(String path, boolean decode) {
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
                extractParameters(matrixParameters, ";", matrixMap, decode);
            }
            
            PathSegment pathSegment = new PathSegmentImpl(subPath, matrixMap);
            pathSegments.add(pathSegment);
        }
        
        return pathSegments;
    }
    
    /**
     * Extract the query parameters from a string and add
     * them to the query parameters map.
     * TODO: This is not very efficient
     */
    protected MultivaluedMap<String, String> extractQueryParameters(String queryString, boolean decode) {
        MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
        
        if (queryString == null || queryString.length() == 0)
            return queryParameters;
        
        extractParameters(queryString, "&", queryParameters, decode);
        return queryParameters;
    }
    
    /**
     * TODO: This is not very efficient
     */
    protected void extractParameters(String parameters, String deliminator, 
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