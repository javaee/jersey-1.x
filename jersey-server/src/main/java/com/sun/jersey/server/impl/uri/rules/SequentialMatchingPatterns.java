/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.uri.rules;

import com.sun.jersey.api.uri.UriPattern;
import com.sun.jersey.spi.uri.rules.UriMatchResultContext;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.MatchResult;

/**
 * Rules associated with instances of {@link UriPattern) and matched 
 * in order. Zero or more matches may occur.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class SequentialMatchingPatterns<R> implements UriRules<R> {

    private final List<PatternRulePair<R>> rules;

    public SequentialMatchingPatterns(List<PatternRulePair<R>> rules) {
        this.rules = rules;
    }
    
    public Iterator<R> match(CharSequence path, UriMatchResultContext resultContext) {
        return new XInterator(path, resultContext);
    }

    private final class XInterator implements Iterator<R> {

        private final CharSequence path;
        private final UriMatchResultContext resultContext;
        private final Iterator<PatternRulePair<R>> i;
        private R r;

        XInterator(CharSequence path, UriMatchResultContext resultContext) {
            this.path = path;
            this.resultContext = resultContext;
            this.i = rules.iterator();
        }
        
        public boolean hasNext() {
            if (r != null) return true;
            
            while(i.hasNext()) {
                final PatternRulePair<R> prp = i.next();
                final MatchResult mr = prp.p.match(path);
                if (mr != null) {
                    resultContext.setMatchResult(mr);
                    r = prp.r;
                    return true;
                }                
            }
            r = null;
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
