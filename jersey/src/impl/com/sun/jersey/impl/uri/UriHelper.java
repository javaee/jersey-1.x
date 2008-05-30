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

package com.sun.jersey.impl.uri;

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
