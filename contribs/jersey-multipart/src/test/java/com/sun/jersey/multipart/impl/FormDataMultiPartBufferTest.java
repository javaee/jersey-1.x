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

package com.sun.jersey.multipart.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FormDataMultiPartBufferTest extends AbstractGrizzlyServerTester {

    public FormDataMultiPartBufferTest(String testName) {
        super(testName);
    }

    Client client = null;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(MultiPartBeanProvider.class);
        client = Client.create(config);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        client = null;
    }

    public static class MyFilter implements ContainerRequestFilter {

        @Override
        public ContainerRequest filter(ContainerRequest request) {
           // Buffer
            InputStream in = request.getEntityInputStream();
            if (in.getClass() != ByteArrayInputStream.class) {
                // Buffer input
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ReaderWriter.writeTo(in, baos);
                } catch (IOException ex) {
                    throw new ContainerException(ex);
                }
                in = new ByteArrayInputStream(baos.toByteArray());
                request.setEntityInputStream(in);
            }

            // Read entity
            FormDataMultiPart multiPart = request.getEntity(FormDataMultiPart.class);

            assertEquals(3, multiPart.getBodyParts().size());
            assertNotNull(multiPart.getField("foo"));
            assertEquals("bar", multiPart.getField("foo").getValue());
            assertNotNull(multiPart.getField("baz"));
            assertEquals("bop", multiPart.getField("baz").getValue());

            assertNotNull(multiPart.getField("bean"));
            MultiPartBean bean = multiPart.getField("bean").getValueAs(MultiPartBean.class);
            assertEquals("myname", bean.getName());
            assertEquals("myvalue", bean.getValue());
 
            // Reset buffer
            ByteArrayInputStream bais = (ByteArrayInputStream)in;
            bais.reset();

            request.getProperties().put("filtered", "true");
            return request;
        }
        
    }
    @Path("/")
    public static class ConsumesFormDataResource {

        @Context HttpContext hc;
        
        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public Response get(FormDataMultiPart multiPart) {
            Object p = hc.getProperties().get("filtered");
            assertNotNull(p);
            assertEquals("true", p);
            
            if (!(multiPart.getBodyParts().size() == 3)) {
                return Response.ok("FAILED:  Number of body parts is " + multiPart.getBodyParts().size() + " instead of 3").build();
            }
            if (multiPart.getField("foo") == null) {
                return Response.ok("FAILED:  Missing field 'foo'").build();
            } else if (!"bar".equals(multiPart.getField("foo").getValue())) {
                return Response.ok("FAILED:  Field 'foo' has value '" + multiPart.getField("foo").getValue() + "' instead of 'bar'").build();
            }
            if (multiPart.getField("baz") == null) {
                return Response.ok("FAILED:  Missing field 'baz'").build();
            } else if (!"bop".equals(multiPart.getField("baz").getValue())) {
                return Response.ok("FAILED:  Field 'baz' has value '" + multiPart.getField("baz").getValue() + "' instead of 'bop'").build();
            }
            if (multiPart.getField("bean") == null) {
                return Response.ok("FAILED:  Missing field 'bean'").build();
            }
            MultiPartBean bean = multiPart.getField("bean").getValueAs(MultiPartBean.class);
            if (!bean.getName().equals("myname")) {
                return Response.ok("FAILED:  Second part name = " + bean.getName()).build();
            }
            if (!bean.getValue().equals("myvalue")) {
                return Response.ok("FAILED:  Second part value = " + bean.getValue()).build();
            }
            return Response.ok("SUCCESS:  All tests passed").build();
        }
    }

    public void testConsumesFormDataResource() {
        ResourceConfig rc = new DefaultResourceConfig(ConsumesFormDataResource.class,
                MultiPartBeanProvider.class);
        List l = rc.getContainerRequestFilters();
        l.add(MyFilter.class);
        startServer(rc);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            FormDataMultiPart entity = new FormDataMultiPart().
                field("foo", "bar").
                field("baz", "bop").
                field("bean", bean, new MediaType("x-application", "x-format"));
            String response = builder.put(String.class, entity);
            if (!response.startsWith("SUCCESS:")) {
                fail("Response is '" + response + "'");
            }
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    private void report(UniformInterfaceException e) {
        System.out.println("Got UniformInterfaceException: " + e.getMessage());
        e.printStackTrace(System.out);
        ClientResponse r = e.getResponse();
        System.out.println("Response:");
        System.out.println("  Location=" + r.getLocation());
        System.out.println("  Status=" + r.getStatus());
        MultivaluedMap<String,String> metadata = r.getMetadata();
        for (Map.Entry<String,List<String>> entry : metadata.entrySet()) {
            for (String value : entry.getValue()) {
                System.out.println("  Header=" + entry.getKey() + ", Value=" + value);
            }
        }
    }

}
