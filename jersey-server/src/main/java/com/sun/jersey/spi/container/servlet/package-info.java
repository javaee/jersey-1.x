/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
/**
 * Provides support for servlet-based and filter-based Web applications.
 * <p>
 * Web application support is enabled by referencing the servlet
 * {@link com.sun.jersey.spi.container.servlet.ServletContainer} in the
 * web.xml.
 * <p>
 * For example, the following will deploy Jersey and automatically
 * register any root resource or provider classes present in the directory
 * "/WEB-INF/classes" or jar files present in the directory "/WEB-INF/lib":
 * <blockquote><pre>
 *   &lt;web-app&gt;
 *     &lt;servlet&gt;
 *       &lt;servlet-name&gt;Jersey Web Application&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;com.sun.jersey.spi.container.servlet.ServletContainer&lt;/servlet-class&gt;
 *     &lt;/servlet&gt;
 *     &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;Jersey Web Application&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 *   &lt;/web-app&gt;
 * </blockquote></pre>
 * <p>
 * A deployment approach, that is more portable with respect to maven and 
 * application servers, is to declare the package names where root resource and provider
 * classes reside. For example, the following will deploy Jersey and
 * automatically register any root resource or provider classes present
 * in the package "managed", or any sub-packages.
 * <blockquote><pre>
 *   &lt;web-app&gt;
 *     &lt;servlet&gt;
 *       &lt;servlet-name&gt;Jersey Web Application&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;com.sun.jersey.spi.container.servlet.ServletContainer&lt;/servlet-class&gt;
 *       &lt;init-param&gt;
 *           &lt;param-name&gt;com.sun.jersey.config.property.packages&lt;/param-name&gt;
 *           &lt;param-value&gt;managed&lt;/param-value&gt;
 *       &lt;/init-param>
 *     &lt;/servlet&gt;
 *     &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;Jersey Web Application&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 *   &lt;/web-app&gt;
 * </blockquote></pre>
 * The deployment approach that is portable accross JAX-RS implementations is to
 * register an implementation of {@link javax.ws.rs.core.Application}. For
 * example given an implementation as follows:
 * <blockquote><pre>
 *   package com.foo;
 *
 *   import ...
 * 
 *   public class MyApplicaton extends Application {
 *       public Set&lt;Class&lt;?&gt;&gt; getClasses() {
 *           Set&lt;Class&lt;?&gt;&gt; s = new HashSet&lt;Class&lt;?&gt;&gt;();
 *           s.add(HelloWorldResource.class);
 *           return s;
 *       }
 *   }
 * </blockquote></pre>
 * then that implementation can be registered as follows:
 * <blockquote><pre>
 *   &lt;web-app&gt;
 *     &lt;servlet&gt;
 *       &lt;servlet-name&gt;Jersey Web Application&lt;/servlet-name&gt;
 *       &lt;servlet-class&gt;com.sun.jersey.spi.container.servlet.ServletContainer&lt;/servlet-class&gt;
 *       &lt;init-param&gt;
 *           &lt;param-name&gt;javax.ws.rs.Application&lt;/param-name&gt;
 *           &lt;param-value&gt;com.foo.MyApplication&lt;/param-value&gt;
 *       &lt;/init-param>
 *     &lt;/servlet&gt;
 *     &lt;servlet-mapping&gt;
 *       &lt;servlet-name&gt;Jersey Web Application&lt;/servlet-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/servlet-mapping&gt;
 *   &lt;/web-app&gt;
 * </blockquote></pre>
 * It is possible to combine package-based registration and 
 * {@link javax.ws.rs.core.Application}
 * registered by extending {@link com.sun.jersey.api.core.PackagesResourceConfig}
 * and registering the extended class, for example:
 * <blockquote><pre>
 *   public class MyApplication extends PackagesResourceConfig {
 *       public MyApplication() {
 *           super("org.foo.rest;org.bar.rest");
 *       }
 *   }
 * </blockquote></pre>
 * The above examples apply to Servlet-based configurations but they equally
 * applicable to Filter-based configurations. For example, the following
 * presents the same package-based configuration as above but utilizing a filter:
 * <blockquote><pre>
 *   &lt;web-app&gt;
 *     &lt;filter&gt;
 *       &lt;filter-name&gt;Jersey Web Application&lt;/filter-name&gt;
 *       &lt;filter-class&gt;com.sun.jersey.spi.container.servlet.ServletContainer&lt;/filter-class&gt;
 *       &lt;init-param&gt;
 *           &lt;param-name&gt;com.sun.jersey.config.property.packages&lt;/param-name&gt;
 *           &lt;param-value&gt;managed&lt;/param-value&gt;
 *       &lt;/init-param>
 *     &lt;/filter&gt;
 *     &lt;filter-mapping&gt;
 *       &lt;filter-name&gt;Jersey Web Application&lt;/filter-name&gt;
 *       &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *     &lt;/filter-mapping&gt;
 *   &lt;/web-app&gt;
 * </blockquote></pre>
 *
 */
package com.sun.jersey.spi.container.servlet;
