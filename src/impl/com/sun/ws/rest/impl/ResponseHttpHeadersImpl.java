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

import com.sun.ws.rest.impl.util.KeyComparatorHashMap;
import com.sun.ws.rest.impl.util.StringIgnoreCaseKeyComparator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ResponseHttpHeadersImpl 
        extends KeyComparatorHashMap<String, List<Object>> 
        implements MultivaluedMap<String, Object> {
    
    static final long serialVersionUID = -6052320403766368902L;
    
    /**
     * Creates a new instance of MultivaluedMapImpl
     */
    public ResponseHttpHeadersImpl() {
        super(StringIgnoreCaseKeyComparator.SINGLETON);
    }

    // MultivaluedMap
    
    public void putSingle(String key, Object value) {
        if (value == null)
            return;
        
        List<Object> l = getList(key);        
        l.clear();
        l.add(value);
    }
    
    public void add(String key, Object value) {
        if (value == null)
            return;
        
        List<Object> l = getList(key);
        l.add(value);        
    }
    
    public Object getFirst(String key) {
        List<Object> values = get(key);
        if (values != null && values.size() > 0)
            return values.get(0);
        else
            return null;
    }


    // 
    
    @SuppressWarnings("unchecked")
    public <A> List<A> get(String key, Class<A> type) {
        ArrayList<A> l = null;
        List<Object> values = get(key);
        if (values != null) {
            l = new ArrayList<A>();
            for (Object value : values) {
                if (type.isInstance(value)) {
                    l.add((A)value);
                } else {
                    throw new IllegalArgumentException(type + " is not an instance of " + value.getClass());            
                }
            }
        }
        return l;
    }
    
    @SuppressWarnings("unchecked")
    public <A> A getFirst(String key, Class<A> type) {
        Object value = getFirst(key);
        if (value == null)
            return null;

        if (type.isInstance(value)) {
            return (A)value;
        } else {
            throw new IllegalArgumentException(type + " is not an instance of " + value.getClass());            
        }        
    }
    
    @SuppressWarnings("unchecked")
    public <A> A getFirst(String key, A defaultValue) {
        Object value = getFirst(key);
        if (value == null)
            return defaultValue;
        
        if (defaultValue.getClass().isInstance(value)) {
            return (A)value;
        } else {
            throw new IllegalArgumentException(defaultValue.getClass() + " is not an instance of " + value.getClass());            
        }        
    }

    private List<Object> getList(String key) {
        List<Object> l = get(key);
        if (l == null) {
            l = new LinkedList<Object>();
            put(key, l);
        }
        return l;
    }    
}
