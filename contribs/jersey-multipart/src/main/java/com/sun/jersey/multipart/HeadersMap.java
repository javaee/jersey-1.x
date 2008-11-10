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

package com.sun.jersey.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.ws.rs.core.MultivaluedMap;

/**
 * <p>Custom mutable {@link Map} implementation to support HTTP headers, with
 * keys that are compared in a case insensitive fashion, and values that are a
 * list of zero or more arbitrary Java objects.  Also, the implementation does
 * NOT allow <code>null</code> key values, and will throw
 * <code>IllegalArgumentException</code> if an attempt is made to add such a key.</p>
 */
class HeadersMap implements MultivaluedMap<String,String> {

    private Map<String,List<String>> map = new HashMap<String,List<String>>();

    public void add(String key, String value) {
        List<String> values = get(key);
        if (values == null) {
            values = new ArrayList<String>();
            put(key, values);
        }
        values.add(value);
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        for (String mapKey : map.keySet()) {
            if (mapKey.equalsIgnoreCase((String) key)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsValue(Object arg) {
        List<String> test = (List<String>) arg;
        for (List<String> value : map.values()) {
            if (value.equals(test)) {
                return true;
            }
        }
        return false;
    }

    public Set<Entry<String, List<String>>> entrySet() {
        return new HeadersEntries(this);
    }

    // FIXME - equals()

    public List<String> get(Object key) {
        for (Map.Entry<String,List<String>> entry : entrySet()) {
            if (entry.getKey().equalsIgnoreCase((String) key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getFirst(String key) {
        List<String> values = get(key);
        if ((values != null) && (values.size() > 0)) {
            return values.get(0);
        }
        return null;
    }

    // FIXME - hashCode()

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Set<String> keySet() {
        return new HeadersKeys(this);
    }

    public List<String> put(String key, List<String> values) {
        if ((key == null) || (values == null)) {
            throw new IllegalArgumentException();
        }
        List<String> old = null;
        for (Map.Entry<String,List<String>> entry : entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                old = entry.getValue();
                map.remove(entry.getKey());
                break;
            }
        }
        map.put(key, values);
        return old;
    }

    public void putAll(Map<? extends String, ? extends List<String>> arg) {
        for (Map.Entry entry : arg.entrySet()) {
            put((String) entry.getKey(), (List<String>) entry.getValue());
        }
    }

    public void putSingle(String key, String value) {
        List<String> list = new ArrayList<String>(1);
        list.add(value);
        put(key, list);
    }

    public List<String> remove(Object key) {
        for (String id : keySet()) {
            if (id.equalsIgnoreCase((String) key)) {
                return map.remove(key);
            }
        }
        return null;
    }

    public int size() {
        return map.size();
    }

    public Collection<List<String>> values() {
        return map.values();
    }


    /**
     * <p>An immutable {@link Set} listing the {@link Map.Entry} pairs of
     * the {@link HeadersMap} specified to our constructor, which performs
     * case insensitive comparisions for membership.</p>
     */
    class HeadersEntries implements Set<Entry<String, List<String>>> {

        public HeadersEntries(HeadersMap map) {
            this.map = map;
        }

        private HeadersMap map = null;

        public boolean add(Entry<String, List<String>> arg0) {
            throw new UnsupportedOperationException("add() is not supported");
        }

        public boolean addAll(Collection<? extends Entry<String, List<String>>> arg0) {
            throw new UnsupportedOperationException("addAll() is not supported");
        }

        public void clear() {
            throw new UnsupportedOperationException("clear() is not supported");
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry) o;
            if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof List)) {
                return false;
            }
            if (!(map.containsKey(entry.getKey()))) {
                return false;
            }
            List value = map.get(entry.getKey());
            if ((value == null) && (entry.getValue() == null)) {
                return true;
            } else if ((value != null) && (entry.getValue() != null) && value.equals(entry.getValue())) {
                return true;
            } else {
                return false;
            }
        }

        public boolean containsAll(Collection<?> items) {
            for (Object item : items) {
                if (!contains(item)) {
                    return false;
                }
            }
            return true;
        }

        // FIXME - equals()

        // FIXME - hashCode()

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public Iterator<Entry<String, List<String>>> iterator() {
            return map.map.entrySet().iterator();
        }

        public boolean remove(Object arg0) {
            throw new UnsupportedOperationException("remove() is not supported");
        }

        public boolean removeAll(Collection<?> arg0) {
            throw new UnsupportedOperationException("removeAll() is not supported");
        }

        public boolean retainAll(Collection<?> arg0) {
            throw new UnsupportedOperationException("retainAll() is not supported");
        }

        public int size() {
            return map.size();
        }

        public Object[] toArray() {
            return map.map.entrySet().toArray();
        }

        public <T> T[] toArray(T[] array) {
            return map.map.entrySet().toArray(array);
        }

    }


    /**
     * <p>An immutable {@link Set} listing the keys of the {@link HeadersMap}
     * specified to our constructor, which performs case insensitive
     * comparisons for membership.</p>
     */
    class HeadersKeys implements Set<String> {

        public HeadersKeys(HeadersMap map) {
            this.map = map;
        }

        private HeadersMap map = null;

        public boolean add(String key) {
            throw new UnsupportedOperationException("add() is not supported");
        }

        public boolean addAll(Collection<? extends String> keys) {
            throw new UnsupportedOperationException("addAll() is not supported");
        }

        public void clear() {
            throw new UnsupportedOperationException("clear() is not supported");
        }
        public boolean contains(Object key) {
            return map.containsKey(key);
        }

        public boolean containsAll(Collection<?> keys) {
            for (Object key : keys) {
                if (!map.containsKey(key)) {
                    return false;
                }
            }
            return true;
        }

        // FIXME - equals()

        // FIXME - hashCode()

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public Iterator<String> iterator() {
            return map.map.keySet().iterator();
        }

        public boolean remove(Object key) {
            throw new UnsupportedOperationException("remove() is not supported");
        }

        public boolean removeAll(Collection<?> keys) {
            throw new UnsupportedOperationException("removeAll() is not supported");
        }

        public boolean retainAll(Collection<?> keys) {
            throw new UnsupportedOperationException("retainAll() is not supported");
        }

        public int size() {
            return map.size();
        }

        public Object[] toArray() {
            return map.map.keySet().toArray();
        }

        public <T> T[] toArray(T[] array) {
            return map.map.keySet().toArray(array);
        }

    }


}
