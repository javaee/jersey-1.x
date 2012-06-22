/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerResponseWriter;

/**
 * A GZIP content encoding filter.
 * <p>
 * If the request contains a Content-Encoding header of "gzip"
 * then the request entity (if any) is uncompressed using gzip.
 * If the request contains an Accept-Encoding header containing "gzip" and an "If-None-Match" Header, entitytag value is checked:
 * if it contains the -gzip suffix, remove this suffix, otherwise, completely remove the "if-none-match" header.
 * <p>
 * If the request contains a Accept-Encoding header that contains
 * "gzip" then the response entity (if any) is compressed using gzip and a
 * Content-Encoding header of "gzip" is added to the response.
 * As this filter is active, the resource representation can be compressed. the value "Accept-Encoding" is so added to the Vary header.
 * If any entityTag is used and content may be gzipped, the "-gzip" suffix is added to entitytag value.
 * <p>
 * When an application is deployed as a Servlet or Filter this Jersey filter can be
 * registered using the following initialization parameters:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.GZIPContentEncodingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt
 *     &lt;init-param&gt
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerResponseFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.GZIPContentEncodingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * </pre></blockquote>
 * Note that if you're using Entitag for cache control and want to use this filter, you shoud add this filter to both ContainerFilters (Request and Response).
 *
 * @author Paul Sandoz
 * @author Edouard Chevalier
 * @see com.sun.jersey.api.container.filter
 */
public class GZIPContentEncodingFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final String ENTITY_TAG_GZIP_SUFFIX_VALUE = "-gzip";
	private static final String ENTITY_TAG_GZIP_SUFFIX_HEADER_VALUE = "-gzip\""; // Entity tag raw values always finish with a quotation mark within http headers.

	public ContainerRequest filter(ContainerRequest request) {
        String contentEncoding = request.getRequestHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding != null && contentEncoding.trim().equals("gzip")) {
            request.getRequestHeaders().remove(HttpHeaders.CONTENT_ENCODING);
            try {
                request.setEntityInputStream(
                        new GZIPInputStream(request.getEntityInputStream()));
            } catch (IOException ex) {
                throw new ContainerException(ex);
            }
        }

        // Check for entity tag header 'If-None-Match' in case of accept-encoding = gzip
        String acceptEncoding = request.getRequestHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING);
        String entityTag = request.getRequestHeaders().getFirst(HttpHeaders.IF_NONE_MATCH);
        if (acceptEncoding != null && acceptEncoding.contains("gzip") && entityTag != null) {
            // normalize entitytag (Note: maybe it should check whether it ends with a quotation mark ?)
            if (entityTag.endsWith(ENTITY_TAG_GZIP_SUFFIX_HEADER_VALUE)) {
            	final int gzipsuffixbeginIndex = entityTag.lastIndexOf(ENTITY_TAG_GZIP_SUFFIX_HEADER_VALUE);
            	final StringBuilder sb = new StringBuilder();
            	sb.append(entityTag.substring(0, gzipsuffixbeginIndex));
            	sb.append('\"');
            	request.getRequestHeaders().putSingle(HttpHeaders.IF_NONE_MATCH, sb.toString());
            } else {
                // otherwise, remove if-none-match header
            	request.getRequestHeaders().remove(HttpHeaders.IF_NONE_MATCH);
            }
        }
        return request;
    }

    private static final class Adapter implements ContainerResponseWriter {
        private final ContainerResponseWriter crw;

        private GZIPOutputStream gos;

        Adapter(ContainerResponseWriter crw) {
            this.crw = crw;
        }

        public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException {
           gos = new GZIPOutputStream(crw.writeStatusAndHeaders(-1, response));
           return gos;
        }

        public void finish() throws IOException {
            gos.finish();
            crw.finish();
        }
    }

    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
    	response.getHttpHeaders().add(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING); // add vary header

        String acceptEncoding = request.getRequestHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING);
        String contentEncoding = (String) response.getHttpHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);

        if (acceptEncoding != null && contentEncoding == null && acceptEncoding.contains("gzip")) {
            // Check EntityTag header
            if (response.getHttpHeaders().containsKey(HttpHeaders.ETAG)) {
                 EntityTag entityTag  = (EntityTag) response.getHttpHeaders().getFirst(HttpHeaders.ETAG);
                 if(entityTag != null){
                    response.getHttpHeaders().putSingle(HttpHeaders.ETAG, new EntityTag(entityTag.getValue() + ENTITY_TAG_GZIP_SUFFIX_VALUE,entityTag.isWeak()));
                 }
            }

            // wrap entity with gzip
            if (response.getEntity() != null ) {
                response.getHttpHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");
                response.setContainerResponseWriter(new Adapter(response.getContainerResponseWriter()));
            }
        }
        return response;
    }
}
