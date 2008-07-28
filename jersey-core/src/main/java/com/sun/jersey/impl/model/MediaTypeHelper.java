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

package com.sun.jersey.impl.model;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.sun.jersey.impl.http.header.AcceptableMediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Helper claases for media types.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MediaTypeHelper {
    /**
     * Comparator for media types.
     * <p>
     * x/y < x/* < *\\/*
     */
    static public final Comparator<MediaType> MEDIA_TYPE_COMPARATOR = new Comparator<MediaType>() {
        public int compare(MediaType o1, MediaType o2) {
            if (o1.getType().equals("*") && !o2.getType().equals("*")) {
                return 1;
            }
            
            if (o2.getType().equals("*") && !o1.getType().equals("*")) {
                return -1;
            }
            
            if (o1.getSubtype().equals("*") && !o2.getSubtype().equals("*")) {
                return 1;
            }
            
            if (o2.getSubtype().equals("*") && !o1.getSubtype().equals("*")) {
                return -1;
            }
            
            return 0;
        }
    };
   
    /**
     * Comparator for lists of media types.
     * <p>
     * The least specific content type of each list is obtained and then compared
     * using {@link #MEDIA_TYPE_COMPARATOR}.
     * <p>
     * Assumes each list is already ordered according to {@link #MEDIA_TYPE_COMPARATOR}
     * and therefore the least specific media type is at the end of the list.
     */
    static public final Comparator<List<MediaType>> MEDIA_TYPE_LIST_COMPARATOR = 
            new Comparator<List<MediaType>>() {
        public int compare(List<MediaType> o1, List<MediaType> o2) {
            return MEDIA_TYPE_COMPARATOR.compare(getLeastSpecific(o1), getLeastSpecific(o2));
        }
        
        public MediaType getLeastSpecific(List<MediaType> l) {
            return l.get(l.size() - 1);
        }
    };
        
    
    public static final MediaType GENERAL_MEDIA_TYPE = new MediaType("*", "*");
    
    public static final List<MediaType> GENERAL_MEDIA_TYPE_LIST = 
            createMediaTypeList();
    
    private static List<MediaType> createMediaTypeList() {
        List<MediaType> l = new ArrayList<MediaType>();
        l.add(GENERAL_MEDIA_TYPE);
        return l;
    }
                
    
    public static final AcceptableMediaType GENERAL_ACCEPT_MEDIA_TYPE = 
            new AcceptableMediaType("*", "*");
    
    public static final List<AcceptableMediaType> GENERAL_ACCEPT_MEDIA_TYPE_LIST = 
            createAcceptMediaTypeList();
    
    private static List<AcceptableMediaType> createAcceptMediaTypeList() {
        List<AcceptableMediaType> l = new ArrayList<AcceptableMediaType>();        
        l.add(GENERAL_ACCEPT_MEDIA_TYPE);
        return l;
    }    
    
    /**
     * Create a list of content type from the Consumes annotation.
     * <p>
     * @param mime the Consumes annotation.
     * @return the list of MediaType, ordered according to {@link #MEDIA_TYPE_COMPARATOR}.
     */
    public static List<MediaType> createMediaTypes(Consumes mime) {
        if (mime == null) {
            return GENERAL_MEDIA_TYPE_LIST;
        }
        
        return createMediaTypes(mime.value());
    }
        
    /**
     * Create a list of content type from the Produces annotation.
     * <p>
     * @param mime the Produces annotation.
     * @return the list of MediaType, ordered according to {@link #MEDIA_TYPE_COMPARATOR}.
     */
    public static List<MediaType> createMediaTypes(Produces mime) {
        if (mime == null) {
            return GENERAL_MEDIA_TYPE_LIST;
        }
        
        return createMediaTypes(mime.value());
    }
    
    /**
     * Create a list of content type from string array items that are MIME types.
     * <p>
     * @param mediaTypes the array of MIME types.
     * @return the list of MediaType, ordered according to {@link #MEDIA_TYPE_COMPARATOR}.
     */
    public static List<MediaType> createMediaTypes(String[] mediaTypes) {
        List<MediaType> l = new ArrayList<MediaType>();
        for (String mediaType : mediaTypes) {
            l.add(MediaType.valueOf(mediaType));
        }
        
        Collections.sort(l, MEDIA_TYPE_COMPARATOR);
        return l;
    }
}
