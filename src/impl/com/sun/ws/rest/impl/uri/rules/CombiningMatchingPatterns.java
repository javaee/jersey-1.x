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
package com.sun.ws.rest.impl.uri.rules;

import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Rules combining one or more other rules and matching in sequence.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class CombiningMatchingPatterns<R> implements UriRules<R> {

    private final List<UriRules<R>> rs;
        
    public CombiningMatchingPatterns(List<UriRules<R>> rs) {
        this.rs = rs;
    }

    public Iterator<R> match(CharSequence path, List<String> capturingGroupValues) {
        return new XInterator(path, capturingGroupValues);
    }

    private final class XInterator implements Iterator<R> {

        private final CharSequence path;
        private final List<String> capturingGroupValues;
        private Iterator<R> ruleIterator;
        private Iterator<UriRules<R>> rulesIterator;
        private R r;

        XInterator(CharSequence path, List<String> capturingGroupValues) {
            this.path = path;
            this.capturingGroupValues = capturingGroupValues;
            this.rulesIterator = rs.iterator();
            this.ruleIterator = rulesIterator.next().match(path, capturingGroupValues);
        }
        
        public boolean hasNext() {
            if (r != null) return true;

            if (ruleIterator.hasNext()) {
                r = ruleIterator.next();
                return true;
            }
            
            while (rulesIterator.hasNext()) {
                ruleIterator = rulesIterator.next().match(path, capturingGroupValues);                
                if (ruleIterator.hasNext()) {
                    r = ruleIterator.next();
                    return true;                
                }
            }
            
            return false;
        }

        public R next() {
            if (!hasNext()) throw new NoSuchElementException();
            
            final R _r = r;
            r = null;
            return _r;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
