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

package com.sun.jersey.spi.uri.rules;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.List;

/**
 * The context for processing URI rules.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface UriRuleContext extends HttpContext, UriMatchResultContext {

    /**
     * Get the container request.
     *
     * @return the container request.
     */
    ContainerRequest getContainerRequest();

    /**
     * Set the container request.
     *
     * @param request the container request.
     */
    void setContainerRequest(ContainerRequest request);

    /**
     * Get the container response.
     *
     * @return the container response.
     */
    ContainerResponse getContainerResponse();
    
    /**
     * Set the container response.
     *
     * @param response the container response.
     */
    void setContainerResponse(ContainerResponse response);

    /**
     * Push a list of container response filters to apply after the
     * container response has been produced.
     * <p>
     * The list of response filters is processed in reverse order of last
     * to first.
     *
     * @param filters the list container response filters
     */
    void pushContainerResponseFilters(List<ContainerResponseFilter> filters);
    
    /**
     * Get the resource instance from a resource class.
     * 
     * @param resourceClass the resource class
     * @return the resource instance
     */
    Object getResource(Class resourceClass);
    
    /**
     * Get the rules for a resource class.
     * 
     * @param resourceClass the resource class that has rules
     * @return the rules
     */
    UriRules<UriRule> getRules(Class resourceClass);

    /**
     * Push a match.
     * 
     * @param template the matching URI template.
     * @param names the parameter names associated with the capturing group
     *         values.
     */
    void pushMatch(UriTemplate template, List<String> names);

    /**
     * Push a matching resource.
     * 
     * @param resource the matching resource
     */
    void pushResource(Object resource);

    /**
     * Push the matching resource method.
     *
     * @param arm the matching resource method.
     */
    void pushMethod(AbstractResourceMethod arm);
    
    /**
     * Push the right hand path length to calculate the entry for
     * the list of matching (ancestor) URI paths.
     * 
     * @param rhpathlen the right hand length
     */
    void pushRightHandPathLength(int rhpathlen);
}