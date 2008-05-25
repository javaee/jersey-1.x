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

package com.sun.jersey.impl.uri.rules;

import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Rules associated with instances of {@link UriPattern) and matched 
 * to return at most one match.
 * <p>
 * The class will iterate through the collection of {@link UriPattern) until
 * a match is found. Note that this does not scale.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class AtomicMatchingPatterns<R> implements UriRules<R> {
    private final Collection<PatternRulePair<R>> rules;

    public AtomicMatchingPatterns(Collection<PatternRulePair<R>> rules) {
        this.rules = rules;
    }
    
    public Iterator<R> match(CharSequence path, List<String> capturingGroupValues) {
        for (PatternRulePair<R> prp : rules) {
            // Match each template
            if (prp.p.match(path, capturingGroupValues)) {
                return new SingleEntryIterator<R>(prp.r);
            }
        }

        return new EmptyIterator<R>();
    }

    private static final class SingleEntryIterator<T> implements Iterator<T> {
        private T t;
        
        SingleEntryIterator(T t) {
            this.t = t;
        }
        
        public boolean hasNext() {
            return t != null;
        }

        public T next() {
            if (hasNext()) {
                final T _t = t;
                t = null;
                return _t;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class EmptyIterator<T> implements Iterator<T> {        
        public boolean hasNext() {
            return false;
        }

        public T next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
