/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.jersey.server.impl.modelapi.validation;

import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.model.AbstractField;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSetterMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.model.Parameterized;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.impl.ImplMessages;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author japod
 */
public class BasicValidator extends AbstractModelValidator {

    public void visitAbstractResource(AbstractResource resource) {
        // uri template of the resource, if present should not contain null value
        if (resource.isRootResource() && ((null == resource.getPath()) || (null == resource.getPath().getValue()))) {
            issueList.add(new ResourceModelIssue(
                    resource,
                    ImplMessages.ERROR_RES_URI_PATH_INVALID(resource.getResourceClass(), resource.getPath()),
                    true)); // TODO: is it really a fatal issue?
        }

        checkNonPublicMethods(resource);
    }

    public void visitAbstractResourceConstructor(AbstractResourceConstructor constructor) {
        // TODO check parameters
    }

    public void visitAbstractField(AbstractField field) {
        final Field f = field.getField();
        checkParameter(field.getParameters().get(0), f, f.toGenericString(), f.getName());
    }

    public void visitAbstractSetterMethod(AbstractSetterMethod setterMethod) {
        final Method m = setterMethod.getMethod();
        checkParameter(setterMethod.getParameters().get(0), m, m.toGenericString(), "1");
    }

    public void visitAbstractResourceMethod(AbstractResourceMethod method) {
        checkParameters(method, method.getMethod());

        if ("GET".equals(method.getHttpMethod())) {
            if (!isRequestResponseMethod(method)) {
                // ensure GET returns non-void value
                if (void.class == method.getMethod().getReturnType()) {
                    issueList.add(new ResourceModelIssue(
                            method,
                            ImplMessages.ERROR_GET_RETURNS_VOID(method.getMethod()),
                            false));
                }

                // ensure GET does not consume an entity parameter
                if (method.hasEntity()) {
                    issueList.add(new ResourceModelIssue(
                            method,
                            ImplMessages.ERROR_GET_CONSUMES_ENTITY(method.getMethod()),
                            false));
                }
            }
        }

        // ensure there is not multiple HTTP method designators specified on the method
        List<String> httpAnnotList = new LinkedList<String>();
        for (Annotation a : method.getMethod().getDeclaredAnnotations()) {
            if (null != a.annotationType().getAnnotation(HttpMethod.class)) {
                httpAnnotList.add(a.toString());
            } else if ((a.annotationType() == Path.class) && !(method instanceof AbstractSubResourceMethod)) {
                issueList.add(new ResourceModelIssue(
                        method, ImplMessages.SUB_RES_METHOD_TREATED_AS_RES_METHOD(method.getMethod(), ((Path)a).value()), false));
            }
        }
        if (httpAnnotList.size() > 1) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.MULTIPLE_HTTP_METHOD_DESIGNATORS(method.getMethod(), httpAnnotList.toString()),
                    true));
        }

        final Type t = method.getGenericReturnType();
        if (!isConcreteType(t)) {
            issueList.add(new ResourceModelIssue(
                    method.getMethod(),
                    "Return type " + t + " of method " + method.getMethod().toGenericString() + " is not resolvable to a concrete type",
                    false));
        }
    }

    public void visitAbstractSubResourceMethod(AbstractSubResourceMethod method) {
        // check the same things that are being checked for resource methods
        visitAbstractResourceMethod(method);
        // and make sure the Path is not null
        if ((null == method.getPath()) || (null == method.getPath().getValue()) || (method.getPath().getValue().length() == 0)) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.ERROR_SUBRES_METHOD_URI_PATH_INVALID(method.getMethod(), method.getPath()),
                    true));
        }
    }

    public void visitAbstractSubResourceLocator(AbstractSubResourceLocator locator) {
        checkParameters(locator, locator.getMethod());
        if (void.class == locator.getMethod().getReturnType()) {
            issueList.add(new ResourceModelIssue(
                    locator,
                    ImplMessages.ERROR_SUBRES_LOC_RETURNS_VOID(locator.getMethod()),
                    true));
        }
        if ((null == locator.getPath()) || (null == locator.getPath().getValue()) || (locator.getPath().getValue().length() == 0)) {
            issueList.add(new ResourceModelIssue(
                    locator,
                    ImplMessages.ERROR_SUBRES_LOC_URI_PATH_INVALID(locator.getMethod(), locator.getPath()),
                    true));
        }
        // Sub-resource locator can not have an entity parameter
        for (Parameter parameter : locator.getParameters()) {
            if (Parameter.Source.ENTITY == parameter.getSource()) {
                issueList.add(new ResourceModelIssue(
                        locator,
                        ImplMessages.ERROR_SUBRES_LOC_HAS_ENTITY_PARAM(locator.getMethod()),
                        true));
            }
        }
    }
    private static final Set<Class> ParamAnnotationSET = createParamAnnotationSet();

    private static Set<Class> createParamAnnotationSet() {
        Set<Class> set = new HashSet<Class>(6);
        set.add(Context.class);
        set.add(HeaderParam.class);
        set.add(CookieParam.class);
        set.add(MatrixParam.class);
        set.add(QueryParam.class);
        set.add(PathParam.class);
        return Collections.unmodifiableSet(set);
    }

    private void checkParameter(Parameter p, Object source, String nameForLogging, String paramNameForLogging) {
        int annotCount = 0;
        for (Annotation a : p.getAnnotations()) {
            if (ParamAnnotationSET.contains(a.annotationType())) {
                annotCount++;
                if (annotCount > 1) {
                    issueList.add(new ResourceModelIssue(
                            source,
                            ImplMessages.AMBIGUOUS_PARAMETER(nameForLogging, paramNameForLogging),
                            false));
                    break;
                }
            }
        }

        final Type t = p.getParameterType();
        if (!isConcreteType(t)) {
            issueList.add(new ResourceModelIssue(
                    source,
                    "Parameter " + paramNameForLogging + " of type " + t + " from " + nameForLogging + " is not resolvable to a concrete type",
                    false));
        }
    }

    private boolean isConcreteType(Type t) {
        if (t instanceof ParameterizedType) {
            return isConcreteParameterizedType((ParameterizedType)t);
        } else if (!(t instanceof Class)) {
            // GenericArrayType, WildcardType, TypeVariable
            return false;
        }

        return true;
    }

    private boolean isConcreteParameterizedType(ParameterizedType pt) {
        boolean isConcrete = true;
        for (Type t : pt.getActualTypeArguments()) {
            isConcrete &= isConcreteType(t);
        }

        return isConcrete;
    }

    private void checkParameters(Parameterized pl, Method m) {
        int paramCount = 0;
        for (Parameter p : pl.getParameters()) {
            checkParameter(p, m, m.toGenericString(), Integer.toString(++paramCount));
        }
    }


    private List<Method> getDeclaredMethods(final Class _c) {
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

    private void checkNonPublicMethods(final AbstractResource ar) {

        final MethodList declaredMethods = new MethodList(
                getDeclaredMethods(ar.getResourceClass()));

        // non-public resource methods
        for (AnnotatedMethod m : declaredMethods.hasMetaAnnotation(HttpMethod.class).
                hasNotAnnotation(Path.class).isNotPublic()) {
            issueList.add(new ResourceModelIssue(ar, ImplMessages.NON_PUB_RES_METHOD(m.getMethod().toGenericString()), false));
        }
        // non-public subres methods
        for (AnnotatedMethod m : declaredMethods.hasMetaAnnotation(HttpMethod.class).
                hasAnnotation(Path.class).isNotPublic()) {
            issueList.add(new ResourceModelIssue(ar, ImplMessages.NON_PUB_SUB_RES_METHOD(m.getMethod().toGenericString()), false));
        }
        // non-public subres locators
        for (AnnotatedMethod m : declaredMethods.hasNotMetaAnnotation(HttpMethod.class).
                hasAnnotation(Path.class).isNotPublic()) {
            issueList.add(new ResourceModelIssue(ar, ImplMessages.NON_PUB_SUB_RES_LOC(m.getMethod().toGenericString()), false));
        }
    }


    // TODO: the method could probably have more then 2 params...
    private boolean isRequestResponseMethod(AbstractResourceMethod method) {
        return (method.getMethod().getParameterTypes().length == 2) && 
                (HttpRequestContext.class == method.getMethod().getParameterTypes()[0]) &&
                (HttpResponseContext.class == method.getMethod().getParameterTypes()[1]);
    }
}