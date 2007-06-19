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

package com.sun.ws.rest.impl.model.method;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link HashMap} with HTTP methods as keys and a list of 
 * {@link ResourceMethodList} as values.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceMethodMap extends HashMap<String, ResourceMethodList> {
    
    public ResourceMethodMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }
  
    public ResourceMethodMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ResourceMethodMap() {
        super();
    }

    public ResourceMethodMap(ResourceMethodMap m) {
        super(m);
    }
    
    /**
     * Merge a method map with this method map.
     *
     * @param from the method map to merge.
     */
    public void merge(ResourceMethodMap from) {
        for (Map.Entry<String, ResourceMethodList> fromEntry : from.entrySet()) {
            // Get the list of methods for the HTTP method
            ResourceMethodList toList = get(fromEntry.getKey());
            if (toList == null) {
                // Create one if the list does not exist
                toList = new ResourceMethodList();
                put(fromEntry.getKey(), toList);            
            }
            // Add the methods
            toList.addAll(fromEntry.getValue());
        }
    }

    /**
     * Merge a HTTP method and associated Web resource method.
     * 
     * @param method the Web resource method.
     */
    public void put(ResourceMethod method) {
        ResourceMethodList l = get(method.httpMethod);
        if (l == null) {
            l = new ResourceMethodList();
            put(method.httpMethod, l);            
        }
        l.add(method);
        
    }
    
    /**
     * Sort the list methods for each value in the method map.
     */
    public void sort() {
        for (Map.Entry<String, ResourceMethodList> e : entrySet()) {
            Collections.sort(e.getValue(), ResourceMethod.COMPARATOR);
        }
    } 
}