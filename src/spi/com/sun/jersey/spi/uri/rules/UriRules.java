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

package com.sun.jersey.spi.uri.rules;

import java.util.Iterator;
import java.util.List;

/**
 * A collection of URI rules that can be matched using associated patterns.
 * <p>
 * The precedence of the rules and the type of patterns is specified by an 
 * implementation of this interface.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface UriRules<R> {
    /**
     * Match a URI path to the collection of rules and iterate over
     * the matching rules.
     *
     * @param path the URI path to be matched
     * @param capturingGroupValues the list to store the values of a pattern's 
     *        capturing groups. This list will be cleared and modified each time 
     *        {@link Iterator#next} is called according to the pattern 
     *        associated with the returned rule. The values are stored in
     *        the same order as the pattern's capturing groups.
     * @return an iterator of matching rules
     */
    Iterator<R> match(CharSequence path, List<String> capturingGroupValues);
}