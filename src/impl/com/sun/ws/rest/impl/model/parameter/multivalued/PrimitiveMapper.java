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

package com.sun.ws.rest.impl.model.parameter.multivalued;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class PrimitiveMapper {
    
    static final Map<Class, Class> primitiveToClassMap = 
            getPrimitiveToClassMap();
    
    static final Map<Class, Object> primitiveToDefaultValueMap = 
            getPrimitiveToDefaultValueMap();

    private static Map<Class, Class> getPrimitiveToClassMap() {
        Map<Class, Class> m = new WeakHashMap<Class, Class>();
        // Put all primitive to wrapper class mappings except
        // that for Character
        m.put(Boolean.TYPE, Boolean.class);
        m.put(Byte.TYPE, Byte.class);
        m.put(Short.TYPE, Short.class);
        m.put(Integer.TYPE, Integer.class);
        m.put(Long.TYPE, Long.class);
        m.put(Float.TYPE, Float.class);
        m.put(Double.TYPE, Double.class);
        
        return Collections.unmodifiableMap(m);
    }
    
    private static Map<Class, Object> getPrimitiveToDefaultValueMap() {
        Map<Class, Object> m = new WeakHashMap<Class, Object>();        
        m.put(Boolean.class, Boolean.valueOf(false));
        m.put(Byte.class, Byte.valueOf((byte)0));
        m.put(Short.class, Short.valueOf((short)0));
        m.put(Integer.class, Integer.valueOf(0));
        m.put(Long.class, Long.valueOf(0l));
        m.put(Float.class, Float.valueOf(0.0f));
        m.put(Double.class, Double.valueOf(0.0d));
        
        return Collections.unmodifiableMap(m);
    }
}
