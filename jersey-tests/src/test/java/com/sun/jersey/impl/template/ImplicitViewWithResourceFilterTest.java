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

package com.sun.jersey.impl.template;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ImplicitViewWithResourceFilterTest extends AbstractResourceTester {
    
    public ImplicitViewWithResourceFilterTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ImplicitTemplate {
        public String toString() {
            return "ImplicitTemplate";
        }
    }

    public static class FilterOne implements ResourceFilterFactory, ResourceFilter, ContainerRequestFilter, ContainerResponseFilter {
        public ContainerRequest filter(ContainerRequest request) {
            List<String> xTest = request.getRequestHeaders().get("X-TEST");
            assertNull(xTest);

            request.getRequestHeaders().add("X-TEST", "one");
            return request;
        }

        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            List<String> rxTest = request.getRequestHeaders().get("X-TEST");
            assertEquals(2, rxTest.size());
            assertEquals("one", rxTest.get(0));
            assertEquals("two", rxTest.get(1));

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
            List<String> rxTest = request.getRequestHeaders().get("X-TEST");
            assertEquals(2, rxTest.size());
            assertEquals("one", rxTest.get(0));
            assertEquals("two", rxTest.get(1));
            
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

    public void testImplicitTemplate() throws IOException {
        ResourceConfig rc = new DefaultResourceConfig(ImplicitTemplate.class,
                TestTemplateProcessor.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES, true);
        FilterOne f1 = new FilterOne();
        FilterTwo f2 = new FilterTwo();
        rc.getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                Arrays.asList(f1, f2));
        initiateWebApplication(rc);
        
        WebResource r = resource("/");


        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        List<String> xTest = cr.getHeaders().get("X-TEST");
        assertEquals(2, xTest.size());
        assertEquals("two", xTest.get(0));
        assertEquals("one", xTest.get(1));

        Properties p = new Properties();
        p.load(cr.getEntityInputStream());
        assertEquals("/com/sun/jersey/impl/template/ImplicitViewWithResourceFilterTest/ImplicitTemplate/index.testp", p.getProperty("path"));
        assertEquals("ImplicitTemplate", p.getProperty("model"));
    }
}