/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.server.impl.uri.rules.automata;

import com.sun.jersey.server.impl.uri.PathPattern;
import com.sun.jersey.server.impl.uri.rules.PatternRulePair;
import com.sun.jersey.spi.uri.rules.UriMatchResultContext;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * UriRules implementation based on a TRIE/Finite Automata.
 *
 * This class has been made abstract because it needs to fixed in terms
 * of supporting the UriRules interface and matching using more general regular
 * expressions.
 * 
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public class AutomataMatchingUriTemplateRules<R> implements UriRules<R> {
    /** Trie/Automata Index */
    private final TrieNode<R> automata;

    public AutomataMatchingUriTemplateRules(List<PatternRulePair<R>> rules) {
        this.automata = initTrie(rules);
    }
    
    public Iterator<R> match(CharSequence path, UriMatchResultContext resultContext) {
        List<String> capturingGroupValues = new ArrayList<String>();
        TrieNode<R> node = find(path, capturingGroupValues);
        if (node != null) {
            return node.getValue();
        }
        return new TrieNodeValue.EmptyIterator<R>();
    }

    /** 
     * Trie initialization 
     */
    private TrieNode<R> initTrie(List<PatternRulePair<R>> rules) {
        TrieNode<R> a = new TrieNode<R>();
        for (PatternRulePair<R> prp : rules) {
            if (prp.p instanceof PathPattern) {
                PathPattern p = (PathPattern)prp.p;            
                a.add(p.getTemplate().getTemplate(), prp.r, prp.p);
            } else {
                throw new IllegalArgumentException(
                        "The automata matching algorithm currently only works" +
                        "for UriPattern instance that are instances of " +
                        "PathPattern");
            }
        }
        a.pack();
        return a;
    }
    
    /**
     * Backtracking state struct
     */
    private static final class SearchState<E> {
        
        // Saved node
        final TrieNode<E> node;
        
        // Saved arch
        final TrieArc<E> arc;
        
        // Saved input position
        final int i;
        
        /** Constructor */
        public SearchState(TrieNode<E> node, TrieArc<E> arc, int i) {
            this.node = node;
            this.arc = arc;
            this.i = i;
        }

    }

    /**
     * Trie/Automata search algorithm.
     */
    private TrieNode<R> find(CharSequence uri, List<String> templateValues) {
        
        // URI Length
        final int length = uri.length();
        
        // Backtracking stack
        final Stack<SearchState<R>> stack = new Stack<SearchState<R>>();
        
        // Candidates saved by the way
        final Stack<TrieNode<R>> candidates = new Stack<TrieNode<R>>();
        
        // Arcs marked as visited
        final Set<TrieArc<R>> visitedArcs = new HashSet<TrieArc<R>>();
        
        // Actual node
        TrieNode<R> node = automata;
        
        // Actual matching arc
        TrieArc<R> nextArc = node.getFirstArc();
        
        // URI character pointer
        int i = 0;
        
        // =====================================================================
        // Trie Search with backtracking
        // NFA simulation
        // =====================================================================
        
        while (true) {
            
            // End of input reached
            if (i >= length) {
                
                // Resource matched
                if (node.hasValue()) break; // <<< EXIT POINT <<<<<<<<<<<<<<<<<<
                
                // Restore backtracking state
                nextArc = null;
                while (!stack.isEmpty() && nextArc == null) {
                    SearchState<R> state = stack.pop();
                    nextArc = state.arc.next;
                    node = state.node;
                    i = state.i;
                }
                
                // Skip visited arcs if necesary
                if (nextArc != null) {
                    while (visitedArcs.contains(nextArc)) {
                        nextArc = nextArc.next;
                    }
                    if (nextArc != null) visitedArcs.add(nextArc);
                }
                
                // No more chance to match
                if (nextArc == null) break; // <<< EXIT POINT <<<<<<<<<<<<<<<<<<
                
                // Go backtrack
                continue;
                
            }
            
            // Accept a wildcard (Parameter)
            if (nextArc == null && node.isWildcard()) {
                int p = 0;
                TrieArc<R> exitArc = null;
                while ((i+p) < length && 
                    (exitArc = node.matchExitArc(uri, i+p)) == null) p++;
                if (exitArc != null) {
                    nextArc = exitArc;
                }
                i = i+p;
                continue;
            }
            
            // No wildcard and no more paths, end.
            else if (nextArc == null && !node.isWildcard()) {
                break; // <<< EXIT POINT <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            }
            
            // Save backtracking point
            if (nextArc.next != null && node.isWildcard()) {
                stack.push(new SearchState<R>(node, nextArc, i));
            }
            
            // Save candidate
            if (node.hasValue()) {
                candidates.push(node);
            }
            
            // Matching cases ==================================================
            
            // CASE 0 ----------------------------------------------------------
            // If wildcard matches, exit wildcard.
            if (node.isWildcard() && nextArc.match(uri, i) > 0) {
                i += nextArc.length();
                node = nextArc.target;
                nextArc = node.getFirstArc();
                continue;
            }
            
            // CASE 1 ----------------------------------------------------------
            // If wildcard does not match, try another escape sequence. 
            //    if nothing matches, consume input.
            else if (node.isWildcard() && nextArc.match(uri, i) == 0) {
                nextArc = nextArc.next;
                if (nextArc == null) {
                    i++;
                }
                continue;
            }
            
            // CASE 2 ----------------------------------------------------------
            // Fixed sequence matches, consume input and follow the arc.
            else if (!node.isWildcard() && nextArc.match(uri, i) > 0) {
                i += nextArc.length();
                node = nextArc.target;
                nextArc = node.getFirstArc();
                continue;
            }
            
            // CASE 3 ----------------------------------------------------------
            // Fixed sequence does not match, try the next.
            else if (!node.isWildcard() && nextArc.match(uri, i) == 0) {
                nextArc = nextArc.next;
                continue;
            }
            
        }
        
        // =====================================================================
        // Select a matching candidate 
        // =====================================================================
        
        // A perfect match
        if (node.hasValue()) {
            if (node.getPattern().match(uri, templateValues)) {
                return node;
            }
        }
        
        // No direct matches, looking for a secondary candidate
        while (!candidates.isEmpty()) {
            TrieNode<R> s = candidates.pop();
            if (s.getPattern().match(uri, templateValues)) {
                return s;
            }
        }
        
        // Definitively it does not match
        templateValues.clear();
        return null;
        
    }
    
    
}
