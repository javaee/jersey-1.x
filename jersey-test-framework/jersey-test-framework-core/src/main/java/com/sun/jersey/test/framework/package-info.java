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
 * Provides support for testing JAX-RS and Jersey-based applications.
 * <p>
 * The {@link com.sun.jersey.test.framework.JerseyTest} class may be extended 
 * to define the testing configuration and functionality.
 * <p>
 * For example, the following class is configured to use the low-level Grizzly
 * test container factory,
 * {@link com.sun.jersey.test.framework.spi.container.grizzly.GrizzlyTestContainerFactory},
 * and test that a simple resource, <code>TestResource</code> returns the
 * expected results for a GET request.
 * <blockquote><pre>
public class SimpleLowLevelGrizzlyTest extends JerseyTest {

    &#64;Path("root")
    public static class TestResource {
        &#64;GET
        public String get() {
            return "GET";
        }
    }

    public SimpleLowLevelGrizzlyTest() {
        super(new LowLevelAppDescriptor.Builder(TestResource.class).
                contextPath("context").
                build());
    }

    &#64;Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyTestContainerFactory();
    }

    &#64;Test
    public void testGet() {
        WebResource r = resource().path("root");

        String s = r.get(String.class);
        Assert.assertEquals("GET", s);
    }
}
 * </blockquote></pre>
 * <p>
 * The following tests the same functionality using the Web-based Grizzly
 * test container factory,
 * {@link com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory}.
 * <blockquote><pre>
package foo;

public class WebBasedTest extends JerseyTest {

    &#64;Path("root")
    public static class TestResource {
        &#64;GET
        public String get() {
            return "GET";
        }
    }

    public WebBasedTest() {
        super(new WebAppDescriptor.Builder("foo").
                contextPath("context").
                build());
    }

    &#64;Test
    public void testGet() {
        WebResource r = resource().path("root");

        String s = r.get(String.class);
        Assert.assertEquals("GET", s);
    }
}
 * </blockquote></pre>
 * The above test is actually not specific to any Web-based test container 
 * factory and will work for all provided test container factories, however the
 * default factory will be the
 * {@link com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory}.
 * See the documentation on {@link com.sun.jersey.test.framework.JerseyTest} for
 * more details on how to set the default test container factory.
 */
package com.sun.jersey.test.framework;