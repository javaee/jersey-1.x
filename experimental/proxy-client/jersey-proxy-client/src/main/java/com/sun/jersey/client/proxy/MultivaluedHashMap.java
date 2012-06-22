package com.sun.jersey.client.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Martin Matula (martin.matula at oracle.com)
 */
class MultivaluedHashMap<K, V> extends HashMap<K, List<V>> implements MultivaluedMap<K, V> {
    MultivaluedHashMap() {
    }

    MultivaluedHashMap(MultivaluedMap<K, V> map) {
        super(map);
    }

    @Override
    public void putSingle(K key, V value) {
        List<V> values = new ArrayList<V>();
        values.add(value);
        put(key, values);
    }

    @Override
    public void add(K key, V value) {
        List<V> values = get(key);
        if (values == null) {
            values = new ArrayList<V>();
            put(key, values);
        }
        values.add(value);
    }

    @Override
    public V getFirst(K key) {
        List<V> values = get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
