/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.server.hypermedia.filter;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.core.header.LinkHeader;
import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.ContextualActionSet;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hypermedia filter factory.
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @see com.sun.jersey.api.container.filter
 */
public class HypermediaFilterFactory implements ResourceFilterFactory {

    private final UriInfo uriInfo;

    public HypermediaFilterFactory(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    private class HypermediaFilter 
            implements ResourceFilter, ContainerResponseFilter, ContainerRequestFilter {

        private AbstractMethod abstractMethod;

        private Method contextualActionSetMethod;

        private Map<String, AbstractResourceMethod> actionMethods;

        public HypermediaFilter(AbstractMethod abstractMethod) {
            this.abstractMethod = abstractMethod;
            actionMethods = new HashMap<String, AbstractResourceMethod>();
            prepare();
        }

        private void prepare() {
            AbstractResource ar = abstractMethod.getResource();
            List<Method> methods = getContextualActionSetMethods(ar);
            if (!methods.isEmpty()) {
                contextualActionSetMethod = methods.get(0);
            }

            // Look for @Action methods as sub-resource methods
            for (AbstractResourceMethod m : ar.getSubResourceMethods()) {
                Action action = m.getAnnotation(Action.class);
                if (action != null) {
                    actionMethods.put(action.value(), m);
                }
            }
        }

        private List<Method> getContextualActionSetMethods(AbstractResource resource) {
            final MethodList methodList = new MethodList(resource.getResourceClass(), true);
            final List<Method> contextualActionSetMethods = new ArrayList<Method>();
            for (AnnotatedMethod m : methodList.
                    hasAnnotation(ContextualActionSet.class).
                    hasNumParams(0).
                    hasReturnType(Set.class)) {
                ReflectionHelper.setAccessibleMethod(m.getMethod());
                contextualActionSetMethods.add(m.getMethod());
            }
            return contextualActionSetMethods;
        }

        // ResourceFilter

        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        public ContainerResponseFilter getResponseFilter() {
            return this;
        }

        // ContainerResponseFilter
        
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            Object resourceInstance = uriInfo.getMatchedResources().get(0);
            if (request.isTracingEnabled()) {
                request.trace("HypermediaFilter called for response; " +
                    "resourceInstance = " + resourceInstance);
            }

            // If contract is contextual
            Set<String> actionSet = null;
            if (contextualActionSetMethod != null) {
                try {
                    actionSet = (Set<String>)
                            contextualActionSetMethod.invoke(resourceInstance);
                } catch (Exception _) {
                    // falls through
                }
            }

            // If no contextual set, use all actions defined
            if (actionSet == null) {
                actionSet = actionMethods.keySet();
            }

            // Add link headers and check soundness of actionSet
            if (actionSet != null) {
                for (String action : actionSet) {
                    AbstractResourceMethod rm = actionMethods.get(action);
                    if (rm == null) {
                        throw new RuntimeException("Contextual action set " +
                                "returned by resource " + resourceInstance +
                                "is not sound");
                    }
                    // Bit hacky, but we need to drop path of last action
                    String uri = uriInfo.getRequestUri().toString();
                    if (abstractMethod.isAnnotationPresent(Action.class)) {
                        Path path = abstractMethod.getAnnotation(Path.class);
                        int k = uri.lastIndexOf(path.value());
                        assert k > 0;
                        uri = uri.substring(0, k);
                    }
                    UriBuilder uriBuilder = UriBuilder.fromUri(uri);
                    LinkHeader lh = LinkHeader.uri(uriBuilder.path(rm.getMethod()).build()).
                            rel(action).op(rm.getHttpMethod()).build();
                    response.getHttpHeaders().add("Link", lh);
                }
            }

            return response;
        }

        // ContainerRequestFilter

        public ContainerRequest filter(ContainerRequest request) {
            Object resourceInstance = uriInfo.getMatchedResources().get(0);
            if (request.isTracingEnabled()) {
                request.trace("HypermediaFilter called for request; " +
                    "resourceInstance = " + resourceInstance);
            }

            // If not action method, no need to do any checks
            Action action = abstractMethod.getAnnotation(Action.class);
            if (action == null) {
                return request;
            }            

            // If contract is contextual
            Set<String> actionSet = null;
            if (contextualActionSetMethod != null) {
                try {
                    actionSet = (Set<String>)
                            contextualActionSetMethod.invoke(resourceInstance);
                } catch (Exception _) {
                    // falls through
                }
            }

            // Check if action is in contextual action set
            if (actionSet != null && !actionSet.contains(action.value())) {
                throw new RuntimeException("Action '" +
                        action.value() +
                        "' is not in contextual action set returned by " +
                        resourceInstance);
            }

            return request;
        }
    }
    
    public List<ResourceFilter> create(AbstractMethod am) {
        // Check if method belongs to controller class
        AbstractResource ar = am.getResource();
        HypermediaController ctrl = ar.getAnnotation(HypermediaController.class);

        if (ctrl != null) {
            if (ctrl.linkType() != LinkType.LINK_HEADERS) {
                throw new RuntimeException("Unsupported hypermedia " +
                        "link type " + ctrl.linkType().name());
            }
            // Return type matches model class or action?
            if (am.getMethod().getReturnType() == ctrl.model() ||
                    am.isAnnotationPresent(Action.class))
            {
                List<ResourceFilter> result = new ArrayList<ResourceFilter>(1);
                result.add(new HypermediaFilter(am));
                return result;
            }
        }
        // no filters
        return null;
    }
}
