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

package com.sun.jersey.impl.uri;

import com.sun.jersey.api.uri.UriPattern;
import com.sun.jersey.api.uri.UriTemplate;
import java.util.Comparator;

/**
 * A URI pattern that is a regular expression generated from a URI path.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class PathPattern extends UriPattern {
    public static final PathPattern EMPTY_PATH = new PathPattern();
    
    /**
     * Order the templates according to the string comparison of the
     * template regular expressions.
     */
    static public final Comparator<PathPattern> COMPARATOR = new Comparator<PathPattern>() {
        public int compare(PathPattern o1, PathPattern o2) {
            // TODO define order of limited=true and limited=false
            return UriTemplate.COMPARATOR.compare(o1.template, o2.template);
        }
    };

    /**
     * The regular expression that represents the right hand side of
     * a URI path.
     */
    private static final String LIMITED = "(/.*)?";
        
    /**
     * The regular expression that represents the right hand side of
     * a URI path that is a '/' or null.
     */
    private static final String UNLIMITED = "(/)?";

    private final UriTemplate template;
    
    private PathPattern() {
        super("");
        this.template = UriTemplate.EMPTY;
    }
    
    /**
     * Create a path pattern from a regular expression.
     * 
     * @param regex the regular expression for the path.
     * @param limited if true than the capturing group '(/.*)?' is appended to
     *        the regex (see above), otherwise '(/)?' is appended.
     */
    public PathPattern(UriTemplate template, boolean limited) {
        super(postfixWithCapturingGroup(template.getPattern().getRegex(), 
                limited));
        
        this.template = template;
    }
    
    public UriTemplate getTemplate() {
        return template;
    }
    
    private static String postfixWithCapturingGroup(String regex, boolean limited) {
        if (regex.endsWith("/"))
            regex = regex.substring(0, regex.length() - 1);
            
        return regex + ((limited) ? LIMITED : UNLIMITED);
    }
}