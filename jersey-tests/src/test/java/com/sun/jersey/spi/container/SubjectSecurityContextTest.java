/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.spi.container;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.impl.AbstractResourceTester;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 *
 * @author Martin Matula
 */
public class SubjectSecurityContextTest extends AbstractResourceTester {

    @Path("/resource")
    public static class Resource {

        @GET
        public String resourceGet() {
            return "Resource GET";
        }

        @Path("subresource")
        public SubResource getSubResource() {
            return new SubResource();
        }
    }

    public static class SubResource {
        @GET
        public String subResourceGet() {
            return "SubResource GET";
        }
    }

    public static class MySecurityContext implements SubjectSecurityContext {
        private final StringBuilder sb;

        MySecurityContext(StringBuilder sb) {
            this.sb = sb;
        }

        @Override
        public Object doAsSubject(PrivilegedAction action) {
            sb.append("A");
            return action.run();
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isUserInRole(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getAuthenticationScheme() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public static class MyResourceFilter implements ResourceFilter {
        private final MySecurityContext sc;
        private final StringBuilder sb = new StringBuilder();
        private final int expectedCount;

        MyResourceFilter(int expectedCount) {
            sc = new MySecurityContext(sb);
            this.expectedCount = expectedCount;
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return new ContainerRequestFilter() {
                @Override
                public ContainerRequest filter(ContainerRequest request) {
                    request.setSecurityContext(sc);
                    return request;
                }
            };
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return null;
        }

        public void assertCount() {
            assertEquals(expectedCount, sb.length());
        }
    }

    public static class MyResourceFilterFactory implements ResourceFilterFactory {
        private final Map<String, MyResourceFilter> methodToFilterMap;

        MyResourceFilterFactory(Map<String, MyResourceFilter> mtfm) {
            methodToFilterMap = mtfm;
        }

        @Override
        public List<ResourceFilter> create(AbstractMethod am) {
            ResourceFilter rf = methodToFilterMap.get(am.getMethod().getName());
            if (rf == null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(rf);
            }
        }
    }

    public SubjectSecurityContextTest(String testName) {
        super(testName);
    }

    public void testSecureEachMethod() {
        HashMap<String, MyResourceFilter> hm = new HashMap<String, MyResourceFilter>();
        hm.put("resourceGet", new MyResourceFilter(1));
        hm.put("subResourceGet", new MyResourceFilter(1));
        hm.put("getSubResource", new MyResourceFilter(1));
        _test(hm);
    }

    public void testSecureRMandSRLMethod() {
        HashMap<String, MyResourceFilter> hm = new HashMap<String, MyResourceFilter>();
        hm.put("resourceGet", new MyResourceFilter(1));
        hm.put("getSubResource", new MyResourceFilter(2));
        _test(hm);
    }

    public void testSecureSRMethod() {
        HashMap<String, MyResourceFilter> hm = new HashMap<String, MyResourceFilter>();
        hm.put("subResourceGet", new MyResourceFilter(1));
        _test(hm);
    }

    public void _test(Map<String, MyResourceFilter> hm) {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                Collections.singletonList(new MyResourceFilterFactory(hm)));
        initiateWebApplication(rc);

        WebResource r = resource("/resource");
        assertEquals("Resource GET", r.get(String.class));
        assertEquals("SubResource GET", r.path("subresource").get(String.class));

        for (MyResourceFilter f : hm.values()) {
            f.assertCount();
        }
    }
}
