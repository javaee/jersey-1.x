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

package com.sun.ws.rest.api.core;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.ext.Contract;
import javax.ws.rs.ext.ProviderFactory;

/**
 * URI template aware utility class for building URIs from their components. See
 * {@link javax.ws.rs.UriTemplate#value} for an explanation of URI templates.
 * @see java.net.URI
 * @see javax.ws.rs.UriTemplate
 */
@Contract
public abstract class UriBuilder {
    
    /**
     * Creates a new instance of UriBuilder
     * @return a new instance of UriBuilder
     */
    protected static synchronized UriBuilder newInstance() {
        UriBuilder b = ProviderFactory.getInstance().createInstance(UriBuilder.class);
        if (b==null)
            throw new UnsupportedOperationException("No UriBuilder implementation found");
        return b;
    }
    
    /**
     * Create a new instance initialized from an existing URI.
     * @param uri a URI that will be used to initialize the UriBuilder.
     * @return a new UriBuilder
     */
    public static UriBuilder fromUri(URI uri) {
        UriBuilder b = newInstance();
        b.uri(uri);
        return b;
    }
    
    /**
     * Create a new instance initialized from an unencoded URI path,
     * equivalent to <code>fromPath(path, true)</code>.
     * @param path a URI path that will be used to initialize the UriBuilder.
     * @return a new UriBuilder
     */
    public static UriBuilder fromPath(String path) {
        return fromPath(path, true);
    }

    /**
     * Create a new instance initialized from a URI path.
     * @param path a URI path that will be used to initialize the UriBuilder.
     * @param encode controls whether the supplied value is automatically encoded
     * (true) or not (false). If false, the value must be valid with all illegal
     * characters already escaped.
     * @return a new UriBuilder
     */
    public static UriBuilder fromPath(String path, boolean encode) {
        UriBuilder b = newInstance();
        b.replacePath(path);
        return b;
    }

    /**
     * Create a new instance initialized from a root resource class.
     * @param resource a root resource whose @UriTemplate value will be used 
     * to initialize the UriBuilder.
     * @return a new UriBuilder
     */
    public static UriBuilder fromResource(Class resource) {
        UriBuilder b = newInstance();
        b.path(resource);
        return b;
    }

    /**
     * Copies the non-null components of the supplied URI to the UriBuilder.
     * @param uri the URI to copy components from
     * @return the updated UriBuilder
     */
    public abstract UriBuilder uri(URI uri);
    
    /**
     * Set the URI scheme.
     * @param scheme the URI scheme
     * @return the updated UriBuilder
     */
    public abstract UriBuilder scheme(String scheme);
    
    /**
     * Set the URI scheme-specific-part. This method will overwrite any existing
     * values for authority, user-info, host, port and path.
     * @param ssp the URI scheme-specific-part
     * @return the updated UriBuilder
     */
    public abstract UriBuilder schemeSpecificPart(String ssp);
    
    /**
     * Set the URI authority. This method will overwrite any existing user-info
     * host and port. 
     * @param authority the URI authority
     * @return the updated UriBuilder
     */
    public abstract UriBuilder authority(String authority);
    
    /**
     * Set the URI user-info using an unencoded value. Equivalent to
     * <code>userInfo(ui, true)</code>.
     * @param ui the URI user-info
     * @return the updated UriBuilder
     */
    public UriBuilder userInfo(String ui) {
        return userInfo(ui, true);
    }
    
    /**
     * Set the URI user-info.
     * @param ui the URI user-info
     * @param encode controls whether the supplied value is automatically encoded
     * (true) or not (false). If false, the value must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder userInfo(String ui, boolean encode);
    
    /**
     * Set the URI host.
     * @return the updated UriBuilder
     * @param host the URI host
     */
    public abstract UriBuilder host(String host);
    
    /**
     * Set the URI port.
     * @param port the URI port
     * @return the updated UriBuilder
     */
    public abstract UriBuilder port(int port);
    
    /**
     * Set the URI path using an unencoded value, equivalent to 
     * <code>replacePath(path, true)</code>. This method will overwrite 
     * any existing path segments.
     * @param path the URI path, may contain URI template parameters
     * @return the updated UriBuilder
     */
    public UriBuilder replacePath(String path) {
        return replacePath(path, true);
    }

    /**
     * Set the URI path. This method will overwrite any existing path segments.
     * @param path the URI path, may contain URI template parameters
     * @param encode controls whether the supplied value is automatically encoded
     * (true) or not (false). If false, the value must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder replacePath(String path, boolean encode);

    /**
     * Append unencoded path segments to the existing list of segments, equivalent
     * to <code>path(true, segments)</code>. When constructing
     * the final path, each segment will be separated by '/' if necessary.
     * @param segments the path segments, may contain URI template parameters
     * @return the updated UriBuilder
     */
    public UriBuilder path(String... segments) {
        return path(true, segments);
    }

