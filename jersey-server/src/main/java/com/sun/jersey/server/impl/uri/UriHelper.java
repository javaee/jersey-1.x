/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.server.impl.uri;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * URI helper.
 *
 * @author Jakub Podlesak (japod at sun dot com)
 * @author Yegor Bugayenko (yegor256@java.net)
 */
public final class UriHelper {

    /**
     * Normalize the URI provided and return the normalized
     * copy.
     * @param uri The URI to normalize
     * @param preserveContdSlashes Shall we preserve "///" slashes
     * @return New normalized URI
     */
    public static URI normalize(URI uri, boolean preserveContdSlashes) {
        if (!uri.getRawPath().contains("//")) {
            return uri.normalize();
        }
        String np = UriHelper.removeDotSegments(
            uri.getRawPath(),
            preserveContdSlashes
        );
        if (np.equals(uri.getRawPath())) {
            return uri;
        }
        return UriBuilder.fromUri(uri).replacePath(np).build();
    }

    /**
     * Removal of leading slashes from the path.
     * @param path The path
     * @param preserveSlashes Shall we do anything?
     * @return Path without any leading slashes
     * @todo A better algorithm is required. Maybe we can use Apache StringUtils?
     * @see #normalize(URI, boolean)
     */
    private static String removeLeadingSlashesIfNeeded(
        final String path, final boolean preserveSlashes) {
        if (preserveSlashes) {
            return path;
        }
        String trimmed = path;
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed;
    }

    /**
     * Remove dots from path.
     *
     * alg taken from http://gbiv.com/protocols/uri/rfc/rfc3986.html#relative-dot-segments
     * the alg works as follows:
     *       1. The input buffer is initialized with the now-appended path components and the output buffer is initialized to the empty string.
     *   2. While the input buffer is not empty, loop as follows:
     *         A. If the input buffer begins with a prefix of "../" or "./", then remove that prefix from the input buffer; otherwise,
     *         B. if the input buffer begins with a prefix of "/./"
     *            or "/.", where "." is a complete path segment, then replace that prefix with "/" in the input buffer; otherwise,
     *         C. if the input buffer begins with a prefix of "/../"
     *            or "/..", where ".." is a complete path segment,
     *            then replace that prefix with "/" in the input buffer and remove the last segment and its preceding "/" (if any) from the output buffer; otherwise,
     *         D. if the input buffer consists only of "." or "..", then remove that from the input buffer; otherwise,
     *         E. move the first path segment in the input buffer to the end of the output buffer,
     *            including the initial "/" character (if any) and any subsequent characters up to, but not including,
     *            the next "/" character or the end of the input buffer.
     *   3. Finally, the output buffer is returned as the result of remove_dot_segments.
     *
     * @param path Path provided
     * @param preserveContdSlashes Shall we preserve "///" slashes
     * @return New path
     */
    public static String removeDotSegments(String path, boolean preserveContdSlashes) {
        if (null == path) {
            return null;
        }

        final List<String> outputSegments = new LinkedList<String>();
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

        final StringBuffer result = new StringBuffer();
        for (String segment : outputSegments) {
            result.append(segment);
        }
        return result.toString();
    }

}
