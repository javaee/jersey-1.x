/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

/**
 * Provides common server-side filters.
 * <p>
 * There are two types of filter that may be registered with Jersey:
 * <ol>
 * <li>Container filters, which are filters that are registered to filter
 *     the request before the request is matched and dispatched to a root
 *     resource class, and filters that are registered to filter the response
 *     after the response has returned from a resource method.</li>
 * <li>Resource filters, which are filters that are registered to filter
 *     requests and responses specific to resource methods, sub-resource methods
 *     and sub-resource locators.</li>
 * <p>
 * </ol>
 * A request will be filtered by container filters before the request is filtered
 * by resource filters. A response will be filtered by resource filters before
 * the response is filtered by container filters.
 * <p>
 * Container filters are registered as properties of the {@link com.sun.jersey.api.core.ResourceConfig}.
 * Container request filters, of the class {@link com.sun.jersey.spi.container.ContainerRequestFilter}, are
 * registered using the property  {@link com.sun.jersey.api.core.ResourceConfig#PROPERTY_CONTAINER_REQUEST_FILTERS}.
 * Container response filters, of the class {@link com.sun.jersey.spi.container.ContainerResponseFilter}, are
 * registered using the property  {@link com.sun.jersey.api.core.ResourceConfig#PROPERTY_CONTAINER_RESPONSE_FILTERS}.
 * <p>
 * For example, to log requests and responses when an application is deployed 
 * as a Servlet or Filter a {@link com.sun.jersey.api.container.filter.LoggingFilter} can be registered using
 * the following initialization parameters:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.LoggingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt
 *     &lt;init-param&gt
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerResponseFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.LoggingFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * </pre></blockquote>
 * Note that two or more request or response filters may be registered as a ';' separated list of fully
 * qualified class names.
 * <p>
 * Resource filters are registered using two mechanisms:
 * <ol>
 * <li>As resource filter factories. Resource filter factories, of the class
 *     {@link com.sun.jersey.spi.container.ResourceFilterFactory}, are registered using the
 *     property {@link com.sun.jersey.api.core.ResourceConfig#PROPERTY_RESOURCE_FILTER_FACTORIES}.</li>
 * <li>As resource filters, of the class
 *     {@link com.sun.jersey.spi.container.ResourceFilter}, declared using the
 *     annotation {@link com.sun.jersey.spi.container.ResourceFilters}, which may
 *     occur on a resource class, resource method, sub-resource method or sub-resource locator.</li>
 * </ol>
 * A request will be filtered by filters produced by resource filter factories before a request is filtered
 * by resource filters declared by annotation. A response will be filtered by resource filters declared by annotation
 * before the response is filtered by filters produced by resource filter factories.
 * <p>
 * For example, to support {@link javax.annotation.security.RolesAllowed} on resource classes when an application
 * is deployed as a Servlet or Filter a
 * {@link com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory}
 * can be registered using the following initialization parameter:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ResourceFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory&lt;/param-value&gt;
 *     &lt;/init-param&gt
 * </pre></blockquote>
 * Note that two or more resource filter factories may be registered as a ';' separated list of fully
 * qualified class names.
 * <p>
 * The use of resource filter factories allow the application of filters to any
 * method of a resource class that conforms to a certain pattern as defined by
 * a resource filter factory implementation. For example, the support for the
 * annotation {@link com.sun.jersey.spi.container.ResourceFilters} is implemented
 * as a resource filter factory, that is registered after all user-registered
 * resource filter factories.
 * <p>
 * If an exception is thrown by a request filter (registered using any mechanism)
 * then:
 * <ol>
 * <li>the request processing is terminated;</li>
 * <li>the exception is mapped to a response; and</li>
 * <li>the response is filtered by the response filters.</li>
 * </ol>
 * If an exception is thrown by a response filter (registered using any mechanism)
 * then:
 * <ol>
 * <li>the response processing is terminated;</li>
 * <li>the exception is mapped to a response; and</li>
 * <li>the response is returned to the client.</li>
 * </ol>
 */
package com.sun.jersey.api.container.filter;