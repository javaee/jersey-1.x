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
/**
 * Provides support for Spring-based Web applications.
 * <p>
 * Spring support is enabled by referencing the class
 * {@link com.sun.jersey.spi.spring.container.servlet.SpringServlet} in the
 * web.xml. For example:
 * <blockquote><pre>
 *   &lt;web-app&gt;
 *     &lt;context-param&gt;
 *       &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
 *       &lt;param-value&gt;classpath:applicationContext.xml&lt;/param-value&gt;
 *     &lt;/context-param&gt;
 *     &lt;listener&gt;
 *       &lt;listener-class&gt;org.springframework.web.context.ContextLoaderListener&lt;/listener-class&gt;
 *     &lt;/listener&gt;
 *     &lt;servlet&gt;
 *       &lt;servlet-name&gt;Jersey Spring Web Application&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;com.sun.jersey.spi.spring.container.servlet.SpringServlet&lt;/servlet-class&gt;
 *     &lt;/servlet&gt;
 *     &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;Jersey Spring Web Application&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 *   &lt;/web-app&gt;
 * </blockquote></pre>
 * Any root resource classes or provider classes declared by Spring (in the XML
 * configuration or by auto-wiring) will be automatically registered. It is
 * possible to intermix Spring and non-Spring registration of classes by
 * additionally using the normal Jersey-based registration mechanisms. For example,
 * in the following web.xml:
 * <blockquote><pre>
 *   &lt;web-app&gt;
 *     &lt;context-param&gt;
 *       &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
 *       &lt;param-value&gt;classpath:applicationContext.xml&lt;/param-value&gt;
 *     &lt;/context-param&gt;
 *     &lt;listener&gt;
 *       &lt;listener-class&gt;org.springframework.web.context.ContextLoaderListener&lt;/listener-class&gt;
 *     &lt;/listener&gt;
 *     &lt;servlet&gt;
 *       &lt;servlet-name&gt;Jersey Spring Web Application&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;com.sun.jersey.spi.spring.container.servlet.SpringServlet&lt;/servlet-class&gt;
 *       &lt;init-param&gt;
 *           &lt;param-name&gt;com.sun.jersey.config.property.packages&lt;/param-name&gt;
 *           &lt;param-value&gt;managed&lt;/param-value&gt;
 *       &lt;/init-param>
 *     &lt;/servlet&gt;
 *     &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;Jersey Spring Web Application&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 *   &lt;/web-app&gt;
 * </blockquote></pre>
 * The above examples apply to Servlet-based configurations but they equally
 * applicable to Filter-based configurations. For example, the following
 * presents the same package-based configuration as above but utilizing a filter:
 * <blockquote><pre>
 *   &lt;web-app&gt;
 *     &lt;context-param&gt;
 *       &lt;param-name&gt;contextConfigLocation&lt;/param-name&gt;
 *       &lt;param-value&gt;classpath:applicationContext.xml&lt;/param-value&gt;
 *     &lt;/context-param&gt;
 *     &lt;listener&gt;
 *       &lt;listener-class&gt;org.springframework.web.context.ContextLoaderListener&lt;/listener-class&gt;
 *     &lt;/listener&gt;
 *     &lt;filter&gt;
 *       &lt;filter-name&gt;Jersey Spring Web Application&lt;/filter-name&gt;
 *       &lt;filter-class&gt;com.sun.jersey.spi.spring.container.servlet.SpringServlet&lt;/filter-class&gt;
 *     &lt;/filter&gt;
 *     &lt;filter-mapping&gt;
 *       &lt;filter-name&gt;Jersey Spring Web Application&lt;/filter-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/filter-mapping&gt;
 *   &lt;/web-app&gt;
 * </blockquote></pre>
 * Spring-based classes will be registered and any root resource and provider
 * classes in the package <code>managed</code> will also be registered. A class
 * will only be registered at most once so it does not matter if there are Spring
 * managed classes present in the <code>managed</code> package.
 * Components managed by Spring will not be managed by Jersey and the 
 * Jersey-based life-cycle annotations on a resource class will be ignored.
 * <p>
 *
 */
package com.sun.jersey.spi.spring.container.servlet;
