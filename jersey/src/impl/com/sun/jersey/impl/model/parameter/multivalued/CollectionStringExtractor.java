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

package com.sun.jersey.impl.model.parameter.multivalued;

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
abstract class CollectionStringExtractor<V extends Collection<String>> 
        implements MultivaluedParameterExtractor {
    final String parameter;
    final String defaultValue;

    protected CollectionStringExtractor(String parameter, String defaultValue) {
        this.parameter = parameter;
        this.defaultValue = defaultValue;
    }

    public Object extract(MultivaluedMap<String, String> parameters) {
        List<String> stringList = parameters.get(parameter);
        if (stringList != null) {
            V copy = getInstance();
            copy.addAll(stringList);
            return copy;
        } else if (defaultValue != null) {
            V l = getInstance();
            l.add(defaultValue);
            return l;
        }

        return null;
    }
    
    protected abstract V getInstance();
    
    private static final class ListString extends CollectionStringExtractor<List<String>> {
        public ListString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected List<String> getInstance() {
            return new ArrayList<String>();
        }   
    }
    
    private static final class SetString extends CollectionStringExtractor<Set<String>> {
        public SetString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected Set<String> getInstance() {
            return new HashSet<String>();
        }   
    }
    
    private static final class SortedSetString extends CollectionStringExtractor<SortedSet<String>> {
        public SortedSetString(String parameter, String defaultValue) {
            super(parameter, defaultValue);
        }

        @Override
        protected SortedSet<String> getInstance() {
            return new TreeSet<String>();
        }   
    }
    
    static MultivaluedParameterExtractor getInstance(Class c, 
            String parameter, String defaultValue) {
        if (List.class == c)
            return new ListString(parameter, defaultValue);
        else if (Set.class == c)
            return new SetString(parameter, defaultValue);
        else if (SortedSet.class == c)
            return new SortedSetString(parameter, defaultValue);
        else
            throw new RuntimeException();
    }
}
