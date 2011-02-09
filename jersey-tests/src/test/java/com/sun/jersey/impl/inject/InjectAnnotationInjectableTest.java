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
package com.sun.jersey.impl.inject;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 */
public class InjectAnnotationInjectableTest extends AbstractResourceTester {
    
    public InjectAnnotationInjectableTest(String testName) {
        super(testName);
    }

    @Singleton
    public static class SingletonSubResourceResource {
        @GET
        public String get() {
            return "SINGLETON";
        }
    }

    @Path("/")
    public static class PerRequestResource {
        private final SingletonSubResourceResource sr;

        public PerRequestResource(@Inject SingletonSubResourceResource sr) {
            this.sr = sr;
        }

        @Path("sr")
        public SingletonSubResourceResource get(@Inject SingletonSubResourceResource _sr) {
            assertEquals(sr, _sr);
            return sr;
        }
    }

    public void testPerRequest() {
        initiateWebApplication(PerRequestResource.class);

        String value = resource("/sr").get(String.class);
        assertEquals("SINGLETON", value);
    }


    public static class PerRequestSubResourceResource {
        @GET
        public String get() {
            return "PER-REQUEST";
        }
    }

    @Singleton
    @Path("/")
    public static class SingletonResource {
        private final Injectable<PerRequestSubResourceResource> request;
        private final SingletonSubResourceResource singleton;
        
        public SingletonResource(
                @Inject Injectable<PerRequestSubResourceResource> request,
                @Inject SingletonSubResourceResource singleton) {
            this.request = request;
            this.singleton = singleton;
        }

        @Path("request")
        public PerRequestSubResourceResource get(@Inject PerRequestSubResourceResource _sr) {
            PerRequestSubResourceResource sr = request.getValue();
            assertEquals(sr, _sr);
            return sr;
        }

        @Path("singleton")
        public SingletonSubResourceResource get(@Inject SingletonSubResourceResource _singleton) {
            assertEquals(singleton, _singleton);
            return singleton;
        }
    }

    public void testSingleton() {
        initiateWebApplication(SingletonResource.class);

        String value = resource("/request").get(String.class);
        assertEquals("PER-REQUEST", value);

        value = resource("/singleton").get(String.class);
        assertEquals("SINGLETON", value);
    }


    @Singleton
    @Path("/")
    public static class BadInjectSingletonResource {
        private final PerRequestSubResourceResource sr;

        public BadInjectSingletonResource(@Inject PerRequestSubResourceResource sr) {
            this.sr = sr;
            assertNull(sr);
        }
        
        @GET
        public String get() {
            return "SINGLETON";
        }
    }

    public void testBadInjectSingleton() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(BadInjectSingletonResource.class);
            }
        }, Errors.ErrorMessagesException.class).messages;

        assertEquals(1, messages.size());
    }


    @Path("/")
    public static class PerRequestNamedInjectResource {
        private final SingletonSubResourceResource sr1;

        private final SingletonSubResourceResource sr2;

        public PerRequestNamedInjectResource(
                @Inject("1") SingletonSubResourceResource sr1,
                @Inject("2") SingletonSubResourceResource sr2) {
            this.sr1 = sr1;
            this.sr2 = sr2;
        }

        @Path("sr1")
        public SingletonSubResourceResource get1(@Inject("1") SingletonSubResourceResource _sr1) {
            assertEquals(sr1, _sr1);
            return sr1;
        }

        @Path("sr2")
        public SingletonSubResourceResource get2(@Inject("2") SingletonSubResourceResource _sr2) {
            assertEquals(sr2, _sr2);
            return sr2;
        }
    }

    public void testPerRequestNamedInjectResource() {
        initiateWebApplication(PerRequestNamedInjectResource.class);

        assertEquals("SINGLETON", resource("/sr1").get(String.class));
        assertEquals("SINGLETON", resource("/sr2").get(String.class));
    }



    @Path("/")
    public static class MyResource {
        
        @Inject MyBean myBean;
        
        @GET
        public MyBean get() {
            assertNotNull(myBean);
            return myBean;
        }                
    }
    
    public void testInjected() throws IOException {
        final String value = "foo";

        initiateWebApplication(new MyIoCComponentProviderFactory(value), MyResource.class);

        final MyBean myBean = resource("/").get(MyBean.class);
        assertEquals(value, myBean.value);
    }

    static class MyIoCComponentProviderFactory implements IoCComponentProviderFactory {

        private final String _valueToSet;

        public MyIoCComponentProviderFactory(String valueToSet) {
            _valueToSet = valueToSet;
        }

        public IoCComponentProvider getComponentProvider(Class c) {
            if (c == MyBean.class) {
                return new IoCFullyManagedComponentProvider() {
                    public ComponentScope getScope() {
                        return ComponentScope.PerRequest;
                    }

                    public Object getInstance() {
                        return new MyBean(_valueToSet);
                    }
                };
            } else {
                return null;
            }
        }

        public IoCComponentProvider getComponentProvider(ComponentContext cc, Class c) {
            return getComponentProvider(c);
        }
    }
}