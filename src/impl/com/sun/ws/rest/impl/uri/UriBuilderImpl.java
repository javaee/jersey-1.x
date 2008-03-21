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

package com.sun.ws.rest.impl.uri;

import com.sun.ws.rest.api.uri.UriComponent;
import com.sun.ws.rest.api.uri.UriTemplate;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * @author Paul.Sandoz@Sun.Com
 */
public final class UriBuilderImpl extends UriBuilder {

    // All fields should be in the percent-encoded form
    
    private boolean encode = true;
    
    private String scheme;
    
    private String userInfo;
    
    private String host;
    
    private int port = -1;
    
    private final StringBuilder path;
    
    private final StringBuilder query;
    
    private String fragment;
    
    public UriBuilderImpl() {
        path = new StringBuilder();
        query = new StringBuilder();
    }

    private UriBuilderImpl(UriBuilderImpl that) {
        this.encode = that.encode;
        this.scheme = that.scheme;
        this.userInfo = that.userInfo;
        this.host = that.host;
        this.port = that.port;
        this.path = new StringBuilder(that.path);
        this.query = new StringBuilder(that.query);
        this.fragment = that.fragment;
    }
    
    public UriBuilder clone() {
        return new UriBuilderImpl(this);
    }
    
    public boolean isEncode() {
        return encode;
    }
    
    public UriBuilder encode(boolean enable) {
        encode = enable;
        return this;
    }
    
    public UriBuilder uri(URI uri) {
        if (uri.getScheme() != null) scheme = uri.getScheme();
        if (uri.getRawUserInfo() != null) userInfo = uri.getRawUserInfo();
        if (uri.getHost() != null) host = uri.getHost();
        if (uri.getPort() != -1) port = uri.getPort();
        if (uri.getRawPath() != null && uri.getRawPath().length() > 0) {
            path.setLength(0);
            path.append(uri.getRawPath());
        }
        if (uri.getRawQuery() != null && uri.getRawQuery().length() > 0) {
            query.setLength(0);
            query.append(uri.getRawQuery());
            
        }
        if (uri.getRawFragment() != null) fragment = uri.getRawFragment();
        return this;
    }

    public UriBuilder scheme(String scheme) {
        this.scheme = scheme;
        UriComponent.validate(scheme, UriComponent.Type.SCHEME, true);
        return this;
    }

    public UriBuilder schemeSpecificPart(String ssp) {
        // TODO This is buggy because the spp is percent-encoded
        // Any template present variables will result in an exception
        URI uri = createURI(null, ssp, null);
        userInfo = uri.getRawUserInfo();
        host = uri.getHost();
        port = uri.getPort();
        path.setLength(0);
        path.append(replaceNull(uri.getRawPath()));
        query.setLength(0);
        query.append(replaceNull(uri.getRawQuery()));
        return this;
    }

    public UriBuilder userInfo(String ui) {
        this.userInfo = encode(ui, UriComponent.Type.USER_INFO);
        return this;
    }

    public UriBuilder host(String host) {
        this.host = encode(host, UriComponent.Type.HOST);
        return this;
    }

    public UriBuilder port(int port) {
        this.port = port;
        return this;
    }

    public UriBuilder replacePath(String... segments) {
        this.path.setLength(0);
        path(segments);
        return this;
    }

    public UriBuilder path(String... segments) {        
        for (String segment : segments)
            appendPath(segment);
        
        return this;
    }

    public UriBuilder path(Class resource) {
        @SuppressWarnings("unchecked")
        javax.ws.rs.Path ut = (javax.ws.rs.Path)
                resource.getAnnotation(Path.class);
        appendPath(ut);
        return this;
    }

    public UriBuilder path(Class resource, String methodName) {
        Method[] methods = resource.getDeclaredMethods();
        Method found = null;
        for (Method m : methods) {
            if (methodName.equals(m.getName())) {
                if (found == null) found = m;
                else 
                    throw new IllegalArgumentException();
            }
        }

        if (found == null)
            throw new IllegalArgumentException();
        
        appendPath(found.getAnnotation(Path.class));
        
        return this;
    }

    public UriBuilder path(Method... methods) {
        for (Method m : methods)
            appendPath(m.getAnnotation(Path.class));

        return this;
    }
    
