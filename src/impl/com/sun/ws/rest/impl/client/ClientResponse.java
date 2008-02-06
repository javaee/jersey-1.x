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

package com.sun.ws.rest.impl.client;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ClientResponse {
    protected static final HeaderDelegate<EntityTag> entityTagDelegate = 
            RuntimeDelegate.getInstance().createHeaderDelegate(EntityTag.class);
    
    protected static final HeaderDelegate<Date> dateDelegate = 
            RuntimeDelegate.getInstance().createHeaderDelegate(Date.class);
        
    public abstract int getStatus();
    
    public abstract MultivaluedMap<String, String> getMetadata();
    
    public abstract boolean hasEntity();
    
    public abstract <T> T getEntity(Class<T> c) throws IllegalArgumentException;
        
    public abstract Map<String, Object> getProperties();
    /*
    {
        if (properties != null) return properties;
        
        return properties = new HashMap<String, Object>();
    }
    */
    
    public MediaType getContentType() {
        String ct = getMetadata().getFirst("Content-Type");
        return (ct != null) ? MediaType.parse(ct) : null;
    }
    
    public URI getLocation() {
        String l = getMetadata().getFirst("Location");        
        return (l != null) ? URI.create(l) : null;
    }
    
    public EntityTag getEntityTag() {
        String t = getMetadata().getFirst("ETag");
        
        return (t != null) ? entityTagDelegate.fromString(t) : null;
    }
    
    public Date getLastModified() {
        String d = getMetadata().getFirst("Last-Modified");
        
        return (d != null) ? dateDelegate.fromString(d) : null;
    }

    public String getLangauge() {
        return getMetadata().getFirst("Content-Language");
    }
}