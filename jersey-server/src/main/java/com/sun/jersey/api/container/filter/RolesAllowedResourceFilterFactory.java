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
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * A {@link ResourceFilterFactory} supporting the {@link RolesAllowed},
 * {@link PermitAll} and {@link DenyAll} on resource methods sub-resource methods,
 * and sub-resource locators.
 * <p>
 * The {@link SecurityContext} is utilized, using the
 * {@link SecurityContext#isUserInRole(java.lang.String) } method,
 * to ascertain if the user is in one
 * of the roles declared in by a {@link RolesAllowed}. If a user is in none of 
 * the declared roles then a 403 (Forbidden) response is returned.
 * <p>
 * If the {@link DenyAll} annotation is declared then a 403 (Forbidden) response
 * is returned.
 * <p>
 * If the {@link PermitAll} annotation is declared and is not overridden then
 * this filter will not be applied.
 * <p>
 * When an application is deployed as a Servlet or Filter this Jersey resource
 * filter can be registered using the following initialization parameter:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ResourceFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory&lt;/param-value&gt;
 *     &lt;/init-param&gt
 * </pre></blockquote>
 *
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.container.filter
 */
public class RolesAllowedResourceFilterFactory implements ResourceFilterFactory {

    private @Context SecurityContext sc;
    
    private class Filter implements ResourceFilter, ContainerRequestFilter {

        private final boolean denyAll;
        private final String[] rolesAllowed;

        protected Filter() {
            this.denyAll = true;
            this.rolesAllowed = null;
        }

        protected Filter(String[] rolesAllowed) {
            this.denyAll = false;
            this.rolesAllowed = (rolesAllowed != null) ? rolesAllowed : new String[] {};
        }

        // ResourceFilter

        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        public ContainerResponseFilter getResponseFilter() {
            return null;
        }

        // ContainerRequestFilter
        
        public ContainerRequest filter(ContainerRequest request) {
            if (!denyAll) {
                for (String role : rolesAllowed) {
                    if (sc.isUserInRole(role))
                        return request;
                }
            }
            
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
    }
    
    public List<ResourceFilter> create(AbstractMethod am) {
        // DenyAll on the method take precedence over RolesAllowed and PermitAll
        if (am.isAnnotationPresent(DenyAll.class))
            return Collections.<ResourceFilter>singletonList(new Filter());

        // RolesAllowed on the method takes precedence over PermitAll
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null)
            return Collections.<ResourceFilter>singletonList(new Filter(ra.value()));

        // PermitAll takes precedence over RolesAllowed on the class
        if (am.isAnnotationPresent(PermitAll.class))
            return null;

        // RolesAllowed on the class takes precedence over PermitAll
        ra = am.getResource().getAnnotation(RolesAllowed.class);
        if (ra != null)
            return Collections.singletonList((ResourceFilter)new Filter(ra.value()));

        // No need to check whether PermitAll is present.
        return null;
    }
}
