/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.test.framework.impl.container.grizzlyweb;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;

/**
 * @author pavel.bucek@sun.com
 */
public class GrizzlyWebServletAndFilterTest extends JerseyTest {

    public static class MyServlet extends ServletContainer {

        public static boolean visited = false;

        @Override
        public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.service(req, resp);
            visited = true;
        }
    }

    public static class MyFilter1 implements Filter {

        public static boolean visited = false;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            visited = true;
            filterChain.doFilter(servletRequest, servletResponse);
        }

        @Override
        public void destroy() {
        }
    }

    public static class MyFilter2 implements Filter {

        public static boolean visited = false;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            visited = true;
            filterChain.doFilter(servletRequest, servletResponse);
        }

        @Override
        public void destroy() {
        }
    }

    @Path("GrizzlyWebServletAndFilterTest")
    public static class TestResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    public GrizzlyWebServletAndFilterTest() {
        super(new WebAppDescriptor.Builder().servletClass(MyServlet.class)
                .addFilter(MyFilter1.class, "myFilter1")
                .addFilter(MyFilter2.class, "myFilter2")
                .initParam(PackagesResourceConfig.PROPERTY_PACKAGES, "com.sun.jersey.test.framework.impl.container.grizzlyweb").build());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    public void testGet() {
        WebResource r = resource().path("GrizzlyWebServletAndFilterTest");

        String s = r.get(String.class);
        Assert.assertEquals("GET", s);

        Assert.assertTrue(MyServlet.visited);
        Assert.assertTrue(MyFilter1.visited);
        Assert.assertTrue(MyFilter2.visited);
    }
}
