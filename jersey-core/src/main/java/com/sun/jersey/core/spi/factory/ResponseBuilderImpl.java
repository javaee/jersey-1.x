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

package com.sun.jersey.core.spi.factory;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.Variant;

/**
 * An implementation of {@link ResponseBuilder}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResponseBuilderImpl extends Response.ResponseBuilder {    

    private StatusType statusType = Status.NO_CONTENT;

    private OutBoundHeaders headers;

    private Object entity;

    private Type entityType;

    public ResponseBuilderImpl() { }

    private ResponseBuilderImpl(ResponseBuilderImpl that) {
        this.statusType = that.statusType;
        this.entity = that.entity;
        if (that.headers != null) {
            this.headers = new OutBoundHeaders(that.headers);
        } else {
            this.headers = null;
        }
        this.entityType = that.entityType;
    }

    public Response.ResponseBuilder entityWithType(Object entity, Type entityType) {
        this.entity = entity;
        this.entityType = entityType;
        return this;
    }

    private OutBoundHeaders getHeaders() {
        if (headers == null)
            headers = new OutBoundHeaders();
        return headers;
    }

    // Response.Builder

    public Response build() {
        final Response r = new ResponseImpl(
                statusType,
                getHeaders(),
                entity,
                entityType);
        reset();
        return r;
    }

    private void reset() {
        statusType = Status.NO_CONTENT;
        headers = null;
        entity = null;
        entityType = null;
    }

    @Override
    public ResponseBuilder clone() {
        return new ResponseBuilderImpl(this);
    }

    public Response.ResponseBuilder status(StatusType status) {
        if (status == null)
            throw new IllegalArgumentException();
        this.statusType = status;
        return this;
    };

    public Response.ResponseBuilder status(int status) {
        return status(ResponseImpl.toStatusType(status));
    }

    public Response.ResponseBuilder entity(Object entity) {
        this.entity = entity;
        this.entityType = (entity != null) ? entity.getClass() : null;
        return this;
    }

    public Response.ResponseBuilder type(MediaType type) {
        headerSingle(HttpHeaders.CONTENT_TYPE, type);
        return this;
    }

    public Response.ResponseBuilder type(String type) {
        return type(type == null ? null : MediaType.valueOf(type));
    }

    public Response.ResponseBuilder variant(Variant variant) {
        if (variant == null) {
            type((MediaType)null);
            language((String)null);
            encoding(null);
            return this;
        }

        type(variant.getMediaType());
        // TODO set charset
        language(variant.getLanguage());
        encoding(variant.getEncoding());

        return this;
    }

    public Response.ResponseBuilder variants(List<Variant> variants) {
        if (variants == null) {
            header(HttpHeaders.VARY, null);
            return this;
        }

        if (variants.isEmpty())
            return this;

        MediaType accept = variants.get(0).getMediaType();
        boolean vAccept = false;

        Locale acceptLanguage = variants.get(0).getLanguage();
        boolean vAcceptLanguage = false;

        String acceptEncoding = variants.get(0).getEncoding();
        boolean vAcceptEncoding = false;

        for (Variant v : variants) {
            vAccept |= !vAccept && vary(v.getMediaType(), accept);
            vAcceptLanguage |= !vAcceptLanguage && vary(v.getLanguage(), acceptLanguage);
            vAcceptEncoding |= !vAcceptEncoding && vary(v.getEncoding(), acceptEncoding);
        }

        StringBuilder vary = new StringBuilder();
        append(vary, vAccept, HttpHeaders.ACCEPT);
        append(vary, vAcceptLanguage, HttpHeaders.ACCEPT_LANGUAGE);
        append(vary, vAcceptEncoding, HttpHeaders.ACCEPT_ENCODING);

        if (vary.length() > 0)
            header(HttpHeaders.VARY, vary.toString());
        return this;
    }

    private boolean vary(MediaType v, MediaType vary) {
        return v != null && !v.equals(vary);
    }

    private boolean vary(Locale v, Locale vary) {
        return v != null && !v.equals(vary);
    }

    private boolean vary(String v, String vary) {
        return v != null && !v.equalsIgnoreCase(vary);
    }

    private void append(StringBuilder sb, boolean v, String s) {
        if (v) {
            if (sb.length() > 0)
                sb.append(',');
            sb.append(s);
        }
    }

    public Response.ResponseBuilder language(String language) {
        headerSingle(HttpHeaders.CONTENT_LANGUAGE, language);
        return this;
    }

    public Response.ResponseBuilder language(Locale language) {
        headerSingle(HttpHeaders.CONTENT_LANGUAGE, language);
        return this;
    }

    public Response.ResponseBuilder location(URI location) {
        headerSingle(HttpHeaders.LOCATION, location);
        return this;
    }

    public Response.ResponseBuilder contentLocation(URI location) {
        headerSingle(HttpHeaders.CONTENT_LOCATION, location);
        return this;
    }

    public Response.ResponseBuilder encoding(String encoding) {
        headerSingle(HttpHeaders.CONTENT_ENCODING, encoding);
        return this;
    }

    public Response.ResponseBuilder tag(EntityTag tag) {
        headerSingle(HttpHeaders.ETAG, tag);
        return this;
    }

    public Response.ResponseBuilder tag(String tag) {
        return tag(tag == null ? null : new EntityTag(tag));
    }

    public Response.ResponseBuilder lastModified(Date lastModified) {
        headerSingle(HttpHeaders.LAST_MODIFIED, lastModified);
        return this;
    }

    public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
        headerSingle(HttpHeaders.CACHE_CONTROL, cacheControl);
        return this;
    }

    public Response.ResponseBuilder expires(Date expires) {
        headerSingle(HttpHeaders.EXPIRES, expires);
        return this;
    }

    public Response.ResponseBuilder cookie(NewCookie... cookies) {
        if (cookies != null) {
            for (NewCookie cookie : cookies)
                header(HttpHeaders.SET_COOKIE, cookie);
        } else {
            header(HttpHeaders.SET_COOKIE, null);
        }
        return this;
    }

    public Response.ResponseBuilder header(String name, Object value) {
        return header(name, value, false);
    }

    public Response.ResponseBuilder headerSingle(String name, Object value) {
        return header(name, value, true);
    }

    public Response.ResponseBuilder header(String name, Object value, boolean single) {
        if (value != null) {
            if (single) {
                getHeaders().putSingle(name, value);
            } else {
                getHeaders().add(name, value);
            }
        } else {
            getHeaders().remove(name);
        }
        return this;
    }
}