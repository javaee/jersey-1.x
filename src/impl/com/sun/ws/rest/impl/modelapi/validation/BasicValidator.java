/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.sun.ws.rest.impl.modelapi.validation;

import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceConstructor;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.api.model.AbstractSubResourceLocator;
import com.sun.ws.rest.api.model.AbstractSubResourceMethod;
import com.sun.ws.rest.api.model.ResourceModelIssue;
import com.sun.ws.rest.impl.ImplMessages;

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

    public void visitAbstractResourceConstructor(AbstractResourceConstructor constructor) {
    }

    // TODO: the method could probably have more then 2 params...
    private boolean isRequestResponseMethod(AbstractResourceMethod method) {
        return (method.getMethod().getParameterTypes().length == 2) && (HttpRequestContext.class == method.getMethod().getParameterTypes()[0]) && (HttpResponseContext.class == method.getMethod().getParameterTypes()[1]);
    }
}
