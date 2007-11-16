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

package com.sun.ws.rest.spi.uri.rules;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An ordered collections of URI rules that can be matched.
 * <p>
 * Each rule is associated with a pattern to be matched. The order of the
 * rules is specified by an implementation of this interface.
 * <p>
 * The collection of rules can be matched, using the patterns associated with
 * rules, against a URI path.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface UriRules<P, R> {
    /**
     * Add a pattern to collection of existing patterns, and associate a 
     * rule with that pattern.
     * 
     * @param pattern the pattern to be matched.
     * @param rule the rule associated with the pattern.
     */
    void add(P pattern, R rule);
    
    /**
     * @return the collection of rules.
     */
    Collection<R> getRules();
    
    /**
     * Match a URI path to the collection of patterns and iterate over
     * the rules associated with the matching patterns.
     *
     * @param path the URI path to be matched
     * @param capturingGroupValues the list to store the values of a pattern's 
     *        capturing groups. This list will be modified each time 
     *        {@link Iterator#next} is called according to the pattern 
     *        associated with the returned rule. The values are stored in
     *        the same order as the pattern's capturing groups.
     * @return an iterator of matching rules
     */
    Iterator<R> match(CharSequence path, List<String> capturingGroupValues);
}