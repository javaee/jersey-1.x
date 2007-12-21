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

import com.sun.ws.rest.impl.util.KeyComparatorHashMap;
import com.sun.ws.rest.impl.util.StringIgnoreCaseKeyComparator;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResponseBuilderImpl extends Response.Builder {    
    static final int CACHE_CONTROL     = 0;
    static final int CONTENT_LANGUAGE  = 1;
    static final int CONTENT_LOCATION  = 2;
    static final int CONTENT_TYPE      = 3;
    static final int ETAG              = 4;
    static final int LAST_MODIFIED     = 5;
    static final int LOCATION          = 6;

    private static final Object[] EMPTY_VALUES = new Object[0];
    
    private static final Map<String, Integer> HEADER_MAP = createHeaderMap();
            
    private static final String[] HEADER_ARRAY = createHeaderArray();
    
    private static Map<String, Integer> createHeaderMap() {
        Map<String, Integer> m = new KeyComparatorHashMap<String, Integer>(
                StringIgnoreCaseKeyComparator.SINGLETON);
        
        m.put("Cache-Control", CACHE_CONTROL);
        m.put("Content-Language", CONTENT_LANGUAGE);
        m.put("Content-Location", CONTENT_LOCATION);
        m.put("Content-Type", CONTENT_TYPE);
        m.put("ETag", ETAG);
        m.put("Last-Modified", LAST_MODIFIED);
        m.put("Location", LOCATION);
        
        return Collections.unmodifiableMap(m);
    }
    
    private static String[] createHeaderArray() {
        Map<String, Integer> m = createHeaderMap();
        
        String[] a = new String[m.size()];
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            a[e.getValue()] = e.getKey();
        }
                
        return a;
    }
    
    static String getHeader(int id) {
        return HEADER_ARRAY[id];
    }
    
    private int status = 204;

    private Object entity;
    
    private Object[] values;
    
    private List<Object> nameValuePairs;

    // Response.Builder
    
    // public ResponseBuilderImpl() { }
        
    public Response build() {
        Response r = new ResponseImpl(status, entity, 
                (values != null) ? values : EMPTY_VALUES, 
                (nameValuePairs != null) ? nameValuePairs : Collections.emptyList());
        reset();
        return r;
    }
    
    private void reset() {
        status = 204;
        entity = null;
        values = null;
        nameValuePairs = null;
    }

    
    public Response.Builder status(int status) {
        this.status = status;
        return this;
    }

    public Response.Builder entity(Object entity) {
        this.entity = entity;
        return this;
    }

    public Response.Builder type(MediaType type) {
        set(CONTENT_TYPE, type);
        return this;
    }

    public Response.Builder type(String type) {
        set(CONTENT_TYPE, type);
        return this;
    }

    public Response.Builder variant(Variant variant) {
        type(variant.getMediaType());
        // TODO set charset
        language(variant.getLanguage());
        if (variant.getEncoding() != null)
            header("Content-Encoding", variant.getEncoding());
        
        // TODO this is not the correct way to set the vary field
        // since a dimension may only have one choice and therefore
        // does not vary
        StringBuilder vary = new StringBuilder();
        append(vary, variant.getMediaType() != null, "Accept");
        append(vary, variant.getCharset() != null, "Accept-Charset");
        append(vary, variant.getLanguage() != null, "Accept-Language");
        append(vary, variant.getEncoding() != null, "Accept-Encoding");
        
        if (vary.length() > 0)
            header("Vary", vary.toString());
        
        return this;
    }
    
    public Response.Builder variants(List<Variant> variants) {
        status(406);
        if (variants.isEmpty())
            return this;

        MediaType accept = variants.get(0).getMediaType();
        boolean vAccept = false;
        
        String acceptCharset = variants.get(0).getCharset();
        boolean vAcceptCharset = false;
        
        String acceptLanguage = variants.get(0).getLanguage();
        boolean vAcceptLanguage = false;
        
        String acceptEncoding = variants.get(0).getEncoding();
        boolean vAcceptEncoding = false;

        for (Variant v : variants) {
            vAccept |= !vAccept && vary(v.getMediaType(), accept);
            vAcceptCharset |= !vAcceptCharset && vary(v.getCharset(), acceptCharset);
            vAcceptLanguage |= !vAcceptLanguage && vary(v.getLanguage(), acceptLanguage);
            vAcceptCharset |= !vAcceptEncoding && vary(v.getEncoding(), acceptEncoding);
        }
        
        StringBuilder vary = new StringBuilder();
        append(vary, vAccept, "Accept");
        append(vary, vAcceptCharset, "Accept-Charset");
        append(vary, vAcceptLanguage, "Accept-Language");
        append(vary, vAcceptEncoding, "Accept-Encoding");
        
        if (vary.length() > 0)
            header("Vary", vary.toString());
        return this;
    }
        
    private boolean vary(MediaType v, MediaType vary) {
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
    
    public Response.Builder language(String language) {
        set(CONTENT_LANGUAGE, language);
        return this;
    }

    public Response.Builder location(URI location) {
        set(LOCATION, location);
        return this;
    }

    public Response.Builder contentLocation(URI location) {
        set(CONTENT_LOCATION, location);
        return this;
    }

    public Response.Builder tag(EntityTag tag) {
        set(ETAG, tag);
        return this;
    }

    public Response.Builder tag(String tag) {
        set(ETAG, tag);
        return this;
    }

    public Response.Builder lastModified(Date lastModified) {
        set(LAST_MODIFIED, lastModified);
        return this;
    }

    public Response.Builder cacheControl(CacheControl cacheControl) {
        set(CACHE_CONTROL, cacheControl);
        return this;
    }

    public Response.Builder cookie(NewCookie cookie) {
        add("Set-Cookie", cookie);
        return this;
    }
    
    public Response.Builder header(String name, Object value) {
        Integer id = HEADER_MAP.get(name);
        if (id != null)
            set(id, value);
        else 
            add(name, value);
        return this;
    }

    
    private void add(String name, Object value) {
        if (nameValuePairs == null)
            nameValuePairs = new ArrayList<Object>();

        nameValuePairs.add(name);
        nameValuePairs.add(value);
    }
    
    private void set(int id, Object value) {
        if (values == null)
            values = new Object[HEADER_MAP.size()];
        
        values[id] = value;
    }
}
