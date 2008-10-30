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

package com.sun.jersey.impl.model.method;

import com.sun.jersey.api.MediaTypes;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import java.util.Comparator;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResourceMethod {
    
    /**
     * Comparator for resource methods, comparing the consumed and produced
     * media types.
     * <p>
     * Defer to {@link MediaTypes#MEDIA_TYPE_LIST_COMPARATOR} for comparing
     * the list of media type that are comsumed and produced. The comparison of 
     * consumed media take precedence over the comparison of produced 
     * media.
     */
    static public final Comparator<ResourceMethod> COMPARATOR = 
            new Comparator<ResourceMethod>() {
        public int compare(ResourceMethod o1, ResourceMethod o2) {
            int i = MediaTypes.MEDIA_TYPE_LIST_COMPARATOR.
                    compare(o1.consumeMime, o2.consumeMime);
            if (i == 0)
                i = MediaTypes.MEDIA_TYPE_LIST_COMPARATOR.
                        compare(o1.produceMime, o2.produceMime);
            
            return i;
        }
    };
    
    private final String httpMethod;
    
    private final UriTemplate template;
    
    private final List<MediaType> consumeMime;
    
    private final List<MediaType> produceMime;

    private final boolean isProducesDeclared;

    private final RequestDispatcher dispatcher;
    
    public ResourceMethod(String httpMethod,
            UriTemplate template,
            List<MediaType> consumeMime, 
            List<MediaType> produceMime,
            boolean isProducesDeclared,
            RequestDispatcher dispatcher) {
        this.httpMethod = httpMethod;
        this.template = template;
        this.consumeMime = consumeMime;
        this.produceMime = produceMime;
        this.isProducesDeclared = isProducesDeclared;
        this.dispatcher = dispatcher;
    }

    public final String getHttpMethod() {
        return httpMethod;
    }
    
    public final UriTemplate getTemplate() {
        return template;
    }
    
    public final List<MediaType> getConsumes() {
        return produceMime;
    }
    
    public final List<MediaType> getProduces() {
        return produceMime;
    }

    public final boolean isProducesDeclared() {
        return isProducesDeclared;
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
        
    public final boolean mediaEquals(ResourceMethod that) {
        boolean v = consumeMime.equals(that.consumeMime);
        if (v == false)
            return false;
        
        return produceMime.equals(that.produceMime);
    }
}