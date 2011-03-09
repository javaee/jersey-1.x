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

import com.sun.jersey.api.uri.UriPattern;
import com.sun.jersey.api.uri.UriTemplate;
import java.util.Comparator;

/**
 * A URI pattern that is a regular expression generated from a URI path.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class PathPattern extends UriPattern {

    /**
     * Empty path.
     * @see #PathPattern()
     */
    public static final PathPattern EMPTY_PATH = new PathPattern();

    /**
     * The regular expression that represents the right hand side of
     * a URI path.
     */
    private static final String RIGHT_HAND_SIDE = "(/.*)?";

    /**
     * Defer to comparing the templates associated with the patterns.
     */
    static public final Comparator<PathPattern> COMPARATOR = new Comparator<PathPattern>() {
        public int compare(final PathPattern o1, final PathPattern o2) {
            return UriTemplate.COMPARATOR.compare(o1.template, o2.template);
        }
    };

    /**
     * Empty UriTemplate, initialized just once in one of
     * constructors.
     * @see #PathPattern()
     * @see #PathPattern(UriTemplate)
     * @see #PathPattern(UriTemplate, String)
     */
    private final UriTemplate template;

    /**
     * Private constructor, used only to instantiate EMPTY_PATH.
     */
    private PathPattern() {
        super();
        this.template = UriTemplate.EMPTY;
    }

    /**
     * Public constructor.
     * @param template The template
     */
    public PathPattern(final UriTemplate template) {
        super(
            PathPattern.postfixWithCapturingGroup(template.getPattern().getRegex()),
            PathPattern.indexCapturingGroup(template.getPattern().getGroupIndexes())
        );
        this.template = template;
    }

    /**
     * Public ctor.
     * @param template The template
     * @param rightHandSide Right hand side of the template
     */
    public PathPattern(final UriTemplate template, final String rightHandSide) {
        super(
            PathPattern.postfixWithCapturingGroup(
                template.getPattern().getRegex(),
                rightHandSide
            ),
            PathPattern.indexCapturingGroup(template.getPattern().getGroupIndexes())
        );
        this.template = template;
    }

    /**
     * Get template.
     * @return The template
     */
    public UriTemplate getTemplate() {
        return this.template;
    }

    /**
     * Append tail-matching regex suffix to the regex provided.
     * @param regex Regular expression
     * @return The regex with the default suffix
     * @see #PathPattern(UriTemplate)
     */
    private static String postfixWithCapturingGroup(final String regex) {
        return PathPattern.postfixWithCapturingGroup(
            regex,
            PathPattern.RIGHT_HAND_SIDE
        );
    }

    /**
     * Append tail-matching regex suffix to the regex provided.
     * @param regex Regular expression
     * @param rightHandSide The suffix to append
     * @return The regex with the provided suffix
     * @see #PathPattern(UriTemplate, String)
     */
    private static String postfixWithCapturingGroup(final String regex, final String rightHandSide) {
        return (regex.endsWith("/") ? regex.substring(0, regex.length() - 1) : regex)
            + rightHandSide;
    }

    /**
     * Extend the list of group indexes by adding one more element
     * the end of array, which is equal to last element plus 1.
     * @param indexes Array of indexes
     * @return New array
     * @see #PathPattern(UriTemplate)
     * @see #PathPattern(UriTemplate, String)
     */
    private static int[] indexCapturingGroup(final int[] indexes) {
        if (indexes.length == 0) {
            return indexes;
        }
        final int[] cgIndexes = new int[indexes.length + 1];
        System.arraycopy(indexes, 0, cgIndexes, 0, indexes.length);
        cgIndexes[indexes.length] = cgIndexes[indexes.length - 1] + 1;
        return cgIndexes;
    }

}
