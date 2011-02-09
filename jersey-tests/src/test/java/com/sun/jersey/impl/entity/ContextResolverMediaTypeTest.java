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
package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
public class ContextResolverMediaTypeTest extends AbstractResourceTester {
    
    public ContextResolverMediaTypeTest(String testName) {
        super(testName);
    }

    @Produces("text/plain")
    @Provider
    public static class TextPlainContextResolver implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            return "text/plain";
        }
    }

    @Produces("text/*")
    @Provider
    public static class TextContextResolver implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            return "text/*";
        }
    }

    @Produces("*/*")
    @Provider
    public static class WildcardContextResolver implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            return "*/*";
        }
    }

    @Produces({"text/plain", "text/html"})
    @Provider
    public static class TextPlainHtmlContextResolver implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            return "text/plain/html";
        }

    }

    @Produces("text/html")
    @Provider
    public static class TextHtmlContextResolver implements ContextResolver<String> {
        public String getContext(Class<?> objectType) {
            return "text/html";
        }

    }

    @Path("/")
    public static class ContextResource {
        @Context Providers p;

        @Context ContextResolver<String> cr;

        @GET
        @Path("{id: .+}")
        public String get(@PathParam("id") MediaType m) {
            ContextResolver<String> cr = p.getContextResolver(String.class, m);

            // Verify cache is working
            ContextResolver<String> cachedCr = p.getContextResolver(String.class, m);
            assertEquals(cr, cachedCr);

            if (cr == null)
                return "NULL";
            else
                return cr.getContext(null);
        }
    }

    public void testProduce() throws IOException {
        initiateWebApplication(ContextResource.class, 
                TextPlainContextResolver.class,
                TextContextResolver.class,
                WildcardContextResolver.class);

        WebResource r = resource("/");

        assertEquals("text/plain", r.path("text/plain").get(String.class));
        assertEquals("text/*", r.path("text/*").get(String.class));
        assertEquals("*/*", r.path("*/*").get(String.class));

        assertEquals("text/*", r.path("text/html").get(String.class));

        assertEquals("*/*", r.path("application/xml").get(String.class));
        assertEquals("*/*", r.path("application/*").get(String.class));
    }

    public void testProduces() throws IOException {
        initiateWebApplication(ContextResource.class,
                TextPlainHtmlContextResolver.class,
                TextContextResolver.class,
                WildcardContextResolver.class);

        WebResource r = resource("/");

        assertEquals("text/plain/html", r.path("text/plain").get(String.class));
        assertEquals("text/plain/html", r.path("text/html").get(String.class));
        assertEquals("text/*", r.path("text/*").get(String.class));
        assertEquals("*/*", r.path("*/*").get(String.class));

        assertEquals("text/*", r.path("text/csv").get(String.class));

        assertEquals("*/*", r.path("application/xml").get(String.class));
        assertEquals("*/*", r.path("application/*").get(String.class));
    }

    public void testProducesSeparate() throws IOException {
        initiateWebApplication(ContextResource.class,
                TextPlainContextResolver.class,
                TextHtmlContextResolver.class,
                TextContextResolver.class,
                WildcardContextResolver.class);

        WebResource r = resource("/");

        assertEquals("text/plain", r.path("text/plain").get(String.class));
        assertEquals("text/html", r.path("text/html").get(String.class));
        assertEquals("text/*", r.path("text/*").get(String.class));
        assertEquals("*/*", r.path("*/*").get(String.class));

        assertEquals("text/*", r.path("text/csv").get(String.class));

        assertEquals("*/*", r.path("application/xml").get(String.class));
        assertEquals("*/*", r.path("application/*").get(String.class));
    }

    public void testProducesXXX() throws IOException {
        initiateWebApplication(ContextResource.class,
                TextPlainContextResolver.class,
                TextHtmlContextResolver.class);

        WebResource r = resource("/");

        assertEquals("text/plain", r.path("text/plain").get(String.class));
        assertEquals("text/html", r.path("text/html").get(String.class));
        assertEquals("NULL", r.path("text/*").get(String.class));
        assertEquals("NULL", r.path("*/*").get(String.class));

        assertEquals("NULL", r.path("text/csv").get(String.class));

        assertEquals("NULL", r.path("application/xml").get(String.class));
        assertEquals("NULL", r.path("application/*").get(String.class));
    }
}