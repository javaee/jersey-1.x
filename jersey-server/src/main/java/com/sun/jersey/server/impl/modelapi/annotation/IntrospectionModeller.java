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
package com.sun.jersey.server.impl.modelapi.annotation;

import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.api.model.AbstractField;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSetterMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.model.Parameter.Source;
import com.sun.jersey.api.model.Parameterized;
import com.sun.jersey.api.model.PathValue;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.impl.ImplMessages;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author japod
 */
public class IntrospectionModeller {

    private static final Logger LOGGER = Logger.getLogger(IntrospectionModeller.class.getName());

    public static final AbstractResource createResource(Class<?> resourceClass) {
        final Class<?> annotatedResourceClass = getAnnotatedResourceClass(resourceClass);
        final Path rPathAnnotation = annotatedResourceClass.getAnnotation(Path.class);
        final boolean isRootResourceClass = (null != rPathAnnotation);

        final boolean isEncodedAnotOnClass = 
                (null != annotatedResourceClass.getAnnotation(Encoded.class));

        AbstractResource resource;

        if (isRootResourceClass) {
            resource = new AbstractResource(resourceClass,
                    new PathValue(rPathAnnotation.value()));
        } else { // just a subresource class
            resource = new AbstractResource(resourceClass);
        }

        workOutConstructorsList(resource, resourceClass.getConstructors(), 
                isEncodedAnotOnClass);

        workOutFieldsList(resource, isEncodedAnotOnClass);
        
        final MethodList methodList = new MethodList(resourceClass);

        workOutSetterMethodsList(resource, methodList, isEncodedAnotOnClass);
        
        final Consumes classScopeConsumesAnnotation = 
                annotatedResourceClass.getAnnotation(Consumes.class);
        final Produces classScopeProducesAnnotation = 
                annotatedResourceClass.getAnnotation(Produces.class);
        workOutResourceMethodsList(resource, methodList, isEncodedAnotOnClass, 
                classScopeConsumesAnnotation, classScopeProducesAnnotation);
        workOutSubResourceMethodsList(resource, methodList, isEncodedAnotOnClass, 
                classScopeConsumesAnnotation, classScopeProducesAnnotation);
        workOutSubResourceLocatorsList(resource, methodList, isEncodedAnotOnClass);

        workOutPostConstructPreDestroy(resource, methodList);
        
        logNonPublicMethods(resourceClass);

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(ImplMessages.NEW_AR_CREATED_BY_INTROSPECTION_MODELER(
                    resource.toString()));
        }

