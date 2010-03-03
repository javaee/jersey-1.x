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
package com.sun.jersey.impl.client.inject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import junit.framework.TestCase;


/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class IoCComponentProcessorTest extends TestCase {
    
    public IoCComponentProcessorTest(String testName) {
        super(testName);
    }

    public static interface PostConstructListener {
        void postConstruct();

    }

    public static interface SingletonScope {}
    

    @Provider
    public static class MyProvider implements MessageBodyWriter<String>, PostConstructListener, SingletonScope {
        @Context Providers p;

        @Context MessageBodyWorkers mbw;

        @Context ClientConfig cc;

        @Context FeaturesAndProperties fap;

        @Context Client c;

        public void postConstruct() {
            assertNotNull(p);
            assertNotNull(mbw);
            assertNotNull(cc);
            assertNotNull(fap);
            assertNotNull(c);
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        public long getSize(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(t.getBytes());
        }
    }
    
    static class MyIoCComponentProviderFactory implements
            IoCComponentProviderFactory,
            IoCComponentProcessorFactoryInitializer {

        private IoCComponentProcessorFactory cpf;

        public Class<?> componentClass;

        public void init(IoCComponentProcessorFactory cpf) {
            this.cpf = cpf;
        }

        public IoCComponentProvider getComponentProvider(final Class c) {
            if (PostConstructListener.class.isAssignableFrom(c)) {
                final ComponentScope cs = SingletonScope.class.isAssignableFrom(c) ? ComponentScope.Singleton : ComponentScope.PerRequest;
                final IoCComponentProcessor cp = cpf.get(c, cs);
                return new IoCFullyManagedComponentProvider() {

                    public Object getInstance() {
                        componentClass = c;
                        Object o = null;
                        try {
                            o = c.newInstance();
                        } catch (InstantiationException ex) {
                            throw new RuntimeException(ex);
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                        cp.postConstruct(o);
                        ((PostConstructListener)o).postConstruct();
                        return o;
                    }

                    public ComponentScope getScope() {
                        return cs;
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

    public void testInjected() throws IOException {
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(MyProvider.class);
        MyIoCComponentProviderFactory pf = new MyIoCComponentProviderFactory();
        Client c = Client.create(cc, pf);
        assertEquals(MyProvider.class, pf.componentClass);
    }


    @Provider
    public static class MyProviderNoInject implements MessageBodyWriter<String>, PostConstructListener, SingletonScope {
        public void postConstruct() {
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        public long getSize(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(t.getBytes());
        }
    }

    public void testNoInjected() throws IOException {
        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(MyProviderNoInject.class);
        MyIoCComponentProviderFactory pf = new MyIoCComponentProviderFactory();
        Client c = Client.create(cc, pf);
        assertEquals(MyProviderNoInject.class, pf.componentClass);
    }
}