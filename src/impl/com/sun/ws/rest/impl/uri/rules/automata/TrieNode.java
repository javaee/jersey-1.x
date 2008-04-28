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

import com.sun.jersey.api.uri.UriPattern;
import com.sun.jersey.api.uri.UriTemplate;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a trie automata node.
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public final class TrieNode<T> {

    /** TemplateParameters pattern regexp. */
    public static final Pattern PARAMETER_PATTERN = 
            Pattern.compile("\\{([\\w-\\._~]+?)\\}");
    
    /** Wildcard character */
    private static final char WILDCARD_CHAR = '\0';
    
    /** First child arc (this is a linked tree structure) */
    private TrieArc<T> firstArc;
    
    /** Last child arc (this is a linked tree structure) */
    private TrieArc<T> lastArc;
    
    /** arc counter */
    private int arcs = 0;
    
    /** Node's data */
    private TrieNodeValue<T> value = new TrieNodeValue<T>();
    
    /** Node's UriPattern used to match. */
    private UriPattern pattern;

    /** Tells if this node is a wildcard node */
    private boolean wildcard = false;
    
    /** wildcard setter. 
     * @param b New wildcard value.
     */
    protected void setWildcard(boolean b) { 
        wildcard = b; 
    }
    
    /** value setter. 
     * @param value New value.
     * @param template Associated template.
     */
    protected void setValue(T value, UriPattern pattern) {
        this.value.set(value);
        this.pattern = pattern;
    }
    
    /** Creates a new instance of TrieNode */
    protected TrieNode() {
        super();
    }
    
    /** Creates a new instance of TrieNode 
     * @param value Initial value.
     */
    protected TrieNode(T value) {
        this.value.set(value);
    }
    
    /**
     * Search for a matching escape character in a wildcard sequence.
     * @param c Test char.
     */
    protected TrieArc<T> matchExitArc(CharSequence seq, int i) {
        TrieArc<T> arc = firstArc;
        while (arc != null) {
            if (arc.match(seq, i) > 0) {
                return arc;
            }
            arc = arc.next;
        }
        return null;
    }
    
    /**
     * Tells if there is a value in this node.
     */
    protected boolean hasValue() {
        return !value.isEmpty();
    }
    
    /**
     * Adds an arc at the end.
     * @param arc New arc.
     */
    private void addArc(TrieArc<T> arc) {
        if (firstArc == null) {
            firstArc = arc;
        }
        else {
            lastArc.next = arc;
        }
        lastArc = arc;
        arcs++;
    }
    
    /**
     * Adds a new node in the tree.
     * @param path Tree position (URI)
     * @param i Current position in path.
     * @param value Value to be added at the end of path.
     * @param pattern UriPattern associated with value.
     */
    private boolean add(CharSequence path, int i, T value, 
            UriPattern pattern) {
        
        // Case 1: NULL, The Last ----------------------------------------------
        if (i >= path.length()) {
            setValue(value, pattern);
            return true;
        }
        
        // Case 2: Recursive add -----------------------------------------------
        char input = path.charAt(i);
        boolean added = false;
        TrieArc<T> arc = firstArc;
        while (arc != null) {
            if (arc.match(path, i) > 0) {
                added = arc.target.add(path, i+1, value, pattern);
                if (added) {
                    return added;
                }
            }
            arc = arc.next;
        }
        
        // Case 3: Set as wildcard ---------------------------------------------
        TrieNode<T> node;
        if (input == WILDCARD_CHAR) {
            setWildcard(true);
            return add(path, i+1, value, pattern);
        }
        
        // Case 4: Just Add ----------------------------------------------------
        else {
            node = new TrieNode<T>();
            addArc(new TrieArc<T>(node, input));
            return node.add(path, i+1, value, pattern);
        }
        
    }
    
    /**
     * Adds a new node to the tree.
     * @param path Matching URI
     * @param value Value to be added.
     * @param template Associated UriPattern.
     */
    protected void add(String path, T value, UriPattern pattern) {
        
        // Replace All parameter macro by a WILDCARD character.
        Matcher matcher = PARAMETER_PATTERN.matcher(path);
        String uri = matcher.replaceAll(String.valueOf(WILDCARD_CHAR));
        
        // If ends with '/', add the parent
        if (uri.endsWith("/") && uri.length() > 1) {
            add(uri.substring(0, uri.length()-1), 0, value, pattern);
        }
        
        // Add to root
        add(uri, 0, value, pattern);
        
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        StringBuilder out = new StringBuilder();
        toStringRepresentation(out, 0, new char[] {'\0'});
        return out.toString();
    }
    
    /**
     * Builds a string representation of this subtree.
     * @param out Output string.
     * @param level indentation level.
     * @param c Transition char.
     */
    private void toStringRepresentation(StringBuilder out, int level, char[] c) {
        for (int i=0; i<level; i++) out.append(' ');
        out.append("ARC(" + new String(c) + ") ->");
        out.append(getClass().getSimpleName() + (wildcard?"*":""));
        out.append(" ");
        out.append(value);
        out.append('\n');
        TrieArc<T> arc = firstArc;
        while (arc != null) {
            arc.target.toStringRepresentation(out, level+2, arc.code);
            arc = arc.next;
        }
    }

    /** pattern getter. */
    public UriPattern getPattern() {
        return pattern;
    }

    /** value getter. */
    public Iterator<T> getValue() {
        return value.getIterator();
    }

    /** wildcard getter. */
    protected boolean isWildcard() {
        return wildcard;
    }

    /** firstArch getter. */
    protected TrieArc<T> getFirstArc() {
        return firstArc;
    }

    /** arcs getter. */
    public int getArcs() {
        return arcs;
    }
    
    /**
     * Pack and optimize the automata.
     */
    public void pack() {
        TrieArc<T> arc = firstArc;
        while (arc != null) {
            arc.pack();
            arc = arc.next;
        }
    }
    
}
