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

package com.sun.ws.rest.impl.uri.rules.automata;

import com.sun.ws.rest.impl.uri.PathPattern;
import com.sun.ws.rest.impl.uri.rules.PatternRulePair;
import com.sun.ws.rest.spi.uri.rules.UriRules;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * UriRules implementation based on a TRIE/Finite Automata.
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public final class AutomataMatchingUriTemplateRules<R> implements UriRules<R> {
    /** Trie/Automata Index */
    private final TrieNode<R> automata;

    public AutomataMatchingUriTemplateRules(List<PatternRulePair<R>> rules) {
        this.automata = initTrie(rules);
    }
    
    public Iterator<R> match(CharSequence path, List<String> capturingGroupValues) {
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
