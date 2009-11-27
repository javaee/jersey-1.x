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

package com.sun.jersey.server.impl.uri.rules;

import com.sun.jersey.api.uri.UriPattern;
import com.sun.jersey.spi.uri.rules.UriMatchResultContext;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;

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
    
    public Iterator<R> match(CharSequence path, UriMatchResultContext resultContext) {
        if (resultContext.isTracingEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("match path \"").append(path).append("\" -> ");
            boolean first = true;
            for (PatternRulePair<R> prp : rules) {
                if (!first)
                    sb.append(", ");
                sb.append("\"").append(prp.p.toString()).append("\"");
                first = false;
            }
            resultContext.trace(sb.toString());
        }

        for (PatternRulePair<R> prp : rules) {
            // Match each template
            final MatchResult mr = prp.p.match(path);
            if (mr != null) {
                resultContext.setMatchResult(mr);
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
