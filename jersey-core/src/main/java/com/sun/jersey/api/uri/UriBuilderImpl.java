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

package com.sun.jersey.api.uri;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * An implementaton of {@link UriBuilder}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class UriBuilderImpl extends UriBuilder {

    // All fields should be in the percent-encoded form
    
    private String scheme;
    
    private String ssp;
    
    private String userInfo;
    
    private String host;
    
    private int port = -1;
    
    private final StringBuilder path;

    private MultivaluedMap<String, String> matrixParams;
            
    private final StringBuilder query;
    
    private MultivaluedMap<String, String> queryParams;

    private String fragment;
    
    public UriBuilderImpl() {
        path = new StringBuilder();
        query = new StringBuilder();
    }

    private UriBuilderImpl(UriBuilderImpl that) {
        this.scheme = that.scheme;
        this.ssp = that.ssp;
        this.userInfo = that.userInfo;
        this.host = that.host;
        this.port = that.port;
        this.path = new StringBuilder(that.path);
        this.query = new StringBuilder(that.query);
        this.fragment = that.fragment;
    }
    
    @Override
    public UriBuilder clone() {
        return new UriBuilderImpl(this);
    }
    
    @Override
    public UriBuilder uri(URI uri) {
        if (uri == null) 
            throw new IllegalArgumentException("URI parameter is null");
        
        if (uri.getRawFragment() != null) fragment = uri.getRawFragment();

        if (uri.isOpaque()) {
            scheme = uri.getScheme();
            ssp = uri.getRawSchemeSpecificPart();
            return this;
        }

        if (uri.getScheme() == null) {
            if (ssp != null) {
                if (uri.getRawSchemeSpecificPart() != null) {
                    ssp = uri.getRawSchemeSpecificPart();
                    return this;
                }
            }
        } else {
            scheme = uri.getScheme();
        }

        ssp = null;
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

        return this;
    }

    @Override
    public UriBuilder scheme(String scheme) {
        if (scheme != null) {
            this.scheme = scheme;
            UriComponent.validate(scheme, UriComponent.Type.SCHEME, true);
        } else {
            this.scheme = null;
        }
        return this;
    }

    @Override
    public UriBuilder schemeSpecificPart(String ssp) {
        if (ssp == null)
            throw new IllegalArgumentException("Scheme specific part parameter is null");
        
        // TODO encode or validate scheme specific part
        // This will not work for template variables present in the spp
        StringBuilder sb = new StringBuilder();        
        if (scheme != null) sb.append(scheme).append(':');
        if (ssp != null)
            sb.append(ssp);
        if (fragment != null && fragment.length() > 0) sb.append('#').append(fragment);
        URI uri = createURI(sb.toString());
        
        if (uri.getRawSchemeSpecificPart() != null && uri.getRawPath() == null) {
            this.ssp = uri.getRawSchemeSpecificPart();
        } else {
            this.ssp = null;
            userInfo = uri.getRawUserInfo();
            host = uri.getHost();
            port = uri.getPort();
            path.setLength(0);
            path.append(replaceNull(uri.getRawPath()));
            query.setLength(0);
            query.append(replaceNull(uri.getRawQuery()));
        }
        return this;
    }

    @Override
    public UriBuilder userInfo(String ui) {
        checkSsp();
        this.userInfo = (ui != null) ?
            encode(ui, UriComponent.Type.USER_INFO) : null;
        return this;
    }

    @Override
    public UriBuilder host(String host) {
        checkSsp();
        if(host != null) {
            if(host.length() == 0) // null is used to reset host setting
                throw new IllegalArgumentException("Invalid host name");
            this.host = encode(host, UriComponent.Type.HOST);
        } else {
            this.host = null;
        }
        return this;
    }

    @Override
    public UriBuilder port(int port) {
        checkSsp();
        if(port < -1) // -1 is used to reset port setting and since URI allows
                      // as port any positive integer, so do we.
            throw new IllegalArgumentException("Invalid port value");
        this.port = port;
        return this;
    }

    @Override
    public UriBuilder replacePath(String path) {
        checkSsp();
        this.path.setLength(0);
        appendPath(path);
        return this;
    }

    @Override
    public UriBuilder path(String path) {        
        checkSsp();
        appendPath(path);        
        return this;
    }

    @Override
    public UriBuilder path(Class resource) {
        checkSsp();
        if (resource == null) 
            throw new IllegalArgumentException("Resource parameter is null");

        Class<?> c = resource;
        Path p = c.getAnnotation(Path.class);
        if (p == null)
            throw new IllegalArgumentException("The class, " + resource + " is not annotated with @Path");
        appendPath(p);
        return this;
    }

    @Override
    public UriBuilder path(Class resource, String methodName) {
        checkSsp();
        if (resource == null) 
            throw new IllegalArgumentException("Resource parameter is null");
        if (methodName == null) 
            throw new IllegalArgumentException("MethodName parameter is null");
        
        Method[] methods = resource.getMethods();
        Method found = null;
        for (Method m : methods) {
            if (methodName.equals(m.getName())) {
                if (found == null) found = m;
                else 
                    throw new IllegalArgumentException();
            }
        }

        if (found == null)
            throw new IllegalArgumentException("The method named, " + methodName +
                    ", is not specified by " + resource);

        appendPath(getPath(found));
        
        return this;
    }

    @Override
    public UriBuilder path(Method method) {
        checkSsp();
        if (method == null)
            throw new IllegalArgumentException("Method is null");
        appendPath(getPath(method));
        return this;
    }
    
    private Path getPath(AnnotatedElement ae) {
        Path p = ae.getAnnotation(Path.class);
        if (p == null)
            throw new IllegalArgumentException("The annotated element, " +
                    ae + " is not annotated with @Path");
        return p;
    }


    @Override
    public UriBuilder segment(String... segments) throws IllegalArgumentException {
        checkSsp();
        if (segments == null) 
            throw new IllegalArgumentException("Segments parameter is null");

        for (String segment: segments)
            appendPath(segment, true);        
        return this;
    }
    
    @Override
    public UriBuilder replaceMatrix(String matrix) {
        checkSsp();
        int i = path.lastIndexOf("/");
        if (i != -1) i = 0;
        i = path.indexOf(";", i);
        if (i != -1) path.setLength(i + 1);
        
        if (matrix != null)
            path.append(encode(matrix, UriComponent.Type.PATH));
        return this;
    }

    @Override
    public UriBuilder matrixParam(String name, Object... values) {
        checkSsp();
        if (name == null)
            throw new IllegalArgumentException("Name parameter is null");
        if (values == null)
            throw new IllegalArgumentException("Value parameter is null");
        if (values.length == 0)
            return this;
        
        if (matrixParams == null) {
            name = encode(name, UriComponent.Type.MATRIX_PARAM);
            for (Object value : values) {
                path.append(';').append(name);

                final String stringValue = value.toString();
                if (stringValue.length() > 0)
                    path.append('=').append(encode(stringValue, UriComponent.Type.MATRIX_PARAM));
            }
        } else {
            for (Object value : values) {
                matrixParams.add(name, value.toString());
            }            
        }
        return this;
    }

    @Override
    public UriBuilder replaceMatrixParam(String name, Object... values) {
        checkSsp();
        
        if (matrixParams == null) {
            int i = path.lastIndexOf("/");
            if (i != -1) i = 0;
            matrixParams = UriComponent.decodeMatrix((i != -1) ? path.substring(i) : "", false);
            i = path.indexOf(";", i);
            if (i != -1) path.setLength(i);
        }
        
        matrixParams.remove(name);
        for (Object value : values) {
            matrixParams.add(name, value.toString());
        }
        return this;
    }
    
    @Override
    public UriBuilder replaceQuery(String query) {
        checkSsp();
        this.query.setLength(0);
        if (query != null)
            this.query.append(encode(query, UriComponent.Type.QUERY));
        return this;
    }

    @Override
    public UriBuilder queryParam(String name, Object... values) {
        checkSsp();
        if (name == null)
            throw new IllegalArgumentException("Name parameter is null");
        if (values == null)
            throw new IllegalArgumentException("Value parameter is null");
        if (values.length == 0)
            return this;

        if (queryParams == null) {
            name = encode(name, UriComponent.Type.QUERY_PARAM);
            for (Object value : values) {
                if (query.length() > 0) query.append('&');
                query.append(name);

                if(value == null)
                    throw new IllegalArgumentException("One or more of value parameters are null");

                final String stringValue = value.toString();
                if (stringValue.length() > 0)
                    query.append('=').append(encode(stringValue, UriComponent.Type.QUERY_PARAM));
            }
        } else {
            for (Object value : values) {
                if(value == null)
                    throw new IllegalArgumentException("One or more of value parameters are null");

                queryParams.add(name,  value.toString());
            }
        }
        return this;
    }

    @Override
    public UriBuilder replaceQueryParam(String name, Object... values) {
        checkSsp();

        if (queryParams == null) {
            queryParams = UriComponent.decodeQuery(query.toString(), false);
            query.setLength(0);
        }

        queryParams.remove(name);

        if(values == null) return this;

        for (Object value : values) {
            if(value != null && !value.equals(""))
                queryParams.add(name, value.toString());
        }
        return this;
    }
    
    @Override
    public UriBuilder fragment(String fragment) {
        this.fragment = (fragment != null) ? 
            encode(fragment, UriComponent.Type.FRAGMENT) :
            null;
        return this;
    }

    private void checkSsp() {
        if (ssp != null) 
            throw new IllegalArgumentException("Schema specific part is opaque");                
    }
    
    private void appendPath(Path t) {
        if (t == null)
            throw new IllegalArgumentException("Path is null");
        
        appendPath(t.value());
    }
    
    private void appendPath(String path) {
        appendPath(path, false);
    }
    
    private void appendPath(String segments, boolean isSegment) {
        if (segments == null)
            throw new IllegalArgumentException("Path segment is null");
        if (segments.length() == 0)
            return;

        // Encode matrix parameters on current path segment
        encodeMatrix();

        segments = encode(segments, 
                (isSegment) ? UriComponent.Type.PATH_SEGMENT : UriComponent.Type.PATH);
        
        final boolean pathEndsInSlash = path.length() > 0 && path.charAt(path.length() - 1) == '/';
        final boolean segmentStartsWithSlash = segments.charAt(0) == '/';
        
        if (path.length() > 0 && !pathEndsInSlash && !segmentStartsWithSlash) {
            path.append('/');
        } else if (pathEndsInSlash && segmentStartsWithSlash) {
            segments = segments.substring(1);
            if (segments.length() == 0)
                return;
        }
        
        path.append(segments);
    }

    private void encodeMatrix() {
        if (matrixParams == null || matrixParams.isEmpty())
            return;

        for (Map.Entry<String, List<String>> e : matrixParams.entrySet()) {
            String name = encode(e.getKey(), UriComponent.Type.MATRIX_PARAM);
            
            for (String value : e.getValue()) {
                path.append(';').append(name);
                if (value.length() > 0)
                    path.append('=').append(encode(value, UriComponent.Type.MATRIX_PARAM));
            }
        }
        matrixParams = null;
    }

    private void encodeQuery() {
        if (queryParams == null || queryParams.isEmpty())
            return;

        for (Map.Entry<String, List<String>> e : queryParams.entrySet()) {
            String name = encode(e.getKey(), UriComponent.Type.QUERY_PARAM);

            for (String value : e.getValue()) {
                if (query.length() > 0) query.append('&');
                query.append(name);

                if (value.length() > 0)
                    query.append('=').append(encode(value, UriComponent.Type.QUERY_PARAM));
            }
        }
        queryParams = null;
    }

    private String encode(String s, UriComponent.Type type) {
        return UriComponent.contextualEncode(s, type, true);
    }
    
    public URI buildFromMap(Map<String, ? extends Object> values) {
        return _buildFromMap(true, values);
    }

    @Override
    public URI buildFromEncodedMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
        return _buildFromMap(false, values);
    }

    private URI _buildFromMap(boolean encode, Map<String, ? extends Object> values) {
        if (ssp != null) 
            throw new IllegalArgumentException("Schema specific part is opaque");
        
        encodeMatrix();
        encodeQuery();
        
        String uri = UriTemplate.createURI(scheme, 
                userInfo, host, (port != -1) ? String.valueOf(port) : null,
                path.toString(), query.toString(), fragment, values, encode);
        return createURI(uri);        
    }
    
    @Override
    public URI build(Object... values) {
        return _build(true, values);
    }
    
    @Override
    public URI buildFromEncoded(Object... values) {
        return _build(false, values);
    }
    
    private URI _build(boolean encode, Object... values) {
        if (values == null || values.length == 0)
            return createURI(create());

        if (ssp != null)
            throw new IllegalArgumentException("Schema specific part is opaque");

        encodeMatrix();
        encodeQuery();

        String uri = UriTemplate.createURI(scheme,
                userInfo, host, (port != -1) ? String.valueOf(port) : null,
                path.toString(), query.toString(), fragment, values, encode);
        return createURI(uri);
    }

    private String create() {
        encodeMatrix();
        encodeQuery();
        
        StringBuilder sb = new StringBuilder();
        
        if (scheme != null) sb.append(scheme).append(':');

        if (ssp != null) {
            sb.append(ssp);
        } else {        
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
        }
        
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
    
    private String replaceNull(String s) {
        return (s != null) ? s : "";
    }
}