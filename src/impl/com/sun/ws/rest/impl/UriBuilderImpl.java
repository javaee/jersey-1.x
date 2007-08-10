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

import com.sun.ws.rest.api.core.UriBuilder;
import com.sun.ws.rest.spi.dispatch.URITemplateType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.UriTemplate;

/*
 * Notes on API while implementing

I don think we need an encode option. This will open us up to bugs because 
the client may say that the string is already encoded but may not be. 
The only reliable solution is to check the string regardless and encode as 
necessary.


For the following methods are template values expected to be encoded or not:

    public abstract URI build(Map<String, String> values);    
    public abstract URI build(String... values);


For the following:

    public abstract URI build(String... values);

What do we do for a repeating template such as:

   /{a}/foo/{a}

is it expected to supply one or two values. Two values don't make any sense 
since there is really one one value. So the values correspond to the number 
and order of unique templates.


Specify a value of -1 as no port? 

    public abstract UriBuilder port(int port);


What happens if a path segment contains a '/' for:

    public UriBuilder path(String... segments)


What happens to the matrix parameters when a path is replaced, i presume they 
are removed.

    public UriBuilder replacePath(String path, boolean encode) {


Change:

    public UriBuilder path(Class resource) {

to

    public UriBuilder path(Class<?> resource) {

so that it is easier to get access to an annotation.


What about null segment values, and queryParam/matrixPath names ?
*/

/**
 * TODO template support
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriBuilderImpl extends UriBuilder {

    private String scheme;
    
    private String userInfo;
    
    private String host;
    
    private int port = -1;
    
    private StringBuilder path;
    
    private StringBuilder query;
    
    private String fragment;
    
    public UriBuilderImpl() {
        path = new StringBuilder();
        query = new StringBuilder();
    }

    public UriBuilder uri(URI uri) {
        scheme = uri.getScheme();
        userInfo = uri.getRawUserInfo();
        host = uri.getHost();
        port = uri.getPort();
        path = new StringBuilder(replaceNull(uri.getRawPath()));
        query = new StringBuilder(replaceNull(uri.getRawQuery()));
        fragment = uri.getRawFragment();
        return this;
    }

    public UriBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public UriBuilder schemeSpecificPart(String ssp) {
        URI uri = create(null, ssp, null);
        userInfo = uri.getRawUserInfo();
        host = uri.getHost();
        port = uri.getPort();
        path = new StringBuilder(replaceNull(uri.getRawPath()));
        query = new StringBuilder(replaceNull(uri.getRawQuery()));
        return this;
    }

    public UriBuilder authority(String authority) {
        URI uri = create(null, "localhost:8080", null, null, null);
        userInfo = uri.getRawUserInfo();
        host = uri.getHost();
        port = uri.getPort();
        return this;
    }

    public UriBuilder userInfo(String ui, boolean encode) {
        this.userInfo = ui;
        return this;
    }

    public UriBuilder host(String host) {
        this.host = host;
        return this;
    }

    public UriBuilder port(int port) {
        this.port = port;
        return this;
    }

    public UriBuilder replacePath(String path, boolean encode) {
        this.path.setLength(0);
        this.path.append(path);
        return this;
    }

    public UriBuilder path(boolean encode, String... segments) {
        for (String segment : segments)
            appendPath(segment);
        
        return this;
    }

    @SuppressWarnings("unchecked")
    public UriBuilder path(Class resource) {
        UriTemplate ut = (UriTemplate)resource.getAnnotation(UriTemplate.class);
        if (ut == null)
            throw new IllegalArgumentException();
        
        appendPath(ut.value());
        return this;
    }

    public UriBuilder replaceMatrixParams(String matrix, boolean encode) {
        int i = path.lastIndexOf("/");
        if (i != -1) i = 0;
        i = path.indexOf(";", i);
        if (i != -1) path.setLength(i + 1);
        
        path.append(matrix);
        return this;
    }

    public UriBuilder matrixParam(String name, String value, boolean encode) {
        if (path.length() > 0) path.append(';');
        path.append(name);
        if (value != null && value.length() > 0) path.append('=').append(value);
        return this;
    }

    public UriBuilder replaceQueryParams(String query, boolean encode) {
        this.query.setLength(0);
        this.query.append(query);
        return this;
    }

    public UriBuilder queryParam(String name, String value, boolean encode) {
        if (query.length() > 0) query.append('&');
        query.append(name);
        if (value != null && value.length() > 0) query.append('=').append(value);
        return this;
    }

    public UriBuilder fragment(String fragment, boolean encode) {
        this.fragment = fragment;
        return this;
    }

    
    public URI build() {
        return create();
    }

    public URI build(Map<String, String> values) {
        URI u = create(null, userInfo, host, port, 
                replaceEmptyString(path.toString()), 
                replaceEmptyString(query.toString()), 
                null);
        String ssp = u.getRawSchemeSpecificPart();
        URITemplateType t = new URITemplateType(ssp);
        ssp = t.createURI(values);
        return create(scheme, ssp, fragment);
    }

    public URI build(String... values) {
        URI u = create(null, userInfo, host, port, 
                replaceEmptyString(path.toString()), 
                replaceEmptyString(query.toString()), 
                null);
        String ssp = u.getRawSchemeSpecificPart();
        URITemplateType t = new URITemplateType(ssp);
        ssp = t.createURI(values);
        return create(scheme, ssp, fragment);
    }
    
    private void appendPath(String segment) {
        if (segment == null)
            return;
        
        StringBuilder sb;
        
        final boolean pathEndsInSlash = path.charAt(path.length() - 1) == '/';
        final boolean segmentStartsWithSlash = segment.charAt(0) == '/';
        
        if (!pathEndsInSlash && !segmentStartsWithSlash) {
            path.append('/');
        }
        
        path.append(segment);
    }
    
    private URI create() {
        try {
            return new URI(scheme, 
                    userInfo, 
                    host, 
                    port, 
                    replaceEmptyString(path.toString()), 
                    replaceEmptyString(query.toString()), 
                    fragment);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private URI create(String uri) {
        return URI.create(uri);
    }
    
    private URI create(String scheme,
           String ssp,
           String fragment) {
        try {
            return new URI(scheme, ssp, fragment);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }                
    }
    
    private URI create(String scheme,
           String userInfo,
           String host,
           int port,
           String path,
           String query,
           String fragment) {
        try {
            return new URI(scheme, userInfo, host, port, path, query, fragment);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    private URI create(String scheme,
           String authority,
           String path,
           String query,
           String fragment) {
        try {
            return new URI(scheme, authority, path, query, fragment);
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }        
    }
     
    private String replaceNull(String s) {
        return (s != null) ? s : "";
    }
    
    private String replaceEmptyString(String s) {
        return (s.length() != 0) ? s : null;
    }
}
