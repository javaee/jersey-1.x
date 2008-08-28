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

package com.sun.jersey.impl;

import com.sun.jersey.impl.util.KeyComparatorHashMap;
import com.sun.jersey.impl.util.StringIgnoreCaseKeyComparator;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResponseBuilderImpl extends Response.ResponseBuilder {    
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
    
    private Type entityType;
    
    private Object[] values;
    
    private List<Object> nameValuePairs;

    public ResponseBuilderImpl() { }
        
    private ResponseBuilderImpl(ResponseBuilderImpl that) {
        this.status = that.status;
        this.entity = that.entity;
        this.entityType = that.entityType;
        if (that.values != null)
            this.values = that.values.clone();
        if (that.nameValuePairs != null)
            this.nameValuePairs = new ArrayList<Object>(that.nameValuePairs);    
    }

    public Response.ResponseBuilder entityWithType(Object entity, Type entityType) {
        this.entity = entity;
        this.entityType = entityType;
        return this;
    }
   
    // Response.Builder
    
    public Response build() {
        Response r = new ResponseImpl(status, entity, entityType,
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

    @Override
    public ResponseBuilder clone() {
        return new ResponseBuilderImpl(this);
    }
    
    public Response.ResponseBuilder status(int status) {
        this.status = status;
        return this;
    }

    public Response.ResponseBuilder entity(Object entity) {
        this.entity = entity;
        this.entityType = (entity != null) ? entity.getClass() : null;
        return this;
    }

    public Response.ResponseBuilder type(MediaType type) {
        set(CONTENT_TYPE, type);
        return this;
    }

    public Response.ResponseBuilder type(String type) {
        if (type != null)
            set(CONTENT_TYPE, MediaType.valueOf(type));
        else
            set(CONTENT_TYPE, null);            
        return this;
    }

    public Response.ResponseBuilder variant(Variant variant) {
        if (variant == null) {
            type((MediaType)null);
            language((String)null);
            header(HttpHeaders.CONTENT_ENCODING, null);
            return this;
        }
        
        type(variant.getMediaType());
        // TODO set charset
        language(variant.getLanguage());
        if (variant.getEncoding() != null)
            header(HttpHeaders.CONTENT_ENCODING, variant.getEncoding());
        
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
        set(CONTENT_LANGUAGE, language);
        return this;
    }
    
    public Response.ResponseBuilder language(Locale language) {
        if (language != null)
            set(CONTENT_LANGUAGE, language.toString());
        else
            set(CONTENT_LANGUAGE, null);
        return this;
    }

    public Response.ResponseBuilder location(URI location) {
        set(LOCATION, location);
        return this;
    }

    public Response.ResponseBuilder contentLocation(URI location) {
        set(CONTENT_LOCATION, location);
        return this;
    }

    public Response.ResponseBuilder tag(EntityTag tag) {
        set(ETAG, tag);
        return this;
    }

    public Response.ResponseBuilder tag(String tag) {
        if (tag != null)
            set(ETAG, new EntityTag(tag));
        else
            set(ETAG, null);
        return this;
    }

    public Response.ResponseBuilder lastModified(Date lastModified) {
        set(LAST_MODIFIED, lastModified);
        return this;
    }

    public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
        set(CACHE_CONTROL, cacheControl);
        return this;
    }

    public ResponseBuilder expires(Date expires) {
        add(HttpHeaders.EXPIRES, expires);
        return this;
    }
    
    public Response.ResponseBuilder cookie(NewCookie... cookies) {
        if (cookies != null) {
            for (NewCookie cookie : cookies)
                add(HttpHeaders.SET_COOKIE, cookie);
        } else {
            remove(HttpHeaders.SET_COOKIE);
        }
        return this;
    }
    
    public Response.ResponseBuilder header(String name, Object value) {
        Integer id = HEADER_MAP.get(name);
        if (id != null)
            set(id, value);
        else 
            add(name, value);
        return this;
    }

    
    private void add(String name, Object value) {
        if (value != null) {
            if (nameValuePairs == null)
                nameValuePairs = new LinkedList<Object>();
            
            nameValuePairs.add(name);
            nameValuePairs.add(value);
        } else {
            remove(name);
        }
    }
    
    private void remove(String name) {
        if (nameValuePairs == null) return;
        
        Iterator<Object> i = nameValuePairs.iterator();
        while(i.hasNext()) {
            if (i.next().toString().equalsIgnoreCase(name)) {
                i.remove();
                i.next();
                i.remove();
            } else {
                i.next();
            }
        }
    }
    
    private void set(int id, Object value) {
        if (values == null)
            values = new Object[HEADER_MAP.size()];
        
        values[id] = value;
    }
}
