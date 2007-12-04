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
                    "No resource/subresource methods neither subresource locators found",
                    true));
        }
        // uri template of the resource, if present should not contain null or empty value
        if (resource.isRootResource() && ((null == resource.getUriTemplate()) || (null == resource.getUriTemplate().getValue()) || (resource.getUriTemplate().getValue().length() == 0))) {
            issueList.add(new ResourceModelIssue(
                    resource,
                    "URI template associated with abstract resource has a null or empty value",
                    true)); // TODO: is it really a fatal issue?
        }
    }

    public void visitAbstractResourceMethod(AbstractResourceMethod method) {
        // TODO: check in req/resp case the method has both req and resp params
        if (!isRequestResponseMethod(method) && "PUT".equals(method.getHttpMethod()) && (0 == method.getParameters().size())) {
            issueList.add(new ResourceModelIssue(
                    method,
                    "PUT method does not take any parameter",
                    false));
        }
        if (!isRequestResponseMethod(method) && "POST".equals(method.getHttpMethod()) && (0 == method.getParameters().size())) {
            issueList.add(new ResourceModelIssue(
                    method,
                    "POST method does not take any parameter",
                    false));
        }
        if (!isRequestResponseMethod(method) && ("GET".equals(method.getHttpMethod()) && (void.class == method.getMethod().getReturnType()))) {
            issueList.add(new ResourceModelIssue(
                    method,
                    "GET method should return something!",
                    true));
        }
    // TODO: anything else ?
    }

    public void visitAbstractSubResourceMethod(AbstractSubResourceMethod method) {
        visitAbstractResourceMethod(method);
        if ((null == method.getUriTemplate()) || (null == method.getUriTemplate().getValue()) || (method.getUriTemplate().getValue().length() == 0)) {
            issueList.add(new ResourceModelIssue(
                    method,
                    "URI template associated with subresource method can not have null neither empty value",
                    true));
        }
    }

    public void visitAbstractSubResourceLocator(AbstractSubResourceLocator locator) {
        if (void.class == locator.getMethod().getReturnType()) {
            issueList.add(new ResourceModelIssue(
                    locator,
                    "Subresource locator must return something!",
                    true));
        }
        if ((null == locator.getUriTemplate()) || (null == locator.getUriTemplate().getValue()) || (locator.getUriTemplate().getValue().length() == 0)) {
            issueList.add(new ResourceModelIssue(
                    locator,
                    "URI template associated with abstract resource can not have null neither empty value!",
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
