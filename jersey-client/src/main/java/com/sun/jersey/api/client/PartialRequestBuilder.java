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

import com.sun.jersey.impl.container.OutBoundHeaders;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * A partial implementation of {@link RequestBuilder} that implements
 * the methods on {@link RequestBuilder} but leaves undefined the build 
 * methods for constructing the request.
 * 
 * @param T the type than implements {@link RequestBuilder}.
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public abstract class PartialRequestBuilder<T extends RequestBuilder> 
        implements RequestBuilder<T> {

    protected Object entity;
    
    protected MultivaluedMap<String, Object> metadata;
    
    protected PartialRequestBuilder() {
        metadata = new OutBoundHeaders();
    }
    
    public T entity(Object entity) {
        this.entity = entity;
        return (T)this;
    }

    public T entity(Object entity, MediaType type) {
        entity(entity);
        type(type);
        return (T)this;
    }

    public T entity(Object entity, String type) {
        entity(entity);
        type(type);
        return (T)this;
    }
    
    public T type(MediaType type) {
        getMetadata().putSingle("Content-Type", type);        
        return (T)this;
    }
        
    public T type(String type) {
        getMetadata().putSingle("Content-Type", type);        
        return (T)this;
    }
        
    public T accept(MediaType... types) {
        for (MediaType type : types)
            getMetadata().add("Accept", type);
        return (T)this;
    }
    
    public T accept(String... types) {
        for (String type : types)
            getMetadata().add("Accept", type);
        return (T)this;
    }
    
    public T cookie(Cookie cookie) {
        getMetadata().add("Cookie", cookie);
        return (T)this;
    }
    
    public T header(String name, Object value) {
        getMetadata().add(name, value);
        return (T)this;
    }
    
    private MultivaluedMap<String, Object> getMetadata() {
        if (metadata != null) return metadata;
        
        return metadata = new OutBoundHeaders();
    }
}