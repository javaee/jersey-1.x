/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.server.linking;

import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.PropertyNotFoundException;

/**
 * Describes an entity in terms of its fields, bean properties and {@link Link}
 * annotated fields.
 * @author mh124079
 */
public class EntityDescriptor {

    // Maintains an internal static cache to optimize processing

    private static Map<Class<?>, EntityDescriptor> descriptors
        = new HashMap<Class<?>, EntityDescriptor>();

    public static synchronized EntityDescriptor getInstance(Class<?> entityClass) {
        if (descriptors.containsKey(entityClass)) {
            return descriptors.get(entityClass);
        } else {
            EntityDescriptor descriptor = new EntityDescriptor(entityClass);
            descriptors.put(entityClass, descriptor);
            return descriptor;
        }
    }

    // instance

    private Map<String, FieldDescriptor> nonLinkFields;
    private Map<String, LinkFieldDescriptor> linkFields;
    private Map<String, Method> beanPropertyGetters;

    /**
     * Construct an new descriptor by inspecting the supplied class.
     * @param entityClass
     */
    private EntityDescriptor(Class<?> entityClass) {

        // create a list of field names
        this.nonLinkFields = new HashMap<String, FieldDescriptor>();
        this.linkFields = new HashMap<String, LinkFieldDescriptor>();
        findFields(entityClass);
        this.nonLinkFields = Collections.unmodifiableMap(this.nonLinkFields);
        this.linkFields = Collections.unmodifiableMap(this.linkFields);

        // create map of bean property names to getter methods
        beanPropertyGetters = new HashMap<String, Method>();
        findGetters(entityClass);
        beanPropertyGetters = Collections.unmodifiableMap(beanPropertyGetters);
    }

    public Collection<LinkFieldDescriptor> getLinkFields() {
        return linkFields.values();
    }

    public Collection<FieldDescriptor> getNonLinkFields() {
        return nonLinkFields.values();
    }

    /**
     * Get a map of bean property names to values
     * @param parameterNames
     * @param entity
     * @return
     */
    public Map<String, Object> getValueMap(List<String> parameterNames, Object entity) {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        Set<String> parameterNameSet = new HashSet<String>(parameterNames);
        for (String parameterName: parameterNameSet) {
            valueMap.put(parameterName, getValue(parameterName, entity));
        }
        return valueMap;
    }

    public Object getValue(String parameterName, Object entity) {
        // bean getters take precedence over similarly named fields
        if (beanPropertyGetters.containsKey(parameterName)) {
            Method getter = beanPropertyGetters.get(parameterName);
            try {
                Object value = getter.invoke(entity);
                return value;
            } catch (Exception ex) {
                Logger.getLogger(EntityDescriptor.class.getName()).log(Level.SEVERE, null, ex);
                throw new PropertyNotFoundException(parameterName, ex);
            }
        } else if (nonLinkFields.containsKey(parameterName)) {
            FieldDescriptor desc = nonLinkFields.get(parameterName);
            Object value = desc.getFieldValue(entity);
            return value;
        } else {
            throw new PropertyNotFoundException(parameterName);
        }
    }

    /**
     * Find and cache bean property getter methods. Only public methods are
     * used.
     * @param entityClass
     */
    private void findGetters(Class<?> entityClass) {
        Iterator<AnnotatedMethod> beanProperties = new MethodList(entityClass).nameStartsWith("get").hasNumParams(0).iterator();
        while (beanProperties.hasNext()) {
            AnnotatedMethod computedProperty = beanProperties.next();
            Method getter = computedProperty.getMethod();
            int flags = getter.getModifiers();
            String beanPropertyName = getter.getName().substring(3);
            if (Modifier.isPublic(flags) && beanPropertyName.length() > 0) {
                beanPropertyName = beanPropertyName.substring(0, 1).toLowerCase() + beanPropertyName.substring(1);
                if (!beanPropertyGetters.containsKey(beanPropertyName)) {
                    beanPropertyGetters.put(beanPropertyName, getter);
                }
            }
        }
    }

    /**
     * Find and cache the fields of the supplied class and its superclasses and
     * interfaces.
     * @param entityClass the class
     */
    private void findFields(Class<?> entityClass) {
        for (Field f: entityClass.getDeclaredFields()) {
            Link a = f.getAnnotation(Link.class);
            if (a != null) {
                Class<?> t = f.getType();
                if (t.equals(String.class) || t.equals(URI.class)) {
                    if (!linkFields.containsKey(f.getName())) {
                        linkFields.put(f.getName(), new LinkFieldDescriptor(f, a, t));
                    }
                } else {
                    // TODO unsupported type
                }
            } else {
                nonLinkFields.put(f.getName(), new FieldDescriptor(f));
            }
        }

        // look for nonLinkFields in superclasses
        Class<?> sc = entityClass.getSuperclass();
        if (sc != null && sc != Object.class) {
            findFields(sc);
        }

        // look for nonLinkFields in interfaces
        for (Class<?> ic : entityClass.getInterfaces()) {
            findFields(ic);
        }
    }
}
