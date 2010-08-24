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

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCProxiedComponentProvider;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class MethodIAnnotationnheritenceTest extends AbstractResourceTester {
    
    public MethodIAnnotationnheritenceTest(String testName) {
        super(testName);
    }
    
    public static class SubResource {
        UriInfo ui;
        String p;
        String q;
        String h;

        SubResource(UriInfo ui, String p, String q, String h) {
            this.ui = ui;
            this.p = p;
            this.q = q;
            this.h = h;
        }

        @GET
        public String get() {
            return p + q + h;
        }
    }

    public static interface Interface {
        @GET
        @Produces("application/get")
        String get();

        @GET
        @Produces("application/getParams")
        String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h);

        @POST
        @Produces("application/xml")
        @Consumes("text/plain")
        String post(String s);

        @Path("sub")
        SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h);

        @GET
        @Path("submethod")
        String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h);
    }

    @Path("/{p}")
    public static class InterfaceImplementation implements Interface {
        public String get() {
            return "implementation";
        }

        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }

        public String post(String s) {
            return "<root>" + s + "</root>";
        }

        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }

        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }

    public void testInterfaceImplementation() {
        initiateWebApplication(InterfaceImplementation.class);

        _test();
    }


    static class ProxyIoCComponentProviderFactory implements IoCComponentProviderFactory {
        public IoCComponentProvider getComponentProvider(Class c) {
            if (Interface.class.isAssignableFrom(c)) {
                return new IoCProxiedComponentProvider() {
                    public Object proxy(final Object o) {
                        return Proxy.newProxyInstance(
                                this.getClass().getClassLoader(),
                                new Class[]{Interface.class},
                                new InvocationHandler() {
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                return method.invoke(o, args);
                            }
                        });

                    }

                    public Object getInstance() {
                        throw new IllegalStateException();
                    }
                };
            } else
                return null;
        }

        public IoCComponentProvider getComponentProvider(ComponentContext cc, Class c) {
            return getComponentProvider(c);
        }
    }

    public void testInterfaceImplementationComponentProviderProxy() {
        initiateWebApplication(new ProxyIoCComponentProviderFactory(), InterfaceImplementation.class);

        _test();
    }

    public void _test() {
        String s = resource("/a").accept("application/get").
                get(String.class);
        assertEquals("implementation", s);

        s = resource("/a?q=b").accept("application/getParams").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);

        ClientResponse cr = resource("/a").
                type("text/plain").
                accept("application/xml").
                post(ClientResponse.class, "content");
        assertEquals("<root>content</root>", cr.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/xml"), cr.getType());

        s = resource("/a/sub?q=b").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);

        s = resource("/a/submethod?q=b").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);
    }

    public static abstract class AbstractClass {
        @GET
        @Produces("application/get")
        public abstract String get();

        @GET
        @Produces("application/getParams")
        public abstract String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h);

        @POST
        @Produces("application/xml")
        @Consumes("text/plain")
        public abstract String post(String s);

        @Path("sub")
        public abstract SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h);

        @GET
        @Path("submethod")
        public abstract String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h);
    }

    @Path("/{p}")
    public static class AbstractClassImplementation extends AbstractClass {
        public String get() {
            return "implementation";
        }

        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }

        public String post(String s) {
            return "<root>" + s + "</root>";
        }

        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }

        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }

    public void testAbstractClassImplementation() {
        initiateWebApplication(AbstractClassImplementation.class);

        _test();
    }


    public static class ConcreteClass {
        @GET
        @Produces("application/get")
        public String get() {
            return "void";
        }

        @GET
        @Produces("application/getParams")
        public String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h) {
            return "void";
        }

        @POST
        @Produces("application/xml")
        @Consumes("text/plain")
        public String post(String s) {
            return "void";
        }

        @Path("sub")
        public SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h) {
            return null;
        }

        @GET
        @Path("submethod")
        public String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q,
                @HeaderParam("h") String h) {
            return "void";
        }
    }

    @Path("/{p}")
    public static class ConcreteClassOverride extends ConcreteClass {
        public String get() {
            return "implementation";
        }

        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }

        public String post(String s) {
            return "<root>" + s + "</root>";
        }

        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }

        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }

    public void testConcreteClassOverride() {
        initiateWebApplication(ConcreteClassOverride.class);

        _test();
    }


    public static abstract class AbstractOverrideClass implements Interface {
        public abstract String get();

        public abstract String getParams(UriInfo ui, String p, String q, String h);

        public abstract String post(String s);

        public abstract SubResource subResource(UriInfo ui, String p, String q, String h);

        public abstract String subMethod(UriInfo ui, String p, String q, String h);
    }

    @Path("/{p}")
    public static class AbstractOverrideClassInterface extends AbstractOverrideClass {
        public String get() {
            return "implementation";
        }

        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }

        public String post(String s) {
            return "<root>" + s + "</root>";
        }

        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }

        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }

    public void testAbstractOverrideClassInterface() {
        initiateWebApplication(AbstractOverrideClassInterface.class);

        _test();
    }

    @Path("/{x_p}")
    public static class InterfaceImplementationOverride implements Interface {

        @GET
        @Produces("application/getoverride")
        public String get() {
            return "override";
        }

        @GET
        @Produces("application/getParamsoverride")
        public String getParams(
                @Context UriInfo ui,
                @PathParam("x_p") String p,
                @QueryParam("_q") String q,
                @HeaderParam("_h") String h) {
            return p + q + h;
        }

        @POST
        @Produces("application/xhtml")
        @Consumes("application/octet-stream")
        public String post(String s) {
            return "<root>" + s + "</root>";
        }

        @Path("suboverride")
        public SubResource subResource(
                @Context UriInfo ui,
                @PathParam("x_p") String p,
                @QueryParam("_q") String q,
                @HeaderParam("_h") String h) {
            return new SubResource(ui, p, q, h);
        }

        @GET
        @Path("submethodoverride")
        public String subMethod(
                @Context UriInfo ui,
                @PathParam("x_p") String p,
                @QueryParam("_q") String q,
                @HeaderParam("_h") String h) {
            return p + q + h;
        }
    }

    public void testInterfaceImplementationOverride() {
        initiateWebApplication(InterfaceImplementationOverride.class);

        String s = resource("/a").accept("application/getoverride")
                .get(String.class);
        assertEquals("override", s);

        s = resource("/a?_q=b").
                accept("application/getParamsoverride").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);

        ClientResponse cr = resource("/a").
                type("application/octet-stream").
                accept("application/xhtml").
                post(ClientResponse.class, "content");
        assertEquals("<root>content</root>", cr.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/xhtml"), cr.getType());

        s = resource("/a/suboverride?_q=b").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);

        s = resource("/a/submethodoverride?_q=b").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);


        cr = resource("/a", false).accept("application/get").
                get(ClientResponse.class);
        assertEquals(406, cr.getStatus());

        cr = resource("/a?q=b", false).accept("application/getParams")
                .header("h", "c").
                get(ClientResponse.class);
        assertEquals(406, cr.getStatus());

        cr = resource("/a", false).
                type("text/plain").
                accept("application/xml").
                post(ClientResponse.class, "content");
        assertEquals(415, cr.getStatus());

        cr = resource("/a/sub", false).
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());

        cr = resource("/a/submethod", false).
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
    }


    public static abstract class SimpleBaseResource {
        @GET
        @Produces("text/plain")
        public abstract Object get();
    }

    @Path("concrete")
    public static class SimpleConcreteSubTypeResource extends SimpleBaseResource {
        public String get() {
            return "get";
        }
    }

    public void testSimpleConcreteSubTypeResource() {
        initiateWebApplication(SimpleConcreteSubTypeResource.class);

        String s = resource("/concrete")
                .get(String.class);
        assertEquals("get", s);
    }


    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Shared {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BaseMethod {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubMethod {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BaseParam {
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SubParam {
    }

    public static abstract class BaseResource {
        @Shared("base")
        @BaseMethod
        @GET
        @Produces("text/plain")
        public abstract Object get(@Shared("base") @BaseParam @QueryParam("x") String x, @Shared("base") @BaseParam @QueryParam("y") String y);
    }

    @Path("concrete")
    public static class ConcreteSubTypeResource extends BaseResource {
        @Shared("sub")
        @SubMethod
        public String get(@Shared("sub") @SubParam String x, @Shared("sub") @SubParam String y) {
            return x + y;
        }
    }

    public void testConcreteSubTypeResource() {
        initiateWebApplication(ConcreteSubTypeResource.class);

        String s = resource("/concrete")
                .queryParam("x", "X")
                .queryParam("y", "Y")
                .get(String.class);
        assertEquals("XY", s);

        AbstractResource ar = IntrospectionModeller.createResource(ConcreteSubTypeResource.class);
        AbstractResourceMethod am = ar.getResourceMethods().get(0);

        assertTrue(am.isAnnotationPresent(SubMethod.class));
        assertTrue(am.isAnnotationPresent(BaseMethod.class));
        assertEquals("sub", am.getAnnotation(Shared.class).value());

        assertTrue(am.getParameters().get(0).isAnnotationPresent(SubParam.class));
        assertTrue(am.getParameters().get(0).isAnnotationPresent(BaseParam.class));
        assertEquals("sub", am.getParameters().get(0).getAnnotation(Shared.class).value());

        assertTrue(am.getParameters().get(1).isAnnotationPresent(SubParam.class));
        assertTrue(am.getParameters().get(1).isAnnotationPresent(BaseParam.class));
        assertEquals("sub", am.getParameters().get(1).getAnnotation(Shared.class).value());
    }
}