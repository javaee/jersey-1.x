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
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.impl.ImplMessages;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author japod
 */
public class BasicValidator extends AbstractModelValidator {

    public void visitAbstractResource(AbstractResource resource) {
        // resource should have at least one resource method, subresource method or subresource locator
        if ((resource.getResourceMethods().size() + resource.getSubResourceMethods().size() + resource.getSubResourceLocators().size()) == 0) {
            issueList.add(new ResourceModelIssue(
                    resource,
                    ImplMessages.ERROR_NO_SUB_RES_METHOD_LOCATOR_FOUND(resource.getResourceClass()),
                    false)); // there might still be Views associated with the resource
        }
        // uri template of the resource, if present should not contain null value
        if (resource.isRootResource() && ((null == resource.getPath()) || (null == resource.getPath().getValue()))) {
            issueList.add(new ResourceModelIssue(
                    resource,
                    ImplMessages.ERROR_RES_URI_PATH_INVALID(resource.getResourceClass(), resource.getPath()),
                    true)); // TODO: is it really a fatal issue?
        }
        // root resource should have at least one public contructor
        if (resource.isRootResource() && resource.getConstructors().isEmpty()) {
            issueList.add(new ResourceModelIssue(
                    resource,
                    ImplMessages.ROOT_RES_NO_PUBLIC_CTOR(resource.getResourceClass()),
                    true));
        }
        // check sub-resource locators for ambiguities
        Map<UriTemplate, String> srlUriTemplates = new HashMap<UriTemplate, String>();
        Map<UriTemplate, String> srlUriTemplatesWithSlash = new HashMap<UriTemplate, String>();
        for (AbstractSubResourceLocator srl : resource.getSubResourceLocators()) {
            UriTemplate srlUriTemplate = new UriTemplate(srl.getPath().getValue());
            UriTemplate srlUriTemplateWithSlash =
                    srlUriTemplate.endsWithSlash() ? srlUriTemplate : new UriTemplate(srl.getPath().getValue() + '/');
            if (srlUriTemplates.containsKey(srlUriTemplate)) {
                issueList.add(new ResourceModelIssue(
                        resource,
                        ImplMessages.AMBIGUOUS_SRLS(resource.getResourceClass(), srlUriTemplate.getTemplate(), srlUriTemplates.get(srlUriTemplate)),
                        true));
            } else {
                if (srlUriTemplatesWithSlash.containsKey(srlUriTemplateWithSlash)) {
                    issueList.add(new ResourceModelIssue(
                            resource,
                            ImplMessages.AMBIGUOUS_SRLS(resource.getResourceClass(), srlUriTemplate.getTemplate(), srlUriTemplatesWithSlash.get(srlUriTemplate)),
                            true));
                } else {
                    srlUriTemplatesWithSlash.put(srlUriTemplateWithSlash, srlUriTemplate.getTemplate());
                }
                srlUriTemplates.put(srlUriTemplate, srlUriTemplate.getTemplate());
            }
        }

        // check resource methods for ambiguities
        findOutMTAmbiguities(resource, resource.getResourceMethods(), new ResourceMethodAmbiguityErrMsgGenerator<AbstractResourceMethod>() {

            void generateInErrMsg(AbstractResource resource, AbstractResourceMethod arm1, AbstractResourceMethod arm2, MediaType mt) {
                issueList.add(new ResourceModelIssue(
                        resource,
                        ImplMessages.AMBIGUOUS_RMS_IN(resource.getResourceClass(), arm1.getHttpMethod(), mt, arm1.getMethod().getName(), arm2.getMethod().getName(), arm1.getSupportedInputTypes(), arm2.getSupportedInputTypes()),
                        false));
            }
            ;

            void generateOutErrMsg(AbstractResource resource, AbstractResourceMethod arm1, AbstractResourceMethod arm2, MediaType mt) {
                issueList.add(new ResourceModelIssue(
                        resource,
                        ImplMessages.AMBIGUOUS_RMS_OUT(resource.getResourceClass(), arm1.getHttpMethod(), mt, arm1.getMethod().getName(), arm2.getMethod().getName(), arm1.getSupportedOutputTypes(), arm2.getSupportedOutputTypes()),
                        true));
            }
            ;
        });

        // check sub-resource methods for ambiguities
        findOutMTAmbiguities(resource, resource.getSubResourceMethods(), new ResourceMethodAmbiguityErrMsgGenerator<AbstractSubResourceMethod>() {

            boolean isConflictingPaths(String path1, String path2) {
                UriTemplate t1 = new UriTemplate(path1);
                UriTemplate t2 = new UriTemplate(path2);
                if (t1.equals(t2)) {
                    return true;
                } else {
                    if (t1.endsWithSlash()) {
                        return (!t2.endsWithSlash()) && t1.equals(new UriTemplate(path2 + "/"));
                    } else {
                        return t2.endsWithSlash() && t2.equals(new UriTemplate(path1 + "/"));
                    }
                }
            }
            ;

            void generateInErrMsg(AbstractResource resource, AbstractSubResourceMethod arm1, AbstractSubResourceMethod arm2, MediaType mt) {
                if (isConflictingPaths(arm1.getPath().getValue(), arm2.getPath().getValue())) {
                    issueList.add(new ResourceModelIssue(
                            resource,
                            ImplMessages.AMBIGUOUS_SRMS_IN(resource.getResourceClass(), arm1.getHttpMethod(), arm1.getPath().getValue(), mt, arm1.getMethod().getName(), arm2.getMethod().getName(), arm1.getSupportedInputTypes(), arm2.getSupportedInputTypes()),
                            false));
                }
            }
            ;

            void generateOutErrMsg(AbstractResource resource, AbstractSubResourceMethod arm1, AbstractSubResourceMethod arm2, MediaType mt) {
                if (isConflictingPaths(arm1.getPath().getValue(), arm2.getPath().getValue())) {
                    issueList.add(new ResourceModelIssue(
                            resource,
                            ImplMessages.AMBIGUOUS_SRMS_OUT(resource.getResourceClass(), arm1.getHttpMethod(), arm1.getPath().getValue(), mt, arm1.getMethod().getName(), arm2.getMethod().getName(), arm1.getSupportedOutputTypes(), arm2.getSupportedOutputTypes()),
                            true));
                }
            }
        });

        // check setters for ambiguities
        for (AbstractSetterMethod asm : resource.getSetterMethods()) {
            checkParameter(asm, asm.getMethod().toString(), "1", asm.getMethod().getDeclaredAnnotations());
        }

        // check fields for ambiguities
        for (AbstractField af : resource.getFields()) {
            checkParameter(af, af.getField().toString(), af.getField().getName(), af.getField().getDeclaredAnnotations());
        }

        checkNonPublicMethods(resource);
    }

