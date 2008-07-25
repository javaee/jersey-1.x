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

package com.sun.jersey.impl.model.parameter.multivalued;

import com.sun.jersey.api.container.ContainerException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
abstract class CollectionValueOfExtractor<V extends Collection> 
        extends BaseValueOfExtractor 
        implements MultivaluedParameterExtractor {
    final String parameter;
    final Object defaultValue;

    protected CollectionValueOfExtractor(Method valueOf, String parameter, String defaultValueString) 
    throws IllegalAccessException, InvocationTargetException {
        super(valueOf);
        this.parameter = parameter;
        this.defaultValue = (defaultValueString != null) ? 
            getValue(defaultValueString) : null;
    }

    @SuppressWarnings("unchecked")
    public Object extract(MultivaluedMap<String, String> parameters) {
        List<String> stringList = parameters.get(parameter);
        if (stringList != null) {            
            V valueList = getInstance();
            for (String v : stringList) {
                try {
                    valueList.add(getValue(v));
                } catch (Exception e) {
                    throw new ContainerException(e);
                }
            }

            return valueList;
        } else if (defaultValue != null) {
            V valueList = getInstance();
            // TODO do we need to clone the default value
            valueList.add(defaultValue);
            return valueList;
        }

        return null;
    }
    
    protected abstract V getInstance();
    
    private static final class ListValueOf extends CollectionValueOfExtractor<List> {
        ListValueOf(Method valueOf, String parameter, String defaultValueString) 
        throws IllegalAccessException, InvocationTargetException {
            super(valueOf, parameter, defaultValueString);
        }
        
        protected List getInstance() {
            return new ArrayList();
        }
    }
    
    private static final class SetValueOf extends CollectionValueOfExtractor<Set> {
        SetValueOf(Method valueOf, String parameter, String defaultValueString) 
        throws IllegalAccessException, InvocationTargetException {
            super(valueOf, parameter, defaultValueString);
        }
        
        protected Set getInstance() {
            return new HashSet();
        }
    }
    
    private static final class SortedSetValueOf extends CollectionValueOfExtractor<SortedSet> {
        SortedSetValueOf(Method valueOf, String parameter, String defaultValueString) 
        throws IllegalAccessException, InvocationTargetException {
            super(valueOf, parameter, defaultValueString);
        }
        
        protected SortedSet getInstance() {
            return new TreeSet();
        }
    }
    
    static MultivaluedParameterExtractor getInstance(Class c, 
            Method valueOf, String parameter, String defaultValueString) 
    throws InstantiationException, IllegalAccessException, InvocationTargetException {
        if (List.class == c)
            return new ListValueOf(valueOf, parameter, defaultValueString);
        else if (Set.class == c)
            return new SetValueOf(valueOf, parameter, defaultValueString);
        else if (SortedSet.class == c)
            return new SortedSetValueOf(valueOf, parameter, defaultValueString);
        else
            throw new RuntimeException();
    }   
}
