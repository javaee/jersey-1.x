/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.api.container.filter;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;

/**
 * A {@link ResourceFilterFactory} provides tracking of resource
 * matching. Every successful match is logged and developers can easily discover
 * which resource / method was matched and see how the request path is being
 * consumed.
 *
 * When an application is deployed as a Servlet or Filter this Jersey resource
 * filter can be registered using the following initialization parameter:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ResourceFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.ResourceDebuggingFilterFactory&lt;/param-value&gt;
 *     &lt;/init-param&gt
 * </pre></blockquote>
 *
 * @author pavel.bucek@sun.com
 * @see com.sun.jersey.api.container.filter
 */
public class ResourceDebuggingFilterFactory implements ResourceFilterFactory {

    private final HttpContext context;

    public ResourceDebuggingFilterFactory(@Context HttpContext hc) {
        this.context = hc;
    }

    abstract class AbstractRequestFilter implements ResourceFilter, ContainerRequestFilter {

        protected Logger LOGGER = Logger.getLogger(AbstractRequestFilter.class.getCanonicalName());

        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        public ContainerResponseFilter getResponseFilter() {
            return null;
        }
    }

    private class ResourceMethodFilter extends AbstractRequestFilter {
        
        private final AbstractResourceMethod arm;

        public ResourceMethodFilter(AbstractResourceMethod arm) {
            this.arm = arm;
        }

        public ContainerRequest filter(ContainerRequest request) {
                LOGGER.log(Level.INFO,
                        "Resource Method matched." +
                        "\n HttpMethod: " + arm.getHttpMethod() +
                        "\n Resource: " + arm.getDeclaringResource().getResourceClass().getName() +
                        "\n Method: " + arm.getMethod().toGenericString());

                return request;
        }
    }

    private class SubResourceMethodFilter extends AbstractRequestFilter {

        private final AbstractSubResourceMethod asrm;

        public SubResourceMethodFilter(AbstractSubResourceMethod asrm) {
            this.asrm = asrm;
        }

        public ContainerRequest filter(ContainerRequest request) {
            LOGGER.log(Level.INFO, 
                    "Sub-Resource Method matched." +
                    "\n Path: " + asrm.getPath().getValue() +
                    (context != null ? "\n Matched Result: " + context.getUriInfo().getMatchedResults().get(0) : "") +
                    "\n HttpMethod: " + asrm.getHttpMethod() +
                    "\n Resource: " + asrm.getDeclaringResource().getResourceClass().getName() +
                    "\n Method: " + asrm.getMethod().toGenericString());

            return request;
        }
    }

    private class SubResourceLocatorFilter extends AbstractRequestFilter {

        private final AbstractSubResourceLocator asrl;

        public SubResourceLocatorFilter(AbstractSubResourceLocator asrl) {
            this.asrl = asrl;
        }

        public ContainerRequest filter(ContainerRequest request) {
            LOGGER.log(Level.INFO, 
                    "Sub-Resource Locator matched. " +
                    "\n Path: " + asrl.getPath().getValue() +
                    (context != null ? "\n Matched Result: " + context.getUriInfo().getMatchedResults().get(0) : "") +
                    "\n Resource: " + asrl.getResource().getResourceClass().getName() +
                    "\n Method: " + asrl.getMethod().toGenericString());

            return request;
        }
    }

    public List<ResourceFilter> create(AbstractMethod am) {
        
        if(am instanceof AbstractSubResourceMethod) {
            return Collections.<ResourceFilter>singletonList(new SubResourceMethodFilter((AbstractSubResourceMethod)am));
        } else if(am instanceof AbstractResourceMethod) {
            return Collections.<ResourceFilter>singletonList(new ResourceMethodFilter((AbstractResourceMethod)am));
        } else if(am instanceof AbstractSubResourceLocator) {
            return Collections.<ResourceFilter>singletonList(new SubResourceLocatorFilter((AbstractSubResourceLocator)am));
        } else {
            return null;
        }
    }
}