    private abstract class ResourceMethodAmbiguityErrMsgGenerator<T extends AbstractResourceMethod> {

        abstract void generateInErrMsg(AbstractResource resource, T arm1, T arm2, MediaType mt);

        abstract void generateOutErrMsg(AbstractResource resource, T arm1, T arm2, MediaType mt);
    }

    private <T extends AbstractResourceMethod> void findOutMTAmbiguities(AbstractResource resource, List<T> methods, ResourceMethodAmbiguityErrMsgGenerator generator) {
        for (int i = 0; i < methods.size(); i++) {
            T arm1 = methods.get(i);
            for (int j = i + 1; j < methods.size(); j++) {
                T arm2 = methods.get(j);
                if (arm1.getHttpMethod().equalsIgnoreCase(arm2.getHttpMethod())) {
                    for (MediaType mt1 : arm1.getSupportedInputTypes()) {
                        for (MediaType mt2 : arm2.getSupportedInputTypes()) {
                            if (mt1.isCompatible(mt2) && ((mt1.isWildcardType() == mt2.isWildcardType()) && (mt1.isWildcardSubtype() == mt2.isWildcardSubtype()))) {
                                // check also output mime types
                                for (MediaType outmt1 : arm1.getSupportedOutputTypes()) {
                                    for (MediaType outmt2 : arm2.getSupportedOutputTypes()) {
                                        if (outmt1.isCompatible(outmt2) && ((outmt1.isWildcardType() == outmt2.isWildcardType()) && (outmt1.isWildcardSubtype() == outmt2.isWildcardSubtype()))) {
                                            // TODO: check only if an entity parameter is present, do not hardcode the http GET/HEAD method
                                            if (!(("GET".equalsIgnoreCase(arm1.getHttpMethod())) || ("HEAD".equalsIgnoreCase(arm1.getHttpMethod())))) {
                                                generator.generateInErrMsg(resource, arm1, arm2, mt1);
                                            }
                                            generator.generateOutErrMsg(resource, arm1, arm2, outmt1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void visitAbstractResourceConstructor(AbstractResourceConstructor constructor) {
    }

    public void visitAbstractField(AbstractField field) {
    }

    public void visitAbstractSetterMethod(AbstractSetterMethod setterMethod) {
    }

    public void visitAbstractResourceMethod(AbstractResourceMethod method) {
        checkParameters(method.getMethod());
        // ensure GET returns non-void value
        if (!isRequestResponseMethod(method) && ("GET".equals(method.getHttpMethod()) && (void.class == method.getMethod().getReturnType()))) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.ERROR_GET_RETURNS_VOID(method.getMethod()),
                    false));
        }
        // ensure GET does not consume an entity parameter
        if (!isRequestResponseMethod(method) && ("GET".equals(method.getHttpMethod()))) {
            for (Parameter p : method.getParameters()) {
                if (Parameter.Source.ENTITY == p.getSource()) {
                    issueList.add(new ResourceModelIssue(
                            method,
                            ImplMessages.ERROR_GET_CONSUMES_ENTITY(method.getMethod()),
                            true));
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
        checkParameters(locator.getMethod());
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

    private void checkParameter(Object source, String nameForLogging, String paramNameForLogging, Annotation[] pa) {
            int annotCount = 0;
            for (Annotation a : pa) {
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
    }

    private void checkParameters(Method m) {
        Annotation[][] pas = m.getParameterAnnotations();
        int paramCount = 0;
        for (Annotation[] pa : pas) {
            paramCount++;
            checkParameter(m, m.toString(), Integer.toString(paramCount), pa);
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
        return (method.getMethod().getParameterTypes().length == 2) && (HttpRequestContext.class == method.getMethod().getParameterTypes()[0]) && (HttpResponseContext.class == method.getMethod().getParameterTypes()[1]);
    }
}