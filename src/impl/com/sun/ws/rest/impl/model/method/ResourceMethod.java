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

package com.sun.ws.rest.impl.model.method;

import com.sun.ws.rest.spi.dispatch.RequestDispatcher;
import javax.ws.rs.core.MediaType;
import com.sun.ws.rest.impl.http.header.AcceptMediaType;
import com.sun.ws.rest.impl.model.MediaTypeList;
import com.sun.ws.rest.impl.model.MimeHelper;
import com.sun.ws.rest.impl.model.ResourceClass;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceMethod {
    /**
     * Comparator for resource methods.
     * <p>
     * Annotated methods < WebResource.handleRequest.
     * <p>
     * Defer to {@link MimeHelper#MEDIA_TYPE_LIST_COMPARATOR} for comparing
     * the list of MIME type that are comsumed and produced. The comparison of consumed
     * MIME type take precedence over the comparison of produced MIME type.
     */
    static public final Comparator<ResourceMethod> COMPARATOR = new Comparator<ResourceMethod>() {
        public int compare(ResourceMethod o1, ResourceMethod o2) {
            // Annotated methods < WebResource.handleRequest
            if (o1.httpMethod != null && o2.httpMethod == null) 
                return -1;
            if (o1.httpMethod == null && o2.httpMethod != null) 
                return 1;
            
            int i = MimeHelper.MEDIA_TYPE_LIST_COMPARATOR.compare(o1.consumeMime, o2.consumeMime);
            if (i == 0)
                i = MimeHelper.MEDIA_TYPE_LIST_COMPARATOR.compare(o1.produceMime, o2.produceMime);
            
            return i;
        }
    };
    
    protected final ResourceClass resourceClass;
    
    protected String httpMethod;
    
    protected MediaTypeList consumeMime;
    
    protected MediaTypeList produceMime;
        
    protected ResourceMethod(ResourceClass resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Class<?> getResourceClass() {
        return resourceClass.resource.getResourceClass();
    }
    
    public abstract Method getMethod();
    
    public abstract RequestDispatcher getDispatcher();
        
    
     /**
     * Ascertain if the method is capable of consuming an entity of a certain 
     * Media type.
     *
     * @param contentType the Media type of the entity that is to be consumed.
     * @return true, if the method is capable of consuming the entity of the contentType.
     */
    public boolean consumes(MediaType contentType) {
        for (MediaType c : consumeMime) {
            if (c.getType().equals("*")) return true;
            
            if (contentType.isCompatible(c)) return true;
        }
        
        return false;
    }
        
    /**
     * Ascertain if the method is capable of producing an entity of a specific 
     * Media type.
     *
     * @param contentType The Media type.
     * @return true if the Media type can be produced, otherwise false.
     */
    public boolean produces(MediaType contentType) {
        for (MediaType c : produceMime) {
            if (c.getType().equals("*")) return true;
            
            if (c.isCompatible(contentType)) return true;
        }
        
        return false;
    }
    
    /**
     * Ascertain if the method is capable of producing one or more entities from a list
     * of Media types.
     *
     * @param accept The list of Media types of entities that may be produced. This list
     *        MUST be ordered with the highest quality acceptable MIME type occuring first
     *        (see {@link MimeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     * @return the quality of the first acceptable Media type in the accept list, 
     *         otherwise -1 if no Media types are acceptable.
     */
    public int produces(List accept) {
        Iterator i = accept.iterator();
        while (i.hasNext()) {
            AcceptMediaType a = (AcceptMediaType)i.next();
            if (a.getType().equals("*")) return a.getQ();
        
            for (MediaType c : produceMime) {
                if (c.getType().equals("*")) return a.getQ();
                
                if (c.isCompatible(a)) return a.getQ();
            }            
        }
        return -1;
    }
    
    public boolean mediaEquals(ResourceMethod that) {
        boolean v = consumeMime.equals(that.consumeMime);
        if (v == false)
            return false;
        
        return produceMime.equals(that.produceMime);
    }
}