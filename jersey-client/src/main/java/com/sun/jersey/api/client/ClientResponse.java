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
package com.sun.jersey.api.client;

import java.io.InputStream;
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
import javax.ws.rs.core.Response;
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
     * Get a list of response properties.
     * 
     * @return the list of response properties.
     */
    public abstract Map<String, Object> getProperties();
    
    /**
     * Get the status code.
     * 
     * @return the status code.
     */
    public abstract int getStatus();
    
    /**
     * Set the status code.
     * 
     * @param status the status code.
     */
    public abstract void setStatus(int status);

    /**
     * Get the status code.
     * 
     * @return the status code, or null if the underlying status code was set
     *         using the method {@link #setStatus(int)} and there is no
     *         mapping between the the integer value and the Response.Status
     *         enumeration value.
     */
    public abstract Response.Status getResponseStatus();
    
    /**
     * Set the status code.
     * 
     * @param status the status code.
     */
    public abstract void setResponseStatus(Response.Status status);
    
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
     * Get the input stream of the response.
     * 
     * @return the input stream of the response, otherwise null if
     *         no entity is present.
     */
    public abstract InputStream getEntityInputStream();

    /**
     * Set the input stream of the response.
     * 
     * @param in the input stream of the response.
     */
    public abstract void setEntityInputStream(InputStream in);

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
     * Get the entity of the response.
     * 
     * @param gt the generic type of the entity.
     * @return an instance of the type represented by the generic type.
     * 
     * @throws java.lang.IllegalArgumentException
     */
    public abstract <T> T getEntity(GenericType<T> gt) throws IllegalArgumentException;

    /**
     * Get the media type of the response
     * 
     * @return the media type.
     */
    public MediaType getType() {
        String ct = getMetadata().getFirst("Content-Type");
        return (ct != null) ? MediaType.valueOf(ct) : null;
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
    
    /**
     * Get the list of cookies.
     * 
     * @return the cookies.
     */
    public List<NewCookie> getCookies() {
        List<String> hs = getMetadata().get("Set-Cookie");
        if (hs == null) return Collections.emptyList();
        
        List<NewCookie> cs = new ArrayList<NewCookie>();
        for (String h : hs) {
            cs.add(NewCookie.valueOf(h));
        }
        return cs;
    }
}