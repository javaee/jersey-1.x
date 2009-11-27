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
package com.sun.jersey.impl.trace;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.Arrays;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class TraceTest extends AbstractResourceTester {
    
    @Path("/root")
    public static class Resource {
        @POST
        public String post(String post) {
            return post;
        }

        @Path("sub-resource-method")
        @POST
        public String postSub(String post) {
            return post;
        }

        @Path("sub-resource-locator")
        public SubResource getSubLoc() {
            return new SubResource();
        }

        @Path("sub-resource-locator-null")
        public Object getSubLocNull() {
            return null;
        }
    }
    
    @Path("/")
    public static class SubResource {
        @POST
        public String post(String post) {
            return post;
        }

        @Path("sub-resource-method")
        @POST
        public String postSub(String post) {
            return post;
        }
    }

    public TraceTest(String testName) {
        super(testName);
    }
        
    public void testPostPerRequest() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_TRACE_PER_REQUEST,
                Boolean.TRUE);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new LoggingFilter()));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(new LoggingFilter()));
        initiateWebApplication(rc);

        WebResource r = resource("/root");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertFalse(hasX_Jersey_Trace(cr));
        assertEquals("POST", cr.getEntity(String.class));

        cr = r.header("X-Jersey-Trace-Accept", "true").post(ClientResponse.class, "POST");
        assertTrue(hasX_Jersey_Trace(cr));
        assertEquals("POST", cr.getEntity(String.class));
    }

    private void init() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_TRACE,
                Boolean.TRUE);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new LoggingFilter()));
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                Arrays.asList(new LoggingFilter()));
        initiateWebApplication(rc);
    }
    
    public void testPost() {
        init();

        WebResource r = resource("/root");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    public void testPostSubResourceMethod() {
        init();

        WebResource r = resource("/root").path("sub-resource-method");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    public void testPostSubResourceLocator() {
        init();

        WebResource r = resource("/root").path("sub-resource-locator");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    public void testPostSubResourceLocatorNull() {
        init();

        WebResource r = resource("/root", false).path("sub-resource-locator-null");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        assertEquals(404, cr.getStatus());
    }

    public void testPostSubResourceLocatorSubResourceMethod() {
        init();

        WebResource r = resource("/root").path("sub-resource-locator").path("sub-resource-method");

        ClientResponse cr = r.post(ClientResponse.class, "POST");
        test(cr);
        assertEquals("POST", cr.getEntity(String.class));
    }

    private void test(ClientResponse cr) {
        assertTrue(hasX_Jersey_Trace(cr));
    }

    private boolean hasX_Jersey_Trace(ClientResponse cr) {
        for (String k : cr.getHeaders().keySet()) {
            if (k.startsWith("X-Jersey-Trace-"))
                return true;
        }

        return false;
    }
}