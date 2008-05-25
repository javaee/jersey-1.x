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

package com.sun.jersey.impl.modelapi.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
class MethodList implements Iterable<AnnotatedMethod> {

    private AnnotatedMethod[] methods;
    
    public MethodList(Class c) {
        this(c.getMethods());
    }
    
    public MethodList(List<Method> methods) {
        this.methods = new AnnotatedMethod[methods.size()];
        for (int i = 0; i < methods.size(); i++)
            this.methods[i] = new AnnotatedMethod(methods.get(i));        
    }
    
    public MethodList(Method... methods) {
        this.methods = new AnnotatedMethod[methods.length];
        for (int i = 0; i < methods.length; i++)
            this.methods[i] = new AnnotatedMethod(methods[i]);
    }
    
    public MethodList(AnnotatedMethod... methods) {        
        this.methods = methods;
    }

    public Iterator<AnnotatedMethod> iterator() {
      return Arrays.asList(methods).iterator();
    }
    
    <T extends Annotation> MethodList isNotPublic() {
        return filter(new Filter() {
            public boolean keep(AnnotatedMethod m) {
                return !Modifier.isPublic(m.getMethod().getModifiers());
            }
        });
    }
    
    <T extends Annotation> MethodList hasAnnotation(final Class<T> annotation) {
        return filter(new Filter() {
            public boolean keep(AnnotatedMethod m) {
                return m.getAnnotation(annotation) != null;
            }
        });
    }
    
    <T extends Annotation> MethodList hasMetaAnnotation(final Class<T> annotation) {
        return filter(new Filter() {
            public boolean keep(AnnotatedMethod m) {
                for (Annotation a : m.getAnnotations()) {
                    if (a.annotationType().getAnnotation(annotation) != null)
                        return true;
                }
                return false;
            }
        });
    }
    
    <T extends Annotation> MethodList hasNotAnnotation(final Class<T> annotation) {
        return filter(new Filter() {
            public boolean keep(AnnotatedMethod m) {
                return m.getAnnotation(annotation) == null;
            }
        });
    }
    
    <T extends Annotation> MethodList hasNotMetaAnnotation(final Class<T> annotation) {
        return filter(new Filter() {
            public boolean keep(AnnotatedMethod m) {
                for (Annotation a : m.getAnnotations()) {
                    if (a.annotationType().getAnnotation(annotation) != null)
                        return false;
                }                
                return true;
            }
        });
    }
    
    private interface Filter {
        boolean keep(AnnotatedMethod m);
    }
    
    private MethodList filter(Filter f) {
        List<AnnotatedMethod> r = new ArrayList<AnnotatedMethod>();
        for (AnnotatedMethod m : methods)
            if (f.keep(m))
                r.add(m);
        return new MethodList(r.toArray(new AnnotatedMethod[0]));
    }
}