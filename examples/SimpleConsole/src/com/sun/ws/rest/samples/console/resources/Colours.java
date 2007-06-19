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

package com.sun.ws.rest.samples.console.resources;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriTemplate;

/**
 * A web resource for a list of colours.
 */
@UriTemplate("colours")
public class Colours {
    
    private static String colours[] = {"red","orange","yellow","green","blue","indigo","violet"};
    
    /**
     * Returns a list of colours as plain text, one colour per line.
     * @param filter If not empty, constrains the list of colours to only
     * those that contain this substring
     * @return the list of colours matching the filter
     */
    @HttpMethod
    @ProduceMime("text/plain")
    public String getColourListAsText(@QueryParam("match") String filter) {
        StringBuffer buf = new StringBuffer();
        for (String colour: getMatchingColours(filter)) {
            buf.append(colour);
            buf.append('\n');
        }
        return buf.toString();
    }
    
    /**
     * Returns a list of colours as a JSON array.
     * @param filter If not empty, constrains the list of colours to only 
     * those that contain this substring
     * @return the list of colours matching the filter
     */
    @HttpMethod
    @ProduceMime("application/json")
    public String getColourListAsJSON(@QueryParam("match") String filter) {
        StringBuffer buf = new StringBuffer();
        buf.append('[');
        boolean first = true;
        for (String colour: getMatchingColours(filter)) {
            if (!first)
                buf.append(',');
            buf.append('\'');
            buf.append(colour);
            buf.append('\'');
            first = false;
        }
        buf.append(']');
        return buf.toString();
    }
    
    /**
     * Returns a list of colours.
     * @param filter If not empty, constrains the list of colours to only
     * those that contain this substring
     * @return the list of colours matching the filter
     */
    public static List<String> getMatchingColours(String filter) {
        List<String> matches = new ArrayList<String>();

        for (String colour: colours) {
            if (filter==null || filter.length()==0 || colour.contains(filter)) {
                matches.add(colour);
            }
        }
        
        return matches;
    }
}
