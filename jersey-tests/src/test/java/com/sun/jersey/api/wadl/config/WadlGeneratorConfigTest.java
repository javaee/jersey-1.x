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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.api.wadl.config;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;
import junit.framework.TestCase;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Aug 2, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlGeneratorConfigTest extends TestCase {

    public WadlGeneratorConfigTest(String testName) {
        super(testName);
    }

    public void testBuildWadlGeneratorFromGenerators() {
        final MyWadlGenerator generator = new MyWadlGenerator();
        final MyWadlGenerator2 generator2 = new MyWadlGenerator2();
        WadlGeneratorConfig config = WadlGeneratorConfig.
                generator(generator).
                generator(generator2).
                build();

        WadlGenerator wadlGenerator = config.getWadlGenerator();

        assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());
        assertEquals(MyWadlGenerator.class, ((MyWadlGenerator2) wadlGenerator).getDelegate().getClass());
    }

    public void testBuildWadlGeneratorFromDescriptions() {
        final String propValue = "bar";
        WadlGeneratorConfig config = WadlGeneratorConfig.generator(MyWadlGenerator.class).
                prop("foo", propValue).
                build();
        WadlGenerator wadlGenerator = config.getWadlGenerator();
        assertEquals(MyWadlGenerator.class, wadlGenerator.getClass());
        assertEquals(((MyWadlGenerator) wadlGenerator).getFoo(), propValue);

        final String propValue2 = "baz";
        config = WadlGeneratorConfig.generator(MyWadlGenerator.class).
                prop("foo", propValue).generator(MyWadlGenerator2.class).
                prop("bar", propValue2).
                build();
        wadlGenerator = config.getWadlGenerator();
        assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());
        final MyWadlGenerator2 wadlGenerator2 = (MyWadlGenerator2) wadlGenerator;
        assertEquals(wadlGenerator2.getBar(), propValue2);

        assertEquals(MyWadlGenerator.class, wadlGenerator2.getDelegate().getClass());
        assertEquals(((MyWadlGenerator) wadlGenerator2.getDelegate()).getFoo(), propValue);

    }

    public void testCustomWadlGeneratorConfig() {

        final String propValue = "someValue";
        final String propValue2 = "baz";
        class MyWadlGeneratorConfig extends WadlGeneratorConfig {

            @Override
            public List<WadlGeneratorDescription> configure() {
                return generator(MyWadlGenerator.class).
                        prop("foo", propValue).
                        generator(MyWadlGenerator2.class).
                        prop("bar", propValue2).descriptions();
            }
        }

        WadlGeneratorConfig config = new MyWadlGeneratorConfig();
        WadlGenerator wadlGenerator = config.getWadlGenerator();

        assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());
        final MyWadlGenerator2 wadlGenerator2 = (MyWadlGenerator2) wadlGenerator;
        assertEquals(wadlGenerator2.getBar(), propValue2);

        assertEquals(MyWadlGenerator.class, wadlGenerator2.getDelegate().getClass());
        assertEquals(((MyWadlGenerator) wadlGenerator2.getDelegate()).getFoo(), propValue);

    }

    static abstract class BaseWadlGenerator implements WadlGenerator {

        public Application createApplication() {
            return null;
        }

        public Method createMethod(AbstractResource r, AbstractResourceMethod m) {
            return null;
        }

        public Request createRequest(AbstractResource r,
                AbstractResourceMethod m) {
            return null;
        }

        public Param createParam(AbstractResource r,
                AbstractMethod m, Parameter p) {
            return null;
        }

        public Representation createRequestRepresentation(
                AbstractResource r, AbstractResourceMethod m,
                MediaType mediaType) {
            return null;
        }

        public Resource createResource(AbstractResource r, String path) {
            return null;
        }

        public Resources createResources() {
            return null;
        }

        public Response createResponse(AbstractResource r,
                AbstractResourceMethod m) {
            return null;
        }

        public String getRequiredJaxbContextPath() {
            return null;
        }

        public void init() throws Exception {
        }

        public void setWadlGeneratorDelegate(WadlGenerator delegate) {
        }
    }

    static class MyWadlGenerator extends BaseWadlGenerator {

        private String _foo;

        /**
         * @return the foo
         */
        public String getFoo() {
            return _foo;
        }

        /**
         * @param foo the foo to set
         */
        public void setFoo(String foo) {
            _foo = foo;
        }
    }

    static class MyWadlGenerator2 extends BaseWadlGenerator {

        private String _bar;
        private WadlGenerator _delegate;

        /**
         * @return the delegate
         */
        public WadlGenerator getDelegate() {
            return _delegate;
        }

        /**
         * @return the foo
         */
        public String getBar() {
            return _bar;
        }

        /**
         * @param foo the foo to set
         */
        public void setBar(String foo) {
            _bar = foo;
        }

        public void setWadlGeneratorDelegate(WadlGenerator delegate) {
            _delegate = delegate;
        }
    }

    static class Foo {

        String s;

        public Foo(String s) {
            this.s = s;
        }
    }

    static class Bar {
    }

    static class MyWadlGenerator3 extends BaseWadlGenerator {

        Foo foo;
        Bar bar;

        /**
         * @param foo the foo to set
         */
        public void setFoo(Foo foo) {
            this.foo = foo;
        }

        public void setBar(Bar bar) {
            this.bar = bar;
        }
    }

    public void testBuildWadlGeneratorFromDescriptionsWithTypes() {
        WadlGeneratorConfig config = WadlGeneratorConfig.
                generator(MyWadlGenerator3.class).
                prop("foo", "string").
                prop("bar", new Bar()).build();
        WadlGenerator wadlGenerator = config.getWadlGenerator();

        assertEquals(MyWadlGenerator3.class, wadlGenerator.getClass());

        MyWadlGenerator3 g = (MyWadlGenerator3) wadlGenerator;
        assertNotNull(g.foo);
        assertEquals(g.foo.s, "string");
        assertNotNull(g.bar);
    }
}
