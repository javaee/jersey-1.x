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
/*
 * TrieNodeValue.java
 *
 * Created on November 18, 2007, 11:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.jersey.impl.uri.rules.automata;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author mnesarco
 */
public class TrieNodeValue<T> {
    
    private Object value = null;
    
    /** Creates a new instance of TrieNodeValue */
    public TrieNodeValue() {
    }

    public void set(T value) {
        if (this.value == null) {
            this.value = value;
        }
        else if (this.value.getClass().isArray()) {
            Object[] old = (Object[])this.value;
            Object[] copy = new Object[old.length+1];
            System.arraycopy(old, 0, copy, 0, old.length+1);
            copy[copy.length-1] = value;
            this.value = copy;
        }
        else {
            this.value = new Object[] { this.value, value };
        }
    }
    
    @SuppressWarnings("unchecked")
    public Iterator<T> getIterator() {
        if (this.value == null) {
            return new EmptyIterator();
        }
        else if (this.value.getClass().isArray()) {
            return new ArrayIterator<T>((Object[])this.value);
        }
        else {
            return new SingleEntryIterator<T>((T)this.value);
        }
    }
    
    public boolean isEmpty() {
        return value == null;
    }
    
    private static final class ArrayIterator<T> implements Iterator<T> {
        
        private Object[] data;
        
        private int cursor = 0;
        
        public ArrayIterator(Object[] data) {
            this.data = data;
        }
        
        public boolean hasNext() {
            return cursor < data.length;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            if (hasNext()) {
                return (T)data[cursor++];
            }
            else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
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
    
    static final class EmptyIterator<T> implements Iterator<T> {        
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
