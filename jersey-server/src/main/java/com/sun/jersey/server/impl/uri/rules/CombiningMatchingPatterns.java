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

import com.sun.jersey.spi.uri.rules.UriMatchResultContext;
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

    public Iterator<R> match(CharSequence path, UriMatchResultContext resultContext) {
        return new XInterator(path, resultContext);
    }

    private final class XInterator implements Iterator<R> {

        private final CharSequence path;
        private final UriMatchResultContext resultContext;
        private Iterator<R> ruleIterator;
        private Iterator<UriRules<R>> rulesIterator;
        private R r;

        XInterator(CharSequence path, UriMatchResultContext resultContext) {
            this.path = path;
            this.resultContext = resultContext;
            this.rulesIterator = rs.iterator();
            this.ruleIterator = rulesIterator.next().match(path, resultContext);
        }
        
        public boolean hasNext() {
            if (r != null) return true;

            if (ruleIterator.hasNext()) {
                r = ruleIterator.next();
                return true;
            }
            
            while (rulesIterator.hasNext()) {
                ruleIterator = rulesIterator.next().match(path, resultContext);                
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
