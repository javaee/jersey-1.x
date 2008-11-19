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

package com.sun.jersey.server.impl.uri.rules.automata;

/**
 * Represents an arc (transition) between trie nodes.
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public class TrieArc<T> {
    
    /** Character that match this arc. */
    protected char[] code;
    
    /** Target node at the end of this arc. */
    protected TrieNode<T> target;
    
    /** Next alternative node if this node does not match. */
    protected TrieArc<T> next;
    
    /** 
     * Creates a new instance of TrieArc.
     * @param target Target node at the end of this arc.
     * @param code Matching character.
     */
    public TrieArc(TrieNode<T> target, char code) {
        this.target = target;
        this.code = new char[] {code};
    }
    
    /**
     * Merge neighbour nodes if they are degenerated trees.
     */
    private void merge(TrieArc<T> arc) {
        int p = code.length;
        // Cannot depend on SE 6 specific features
        // code = Arrays.copyOf(code, code.length + arc.code.length);
        code = copyOf(code, code.length + arc.code.length);
        System.arraycopy(arc.code, 0, code, p, arc.code.length);
        this.target = arc.target;
        if (target.getArcs() == 1 && !target.hasValue() && !target.isWildcard()) {
            merge(target.getFirstArc());
        }
    }
    
    /**
     * Merge neighbour nodes if they are degenerated trees.
     */
    public void pack() {
        if (target.getArcs() == 1 && !target.hasValue() && !target.isWildcard()) {
            merge(target.getFirstArc());
        }
        target.pack();
    }
    
    /**
     * Number of characters to be consumed if this arc matches the input.
     **/
    public int length() {
        return code.length;
    }
    
    /**
     * Returns length() if this arc matches the input, 0 otherwise.
     */
    public int match(CharSequence seq, int i) {
        if (i+code.length > seq.length()) return 0;
        for (int j=0; j<code.length; j++) {
            if (code[j] != seq.charAt(i++)) return 0;
        }
        return code.length;
    }
    
    @Override public String toString() {
        if (target.hasValue()) {
            return "ARC(" + new String(code) + ") --> " + target.getPattern().getRegex();
        }
        else {
            return "ARC(" + new String(code) + ") --> null";
        }
    }
    
    // Copied from SE 6 source
    /**
     * Copies the specified array, truncating or padding with null characters (if necessary)
     * so the copy has the specified length.  For all indices that are valid
     * in both the original array and the copy, the two arrays will contain
     * identical values.  For any indices that are valid in the copy but not
     * the original, the copy will contain <tt>'\\u000'</tt>.  Such indices
     * will exist if and only if the specified length is greater than that of
     * the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with null characters
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    private static char[] copyOf(char[] original, int newLength) {
        char[] copy = new char[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
}
