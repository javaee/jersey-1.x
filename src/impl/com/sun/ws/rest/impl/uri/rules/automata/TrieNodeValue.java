/*
 * TrieNodeValue.java
 *
 * Created on November 18, 2007, 11:57 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.uri.rules.automata;

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