    /**
     * Append path segments to the existing list of segments. When constructing
     * the final path, each segment will be separated by '/' if necessary.
     * @param segments the path segments, may contain URI template parameters
     * @param encode controls whether the supplied values are automatically encoded
     * (true) or not (false). If false, the values must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder path(boolean encode, String... segments);

    /**
     * Append path segments to the existing list of segments. When constructing
     * the final path, each segment will be separated by '/' if necessary.
     * @param resource a root resource whose @UriTemplate value will be used to
     * obtain the path segments
     * @return the updated UriBuilder
     */
    public abstract UriBuilder path(Class resource);
    
    /**
     * Set the matrix parameters of the final segment of the current URI path
     * using an unencoded value, equivalent to 
     * <code>replaceMatrixParams(matrix, true)</code>.
     * This method will overwrite any existing matrix parameters on the final
     * segment of the current URI path.
     * @param matrix the matrix parameters, may contain URI template parameters
     * @return the updated UriBuilder
     */
    public UriBuilder replaceMatrixParams(String matrix) {
        return replaceMatrixParams(matrix, true);
    }

    /**
     * Set the matrix parameters of the final segment of the current URI path.
     * This method will overwrite any existing matrix parameters on the final
     * segment of the current URI path.
     * @param matrix the matrix parameters, may contain URI template parameters
     * @param encode controls whether the supplied value is automatically encoded
     * (true) or not (false). If false, the value must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder replaceMatrixParams(String matrix, boolean encode);

    /**
     * Append a matrix parameter to the existing set of matrix parameters of 
     * the final segment of the current URI path, equivalent to
     * <code>matrixParam(name, value, true)</code>.
     * @param name the matrix parameter name, may contain URI template parameters
     * @param value the matrix parameter value, may contain URI template parameters
     * @return the updated UriBuilder
     */
    public UriBuilder matrixParam(String name, String value) {
        return matrixParam(name, value, true);
    }

    /**
     * Append a matrix parameter to the existing set of matrix parameters of 
     * the final segment of the current URI path.
     * @param name the matrix parameter name, may contain URI template parameters
     * @param value the matrix parameter value, may contain URI template parameters
     * @param encode controls whether the supplied name and value are automatically encoded
     * (true) or not (false). If false, the name and value must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder matrixParam(String name, String value, boolean encode);

    /**
     * Set the URI query string using an unencoded value, equivalent to
     * <code>replaceQueryParams(query, true)</code>. This method will overwrite any existing query
     * parameters.
     * @param query the URI query string
     * @return the updated UriBuilder
     */
    public UriBuilder replaceQueryParams(String query) {
        return replaceQueryParams(query, true);
    }

    /**
     * Set the URI query string. This method will overwrite any existing query
     * parameters.
     * @param query the URI query string
     * @param encode controls whether the supplied value is automatically encoded
     * (true) or not (false). If false, the value must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder replaceQueryParams(String query, boolean encode);

    /**
     * Append a query parameter to the existing set of query parameters, equivalent to
     * <code>queryParam(name, value, true)</code>.
     * @param name the query parameter name, may contain URI template parameters
     * @param value the query parameter value, may contain URI template parameters
     * @return the updated UriBuilder
     */
    public UriBuilder queryParam(String name, String value) {
        return queryParam(name, value, true);
    }
    
    /**
     * Append a query parameter to the existing set of query parameters.
     * @param name the query parameter name, may contain URI template parameters
     * @param value the query parameter value, may contain URI template parameters
     * @param encode controls whether the supplied name and value are automatically encoded
     * (true) or not (false). If false, the name and value must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder queryParam(String name, String value, boolean encode);
    
    /**
     * Set the URI fragment using an unencoded value, equivalent to 
     * <code>fragment(fragment, true)</code>.
     * @param fragment the URI fragment
     * @return the updated UriBuilder
     */
    public UriBuilder fragment(String fragment) {
        return fragment(fragment, true);
    }
    
    /**
     * Set the URI fragment.
     * @param fragment the URI fragment
     * @param encode controls whether the supplied value is automatically encoded
     * (true) or not (false). If false, the value must be valid with all illegal
     * characters already escaped.
     * @return the updated UriBuilder
     */
    public abstract UriBuilder fragment(String fragment, boolean encode);
    
    /**
     * Build a URI, any URI template parameters will be replaced by the empty
     * string.
     * @return the URI built from the UriBuilder
     */
    public abstract URI build();

    /**
     * Build a URI, any URI template parameters will be replaced by the value in
     * the supplied map. Any URI template parameters without a value will be
     * replaced by the empty string.
     * @param values a map of URI template parameter names and values
     * @return the URI built from the UriBuilder
     */
    public abstract URI build(Map<String, String> values);
    
    /**
     * Build a URI, using the supplied values in order to replace any URI
     * template parameters. Any URI template parameters without a value will be
     * replaced by the empty string.
     * @param values a list of URI template parameter values
     * @return the URI built from the UriBuilder
     */
    public abstract URI build(String... values);
}
