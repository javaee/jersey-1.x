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

package com.sun.jersey.impl.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ps23762
 */
final class JSONTransformer {
    @SuppressWarnings("unchecked")
    static <T> Map<String, T> asMap(String jsonObjectVal) throws JSONException {
        if (null == jsonObjectVal) {
            return null;
        }
        Map<String, T> result = new HashMap<String, T>();

        JSONObject sourceMap = new JSONObject(jsonObjectVal);
        Iterator<String> keyIterator = sourceMap.keys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            result.put(key, (T)sourceMap.get(key));
        }
        return result;
    }
    
    
    @SuppressWarnings("unchecked")
    static <T> Collection<T> asCollection(String jsonArrayVal) throws JSONException {
        if (null == jsonArrayVal) {
            return null;
        }
        Collection<T> result = new LinkedList<T>();

        JSONArray arrayVal = new JSONArray(jsonArrayVal);
        for (int i = 0; i < arrayVal.length(); i++) {
            result.add((T)arrayVal.get(i));
        }
        return result;
    }
    
    static String asJsonArray(Collection<? extends Object> collection) {
        return (null == collection) ? "[]" : (new JSONArray(collection)).toString();
    }
    
    static String asJsonObject(Map map) {
        return (null == map) ? "{}" : (new JSONObject(map)).toString();
    }
}
