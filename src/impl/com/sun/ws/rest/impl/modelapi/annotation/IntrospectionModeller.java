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
package com.sun.ws.rest.impl.modelapi.annotation;

import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceConstructor;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.api.model.AbstractSubResourceLocator;
import com.sun.ws.rest.api.model.AbstractSubResourceMethod;
import com.sun.ws.rest.api.model.Parameter;
import com.sun.ws.rest.api.model.Parameterized;
import com.sun.ws.rest.api.model.UriPathValue;
import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.model.MediaTypeHelper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author japod
 */
public class IntrospectionModeller {

    private static final Logger LOGGER = Logger.getLogger(IntrospectionModeller.class.getName());

    public static final AbstractResource createResource(Class<?> resourceClass) {
        final Path rPathAnnotation = resourceClass.getAnnotation(Path.class);
        final boolean isRootResourceClass = (null != rPathAnnotation);

        final boolean isEncodedAnotOnClass = (null != resourceClass.getAnnotation(Encoded.class));

        AbstractResource resource;

        if (isRootResourceClass) {
            resource = new AbstractResource(resourceClass,
                    new UriPathValue(rPathAnnotation.value(), rPathAnnotation.encode(), rPathAnnotation.limited()));
        } else { // just a subresource class
            resource = new AbstractResource(resourceClass);
        }

        workOutConstructorsList(resource, resourceClass.getConstructors(), isEncodedAnotOnClass);

        final MethodList methodList = new MethodList(resourceClass);

        final ConsumeMime classScopeConsumeMimeAnnotation = resourceClass.getAnnotation(ConsumeMime.class);
        final ProduceMime classScopeProduceMimeAnnotation = resourceClass.getAnnotation(ProduceMime.class);
        workOutResourceMethodsList(resource, methodList, isEncodedAnotOnClass, classScopeConsumeMimeAnnotation, classScopeProduceMimeAnnotation);
        workOutSubResourceMethodsList(resource, methodList, isEncodedAnotOnClass, classScopeConsumeMimeAnnotation, classScopeProduceMimeAnnotation);
        workOutSubResourceLocatorsList(resource, methodList, isEncodedAnotOnClass);
        
        logNonPublicMethods(resourceClass);

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(ImplMessages.NEW_AR_CREATED_BY_INTROSPECTION_MODELER(resource.toString()));
        }

