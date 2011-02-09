/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.api.container.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

/**
 * A URI-based content negotiation filter mapping a dot-declared suffix in
 * URI to media type that is the value of the <code>Accept</code> header
 * or a language that is the value of the <code>Accept-Language</code> header.
 * <p>
 * This filter may be used when the accetable media type and acceptable
 * language need to be declared in the URI.
 * <p>
 * This class may be extended to declare the mappings and the extending class,
 * <code>foo.MyUriConnegFilter</code> say, can be registered as a container request
 * filter. When an application is deployed as a Servlet or Filter such a filter
 * can be registered using the following initialization parameters:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;foo.MyUriConnegFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt
 * </pre></blockquote>
 * <p>
 * If a suffix of "atom" is regstered with a media type of
 * "application/atom+xml" then a GET request of:
 * <pre>GET /resource.atom</pre>
 * <p>is transformed to:</p>
 * <pre>GET /resource
 *Accept: application/atom+xml</pre>
 * Any existing "Accept" header value will be replaced.
 * <p>
 * If a suffix of "english: is regstered with a language of
 * "en" then a GET request of:
 * <pre>GET /resource.english</pre>
 * <p>is transformed to:</p>
 * <pre>GET /resource
 *Accept-Language: en</pre>
 * Any existing "Accept-Language"header  value will be replaced.
 * <p>
 * The media type mappings are processed before the language type mappings.
 *
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.container.filter
 */
public class UriConnegFilter implements ContainerRequestFilter {

    private final Map<String, MediaType> mediaExtentions;

    private final Map<String, String> languageExtentions;

    /**
     * Create a filter with suffix to media type mappings.
     *
     * @param mediaExtentions the suffix to media type mappings.
     */
    public UriConnegFilter(Map<String, MediaType> mediaExtentions) {
        if (mediaExtentions == null)
            throw new IllegalArgumentException();

        this.mediaExtentions = mediaExtentions;
        this.languageExtentions = Collections.emptyMap();
    }

    /**
     * Create a filter with suffix to media type mappings and suffix to
     * langauge mappings.
     *
     * @param mediaExtentions the suffix to media type mappings.
     * @param languageExtentions the syffix to language mappings.
     */
    public UriConnegFilter(Map<String, MediaType> mediaExtentions, Map<String, String> languageExtentions) {
        if (mediaExtentions == null)
            throw new IllegalArgumentException();
        if (languageExtentions == null)
            throw new IllegalArgumentException();
        
        this.mediaExtentions = mediaExtentions;
        this.languageExtentions = languageExtentions;
    }

    public ContainerRequest filter(ContainerRequest request) {
        // Quick check for a '.' character
        String path = request.getRequestUri().getRawPath();
        if (path.indexOf('.') == -1)
            return request;
        
        List<PathSegment> l = request.getPathSegments(false);
        if (l.isEmpty())
            return request;

        // Get the last non-empty path segment
        PathSegment segment = null;
        for (int i = l.size() - 1; i >= 0; i--) {
            segment = l.get(i);
            if (segment.getPath().length() > 0)
                break;
        }
        if (segment == null)
            return request;

        final int length = path.length();
        // Get the suffixes
        final String[] suffixes = segment.getPath().split("\\.");
        
        for (int i = suffixes.length - 1; i >= 1; i--) {
            final String suffix = suffixes[i];
            if (suffix.length() == 0)
                continue;
            
            final MediaType accept = mediaExtentions.get(suffix);
            if (accept != null) {
                request.getRequestHeaders().putSingle("Accept", accept.toString());

                final int index = path.lastIndexOf('.' + suffix);
                path = new StringBuilder(path).delete(index, index + suffix.length() + 1).toString();
                suffixes[i] = "";
                break;
            }
        }

        for (int i = suffixes.length - 1; i >= 1; i--) {
            final String suffix = suffixes[i];
            if (suffix.length() == 0)
                continue;

            final String acceptLanguage = languageExtentions.get(suffix);
            if (acceptLanguage != null) {
                request.getRequestHeaders().putSingle("Accept-Language", acceptLanguage);

                final int index = path.lastIndexOf('.' + suffix);
                path = new StringBuilder(path).delete(index, index + suffix.length() + 1).toString();
                suffixes[i] = "";
                break;
            }
        }
        
        if (length != path.length()) {
            request.setUris(
                    request.getBaseUri(),
                    request.getRequestUriBuilder().replacePath(path).build());
        }

        return request;
    }
}
