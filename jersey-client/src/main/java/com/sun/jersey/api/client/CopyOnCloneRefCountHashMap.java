/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.api.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * optimized copy on clone map
 *
 * @author Paul.Sandoz@Sun.Com
 */

public class CopyOnCloneRefCountHashMap<K,V> implements Map<K,V> {
    private WeakHashMap<CopyOnCloneRefCountHashMap<K,V>, Boolean> map;

    private Map<K,V> core;

    private Map<K,V> view;

    public CopyOnCloneRefCountHashMap() {
        this(new HashMap<K, V>());
    }

    public CopyOnCloneRefCountHashMap(Map<K, V> core) {
        this.core = core;
    }

    private CopyOnCloneRefCountHashMap(CopyOnCloneRefCountHashMap<K, V> m) {
        map = m.map;
        core = m.core;
        view = m.view;
    }

    @Override
    public CopyOnCloneRefCountHashMap<K,V> clone() {
        if (map == null) {
            map = new WeakHashMap<CopyOnCloneRefCountHashMap<K,V>, Boolean>();
            // Add reference to self in the weak hash map
            map.put(this, true);
        }

        CopyOnCloneRefCountHashMap<K,V> clone = new CopyOnCloneRefCountHashMap(this);

        synchronized(map) {
            // Add reference to clone in the weak hash map
            map.put(clone, true);
        }

        return clone;
    }

    private void copy() {
        if (map != null) {
            synchronized(map) {
                if (map.size() > 1) {
                    // If the size is > 1 then there are shared copies
                    // remove reference to self in the weak hash map and
                    // copy the map
                    map.remove(this);
                    map = null;

                    core = new HashMap<K, V>(core);
                    view = null;
                }
            }
        }
    }


    public int size() {
        return core.size();
    }

    public boolean isEmpty() {
        return core.isEmpty();
    }

    public boolean containsKey(Object key) {
        return core.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return core.containsValue(value);
    }

    public V get(Object key) {
        return core.get(key);
    }

    public V put(K key, V value) {
        copy();
        return core.put(key,value);
    }

    public V remove(Object key) {
        copy();
        return core.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        copy();
        core.putAll(t);
    }

    public void clear() {
        core = new HashMap<K, V>();
        copy();
    }

    public Set<K> keySet() {
        return getView().keySet();
    }

    public Collection<V> values() {
        return getView().values();
    }

    public Set<Entry<K,V>> entrySet() {
        return getView().entrySet();
    }

    @Override
    public String toString() {
        return core.toString();
    }

    private Map<K, V> getView() {
        if (view == null) {
            view = Collections.unmodifiableMap(core);
        }
        return view;
    }
}