/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.api.core;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.QualitySourceMediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;

/**
 * An abstraction of a HTTP request.
 */
public interface HttpRequestContext extends HttpHeaders, Request, SecurityContext, Traceable {
    /**
     * Get the base URI of the application. URIs of root resource classes
     * are all relative to this base URI.
     * @return the base URI of the application
     */
    URI getBaseUri();
       
    /**
     * Get the base URI of the application in the form of a UriBuilder.
     * @return a UriBuilder initialized with the base URI of the application.
     */
    UriBuilder getBaseUriBuilder();
    
    /**
     * Get the absolute request URI. This includes query parameters and
     * any supplied fragment.
     * @return the absolute request URI
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    URI getRequestUri();
    
    /**
     * Get the absolute request URI in the form of a UriBuilder.
     * @return a UriBuilder initialized with the absolute request URI
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    UriBuilder getRequestUriBuilder();
    
    /**
     * Get the absolute path of the request. This includes everything preceding
     * the path (host, port etc) but excludes query parameters and fragment.
     * This is a shortcut for
     * <code>uriInfo.getBase().resolve(uriInfo.getPath()).</code>
     * @return the absolute path of the request
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    URI getAbsolutePath();
    
    /**
     * Get the absolute path of the request in the form of a UriBuilder.
     * This includes everything preceding the path (host, port etc) but excludes
     * query parameters and fragment.
     * @return a UriBuilder initialized with the absolute path of the request
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    UriBuilder getAbsolutePathBuilder();
    
    /**
     * Get the path of the current request relative to the base URI as
     * a string. All sequences of escaped octets are decoded, equivalent to
     * <code>getPath(true)</code>.
     * 
     * @return the relative URI path
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    String getPath();
    
    /**
     * Get the path of the current request relative to the base URI as
     * a string.
     *
     * @param decode controls whether sequences of escaped octets are decoded
     * (true) or not (false).
     * @return the relative URI path
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    String getPath(boolean decode);

    /**
     * Get the path of the current request relative to the base URI as a 
     * list of {@link PathSegment}. This method is useful when the
     * path needs to be parsed, particularly when matrix parameters may be
     * present in the path. All sequences of escaped octets in path segments
     * and matrix parameter names and values are decoded,
     * equivalent to <code>getPathSegments(true)</code>.
     * @return an unmodifiable list of {@link PathSegment}. The matrix parameter
     * map of each path segment is also unmodifiable.
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     * @see PathSegment
     * @see <a href="http://www.w3.org/DesignIssues/MatrixURIs.html">Matrix URIs</a>
     */
    List<PathSegment> getPathSegments();
    
    /**
     * Get the path of the current request relative to the base URI as a 
     * list of {@link PathSegment}. This method is useful when the
     * path needs to be parsed, particularly when matrix parameters may be
     * present in the path.
     * @param decode controls whether sequences of escaped octets in path segments
     * and matrix parameter names and values are decoded (true) or not (false).
     * @return an unmodifiable list of {@link PathSegment}. The matrix parameter
     * map of each path segment is also unmodifiable.
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     * @see PathSegment
     * @see <a href="http://www.w3.org/DesignIssues/MatrixURIs.html">Matrix URIs</a>
     */
    List<PathSegment> getPathSegments(boolean decode);

    /**
     * Get the URI query parameters of the current request.
     * All sequences of escaped octets in parameter names and values are decoded,
     * equivalent to <code>getQueryParameters(true)</code>.
     * @return an unmodifiable map of query parameter names and values
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    MultivaluedMap<String, String> getQueryParameters();
    
    /**
     * Get the URI query parameters of the current request.
     * @param decode controls whether sequences of escaped octets in parameter
     * names and values are decoded (true) or not (false).
     * @return an unmodifiable map of query parameter names and values
     * @throws java.lang.IllegalStateException if called outside the scope of a request
     */
    MultivaluedMap<String, String> getQueryParameters(boolean decode);

    /**
     * Get a HTTP header value.
     * 
     * @param name the HTTP header
     * @return the HTTP header value. If the HTTP header is not present then
     * null is returned. If the HTTP header is present but has no value then
     * the empty string is returned. If the HTTP header is present more than
     * once then the values of joined together and separated by a ',' character.
     */
    String getHeaderValue(String name);

    /**
     * Select the first media type, from a list of media types, that is most
     * acceptable according to the requested acceptable media types.
     *
     * @deprecated
     * @param mediaTypes the list of media types
     * @return the most acceptable media type, or null if no media type
     *         was found to be acceptable.
     */
    @Deprecated
    MediaType getAcceptableMediaType(List<MediaType> mediaTypes);
        
    /**
     * Get a list of media types that are acceptable for the response.
     *
     * @deprecated
     * @param priorityMediaTypes the list of media types that take priority,
     *        ordered according to the quality source parameter, "qs" as the
     *        primary key.
     * @return a list of requested response media types sorted according
     *         to highest relative quality value, which is product of the
     *         quality parameter, q, of an acceptable media type, and the 
     *         quality source parameter, qs, of matching media type.
     */
    @Deprecated
    List<MediaType> getAcceptableMediaTypes(List<QualitySourceMediaType> priorityMediaTypes);

    /**
     * Get the cookie name value map.
     * 
     * @return the cookie name value map.
     */
    MultivaluedMap<String, String> getCookieNameValueMap();
    
    /**
     * Get the request entity, returns null if the request does not
     * contain an entity body.
     * 
     * @param type the type of entity
     * @return the request entity or null
     * @throws WebApplicationException if the content of the request
     * cannot be mapped to an entity of the requested type
     */
    <T> T getEntity(Class<T> type) throws WebApplicationException;
        
    /**
     * Get the request entity, returns null if the request does not
     * contain an entity body.
     * 
     * @param type the type of entity
     * @param genericType type the generic type of entity, it is the responsibility
     *        of the callee to ensure that the type and generic type are
     *        consistent otherwise the behaviour of this method is undefined.
     * @param as the annotations associated with the type
     * @return the request entity or null
     * @throws WebApplicationException if the content of the request
     * cannot be mapped to an entity of the requested type
     */
    <T> T getEntity(Class<T> type, Type genericType, Annotation[] as) 
            throws WebApplicationException;

    /**
     * Get the form parameters of the request entity.
     * <p>
     * This method will ensure that the request entity is buffered
     * such that it may be consumed by the application.
     *
     * @return the form parameters, if there is a request entity and the
     * content type is "application/x-www-form-urlencoded", otherwise an
     * instance containing no parameters will be returned.
     */
    Form getFormParameters();
}