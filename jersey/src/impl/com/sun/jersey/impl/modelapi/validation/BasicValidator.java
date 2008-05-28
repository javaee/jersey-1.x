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
import com.sun.jersey.impl.ImplMessages;

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
        if (resource.isRootResource() && ((null == resource.getUriPath()) || (null == resource.getUriPath().getValue()))) {
            issueList.add(new ResourceModelIssue(
                    resource,
                    ImplMessages.ERROR_RES_URI_PATH_INVALID(resource.getResourceClass(), resource.getUriPath()),
                    true)); // TODO: is it really a fatal issue?
        }
    }

    public void visitAbstractResourceConstructor(AbstractResourceConstructor constructor) {
    }

    public void visitAbstractField(AbstractField field) {
    }
    
    public void visitAbstractSetterMethod(AbstractSetterMethod setterMethod) {
    }
    
    public void visitAbstractResourceMethod(AbstractResourceMethod method) {
        // TODO: check in req/resp case the method has both req and resp params
        if (!isRequestResponseMethod(method) && ("GET".equals(method.getHttpMethod()) && (void.class == method.getMethod().getReturnType()))) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.ERROR_GET_RETURNS_VOID(method.getMethod()),
                    true));
        }
    // TODO: anything else ?
    }

    public void visitAbstractSubResourceMethod(AbstractSubResourceMethod method) {
        visitAbstractResourceMethod(method);
        if ((null == method.getUriPath()) || (null == method.getUriPath().getValue()) || (method.getUriPath().getValue().length() == 0)) {
            issueList.add(new ResourceModelIssue(
                    method,
                    ImplMessages.ERROR_SUBRES_METHOD_URI_PATH_INVALID(method.getMethod(), method.getUriPath()),
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
        if ((null == locator.getUriPath()) || (null == locator.getUriPath().getValue()) || (locator.getUriPath().getValue().length() == 0)) {
            issueList.add(new ResourceModelIssue(
                    locator,
                    ImplMessages.ERROR_SUBRES_LOC_URI_PATH_INVALID(locator.getMethod(), locator.getUriPath()),
                    true));
        }
    }

    // TODO: the method could probably have more then 2 params...
    private boolean isRequestResponseMethod(AbstractResourceMethod method) {
        return (method.getMethod().getParameterTypes().length == 2) && (HttpRequestContext.class == method.getMethod().getParameterTypes()[0]) && (HttpResponseContext.class == method.getMethod().getParameterTypes()[1]);
    }
}