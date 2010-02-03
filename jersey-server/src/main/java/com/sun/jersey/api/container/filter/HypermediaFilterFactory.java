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
package com.sun.jersey.api.container.filter;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.core.header.LinkHeader;
import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Hypermedia filter factory.
 *
 * @author Santiago.PericasGeertsen@sun.com
 * @see com.sun.jersey.api.container.filter
 */
public class HypermediaFilterFactory implements ResourceFilterFactory {

    private static final Logger LOGGER = Logger.getLogger(HypermediaFilter.class.getName());

    private final UriInfo uriInfo;

    public HypermediaFilterFactory(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    private class HypermediaFilter implements ResourceFilter, ContainerResponseFilter {

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
            List<Method> methods = ar.getContextualActionSetMethods();
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

        // ResourceFilter

        public ContainerRequestFilter getRequestFilter() {
            return null;
        }

        public ContainerResponseFilter getResponseFilter() {
            return this;
        }

        // ContainerResponseFilter
        
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            Object resourceInstance = uriInfo.getMatchedResources().get(0);
            LOGGER.info("HypermediaFilter.filter() called; " +
                    "resourceInstance = " + resourceInstance);

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
                    LinkHeader lh = new LinkHeader();
                    lh.setUri(uriBuilder.path(rm.getMethod()).build().toString());
                    lh.setRel(action);
                    lh.setOp(rm.getHttpMethod());
                    response.getHttpHeaders().add("Link", lh);
                }
            }

            return response;
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
                LOGGER.info("Found hypermedia method " + am.getMethod()
                        + " in controller " + ar.getClass().getName());
                List<ResourceFilter> result = new ArrayList<ResourceFilter>(1);
                result.add(new HypermediaFilter(am));
                return result;
            }
        }
        // no filters
        return null;
    }
}
