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
package com.sun.jersey.impl.errors;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.resource.Singleton;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MissingDependenciesTest extends AbstractResourceTester {
    
    public MissingDependenciesTest(String testName) {
        super( testName );
    }

    private Errors.ErrorMessagesException catches(Closure c) {
        return catches(c, Errors.ErrorMessagesException.class);
    }

    @Path("/")
    public static class MissingDependenciesResource {
        @Context String injectedValue;
        
        public MissingDependenciesResource(@Context String injectedValue) {
            this.injectedValue = injectedValue;
        }

        @Context
        public void set(String injectedValue) {
            this.injectedValue = injectedValue;
        }

        @GET
        public void get(@Context String injectedValue) {
        }

        @POST
        public void post(@Context String injectedValue) {
        }

        @GET
        @Path("subm")
        public void getSub(@Context String injectedValue) {
        }

        @POST
        @Path("subm")
        public void postSub(@Context String injectedValue) {
        }

        @Path("sub")
        public MissingDependenciesResource sub(@Context String injectedValue) {
            return new MissingDependenciesResource("value");
        }

    }

    public void testMissingDependenciesResource() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            @Override
            public void f() {
                initiateWebApplication(MissingDependenciesResource.class);
            }
        }).messages;

        assertEquals(14, messages.size());
    }


    @Path("/")
    public static class MissingDependenciesSubResource {
      @Path("instance")
      public MissingDependenciesResource getInstance() {
          return new MissingDependenciesResource("instance");
      }

      @Path("class")
      public Class<MissingDependenciesResource> getClazz() {
          return MissingDependenciesResource.class;
      }
    }

    public void testMissingDependenciesSubResourceInstance() {
        initiateWebApplication(MissingDependenciesSubResource.class);

        List<Errors.ErrorMessage> messages = catches(new Closure() {
            public void f() {
                resource("/instance").get(String.class);
            }        
        }).messages;


        assertEquals(11, messages.size());
    }
    
    public void testMissingDependenciesSubResourceClass() {
        initiateWebApplication(MissingDependenciesSubResource.class);

        List<Errors.ErrorMessage> messages = catches(new Closure() {
            public void f() {
                resource("/class").get(String.class);
            }
        }).messages;


        assertEquals(3, messages.size());
    }


    @Path("/")
    @Singleton
    public static class MissingDependenciesSingletonResource {
        @QueryParam("foo") String foo;
    }

    public void testMissingDependenciesSingletonResource() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            public void f() {
                initiateWebApplication(MissingDependenciesSingletonResource.class);
            }
        }).messages;

        assertEquals(1, messages.size());
    }


    @Path("/")
    public static class MissingDependenciesProviderResource {
    }

    @Provider
    public static class MissingDependenciesProvider implements MessageBodyWriter<String> {

        @Context String injectedValue;

        public MissingDependenciesProvider(@Context String injectedValue) {
            this.injectedValue = injectedValue;
        }

        @Context
        public void set(String injectedValue) {
            this.injectedValue = injectedValue;
        }
        
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == String.class;
        }

        @Override
        public long getSize(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(String t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(t.getBytes());
        }
    }

    public void testMissingDependenciesProvider() {
        List<Errors.ErrorMessage> messages = catches(new Closure() {
            public void f() {
                initiateWebApplication(MissingDependenciesProviderResource.class,
                        MissingDependenciesProvider.class);
            }
        }).messages;

        assertEquals(3, messages.size());
    }
}
