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
package com.sun.jersey.impl.container.filter;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.CachedEntityContainerRequest;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.util.Arrays;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class CachedEntityFilterTest extends AbstractResourceTester {
    
    @Path("/")
    public static class Resource {
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String post(@FormParam("x") String x, @FormParam("y") String y, @FormParam("z") String z) {
            return x + y + z;
        }

        @Path("badtype")
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String post(String form) {
            return form;
        }
    }

    public static class CachedEntityFilter implements ContainerRequestFilter {
        public ContainerRequest filter(ContainerRequest request) {
            request = new CachedEntityContainerRequest(request);
            Form f = request.getEntity(Form.class);
            f.add("z", "z");
            return request;
        }
    }

    public CachedEntityFilterTest(String testName) {
        super(testName);
    }


    public void testCachedEntity() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new CachedEntityFilter()));
        initiateWebApplication(rc);

        WebResource r = resource("/");

        Form f = new Form();
        f.add("x", "x");
        f.add("y", "y");

        String s = r.type(MediaType.APPLICATION_FORM_URLENCODED).post(String.class, f);
        assertEquals("xyz", s);
    }

    public void testBadType() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new CachedEntityFilter()));
        initiateWebApplication(rc);

        WebResource r = resource("/", false);

        Form f = new Form();
        f.add("x", "x");
        f.add("y", "y");

        boolean caught = false;
        try {
            ClientResponse cr = r.path("badtype").type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, f);
        } catch (ContainerException ex) {
            assertEquals(ClassCastException.class, ex.getCause().getClass());
            caught = true;
        }
        assertTrue(caught);
    }
}