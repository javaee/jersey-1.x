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

package com.sun.jersey.api;

import com.sun.jersey.core.util.KeyComparatorHashMap;
import com.sun.jersey.core.util.StringIgnoreCaseKeyComparator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Out-bound HTTP headers.
 * <p>
 * Such HTTP headers will be associated with the out-bound HTTP request on the
 * client-side and the out-bound HTTP response on the server-side.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class OutBoundHeaders 
        extends KeyComparatorHashMap<String, List<Object>> 
        implements MultivaluedMap<String, Object> {
    
    static final long serialVersionUID = -6052320403766368902L;
    
    /**
     * Creates a new instance of MultivaluedMapImpl
     */
    public OutBoundHeaders() {
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