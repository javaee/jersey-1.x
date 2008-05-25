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

package com.sun.jersey.impl.model.method;

import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.impl.http.header.AcceptableMediaType;
import com.sun.jersey.impl.model.MediaTypeHelper;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceMethod {
    static private final List<String> EMPTY_LIST = Collections.emptyList();
    
    /**
     * Comparator for resource methods, comparing the consumed and produced
     * media types.
     * <p>
     * Defer to {@link MediaTypeHelper#MEDIA_TYPE_LIST_COMPARATOR} for comparing
     * the list of media type that are comsumed and produced. The comparison of 
     * consumed media take precedence over the comparison of produced 
     * media.
     */
    static public final Comparator<ResourceMethod> COMPARATOR = 
            new Comparator<ResourceMethod>() {
        public int compare(ResourceMethod o1, ResourceMethod o2) {
            int i = MediaTypeHelper.MEDIA_TYPE_LIST_COMPARATOR.
                    compare(o1.consumeMime, o2.consumeMime);
            if (i == 0)
                i = MediaTypeHelper.MEDIA_TYPE_LIST_COMPARATOR.
                        compare(o1.produceMime, o2.produceMime);
            
            return i;
        }
    };
    
    private final String httpMethod;
    
    private final UriTemplate template;
    
    private final List<MediaType> consumeMime;
    
    private final List<MediaType> produceMime;
        
    private final RequestDispatcher dispatcher;
    
    public ResourceMethod(String httpMethod,
            UriTemplate template,
            List<MediaType> consumeMime, 
            List<MediaType> produceMime,
            RequestDispatcher dispatcher) {
        this.httpMethod = httpMethod;
        this.template = template;
        this.consumeMime = consumeMime;
        this.produceMime = produceMime;
        this.dispatcher = dispatcher;
    }

    public final String getHttpMethod() {
        return httpMethod;
    }
    
    public final UriTemplate getTemplate() {
        return template;
    }
    
    public final List<MediaType> getConsumeMime() {
        return produceMime;
    }
    
    public final List<MediaType> getProduceMime() {
        return produceMime;
    }
    
    public final RequestDispatcher getDispatcher() {
        return dispatcher;
    }

     /**
     * Ascertain if the method is capable of consuming an entity of a certain 
     * media type.
     *
     * @param contentType the media type of the entity that is to be consumed.
     * @return true if the method is capable of consuming the entity,
      *        otherwise false.
     */
    public final boolean consumes(MediaType contentType) {
        for (MediaType c : consumeMime) {
            if (c.getType().equals("*")) return true;
            
            if (contentType.isCompatible(c)) return true;
        }
        
        return false;
    }
        
    /**
     * Ascertain if the method is capable of producing an entity of a specific 
     * media type.
     *
     * @param contentType the media type.
     * @return true if the media type can be produced, otherwise false.
     */
    public final boolean produces(MediaType contentType) {
        for (MediaType c : produceMime) {
            if (c.getType().equals("*")) return true;
            
            if (c.isCompatible(contentType)) return true;
        }
        
        return false;
    }
    
    /**
     * Ascertain if the method is capable of producing one or more entities 
     * from a list of media types.
     *
     * @param accept The list of media types of entities that may be produced. 
     *        This list MUST be ordered with the highest quality acceptable
     *        media type occuring first 
     *        (see {@link MediaTypeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     * @return the quality of the first acceptable media type in the accept 
     *         list, otherwise -1 if no media types are acceptable.
     */
    public final int produces(List accept) {
        Iterator i = accept.iterator();
        while (i.hasNext()) {
            AcceptableMediaType a = (AcceptableMediaType)i.next();
            if (a.getType().equals("*")) return a.getQuality();
        
            for (MediaType c : produceMime) {
                if (c.getType().equals("*")) return a.getQuality();
                
                if (c.isCompatible(a)) return a.getQuality();
            }            
        }
        return -1;
    }
    
    public final boolean mediaEquals(ResourceMethod that) {
        boolean v = consumeMime.equals(that.consumeMime);
        if (v == false)
            return false;
        
        return produceMime.equals(that.produceMime);
    }
}