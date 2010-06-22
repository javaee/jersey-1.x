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
package com.sun.jersey.impl.container.filter;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ResourceFilterFactoriesTest extends AbstractResourceTester {
    
    @Path("/")
    public static class ResourceWithMethod {
        @GET
        public String get(@Context HttpHeaders hh) { 
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }

        @GET
        @Path("submethod")
        public String getSubmethod(@Context HttpHeaders hh) {
            List<String> xTest = hh.getRequestHeaders().get("X-TEST");
            assertEquals(2, xTest.size());
            return xTest.get(0) + xTest.get(1);
        }
    }
    
    public ResourceFilterFactoriesTest(String testName) {
        super(testName);
    }
        
    public static class FilterOne implements ResourceFilterFactory, ResourceFilter, ContainerRequestFilter, ContainerResponseFilter {
        public ContainerRequest filter(ContainerRequest request) {
            List<String> xTest = request.getRequestHeaders().get("X-TEST");
            assertNull(xTest);

            request.getRequestHeaders().add("X-TEST", "one");
            return request;
        }

        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            List<Object> xTest = response.getHttpHeaders().get("X-TEST");
            assertEquals(1, xTest.size());
            assertEquals("two", xTest.get(0));

            response.getHttpHeaders().add("X-TEST", "one");
            return response;
        }

        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        public ContainerResponseFilter getResponseFilter() {
            return this;
        }

        public List<ResourceFilter> create(AbstractMethod am) {
            return Collections.singletonList((ResourceFilter)this);
        }
    }
        
    public static class FilterTwo implements ResourceFilterFactory, ResourceFilter, ContainerRequestFilter, ContainerResponseFilter {
        public ContainerRequest filter(ContainerRequest request) {
            List<String> xTest = request.getRequestHeaders().get("X-TEST");
            assertEquals(1, xTest.size());
            assertEquals("one", xTest.get(0));

            request.getRequestHeaders().add("X-TEST", "two");
            return request;
        }

        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            List<Object> xTest = response.getHttpHeaders().get("X-TEST");
            assertNull(xTest);

            response.getHttpHeaders().add("X-TEST", "two");
            return response;
        }

        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        public ContainerResponseFilter getResponseFilter() {
            return this;
        }

        public List<ResourceFilter> create(AbstractMethod am) {
            return Collections.singletonList((ResourceFilter)this);
        }
    }

    public void testResourceMethod() {
        ResourceConfig rc = new DefaultResourceConfig(ResourceWithMethod.class);
        
        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertEquals("onetwo", cr.getEntity(String.class));
        List<String> xTest = cr.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

    public void testResourceMethodHead() {
        ResourceConfig rc = new DefaultResourceConfig(ResourceWithMethod.class);

        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        ClientResponse cr = r.head();
        assertEquals(200, cr.getStatus());
        List<String> xTest = cr.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

    @Path("/")
    public static class ResourceWithSubresourceLocator {
        @Path("sub")
        public Object get() {
            return new ResourceWithMethod();
        }
    }

    public static class FilterOneSubresourceLocator extends FilterOne {
        public List<ResourceFilter> create(AbstractMethod am) {
            if (am instanceof AbstractSubResourceLocator)
                return Collections.singletonList((ResourceFilter)this);
            else
                return null;
        }
    }
    
    public static class FilterTwoSubresourceLocator extends FilterTwo {
        public List<ResourceFilter> create(AbstractMethod am) {
            if (am instanceof AbstractSubResourceLocator)
                return Collections.singletonList((ResourceFilter)this);
            else
                return null;
        }
    }

    public void testResourceSubresourcelocator() {
        ResourceConfig rc = new DefaultResourceConfig(ResourceWithSubresourceLocator.class);

        FilterOneSubresourceLocator f1 = new FilterOneSubresourceLocator();
        FilterTwoSubresourceLocator f2 = new FilterTwoSubresourceLocator();
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/sub", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertEquals("onetwo", cr.getEntity(String.class));
        List<String> xTest = cr.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }

    public static class FilterOneMethod extends FilterOne {
        public List<ResourceFilter> create(AbstractMethod am) {
            if (am instanceof AbstractResourceMethod)
                return Collections.singletonList((ResourceFilter)this);
            else
                return null;
        }
    }

    public static class FilterTwoMethod extends FilterTwo {
        public List<ResourceFilter> create(AbstractMethod am) {
            if (am instanceof AbstractResourceMethod)
                return Collections.singletonList((ResourceFilter)this);
            else
                return null;
        }
    }

    public void testResourceMethodOfSubresource() {
        ResourceConfig rc = new DefaultResourceConfig(ResourceWithSubresourceLocator.class);

        FilterOneMethod f1 = new FilterOneMethod();
        FilterTwoMethod f2 = new FilterTwoMethod();
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/sub", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertEquals("onetwo", cr.getEntity(String.class));
        List<String> xTest = cr.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }


    public static class FilterOneSubresourceMethod extends FilterOne {
        public List<ResourceFilter> create(AbstractMethod am) {
            if (am instanceof AbstractSubResourceMethod)
                return Collections.singletonList((ResourceFilter)this);
            else
                return null;
        }
    }

    public static class FilterTwoSubrsourceMethod extends FilterTwo {
        public List<ResourceFilter> create(AbstractMethod am) {
            if (am instanceof AbstractSubResourceMethod)
                return Collections.singletonList((ResourceFilter)this);
            else
                return null;
        }
    }

    public void testResourceSubresourceMethodOfSubresource() {
        ResourceConfig rc = new DefaultResourceConfig(ResourceWithSubresourceLocator.class);

        FilterOneSubresourceMethod f1 = new FilterOneSubresourceMethod();
        FilterTwoSubrsourceMethod f2 = new FilterTwoSubrsourceMethod();
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);

        WebResource r = resource("/sub/submethod", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertEquals("onetwo", cr.getEntity(String.class));
        List<String> xTest = cr.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));
    }
}