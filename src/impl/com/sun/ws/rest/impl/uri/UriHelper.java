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

package com.sun.ws.rest.impl.uri;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public final class UriHelper {
    

    private static final String removeLeadingSlashesIfNeeded(String path, boolean preserveSlashes) {
        if (preserveSlashes) {
            return path;
        }
        // TODO: need some better alg
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
    
    // alg taken from http://gbiv.com/protocols/uri/rfc/rfc3986.html#relative-dot-segments
    // the alg works as follows:
    //       1. The input buffer is initialized with the now-appended path components and the output buffer is initialized to the empty string.
    //   2. While the input buffer is not empty, loop as follows:
    //         A. If the input buffer begins with a prefix of "../" or "./", then remove that prefix from the input buffer; otherwise,
    //         B. if the input buffer begins with a prefix of "/./" 
    //            or "/.", where "." is a complete path segment, then replace that prefix with "/" in the input buffer; otherwise,
    //         C. if the input buffer begins with a prefix of "/../" 
    //            or "/..", where ".." is a complete path segment, 
    //            then replace that prefix with "/" in the input buffer and remove the last segment and its preceding "/" (if any) from the output buffer; otherwise,
    //         D. if the input buffer consists only of "." or "..", then remove that from the input buffer; otherwise,
    //         E. move the first path segment in the input buffer to the end of the output buffer, 
    //            including the initial "/" character (if any) and any subsequent characters up to, but not including, 
    //            the next "/" character or the end of the input buffer.
    //   3. Finally, the output buffer is returned as the result of remove_dot_segments.
    public static String removeDotSegments(String path, boolean preserveContdSlashes) {
        
        if (null == path) {
            return null;
        }
        
        List<String> outputSegments = new LinkedList<String>();
        
        while (path.length() > 0) {
            if (path.startsWith("../")) {   // rule 2A
                path = removeLeadingSlashesIfNeeded(path.substring(3), preserveContdSlashes);
            } else if (path.startsWith("./")) { // rule 2A
                path = removeLeadingSlashesIfNeeded(path.substring(2), preserveContdSlashes);
            } else if (path.startsWith("/./")) { // rule 2B
                path = "/" + removeLeadingSlashesIfNeeded(path.substring(3), preserveContdSlashes);
            } else if ("/.".equals(path)) { // rule 2B
                path = "/";
            } else if (path.startsWith("/../")) { // rule 2C
                path = "/" + removeLeadingSlashesIfNeeded(path.substring(4), preserveContdSlashes);
                if(!outputSegments.isEmpty()) { // removing last segment if any
                    outputSegments.remove(outputSegments.size() - 1);
                }
            } else if ("/..".equals(path)) { // rule 2C
                path = "/";
                if(!outputSegments.isEmpty()) { // removing last segment if any
                    outputSegments.remove(outputSegments.size() - 1);
                }
            } else if ("..".equals(path) || ".".equals(path)) { // rule 2D
                path = "";
            } else { // rule E
                int slashStartSearchIndex;
                if (path.startsWith("/")) {
                    path = "/" + removeLeadingSlashesIfNeeded(path.substring(1), preserveContdSlashes);
                    slashStartSearchIndex = 1;
                } else {
                    slashStartSearchIndex = 0;
                }
                int segLength = path.indexOf('/', slashStartSearchIndex);
                if (-1 == segLength) {
                    segLength = path.length();
                }
                outputSegments.add(path.substring(0, segLength));
                path = path.substring(segLength);
            }
        }
        
        StringBuffer result = new StringBuffer();
        for (String segment : outputSegments) {
            result.append(segment);
        }
        
        return result.toString();
    }
    
    
    public static URI normalize(URI u, boolean preserveContdSlashes) {
        
        if (!u.getRawPath().contains("//")) {
            return u.normalize();
        }
        
        String np = removeDotSegments(u.getRawPath(), preserveContdSlashes);

        if (np.equals(u.getRawPath())) {
            return u;
        }
        
        UriBuilder ub = UriBuilder.fromUri(u);
        return ub.encode(false).replacePath(np).build();
    }
}
