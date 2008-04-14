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

package com.sun.ws.rest.api.uri;

import java.util.List;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

/**
 * Extentions to {@link UriInfo}. 
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface ExtendedUriInfo extends UriInfo {
    /**
     * Get a read-only list of {@link UriTemplate} for ancestor resources. 
     * Each entry is a URI template that is the value of the 
     * {@link javax.ws.rs.Path} that is a partial path that matched a resource 
     * class, a sub-resource method or a sub-resource locator.
     * Entries are ordered in reverse request URI matching order, with the 
     * root resource URI last.
     * 
     * @return a read-only list of URI templates for ancestor resources.
     */
    List<UriTemplate> getAncestorTemplates();
    
    /**
     * Get a path segment that contains a template variable.
     * All sequences of escaped octets are decoded,
     * equivalent to <code>getPathSegment(true)</code>.
     * 
     * @param templateVariable
     * @return the path segment or null if a there the matching path does not 
     *         contain the template
     */
    PathSegment getPathSegment(String templateVariable);
    
    /**
     * Get a path segment that contains a template variable.
     * 
     * @param decode controls whether sequences of escaped octets are decoded
     * (true) or not (false).
     * @param templateVariable
     * @return the path segment or null if a there the matching path does not 
     *         contain the template
     */
    PathSegment getPathSegment(String name, boolean decode);
}