    public UriBuilder replaceMatrixParams(String matrix) {
        int i = path.lastIndexOf("/");
        if (i != -1) i = 0;
        i = path.indexOf(";", i);
        if (i != -1) path.setLength(i + 1);
        
        path.append(encode(matrix, UriComponent.Type.PATH));
        return this;
    }

    public UriBuilder matrixParam(String name, String value) {
        if (path.length() > 0) path.append(';');
        path.append(encode(name, UriComponent.Type.PATH));
        if (value != null && value.length() > 0) 
            path.append('=').append(encode(value, UriComponent.Type.PATH));
        return this;
    }

    public UriBuilder replaceQueryParams(String query) {
        this.query.setLength(0);
        this.query.append(encode(query, UriComponent.Type.QUERY));
        return this;
    }

    public UriBuilder queryParam(String name, String value) {
        if (query.length() > 0) query.append('&');
        query.append(encodeQuery(name));

        if (value != null && value.length() > 0)
            query.append('=').append(encodeQuery(value));
        return this;
    }

    public UriBuilder fragment(String fragment) {
        this.fragment = encode(fragment, UriComponent.Type.FRAGMENT);
        return this;
    }

    private void appendPath(Path t) {
        if (t == null)
            throw new IllegalArgumentException();
        
        boolean _encode = encode;
        encode = t.encode();
        appendPath(t.value());
        encode = _encode;
    }
    
    private void appendPath(String segment) {
        if (segment == null || segment.length() == 0)
            return;

        segment = encode(segment, UriComponent.Type.PATH);
        
        final boolean pathEndsInSlash = path.length() > 0 && path.charAt(path.length() - 1) == '/';
        final boolean segmentStartsWithSlash = segment.charAt(0) == '/';
        
        if (path.length() > 0 && !pathEndsInSlash && !segmentStartsWithSlash) {
            path.append('/');
        } else if (pathEndsInSlash && segmentStartsWithSlash) {
            segment = segment.substring(1);
            if (segment.length() == 0)
                return;
        }
        
        path.append(segment);
    }
        
    private String encode(String s, UriComponent.Type type) {
        if (encode)
            return UriComponent.encode(s, type, true);
        
        UriComponent.validate(s, type, true);
        return s;
    }
    
    private String encodeQuery(String s) {
        if (encode) {
            try {
                return URLEncoder.encode(s, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                assert false;
            }
        }
        
        UriComponent.validate(s, UriComponent.Type.QUERY, true);
        return s;
    }
    
    public URI build() {
        return createURI(create());
    }

    public URI build(Map<String, Object> values) {
        String uri = UriTemplate.createURI(scheme, 
                userInfo, host, String.valueOf(port), 
                path.toString(), query.toString(), fragment, values, encode);
        return createURI(uri);
    }

    public URI build(Object... values) {
        String uri = UriTemplate.createURI(scheme, 
                userInfo, host, String.valueOf(port), 
                path.toString(), query.toString(), fragment, values, encode);
        return createURI(uri);              
    }
    
    
    private String create() {
        StringBuilder sb = new StringBuilder();
        
        if (scheme != null) sb.append(scheme).append(':');

        if (userInfo != null || host != null || port != -1) {
            sb.append("//");
            
            if (userInfo != null && userInfo.length() > 0) 
                sb.append(userInfo).append('@');
            
            if (host != null) {
                // TODO check IPv6 address
                sb.append(host);
            }
            
            if (port != -1) sb.append(':').append(port);
        }

        if (path.length() > 0) {
            if (sb.length() > 0 && path.charAt(0) != '/') sb.append("/");
            sb.append(path);
        }
        
        if (query.length() > 0) sb.append('?').append(query);
        
        if (fragment != null && fragment.length() > 0) sb.append('#').append(fragment);
        
        return sb.toString();
    }
    
    private URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException ex) {
            throw new UriBuilderException(ex);
        }
    }
    
    private URI createURI(String scheme,
           String ssp,
           String fragment) {
        try {
            return new URI(scheme, ssp, fragment);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }                
    }
    
    private String replaceNull(String s) {
        return (s != null) ? s : "";
    }
    
}