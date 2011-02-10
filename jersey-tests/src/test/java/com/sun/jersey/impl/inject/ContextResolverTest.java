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
package com.sun.jersey.impl.inject;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContextResolverTest extends AbstractResourceTester {
    
    public ContextResolverTest(String testName) {
        super(testName);
    }

    @Provider
    public static class IntegerContextResolver implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            if (Integer.class == objectType)
                return objectType.getName();
            else
                return null;
        }
    }

    @Provider
    public static class BigIntegerContextResolver implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            if (BigInteger.class == objectType)
                return objectType.getName();
            else
                return null;
        }
    }

    @Path("/")
    public static class NullContextResource {
        @Context ContextResolver<String> cr;

        @GET
        public String get() {
            return (cr == null) ? "null" : "value";
        }
    }

    @Path("/")
    public static class ContextResource {
        @Context Providers p;

        @Context ContextResolver<String> cr;

        @GET
        public String get() {
            ContextResolver<String> _cr = p.getContextResolver(String.class, null);
            assertEquals(_cr, cr);
            return cr.getContext(Integer.class);
        }

        @GET @Path("big")
        public String getBig() {
            return cr.getContext(BigInteger.class);
        }

        @GET @Path("null")
        public String getNull() {
            String s = cr.getContext(Float.class);
            return (s != null) ? s : "null";
        }
    }

    public void testZero() throws IOException {
        initiateWebApplication(NullContextResource.class);
        WebResource r = resource("/");

        assertEquals("null", resource("/").get(String.class));
    }

    public void testOne() throws IOException {
        initiateWebApplication(ContextResource.class, IntegerContextResolver.class);

        assertEquals("java.lang.Integer", resource("/").get(String.class));

        assertEquals("null", resource("/null").get(String.class));
    }

    public void testTwo() throws IOException {
        initiateWebApplication(ContextResource.class, IntegerContextResolver.class,
                BigIntegerContextResolver.class);

        assertEquals("java.lang.Integer", resource("/").get(String.class));

        assertEquals("java.math.BigInteger", resource("/big").get(String.class));

        assertEquals("null", resource("/null").get(String.class));
    }


    @Provider
    @Produces("application/one")
    public static class IntegerContextResolverMediaOne implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            if (Integer.class == objectType)
                return objectType.getName();
            else
                return null;
        }
    }

    @Provider
    @Produces("application/one")
    public static class ByteContextResolverMediaOne implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            if (Byte.class == objectType)
                return objectType.getName();
            else
                return null;
        }
    }

    @Provider
    @Produces("application/two")
    public static class BigIntegerContextResolverMediaTwo implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            if (BigInteger.class == objectType)
                return objectType.getName();
            else
                return null;
        }
    }

    @Path("/")
    public static class ContextMediaResource {
        ContextResolver<String> crOne;
        ContextResolver<String> crTwo;

        public ContextMediaResource(@Context Providers p) {
            crOne = p.getContextResolver(String.class, MediaType.valueOf("application/one"));
            assertNotNull(crOne);
            crTwo = p.getContextResolver(String.class, MediaType.valueOf("application/two"));
            assertNotNull(crTwo);
        }

        @GET
        public String get() {
            assertNull(crOne.getContext(BigInteger.class));
            return crOne.getContext(Integer.class);
        }

        @GET @Path("byte")
        public String getByte() {
            assertNull(crOne.getContext(BigInteger.class));
            return crOne.getContext(Byte.class);
        }

        @GET @Path("big")
        public String getBig() {
            assertNull(crTwo.getContext(Integer.class));
            assertNull(crTwo.getContext(Byte.class));
            return crTwo.getContext(BigInteger.class);
        }
    }

    public void testMedia() throws IOException {
        initiateWebApplication(ContextMediaResource.class,
                ByteContextResolverMediaOne.class,
                IntegerContextResolverMediaOne.class,
                BigIntegerContextResolverMediaTwo.class);

        assertEquals("java.lang.Integer", resource("/").get(String.class));

        assertEquals("java.lang.Byte", resource("/byte").get(String.class));

        assertEquals("java.math.BigInteger", resource("/big").get(String.class));
    }


    public static class GenericResolver<T> implements ContextResolver<T> {
        private T t;

        GenericResolver(T t) {
            this.t = t;
        }

        public T getContext(Class<?> objectType) {
            if (t.getClass() == objectType)
                return t;
            else
                return null;
        }
    }

    @Provider
    public static class IntegerGeneticContextResolver extends GenericResolver<Integer> {

        public IntegerGeneticContextResolver() {
            super(1);
        }
    }

    @Path("/")
    public static class GenericContextResource {
        @Context Providers p;

        @Context ContextResolver<Integer> cr;

        @GET
        public String get() {
            ContextResolver<Integer> _cr = p.getContextResolver(Integer.class, null);
            assertEquals(_cr, cr);
            return cr.getContext(Integer.class).toString();
        }
    }

    public void testGenericContextResource() throws IOException {
        initiateWebApplication(GenericContextResource.class, IntegerGeneticContextResolver.class);
        WebResource r = resource("/");

        assertEquals("1", resource("/").get(String.class));
    }

    public static class ListStringResolver implements ContextResolver<List<String>> {
        public List<String> getContext(Class<?> objectType) {
            if (Integer.class == objectType)
                return Arrays.asList("1", "2", "3");
            else
                return null;
        }
    }

    @Path("/")
    public static class ListStringContextResource {
        @Context Providers p;

        @Context ContextResolver<List<String>> cr;

        @GET
        public String get() {
            List<String> l = cr.getContext(Integer.class);
            return l.get(0) + l.get(1) + l.get(2);
        }
    }

    public void testListStringContextResource() {
        initiateWebApplication(ListStringContextResource.class, ListStringResolver.class);
        WebResource r = resource("/");

        assertEquals("123", resource("/").get(String.class));
    }

}