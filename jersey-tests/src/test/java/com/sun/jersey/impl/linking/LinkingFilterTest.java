/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.impl.linking;

import java.util.Date;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.server.linking.LinkFilter;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;


/**
 * E2E tests for linking module.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class LinkingFilterTest extends AbstractResourceTester {

    public LinkingFilterTest(final String testName) {
        super(testName);
    }

    @Path("/")
    public static final class SimpleResource {

        @GET
        public String get() {
            return "KO";
        }
    }

    public static abstract class A {
        public abstract String getText();
    }

    @Path("/failing")
    public static final class FailingResource {
        private DateFormat format1 = new DateFormat() {
            @Override
            public StringBuffer format(final Date date, final StringBuffer stringBuffer, final FieldPosition fieldPosition) {
                return null;
            }

            @Override
            public Date parse(final String s, final ParsePosition parsePosition) {
                return null;
            }
        };

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public A getA() {
            return new A() {
                @Override
                public String getText() {
                    return "A";
                }
            };
        }
    }

    @Provider
    public static final class ExceptionThrowingFilter implements ContainerRequestFilter {

        @Override
        public ContainerRequest filter(final ContainerRequest request) {
            throw new WebApplicationException(Response.ok("OK").build());
        }
    }

    /**
     * Tests that {@link LinkFilter}/{@link com.sun.jersey.server.linking.impl.LinkProcessor} doesn't fail when an exception is
     * thrown before a resource method is matched (i.e. in request filter).
     * <p/>
     * JERSEY-801 reproducer.
     */
    @Test
    public void testExceptionInFilter() throws Exception {
        final ClassNamesResourceConfig resourceConfig = new ClassNamesResourceConfig(SimpleResource.class);
        resourceConfig.getContainerRequestFilters().add(ExceptionThrowingFilter.class);
        resourceConfig.getContainerResponseFilters().add(LinkFilter.class);

        initiateWebApplication(resourceConfig);

        final ClientResponse response = resource("/").get(ClientResponse.class);

        assertEquals(200, response.getStatus());
        assertEquals("OK", response.getEntity(String.class));
    }

    /**
     * JERSEY-1656 reproducer; NPE is thrown when classes used in response have invalid hashCode()
     */
    @Test
    public void testLinkFilterWithFailingHashCode() {
        final ClassNamesResourceConfig resourceConfig = new ClassNamesResourceConfig(FailingResource.class);

        resourceConfig.getContainerResponseFilters().add(LoggingFilter.class);
        resourceConfig.getContainerResponseFilters().add(LinkFilter.class);
        resourceConfig.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);

        initiateWebApplication(resourceConfig);

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        final ClientResponse response = resource("/failing", clientConfig).get(ClientResponse.class);

        assertEquals(200, response.getStatus());
    }
}
