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

package com.sun.ws.rest.impl.model;

import com.sun.ws.rest.impl.http.header.AcceptMediaType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * List of {@link MediaType}.
 *
 * TODO remove this class.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MediaTypeList extends ArrayList<MediaType> {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        
        if (o == null)
            return false;
        
        if (!(o instanceof MediaTypeList))
            return false;
        
        MediaTypeList that = (MediaTypeList)o;
        
        if (this.size() != that.size())
            return false;
        
        for (int i = 0; i < this.size(); i++)
            if (!this.get(i).equals(that.get(i)))
                return false;
            
         return true;
    }
    
    /**
     * Get a media type given a list of acceptable media type.
     *
     * @param accept the list of acceptable media type. The list MUST be ordered
     * according to the quality parameter of the media type, the highest quality
     * media type occuring first.
     * @return the acceptable media type
     */
    public MediaType getAcceptableMediaType(List<MediaType> accept) {
        Iterator<MediaType> i = accept.iterator();
        while (i.hasNext()) {
            MediaType a = i.next();
            if (a.getType().equals(MediaType.MEDIA_TYPE_WILDCARD)) 
                return get(0);
        
            for (MediaType m : this)
                if (m.isCompatible(a)) return m;
        }
        return null;
    }
}