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
package com.sun.jersey.impl.modelapi.validation;

import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.model.AbstractField;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSetterMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.impl.ImplMessages;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Produces;
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
        } else if (1 < resource.getConstructors().size()) { // look for ambiguous ctors:
            AbstractResourceConstructor c1 = resource.getConstructors().get(0);
            AbstractResourceConstructor c2 = resource.getConstructors().get(1);
            if (c1.getParameters().size() < c2.getParameters().size()) {
                final AbstractResourceConstructor tmp = c2;
                c2 = c1;
                c1 = tmp;
            }
            for (int i = 2; i < resource.getConstructors().size(); i++) {
                final AbstractResourceConstructor cur = resource.getConstructors().get(i);
                if (cur.getParameters().size() >= c2.getParameters().size()) {
                    if (cur.getParameters().size() > c1.getParameters().size()) {
                        c1 = cur;
                    } else {
                        c2 = cur;
                    }
                }
            }
            if (c1.getParameters().size() == c2.getParameters().size()) {
                issueList.add(new ResourceModelIssue(
                        resource,
                        ImplMessages.AMBIGUOUS_CTORS(resource.getResourceClass()),
                        false));
            }
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
        for (int i = 0; i < resource.getResourceMethods().size(); i++) {
            AbstractResourceMethod arm1 = resource.getResourceMethods().get(i);
            for (int j = i + 1; j < resource.getResourceMethods().size(); j++) {
                AbstractResourceMethod arm2 = resource.getResourceMethods().get(j);
                if (arm1.getHttpMethod().equalsIgnoreCase(arm2.getHttpMethod())) {
                    // check input mime types
                    if ((arm1.getMethod().isAnnotationPresent(Consumes.class)) || (arm1.getMethod().isAnnotationPresent(Consumes.class))) {
                        for (MediaType mt1 : arm1.getSupportedInputTypes()) {
                            for (MediaType mt2 : arm2.getSupportedInputTypes()) {
                                if (mt1.isCompatible(mt2) && (!(mt1.isWildcardType() || mt1.isWildcardSubtype() || mt2.isWildcardType() || mt2.isWildcardSubtype()))) {
                                    issueList.add(new ResourceModelIssue(
                                            resource,
                                            ImplMessages.AMBIGUOUS_RMS_IN(resource.getResourceClass(), arm1.getHttpMethod(), mt1, arm1.getMethod().getName(), arm2.getMethod().getName(), arm1.getSupportedInputTypes(), arm2.getSupportedInputTypes()),
                                            true));

                                }
                            }
                        }
                    }
                    // check output mime types
                    if ((arm1.getMethod().isAnnotationPresent(Produces.class)) || (arm1.getMethod().isAnnotationPresent(Produces.class))) {
                        for (MediaType mt1 : arm1.getSupportedOutputTypes()) {
                            for (MediaType mt2 : arm2.getSupportedOutputTypes()) {
                                if (mt1.isCompatible(mt2) && (!(mt1.isWildcardType() || mt1.isWildcardSubtype() || mt2.isWildcardType() || mt2.isWildcardSubtype()))) {
                                    issueList.add(new ResourceModelIssue(
                                            resource,
                                            ImplMessages.AMBIGUOUS_RMS_OUT(resource.getResourceClass(), arm1.getHttpMethod(), mt1, arm1.getMethod().getName(), arm2.getMethod().getName(), arm1.getSupportedOutputTypes(), arm2.getSupportedOutputTypes()),
                                            true));

                                }
                            }
                        }
                    }
                }
            }
        }
        // TODO: check subresource methods
    }

    public void visitAbstractResourceConstructor(AbstractResourceConstructor constructor) {
    }

    public void visitAbstractField(AbstractField field) {
    }

    public void visitAbstractSetterMethod(AbstractSetterMethod setterMethod) {
    }

    public void visitAbstractResourceMethod(AbstractResourceMethod method) {
        if (!isRequestResponseMethod(method) && ("GET".equals(method.getHttpMethod()) && (void.class == method.getMethod().getReturnType()))) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.ERROR_GET_RETURNS_VOID(method.getMethod()),
                    true));
        }
        List<String> httpAnnotList = new LinkedList<String>();
        for(Annotation a : method.getMethod().getDeclaredAnnotations()) {
            if (null != a.annotationType().getAnnotation(HttpMethod.class)) {
                httpAnnotList.add(a.toString());
            }
        }
        if(httpAnnotList.size() > 1) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.MULTIPLE_HTTP_METHOD_DESIGNATORS(method.getMethod(), httpAnnotList.toString()),
                    true));
        }
    }

    public void visitAbstractSubResourceMethod(AbstractSubResourceMethod method) {
        visitAbstractResourceMethod(method);
        if ((null == method.getPath()) || (null == method.getPath().getValue()) || (method.getPath().getValue().length() == 0)) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.ERROR_SUBRES_METHOD_URI_PATH_INVALID(method.getMethod(), method.getPath()),
                    true));
        }
    }

    public void visitAbstractSubResourceLocator(AbstractSubResourceLocator locator) {
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
    }

    // TODO: the method could probably have more then 2 params...
    private boolean isRequestResponseMethod(AbstractResourceMethod method) {
        return (method.getMethod().getParameterTypes().length == 2) && (HttpRequestContext.class == method.getMethod().getParameterTypes()[0]) && (HttpResponseContext.class == method.getMethod().getParameterTypes()[1]);
    }
}