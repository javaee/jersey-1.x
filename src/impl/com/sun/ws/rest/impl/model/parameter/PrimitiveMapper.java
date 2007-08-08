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

package com.sun.ws.rest.impl.model.parameter;

import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
final class PrimitiveMapper {
    
    static Map<Class, Class> primitiveToClassMap = 
            new WeakHashMap<Class, Class>();
    static Map<Class, Object> primitiveToDefaultValueMap = 
            new WeakHashMap<Class, Object>();

    static {
        // Put all primitive to wrapper class mappings except
        // that for Character
        primitiveToClassMap.put(Boolean.TYPE, Boolean.class);
        primitiveToClassMap.put(Byte.TYPE, Byte.class);
        primitiveToClassMap.put(Short.TYPE, Short.class);
        primitiveToClassMap.put(Integer.TYPE, Integer.class);
        primitiveToClassMap.put(Long.TYPE, Long.class);
        primitiveToClassMap.put(Float.TYPE, Float.class);
        primitiveToClassMap.put(Double.TYPE, Double.class);
        
        primitiveToDefaultValueMap.put(Boolean.class, Boolean.valueOf(false));
        primitiveToDefaultValueMap.put(Byte.class, Byte.valueOf((byte)0));
        primitiveToDefaultValueMap.put(Short.class, Short.valueOf((short)0));
        primitiveToDefaultValueMap.put(Integer.class, Integer.valueOf(0));
        primitiveToDefaultValueMap.put(Long.class, Long.valueOf(0l));
        primitiveToDefaultValueMap.put(Float.class, Float.valueOf(0.0f));
        primitiveToDefaultValueMap.put(Double.class, Double.valueOf(0.0d));
    }
    
}
