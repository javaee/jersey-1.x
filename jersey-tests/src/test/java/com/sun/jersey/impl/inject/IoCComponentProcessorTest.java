/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.impl.inject;

import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactory;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessorFactoryInitializer;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCFullyManagedComponentProvider;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.resource.Singleton;
import com.sun.jersey.spi.template.TemplateContext;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;


/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class IoCComponentProcessorTest extends AbstractResourceTester {
    
    public IoCComponentProcessorTest(String testName) {
        super(testName);
    }

    public static interface PostConstructListener {
        void postConstruct();

    }

    public static interface PerRequestScope {}

    public static interface SingletonScope {}
    
    public static class AbstractResource implements PostConstructListener {
        @Context ResourceConfig rc;

        @Context MessageBodyWorkers mbw;

        @Context TemplateContext tc;

        @Context HttpContext hca;

        @Context HttpHeaders hs;

        @Context UriInfo ui;

        @Context ExtendedUriInfo eui;

        @Context Request r;

        @Context SecurityContext sc;

        @Context Providers p;

        public void postConstruct() {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }
    }

    @Path("/perrequest")
    public static class PerRequestResource extends AbstractResource implements PerRequestScope {
        @QueryParam("q") String q;

        @Override
        public void postConstruct() {
            super.postConstruct();
            assertNotNull(q);
        }
        
        @GET
        public String get() {
            return "GET " + q + " " + ui.getRequestUri();
        }
    }

    @Path("/singleton")
    @Singleton
    public static class SingletonResource extends AbstractResource implements SingletonScope {
        @GET
        public String get(@QueryParam("q") String q) {
            return "GET " + q + " " + ui.getRequestUri();
        }
    }

    @Provider
    public static class MyProvider implements MessageBodyWriter<String>, PostConstructListener, SingletonScope {
        @Context ResourceConfig rc;

        @Context MessageBodyWorkers mbw;

        @Context TemplateContext tc;

        @Context HttpContext hca;

        @Context HttpHeaders hs;

        @Context UriInfo ui;

        @Context ExtendedUriInfo eui;

        @Context Request r;

        @Context SecurityContext sc;

        @Context Providers p;

        public void postConstruct() {
            assertNotNull(rc);
            assertNotNull(mbw);
            assertNotNull(tc);
            assertNotNull(hca);
            assertNotNull(hs);
            assertNotNull(ui);
            assertNotNull(eui);
            assertNotNull(r);
            assertNotNull(sc);
            assertNotNull(p);
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        public long getSize(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            t = t + " " + ui.getRequestUri();
            entityStream.write(t.getBytes());
        }
    }
    
    static class MyIoCComponentProviderFactory implements 
            IoCComponentProviderFactory,
            IoCComponentProcessorFactoryInitializer {

        private IoCComponentProcessorFactory cpf;

        public void init(IoCComponentProcessorFactory cpf) {
            this.cpf = cpf;
        }

        public IoCComponentProvider getComponentProvider(final Class c) {
            if (PostConstructListener.class.isAssignableFrom(c)) {
                final ComponentScope cs = (!c.isAnnotationPresent(Provider.class))
                        ? cpf.getScope(c)
                        : ComponentScope.Singleton;
                final IoCComponentProcessor cp = cpf.get(c, cs);

                if (cp != null) {
                    return new IoCFullyManagedComponentProvider() {

                        public Object getInstance() {
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
                    return new IoCFullyManagedComponentProvider() {

                        public Object getInstance() {
                            Object o = null;
                            try {
                                o = c.newInstance();
                            } catch (InstantiationException ex) {
                                throw new RuntimeException(ex);
                            } catch (IllegalAccessException ex) {
                                throw new RuntimeException(ex);
                            }
                            return o;
                        }

                        public ComponentScope getScope() {
                            return cs;
                        }
                    };
                }
            } else {
                return null;
            }
        }

        public IoCComponentProvider getComponentProvider(ComponentContext cc, Class c) {
            return getComponentProvider(c);
        }
    }

    public void testInjected() throws IOException {
        initiateWebApplication(new MyIoCComponentProviderFactory(), 
                PerRequestResource.class, SingletonResource.class, MyProvider.class);

        String s = resource("/perrequest").queryParam("q", "p").get(String.class);
        assertEquals("GET p test:/base/perrequest?q=p test:/base/perrequest?q=p", s);

        s = resource("/singleton").queryParam("q", "p").get(String.class);
        assertEquals("GET p test:/base/singleton?q=p test:/base/singleton?q=p", s);
    }


    @Path("/")
    public static class MyResourceNoInject implements PostConstructListener, PerRequestScope {
        @GET
        public String get() {
            return "GET";
        }

        public void postConstruct() {
        }
    }

    public void testNoInjected() throws IOException {
        initiateWebApplication(new MyIoCComponentProviderFactory(), MyResourceNoInject.class);

        String s = resource("/").get(String.class);
        assertEquals("GET", s);
    }
}