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

import javax.ws.rs.core.MediaType;
import com.sun.ws.rest.impl.http.header.AcceptMediaType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A {@link ArrayList} of {@link ResourceMethod}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceMethodList extends ArrayList<ResourceMethod> {
    
    public enum MatchStatus {
        MATCH, NO_MATCH_FOR_CONSUME, NO_MATCH_FOR_PRODUCE
    }
    
    /**
     * Find the subset of methods that match the 'Content-Type' and 'Accept'.
     *
     * @param contentType The 'Content-Type'.
     * @param accept The 'Accept' as a list. This list
     *        MUST be ordered with the highest quality acceptable Media type occuring first
     *        (see {@link com.sun.ws.rest.impl.model.MimeHelper#ACCEPT_MEDIA_TYPE_COMPARATOR}).
     * @param matches The list to add the matches to.
     * @return The match status.
     */
    public MatchStatus match(MediaType contentType, 
            List<MediaType> accept,
            LinkedList<ResourceMethod> matches) {
                
        if (contentType != null) {
            // Find all methods that consume the MIME type of 'Content-Type'
            for (ResourceMethod method : this)
                if (method.consumes(contentType))
                    matches.add(method);
            
            if (matches.isEmpty())
                return MatchStatus.NO_MATCH_FOR_CONSUME;
            
        } else {
            matches.addAll(this);
        }

        // Find all methods that produce the one or more Media types of 'Accept'
        ListIterator<ResourceMethod> i = matches.listIterator();
        int currentQuality = AcceptMediaType.MINUMUM_QUALITY;
        int currentIndex = 0;
        while(i.hasNext()) {
            int index = i.nextIndex();
            int quality = i.next().produces(accept);
            
            if (quality == -1) {
                // No match
                i.remove();
            } else if (quality < currentQuality) {
                // Match but of a lower quality than a previous match
                i.remove();
            } else if (quality > currentQuality) {
                // Match and of a higher quality than the pervious match
                currentIndex = index;
                currentQuality = quality;
            }
        }

        if (matches.isEmpty())
            return MatchStatus.NO_MATCH_FOR_PRODUCE;

        // Remove all methods of a lower quality at the 
        // start of the list
        while (currentIndex > 0) {
            matches.removeFirst();
            currentIndex--;
        }
        
        return MatchStatus.MATCH;
    }

    /**
     * Determin if a the resource method list contains a method that 
     * has the same consume/produce media as another resource method.
     * @param method the resource method to check
     * @return true if the list contains a method with the same media as method.
     */
    public boolean containsMediaOfMethod(ResourceMethod method) {
        for (ResourceMethod m : this)
            if (method.mediaEquals(m))
                return true;
        
        return false;
    }
}
