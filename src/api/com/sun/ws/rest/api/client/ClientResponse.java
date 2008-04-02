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

package com.sun.ws.rest.api.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * A client (inbound) HTTP response.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ClientResponse {
    protected static final HeaderDelegate<EntityTag> entityTagDelegate = 
            RuntimeDelegate.getInstance().createHeaderDelegate(EntityTag.class);
    
    protected static final HeaderDelegate<Date> dateDelegate = 
            RuntimeDelegate.getInstance().createHeaderDelegate(Date.class);
        
    /**
     * Get the status code.
     * 
     * @return the status code.
     */
    public abstract int getStatus();
    
    /**
     * Get the HTTP metadata
     * 
     * @return the HTTP metadata.
     */
    public abstract MultivaluedMap<String, String> getMetadata();

    /**
     * 
     * @return true if there is an entity present in the response.
     */
    public abstract boolean hasEntity();

    /**
     * Get the entity of the response.
     * 
     * @param c the type of the entity.
     * @return an instance of the type <code>c</code>.
     * 
     * @throws java.lang.IllegalArgumentException
     */
    public abstract <T> T getEntity(Class<T> c) throws IllegalArgumentException;

    /**
     * Get a list of response properties.
     * 
     * @return the list of response properties.
     */
    public abstract Map<String, Object> getProperties();
    
    /**
     * Get the media type of the response
     * 
     * @return the media type.
     */
    public MediaType getType() {
        String ct = getMetadata().getFirst("Content-Type");
        return (ct != null) ? MediaType.parse(ct) : null;
    }
    
    /**
     * Get the location.
     * 
     * @return the location.
     */
    public URI getLocation() {
        String l = getMetadata().getFirst("Location");        
        return (l != null) ? URI.create(l) : null;
    }
    
    /**
     * Get the entity tag.
     * 
     * @return the entity tag.
     */
    public EntityTag getEntityTag() {
        String t = getMetadata().getFirst("ETag");
        
        return (t != null) ? entityTagDelegate.fromString(t) : null;
    }

    /**
     * Get the last modified date.
     * 
     * @return the last modified date.
     */
    public Date getLastModified() {
        String d = getMetadata().getFirst("Last-Modified");
        
        return (d != null) ? dateDelegate.fromString(d) : null;
    }

    /**
     * Get the language.
     * 
     * @return the language.
     */
    public String getLanguage() {
        return getMetadata().getFirst("Content-Language");
    }
    
    public List<NewCookie> getCookies() {
        List<String> hs = getMetadata().get("Set-Cookie");
        if (hs == null) return Collections.emptyList();
        
        List<NewCookie> cs = new ArrayList<NewCookie>();
        for (String h : hs) {
            cs.add(NewCookie.parse(h));
        }
        return cs;
    }
}