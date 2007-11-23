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

package com.sun.ws.rest.impl.model;

import com.sun.ws.rest.impl.model.method.ResourceMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link HashMap} with HTTP methods as keys and {@link ResourceMethodList} 
 * as values.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
/* package */ final class ResourceMethodMap extends HashMap<String, List<ResourceMethod>> {
    /**
     * Merge a HTTP method and associated resource method.
     * 
     * @param method the resource method.
     */
    public void put(ResourceMethod method) {
        List<ResourceMethod> l = get(method.getHttpMethod());
        if (l == null) {
            l = new ArrayList<ResourceMethod>();
            put(method.getHttpMethod(), l);            
        }
        l.add(method);
    }
    
    /**
     * Sort the list methods for each value in the method map.
     */
    public void sort() {
        for (Map.Entry<String, List<ResourceMethod>> e : entrySet()) {
            Collections.sort(e.getValue(), ResourceMethod.COMPARATOR);
        }
    }
}