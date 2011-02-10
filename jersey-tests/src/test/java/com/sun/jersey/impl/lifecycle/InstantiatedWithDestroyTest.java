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

package com.sun.jersey.impl.lifecycle;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ioc.IoCDestroyable;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class InstantiatedWithDestroyTest extends AbstractResourceTester {
    
    public InstantiatedWithDestroyTest(String testName) {
        super(testName);
    }
    
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ManagedResource {}

    @ManagedResource
    @Path("/")
    public static class PerRequestResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    @ManagedResource
    @Singleton
    @Path("/")
    public static class SingletonResource {
        @GET
        public String get() {
            return "GET";
        }
    }

    @ManagedResource
    @Provider
    public static class StringWriter implements MessageBodyWriter<String> {

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        public long getSize(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(t.getBytes());
        }
    }

    static class InstantiatedIoCComponentProviderFactory implements IoCComponentProviderFactory {
        private Set<Class> destroyedClasses = new HashSet<Class>();

        private boolean hasGetInjectableInstanceBeenCalled = false;
        
        public Set<Class> getDestroyedClasses() {
            return destroyedClasses;
        }

        public boolean hasGetInjectableInstanceBeenCalled() {
            return hasGetInjectableInstanceBeenCalled;
        }
        
        public IoCComponentProvider getComponentProvider(Class c) {
            if (!c.isAnnotationPresent(ManagedResource.class))
                return null;

            return new InstantiatedIoCComponentProvider(c);
        }

        public IoCComponentProvider getComponentProvider(ComponentContext cc, Class c) {
            return getComponentProvider(c);
        }

        private class InstantiatedIoCComponentProvider implements IoCInstantiatedComponentProvider, IoCDestroyable {
            private final Class c;

            InstantiatedIoCComponentProvider(Class c) {
                this.c = c;
            }

            public Object getInjectableInstance(Object o) {
                hasGetInjectableInstanceBeenCalled = true;
                return o;
            }

            public Object getInstance() {
                try {
                    return c.newInstance();
                } catch (Exception ex) {
                    throw new ContainerException(ex);
                }
            }

            public void destroy(Object o) {
                destroyedClasses.add(o.getClass());
            }

        }
    }

    public void testPerRequest() {
        InstantiatedIoCComponentProviderFactory f = new InstantiatedIoCComponentProviderFactory();
        initiateWebApplication(f, PerRequestResource.class);
        
        String s = resource("/").get(String.class);

        assertTrue(f.getDestroyedClasses().contains(PerRequestResource.class));
        assertFalse(f.hasGetInjectableInstanceBeenCalled());
    }

    public void testSingleton() {
        InstantiatedIoCComponentProviderFactory f = new InstantiatedIoCComponentProviderFactory();
        initiateWebApplication(f, SingletonResource.class, StringWriter.class);

        String s = resource("/").get(String.class);

        w.destroy();
        
        assertTrue(f.getDestroyedClasses().contains(SingletonResource.class));
        assertTrue(f.getDestroyedClasses().contains(StringWriter.class));
        assertFalse(f.hasGetInjectableInstanceBeenCalled());
    }
}