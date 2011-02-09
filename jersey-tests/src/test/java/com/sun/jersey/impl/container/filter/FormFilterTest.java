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
package com.sun.jersey.impl.container.filter;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FormFilterTest extends AbstractResourceTester {
    
    @Path("/")
    public static class Resource {
        @POST
        public String post(String s, @Context HttpHeaders hs, @Context UriInfo ui) {
            if (MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(hs.getMediaType())) {
                MultivaluedMap<String, String> qps = ui.getQueryParameters();
                assertNotNull(qps.get("a"));
                assertNotNull(qps.get("b"));
                assertEquals(2, qps.get("a").size());
                assertEquals(2, qps.get("b").size());
            }
            return s;
        }
    }

    public static class MaskInputStreamFilter implements ContainerRequestFilter {
        public ContainerRequest filter(ContainerRequest request) {
            InputStream in = request.getEntityInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                int read;
                final byte[] data = new byte[2048];
                while ((read = in.read(data)) != -1)
                    baos.write(data, 0, read);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }

            in = new ByteArrayInputStream(baos.toByteArray()) { };
            request.setEntityInputStream(in);
            return request;
        }
    }

    public static abstract class AbstractFormFilter implements ContainerRequestFilter {
        public ContainerRequest filter(ContainerRequest request) {
            Form f = request.getFormParameters();
            if (!f.isEmpty()) {
                assertEquals("x", f.getFirst("a"));
                assertEquals("y", f.getFirst("b"));
                URI r = request.getRequestUriBuilder().
                        queryParam("a", f.getFirst("a")).
                        queryParam("b", f.getFirst("b")).build();
                request.setUris(request.getBaseUri(), r);
            }
            return request;
        }
    }

    public FormFilterTest(String testName) {
        super(testName);
    }


    public void testWithInstance() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                Arrays.asList(new MaskInputStreamFilter(), 
                new AbstractFormFilter(){},
                new AbstractFormFilter(){}));
        initiateWebApplication(rc);

        WebResource r = resource("/");

        assertEquals("foo", r.post(String.class, "foo"));

        Form f = new Form();
        f.add("a", "x");
        f.add("b", "y");

        f = r.type(MediaType.APPLICATION_FORM_URLENCODED).post(Form.class, f);
        assertEquals("x", f.getFirst("a"));
        assertEquals("y", f.getFirst("b"));
    }
}