        return resource;
    }
    
    private static final void findOutConsumeMimeTypes(
            AbstractResourceMethod resourceMethod, ConsumeMime classScopeConsumeMimeAnnotation) {

        if (resourceMethod.getMethod().isAnnotationPresent(ConsumeMime.class)) {
            final ConsumeMime consumeMimeAnnotation = resourceMethod.getMethod().getAnnotation(ConsumeMime.class);
            resourceMethod.getSupportedInputTypes().addAll(MediaTypeHelper.createMediaTypes(consumeMimeAnnotation));
        } else { // have to use the annotation from class
            resourceMethod.getSupportedInputTypes().addAll(MediaTypeHelper.createMediaTypes(classScopeConsumeMimeAnnotation));
        }
    }

    private static final void findOutProduceMimeTypes(
            AbstractResourceMethod resourceMethod, ProduceMime classScopeProduceMimeAnnotation) {

        if (resourceMethod.getMethod().isAnnotationPresent(ProduceMime.class)) {
            final ProduceMime produceMimeAnnotation = resourceMethod.getMethod().getAnnotation(ProduceMime.class);
            resourceMethod.getSupportedOutputTypes().addAll(MediaTypeHelper.createMediaTypes(produceMimeAnnotation));
        } else { // have to use the annotation from class
            resourceMethod.getSupportedOutputTypes().addAll(MediaTypeHelper.createMediaTypes(classScopeProduceMimeAnnotation));
        }
    }

    private static final void workOutConstructorsList(AbstractResource resource, Constructor[] ctorArray, boolean isEncoded) {
        if (null != ctorArray) {
            for (Constructor ctor : ctorArray) {
                final AbstractResourceConstructor aCtor = new AbstractResourceConstructor(ctor);
                processParameters(aCtor, ctor, isEncoded);
                resource.getConstructors().add(aCtor);
            }
        }
    }

    private static final void workOutResourceMethodsList(
            AbstractResource resource, MethodList methodList, boolean isEncoded,
            ConsumeMime classScopeConsumeMimeAnnotation, ProduceMime classScopeProduceMimeAnnotation) {

        // TODO what to do about multiple meta HttpMethod annotations?
        // Should the method be added more than once to the model and
        // then validation could reject?
        for (Method method : methodList.hasMetaAnnotation(HttpMethod.class).hasNotAnnotation(Path.class)) {
            final AbstractResourceMethod resourceMethod = new AbstractResourceMethod(
                    resource,
                    method, 
                    getMetaAnnotations(method, HttpMethod.class).get(0).value());

            findOutConsumeMimeTypes(resourceMethod, classScopeConsumeMimeAnnotation);
            findOutProduceMimeTypes(resourceMethod, classScopeProduceMimeAnnotation);
            processParameters(resourceMethod, resourceMethod.getMethod(), isEncoded);

            resource.getResourceMethods().add(resourceMethod);
        }
    }
    
    private static final void workOutSubResourceMethodsList(
            AbstractResource resource, MethodList methodList, boolean isEncoded,
            ConsumeMime classScopeConsumeMimeAnnotation, ProduceMime classScopeProduceMimeAnnotation) {

        // TODO what to do about multiple meta HttpMethod annotations?
        // Should the method be added more than once to the model and
        // then validation could reject?
        for (Method method : methodList.hasMetaAnnotation(HttpMethod.class).hasAnnotation(Path.class)) {
            final Path mPathAnnotation = method.getAnnotation(Path.class);
            final AbstractSubResourceMethod subResourceMethod = new AbstractSubResourceMethod(
                    resource,
                    method,
                    new UriPathValue(mPathAnnotation.value(), mPathAnnotation.encode(), mPathAnnotation.limited()),
                    getMetaAnnotations(method, HttpMethod.class).get(0).value());
       
            findOutConsumeMimeTypes(subResourceMethod, classScopeConsumeMimeAnnotation);
            findOutProduceMimeTypes(subResourceMethod, classScopeProduceMimeAnnotation);
            processParameters(subResourceMethod, subResourceMethod.getMethod(), isEncoded);

            resource.getSubResourceMethods().add(subResourceMethod);
        }
    }
    
    /**
     * Return a list of specific meta-annotations decared on annotations of
     * a method.
     */
    private static final <T extends Annotation> List<T> getMetaAnnotations(
            Method m, Class<T> annotation) {
        List <T> ma = new ArrayList<T>();
        for (Annotation a : m.getAnnotations()) {
            if (a.annotationType().getAnnotation(annotation) != null) {
                ma.add(a.annotationType().getAnnotation(annotation));
            }
        }
        
        return ma;
    }
    
    private static final void workOutSubResourceLocatorsList(AbstractResource resource, MethodList methodList, boolean isEncoded) {

        for (Method method : methodList.hasNotMetaAnnotation(HttpMethod.class).hasAnnotation(Path.class)) {
            final Path mPathAnnotation = method.getAnnotation(Path.class);
            final AbstractSubResourceLocator subResourceLocator = new AbstractSubResourceLocator(
                    method,
                    new UriPathValue(mPathAnnotation.value(), mPathAnnotation.encode(), mPathAnnotation.limited()));

            processParameters(subResourceLocator, subResourceLocator.getMethod(), isEncoded);

            resource.getSubResourceLocators().add(subResourceLocator);
        }
    }

    private static final void processParameters(Parameterized parametrized, Constructor ctor, boolean isEncoded) {
        processParameters(
                ctor.toString(),
                parametrized,
                ((null != ctor.getAnnotation(Encoded.class)) || isEncoded),
                ctor.getParameterTypes(), ctor.getGenericParameterTypes(), ctor.getParameterAnnotations());
    }

    private static final void processParameters(Parameterized parametrized, Method method, boolean isEncoded) {
        processParameters(
                method.toString(),
                parametrized,
                ((null != method.getAnnotation(Encoded.class)) || isEncoded),
                method.getParameterTypes(), method.getGenericParameterTypes(), method.getParameterAnnotations());
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
                    nameForLogging, i + 1,
                    isEncoded, parameterTypes[i], genericParameterTypes[i], parameterAnnotations[i]);
            if (null != parameter) {
                parametrized.getParameters().add(parameter);
            } else {
                // clean up the parameters
                parametrized.getParameters().removeAll(parametrized.getParameters());
                break;
            }
        }
    }

    private static interface ParamAnnotationHelper {

        public String getValueOf(Annotation a);

        public Parameter.Source getSource();
    }

    private static Map<Class, ParamAnnotationHelper> createParamAnotHelperMap() {
        Map<Class, ParamAnnotationHelper> m = new WeakHashMap<Class, ParamAnnotationHelper>();
        m.put(Context.class, new ParamAnnotationHelper() {

            public String getValueOf(Annotation a) {
                return null;
            }

            public Parameter.Source getSource() {
                return Parameter.Source.CONTEXT;
            }
        });
        m.put(HeaderParam.class, new ParamAnnotationHelper() {

            public String getValueOf(Annotation a) {
                return ((HeaderParam) a).value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.HEADER;
            }
        });
        m.put(MatrixParam.class, new ParamAnnotationHelper() {

            public String getValueOf(Annotation a) {
                return ((MatrixParam) a).value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.MATRIX;
            }
        });
        m.put(QueryParam.class, new ParamAnnotationHelper() {

            public String getValueOf(Annotation a) {
                return ((QueryParam) a).value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.QUERY;
            }
        });
        m.put(PathParam.class, new ParamAnnotationHelper() {

            public String getValueOf(Annotation a) {
                return ((PathParam) a).value();
            }

            public Parameter.Source getSource() {
                return Parameter.Source.URI;
            }
        });
        return Collections.unmodifiableMap(m);
    }
    private final static Map<Class, ParamAnnotationHelper> ANOT_HELPER_MAP = createParamAnotHelperMap();

    private static final Parameter createParameter(
            String nameForLogging, int order,
            boolean isEncoded, Class<?> paramClass, Type paramType, Annotation[] annotations) {

        if (null == annotations) {
            return null;
        }

        Parameter.Source paramSource = null;
        String paramName = null;
        boolean paramEncoded = isEncoded;

        String paramDefault = null;

        for (Annotation annotation : annotations) {
            if (ANOT_HELPER_MAP.containsKey(annotation.annotationType())) {
                ParamAnnotationHelper helper = ANOT_HELPER_MAP.get(annotation.annotationType());
                if (null != paramSource) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.warning(ImplMessages.AMBIGUOUS_PARAMETER(nameForLogging, Integer.toString(order)));
                    }
                // TODO: throw an exception ?
                }
                paramSource = helper.getSource();
                paramName = helper.getValueOf(annotation);
            } else if (Encoded.class == annotation.annotationType()) {
                paramEncoded = true;
            } else if (DefaultValue.class == annotation.annotationType()) {
                paramDefault = ((DefaultValue) annotation).value();
            }// TODO: should we really ignore unknown annotations ?
        }

        if (paramSource == null) {
            paramSource = Parameter.Source.ENTITY;
        }

        return new Parameter(paramSource, paramName, paramType, paramClass, paramEncoded, paramDefault);
    }

    private static final boolean isNotPublic(Method method) {
        assert null != method;
        return !Modifier.isPublic(method.getModifiers());
    }

    private static void logNonPublicMethods(final Class resourceClass) {
        assert null != resourceClass;
        
        if (!LOGGER.isLoggable(Level.WARNING)) {
            return; // does not make sense to check when logging is disabled anyway
        }
        
        final MethodList declaredMethods = new MethodList(resourceClass.getDeclaredMethods());
        // non-public resource methods
        for (Method method : declaredMethods.hasMetaAnnotation(HttpMethod.class).hasNotAnnotation(Path.class)) {
            if (isNotPublic(method)) {
                LOGGER.warning(ImplMessages.NON_PUB_RES_METHOD(method.toGenericString()));
            }
        }
        // non-public subres methods
        for (Method method : declaredMethods.hasMetaAnnotation(HttpMethod.class).hasAnnotation(Path.class)) {
            if (isNotPublic(method)) {
                LOGGER.warning(ImplMessages.NON_PUB_SUB_RES_METHOD(method.toGenericString()));
            }
        }
        // non-public subres locators
        for (Method method : declaredMethods.hasNotMetaAnnotation(HttpMethod.class).hasAnnotation(Path.class)) {
            if (isNotPublic(method)) {
                LOGGER.warning(ImplMessages.NON_PUB_SUB_RES_LOC(method.toGenericString()));
            }
        }
    }
}