        return resource;
    }
    
    private static final Class getAnnotatedResourceClass(Class rc) {
        if (rc.isAnnotationPresent(Path.class)) return rc;

        for (Class i : rc.getInterfaces())
            if (i.isAnnotationPresent(Path.class)) return i;

        return rc;
    }
    
    private static final void addConsumes(
            AnnotatedMethod am,            
            AbstractResourceMethod resourceMethod, 
            Consumes consumeMimeAnnotation) {
        // Override annotation is present in method
        if (am.isAnnotationPresent(Consumes.class))
            consumeMimeAnnotation = am.getAnnotation(Consumes.class);
        
        resourceMethod.getSupportedInputTypes().addAll(
                MediaTypes.createMediaTypes(consumeMimeAnnotation));
    }

    private static final void addProduces(
            AnnotatedMethod am,
            AbstractResourceMethod resourceMethod, 
            Produces produceMimeAnnotation) {
        // Override annotation is present in method
        if (am.isAnnotationPresent(Produces.class))
            produceMimeAnnotation = am.getAnnotation(Produces.class);
        
        resourceMethod.setAreInputTypesDeclared(produceMimeAnnotation != null);
        resourceMethod.getSupportedOutputTypes().addAll(
                MediaTypes.createMediaTypes(produceMimeAnnotation));
    }

    private static final void workOutConstructorsList(
            AbstractResource resource, 
            Constructor[] ctorArray, 
            boolean isEncoded) {
        if (null != ctorArray) {
            for (Constructor ctor : ctorArray) {
                final AbstractResourceConstructor aCtor = 
                        new AbstractResourceConstructor(ctor);
                processParameters(aCtor, ctor, isEncoded);
                resource.getConstructors().add(aCtor);
            }
        }
    }

    private static final void workOutFieldsList(
            AbstractResource resource, 
            boolean isEncoded) {        
        Class c = resource.getResourceClass();
        if (c.isInterface())
            return;
        
        while (c != Object.class) {
             for (final Field f : c.getDeclaredFields()) {
                    final AbstractField af = new AbstractField(f);
                    Parameter p = createParameter(f.toString(), 1, isEncoded, 
                            f.getType(),
                            f.getGenericType(),
                            f.getAnnotations());
                    if (null != p) {
                        af.getParameters().add(p);
                        resource.getFields().add(af);
                    }
             }
             c = c.getSuperclass();
        }
    }

    private static void workOutPostConstructPreDestroy(
            AbstractResource resource,
            MethodList methodList) {
        Class postConstruct = ReflectionHelper.classForName("javax.annotation.PostConstruct");
        if (postConstruct == null)
            return;

        Class preDestroy = ReflectionHelper.classForName("javax.annotation.PreDestroy");

        for (AnnotatedMethod m : methodList.
                hasAnnotation(postConstruct).
                hasNumParams(0).
                hasReturnType(void.class)) {
            resource.getPostConstructMethods().add(m.getMethod());
        }

        for (AnnotatedMethod m : methodList.
                hasAnnotation(preDestroy).
                hasNumParams(0).
                hasReturnType(void.class)) {
            resource.getPreDestroyMethods().add(m.getMethod());
        }
    }

    private static final void workOutSetterMethodsList(
            AbstractResource resource, 
            MethodList methodList,
            boolean isEncoded) {
        for (AnnotatedMethod m : methodList.
                hasNotMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class).
                hasNumParams(1).
                hasReturnType(void.class).
                nameStartsWith("set")) {
            
            final AbstractSetterMethod asm = new AbstractSetterMethod(resource, m.getMethod(), m.getAnnotations());
            Parameter p = createParameter(m.toString(), 1, isEncoded, 
                    m.getParameterTypes()[0],
                    m.getGenericParameterTypes()[0],
                    m.getAnnotations());
            if (null != p) {
                asm.getParameters().add(p);
                resource.getSetterMethods().add(asm);
            }
        }        
    }
    
    private static final void workOutResourceMethodsList(
            AbstractResource resource, 
            MethodList methodList, 
            boolean isEncoded,
            Consumes classScopeConsumesAnnotation, 
            Produces classScopeProducesAnnotation) {
        for (AnnotatedMethod m : methodList.hasMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class)) {
            final AbstractResourceMethod resourceMethod = new AbstractResourceMethod(
                    resource,
                    m.getMethod(), 
                    m.getMetaMethodAnnotations(HttpMethod.class).get(0).value(),
                    m.getAnnotations());

            addConsumes(m, resourceMethod, classScopeConsumesAnnotation);
            addProduces(m, resourceMethod, classScopeProducesAnnotation);
            processParameters(resourceMethod, m, isEncoded);

            resource.getResourceMethods().add(resourceMethod);
        }
    }
    
    private static final void workOutSubResourceMethodsList(
            AbstractResource resource, 
            MethodList methodList, 
            boolean isEncoded,
            Consumes classScopeConsumesAnnotation, 
            Produces classScopeProducesAnnotation) {
        for (AnnotatedMethod m : methodList.hasMetaAnnotation(HttpMethod.class).
                hasAnnotation(Path.class)) {
            final Path mPathAnnotation = m.getAnnotation(Path.class);
            final AbstractSubResourceMethod subResourceMethod = new AbstractSubResourceMethod(
                    resource,
                    m.getMethod(),
                    new PathValue(
                        mPathAnnotation.value()),
                    m.getMetaMethodAnnotations(HttpMethod.class).get(0).value(),
                    m.getAnnotations());
       
            addConsumes(m, subResourceMethod, classScopeConsumesAnnotation);
            addProduces(m, subResourceMethod, classScopeProducesAnnotation);
            processParameters(subResourceMethod, m, isEncoded);

            resource.getSubResourceMethods().add(subResourceMethod);
        }
    }
    
    private static final void workOutSubResourceLocatorsList(
            AbstractResource resource, 
            MethodList methodList, 
            boolean isEncoded) {

        for (AnnotatedMethod m : methodList.hasNotMetaAnnotation(HttpMethod.class).
                hasAnnotation(Path.class)) {
            final Path mPathAnnotation = m.getAnnotation(Path.class);
            final AbstractSubResourceLocator subResourceLocator = new AbstractSubResourceLocator(
                    resource,
                    m.getMethod(),
                    new PathValue(
                        mPathAnnotation.value()),
                    m.getAnnotations());

            processParameters(subResourceLocator, m, isEncoded);

            resource.getSubResourceLocators().add(subResourceLocator);
        }
    }

    private static final void processParameters(
            Parameterized parametrized, 
            Constructor ctor, 
            boolean isEncoded) {
        processParameters(
                ctor.toString(),
                parametrized,
                ((null != ctor.getAnnotation(Encoded.class)) || isEncoded),
                ctor.getParameterTypes(), 
                ctor.getGenericParameterTypes(),
                ctor.getParameterAnnotations());
    }

    private static final void processParameters(
            Parameterized parametrized, 
            AnnotatedMethod method, 
            boolean isEncoded) {
        processParameters(
                method.toString(),
                parametrized,
                ((null != method.getAnnotation(Encoded.class)) || isEncoded),
                method.getParameterTypes(), 
                method.getGenericParameterTypes(), 
                method.getParameterAnnotations());
    }

    private static final void processParameters(
            String nameForLogging,
            Parameterized parametrized,
            boolean isEncoded,
            Class[] parameterTypes,
            Type[] genericParameterTypes,
            Annotation[][] parameterAnnotations) {

        for (int i = 0; i < parameterTypes.length; i++) {
            Parameter parameter = createParameter(
                    nameForLogging, 
                    i + 1,
                    isEncoded, parameterTypes[i], 
                    genericParameterTypes[i], 
                    parameterAnnotations[i]);
            if (null != parameter) {
                parametrized.getParameters().add(parameter);
            } else {
                // clean up the parameters
                parametrized.getParameters().removeAll(parametrized.getParameters());
                break;
            }
        }
    }

    private static interface ParamAnnotationHelper<T extends Annotation> {

        public String getValueOf(T a);

        public Parameter.Source getSource();
    }

    private static Map<Class, ParamAnnotationHelper> createParamAnotHelperMap() {
        Map<Class, ParamAnnotationHelper> m = new WeakHashMap<Class, ParamAnnotationHelper>();
        m.put(Context.class, new ParamAnnotationHelper<Context>() {

            public String getValueOf(Context a) {
                return null;
            }

            public Parameter.Source getSource() {
                return Parameter.Source.CONTEXT;
            }
        });
        m.put(HeaderParam.class, new ParamAnnotationHelper<HeaderParam>() {

            public String getValueOf(HeaderParam a) {
                return a.value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.HEADER;
            }
        });
        m.put(CookieParam.class, new ParamAnnotationHelper<CookieParam>() {

            public String getValueOf(CookieParam a) {
                return a.value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.COOKIE;
            }
        });
        m.put(MatrixParam.class, new ParamAnnotationHelper<MatrixParam>() {

            public String getValueOf(MatrixParam a) {
                return a.value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.MATRIX;
            }
        });
        m.put(QueryParam.class, new ParamAnnotationHelper<QueryParam>() {

            public String getValueOf(QueryParam a) {
                return a.value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.QUERY;
            }
        });
        m.put(PathParam.class, new ParamAnnotationHelper<PathParam>() {

            public String getValueOf(PathParam a) {
                return a.value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.PATH;
            }
        });
        return Collections.unmodifiableMap(m);
    }
    private final static Map<Class, ParamAnnotationHelper> ANOT_HELPER_MAP = 
            createParamAnotHelperMap();

    @SuppressWarnings("unchecked")
    private static final Parameter createParameter(
            String nameForLogging, 
            int order,
            boolean isEncoded, 
            Class<?> paramClass, 
            Type paramType, 
            Annotation[] annotations) {

        if (null == annotations) {
            return null;
        }

        Annotation paramAnnotation = null;
        Parameter.Source paramSource = null;
        String paramName = null;
        boolean paramEncoded = isEncoded;
        String paramDefault = null;
        
        /**
         * Create a parameter from the list of annotations.
         * Unknown annotated parameters are also supported, and in such a
         * cases the last unrecognized annotation is taken to be that
         * associated with the parameter.
         */
        for (Annotation annotation : annotations) {
            if (ANOT_HELPER_MAP.containsKey(annotation.annotationType())) {
                ParamAnnotationHelper helper = ANOT_HELPER_MAP.get(annotation.annotationType());
                paramAnnotation = annotation;
                paramSource = helper.getSource();
                paramName = helper.getValueOf(annotation);
            } else if (Encoded.class == annotation.annotationType()) {
                paramEncoded = true;
            } else if (DefaultValue.class == annotation.annotationType()) {
                paramDefault = ((DefaultValue) annotation).value();
            } else {
                paramAnnotation = annotation; 
                paramSource = Source.UNKNOWN;
                paramName = getValue(annotation);
            }
        }

        if (paramAnnotation == null) {
            paramSource = Parameter.Source.ENTITY;
        }

        return new Parameter(annotations, paramAnnotation, paramSource, paramName, paramType, 
                paramClass, paramEncoded, paramDefault);
    }

    private static final String getValue(Annotation a) {
        try {
            Method m = a.annotationType().getMethod("value");
            if (m.getReturnType() != String.class)
                return null;
            return (String)m.invoke(a);
        } catch (Exception ex) {
        }
        return null;
    }
    
    private static void logNonPublicMethods(final Class resourceClass) {
        assert null != resourceClass;
        
        if (!LOGGER.isLoggable(Level.WARNING)) {
            return; // does not make sense to check when logging is disabled anyway
        }
        
        final MethodList declaredMethods = new MethodList(
                getDeclaredMethods(resourceClass));

        // non-public resource methods
        for (AnnotatedMethod m : declaredMethods.hasMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class).isNotPublic()) {
            LOGGER.warning(ImplMessages.NON_PUB_RES_METHOD(m.getMethod().toGenericString()));
        }
        // non-public subres methods
        for (AnnotatedMethod m : declaredMethods.hasMetaAnnotation(HttpMethod.class).
                hasAnnotation(Path.class).isNotPublic()) {
            LOGGER.warning(ImplMessages.NON_PUB_SUB_RES_METHOD(m.getMethod().toGenericString()));
        }
        // non-public subres locators
        for (AnnotatedMethod m : declaredMethods.hasNotMetaAnnotation(HttpMethod.class).
                hasAnnotation(Path.class).isNotPublic()) {
            LOGGER.warning(ImplMessages.NON_PUB_SUB_RES_LOC(m.getMethod().toGenericString()));
        }
    }
    
    private static List<Method> getDeclaredMethods(final Class _c) {
        final List<Method> ml = new ArrayList<Method>();
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            Class c = _c;
            public Object run() {
                while (c != Object.class && c != null) {
                    for (Method m : c.getDeclaredMethods()) ml.add(m);
                    c = c.getSuperclass();
                }
                return null;
            }
        });
                
        return ml;
    }
}