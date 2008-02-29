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

package com.sun.ws.rest.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MultivaluedMapImpl 
        extends HashMap<String, List<String>> 
        implements MultivaluedMap<String, String> {
    
    static final long serialVersionUID = -6052320403766368902L;
    
    /**
     * Creates a new instance of MultivaluedMapImpl
     */
    public MultivaluedMapImpl() {
    }

    // MultivaluedMap
    
    public void putSingle(String key, String value) {
        List<String> l = getList(key);
                
        l.clear();
        if (value != null)
            l.add(value);
        else 
            l.add("");
    }
    
    public void add(String key, String value) {
        List<String> l = getList(key);
        
        if (value != null)
            l.add(value);
        else 
            l.add("");
    }
    
    public String getFirst(String key) {
        List<String> values = get(key);
        if (values != null && values.size() > 0)
            return values.get(0);
        else
            return null;
    }

    
    
    // 
    
    public void addFirst(String key, String value) {
        List<String> l = getList(key);
        
        if (value != null)
            l.add(0, value);
        else 
            l.add(0, "");
    }
    
    public <A> List<A> get(String key, Class<A> type) {
        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName()+" has no String constructor", ex);
        }
        
        ArrayList<A> l = null;
        List<String> values = get(key);
        if (values != null) {
            l = new ArrayList<A>();
            for (String value: values) {
                try {
                    l.add(c.newInstance(value));
                } catch (Exception ex) {
                    l.add(null);
                }
            }
        }
        return l;
    }

    public void putSingle(String key, Object value) {
        List<String> l = getList(key);
                
        l.clear();
        if (value != null)
            l.add(value.toString());
        else 
            l.add("");
    }
    
    public void add(String key, Object value) {
        List<String> l = getList(key);
        
        if (value != null)
            l.add(value.toString());
        else 
            l.add("");
    }

    private List<String> getList(String key) {
        List<String> l = get(key);
        if (l == null) {
            l = new LinkedList<String>();
            put(key, l);
        }
        return l;
    }
    
    public <A> A getFirst(String key, Class<A> type) {
        String value = getFirst(key);
        if (value == null)
            return null;
        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName()+" has no String constructor", ex);
        }
        A retVal = null;
        try {
            retVal = c.newInstance(value);
        } catch (Exception ex) {
        }
        return retVal;
    }
    
    @SuppressWarnings("unchecked")
    public <A> A getFirst(String key, A defaultValue) {
        String value = getFirst(key);
        if (value == null)
            return defaultValue;
        
        Class<A> type = (Class<A>)defaultValue.getClass();
        
        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName()+" has no String constructor", ex);
        }
        A retVal = defaultValue;
        try {
            retVal = c.newInstance(value);
        } catch (Exception ex) {
        }
        return retVal;
    }
}
