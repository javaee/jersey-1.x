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

package com.sun.jersey.impl.lifecycle;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.server.impl.resource.PerRequestFactory;
import com.sun.jersey.server.impl.resource.SingletonFactory;
import com.sun.jersey.spi.resource.PerRequest;
import com.sun.jersey.spi.resource.Singleton;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author Marc Hadley
 */
public class ResourceLifecycleTest extends AbstractResourceTester {
    
    @Path("foo")
    @Singleton
    public static class TestFooBean {
        
        private int count;
        
        public TestFooBean() {
            this.count = 0;
        }
        
        @GET
        public String doGet() {
            count++;
            return Integer.toString(count);
        }
        
    }
    
    @Path("bar")
    @PerRequest
    public static class TestBarBean {
        
        private int count;
        
        public TestBarBean() {
            this.count = 0;
        }
        
        @GET
        public String doGet() {
            count++;
            return Integer.toString(count);
        }
        
    }
    
    @Path("baz")
    public static class TestBazBean {
        
        private int count;
        
        public TestBazBean() {
            this.count = 0;
        }
        
        @GET
        public String doGet() {
            count++;
            return Integer.toString(count);
        }
        
    }
    
    WebApplicationImpl a;
    
    public ResourceLifecycleTest(String testName) {
        super(testName);
    }
    
    private ResourceConfig getResourceConfig() {
        final Set<Class<?>> r = new HashSet<Class<?>>();
        r.add(TestFooBean.class);
        r.add(TestBarBean.class);
        r.add(TestBazBean.class);
        return new DefaultResourceConfig(r);
    }
    
    public void testDefault() {
        initiateWebApplication(getResourceConfig());
        _test();
    }

    public void testOverrideDefaultWithPerRequest() {
        ResourceConfig c = getResourceConfig();
        c.getProperties().put(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS,
                PerRequestFactory.class);

        initiateWebApplication(c);
        _test();
    }

    public void testOverrideDefaultWithSingleton() {
        ResourceConfig c = getResourceConfig();
        c.getProperties().put(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS,
                SingletonFactory.class);

        initiateWebApplication(c);
        _test(true);
    }

    public void testOverrideDefaultWithSingletonClassName() {
        ResourceConfig c = getResourceConfig();
        c.getProperties().put(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS,
                SingletonFactory.class.getName());

        initiateWebApplication(c);
        _test(true);
    }

    public void testNullResourceProviderProperty() {
        ResourceConfig c = getResourceConfig();
        c.getProperties().put(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS,
                null);

        initiateWebApplication(c);
        _test();
    }

    public void testBadTypeResourceProviderProperty() {
        ResourceConfig c = getResourceConfig();
        c.getProperties().put(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS,
                1);

        boolean caught = false;
        try {
            initiateWebApplication(c);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    public void testBadClassNameResourceProviderProperty() {
        ResourceConfig c = getResourceConfig();
        c.getProperties().put(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS,
                "VALUE");

        boolean caught = false;
        try {
            initiateWebApplication(c);
        } catch (ContainerException e) {
            assertEquals(ClassNotFoundException.class, e.getCause().getClass());
            caught = true;
        }
        assertTrue(caught);
    }

    public void testBadClassResourceProviderProperty() {
        ResourceConfig c = getResourceConfig();
        c.getProperties().put(ResourceConfig.PROPERTY_DEFAULT_RESOURCE_COMPONENT_PROVIDER_FACTORY_CLASS,
                String.class);

        boolean caught = false;
        try {
            initiateWebApplication(c);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);
    }

    private void _test() {
        _test(false);
    }

    private void _test(boolean isDefaultSingleton) {
        WebResource r = resource("/foo");
        assertEquals("1", r.get(String.class));
        assertEquals("2", r.get(String.class));
        assertEquals("3", r.get(String.class));

        r = resource("/bar");
        assertEquals("1", r.get(String.class));
        assertEquals("1", r.get(String.class));
        assertEquals("1", r.get(String.class));

        r = resource("/baz");
        if (isDefaultSingleton) {
            assertEquals("1", r.get(String.class));
            assertEquals("2", r.get(String.class));
            assertEquals("3", r.get(String.class));
        } else {
            assertEquals("1", r.get(String.class));
            assertEquals("1", r.get(String.class));
            assertEquals("1", r.get(String.class));
        }
    }
    
    @Path("foo")
    public static class TestFooBeanSingleton {
        
        private int count;
        
        public TestFooBeanSingleton(String junk) {
            assertNotNull(junk);
            this.count = 0;
        }
        
        @GET
        public String doGet(@Context ResourceContext rc) {
            count++;
            TestFooBeanSingleton that = rc.getResource(TestFooBeanSingleton.class);
            assertEquals(this, that);
            return Integer.toString(count);
        }
        
    }
    
    public void testSingleton() {
        ResourceConfig c = new DefaultResourceConfig();
        c.getSingletons().add(new TestFooBeanSingleton("junk"));
        initiateWebApplication(c);
        
        WebResource r = resource("/foo");        
        assertEquals("1", r.get(String.class));
        assertEquals("2", r.get(String.class));
        assertEquals("3", r.get(String.class));
    }
}