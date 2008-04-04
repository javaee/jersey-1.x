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

import com.sun.ws.rest.api.container.ContainerException;
import com.sun.ws.rest.impl.ImplMessages;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ReflectionHelper {
    @SuppressWarnings("unchecked")
    public static <A> A getAnnotiationType(Class<A> c, Annotation[] annotations) {
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == c)
                return (A)annotations[i];
        }
        
        return null;
    }
    
    /**
     * Ascertain if an annotation is a member of an array of annotations.
     */
    public static boolean hasAnnotation(Class<?> c, Annotation[] annotations) {
        for (Annotation a : annotations) 
            if (a.annotationType() == c)
                return true;
        
        return false;
    }
        
    /**
     * Ascertain if an annotation is a member of an array of annotations,
     * if not present check if the annotation is present on an accessible
     * object,
     * if not present check if the annotation is present on the declaring class
     * of the accessible object.
     */
    public static boolean hasAnnotation(Class<?> c, 
            Annotation[] annotations,
            Class<?> declaringClass,
            AccessibleObject ao) {
        if (hasAnnotation(c, annotations))
            return true;
 
        if (hasAnnotation(c, ao.getDeclaredAnnotations()))
            return true;

        return hasAnnotation(c, declaringClass.getDeclaredAnnotations());
    }
    
    public static Class getGenericClass(Type parameterType) {
        if (!(parameterType instanceof ParameterizedType)) return null;
        
        ParameterizedType type = (ParameterizedType)parameterType;        
        Type[] genericTypes = type.getActualTypeArguments();
        if (genericTypes.length != 1) return null;
        
        Type genericType = genericTypes[0];
        if (genericType instanceof Class) {
            return (Class)genericTypes[0];
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType)genericType;
            Type t = arrayType.getGenericComponentType();
            if (t instanceof Class) {
                Class c = (Class)t;
                try {
                    // TODO is there a better way to get the Class object 
                    // representing an array
                    Object o = Array.newInstance(c, 0);
                    return o.getClass();
                } catch (Exception e) {
                    throw new ContainerException(e);
                }
            } else {
                throw new ContainerException(ImplMessages.GENERIC_TYPE_NOT_SUPPORTED(genericType,
                                                                                              parameterType));
            }
        } else {
            throw new ContainerException(ImplMessages.GENERIC_TYPE_NOT_SUPPORTED(genericType,
                                                                                           parameterType));
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Method getValueOfStringMethod(Class c) {
        try {
            Method m = c.getDeclaredMethod("valueOf", String.class);
            if (!Modifier.isStatic(m.getModifiers())) {
                return null;
            }
            return m;
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Constructor getStringConstructor(Class c) {
        try {
            return c.getConstructor(String.class);
        } catch (Exception e) {
            return null;
        }
    }    
    
    /**
     * Given a type variable resolve the Java class of that variable.
     * 
     * @param c the concrete class from which all type variables are resolved
     * @param dc the declaring class where the type variable was defined
     * @param tv the type variable
     * @return the resolved Java class, otherwise null if the type variable
     *         could not be resolved
     */
    public static Class resolveTypeVariable(Class c, Class dc, TypeVariable tv) {
        return resolveTypeVariable(c, dc, tv, new HashMap<TypeVariable, Type>());
    }
    
    private static Class resolveTypeVariable(Class c, Class dc, TypeVariable tv, 
            Map<TypeVariable, Type> map) {
        ParameterizedType pt = (ParameterizedType)c.getGenericSuperclass();
        Type[] typeArguments = pt.getActualTypeArguments();
        
        Class sc = c.getSuperclass();        
        TypeVariable[] typeParameters = sc.getTypeParameters();
        
        Map<TypeVariable, Type> submap = new HashMap<TypeVariable, Type>();
        for (int i = 0; i < typeArguments.length; i++) {
            // Substitute a type variable with the Java class
            if (typeArguments[i] instanceof TypeVariable) {
                Type t = map.get(typeArguments[i]);
                submap.put(typeParameters[i], t);
            } else {
                submap.put(typeParameters[i], typeArguments[i]);
            }
        }
        
        if (sc == dc) {
            Type t = submap.get(tv);
            if (t instanceof Class) {
                return (Class)t;
            } else if (t instanceof GenericArrayType) {
                t = ((GenericArrayType)t).getGenericComponentType();
                if (t instanceof Class) {
                    c = (Class)t;
                    try {
                        // TODO is there a better way to get the Class object 
                        // representing an array
                        Object o = Array.newInstance(c, 0);
                        return o.getClass();
                    } catch (Exception e) {
                    } 
                    return null;
                } else {
                    return null;
                }
            } else if (t instanceof ParameterizedType) {
                pt = (ParameterizedType)t;
                // TODO support parameterized type whose raw type is a class
                return null;
            } else {
                return null;
            }
        } else {    
            return resolveTypeVariable(sc, dc, tv, submap);
        }
    }
    
}
