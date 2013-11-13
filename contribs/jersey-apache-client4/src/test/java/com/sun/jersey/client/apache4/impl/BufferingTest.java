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
package com.sun.jersey.client.apache4.impl;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.junit.Assert;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 * Tests chunk encoding and possibility of buffering the entity.
 *
 * @author Miroslav Fuksa (miroslav.fuksa at oracle.com)
 */
public class BufferingTest extends AbstractGrizzlyServerTester {

    public BufferingTest(String testName) {
        super(testName);
    }

    @Path("resource")
    public static class MyResource {
        @POST
        public String getBuffered(@HeaderParam("content-length") String contentLenght,
                                  @HeaderParam("transfer-encoding") String transferEncoding) {
            if (transferEncoding != null && transferEncoding.equals("chunked")) {
                return "chunked";
            }
            return contentLenght;
        }
    }

    public void testWithBuffering() {
        DefaultResourceConfig resourceConfig = new DefaultResourceConfig(MyResource.class);
        resourceConfig.getContainerRequestFilters().add(new LoggingFilter());
        resourceConfig.getContainerResponseFilters().add(new LoggingFilter());
        startServer(resourceConfig);

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(ApacheHttpClient4Config.PROPERTY_ENABLE_BUFFERING, true);
        ApacheHttpClient4 client = ApacheHttpClient4.create(config);
        String entity = getVeryLongString();
        ClientResponse response = client.resource(getUri().path("resource").build())
                .post(ClientResponse.class, entity);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(String.valueOf(entity.length()), response.getEntity(String.class));
    }

    public void testWithChunkEncoding() {
        DefaultResourceConfig resourceConfig = new DefaultResourceConfig(MyResource.class);
        resourceConfig.getContainerRequestFilters().add(new LoggingFilter());
        resourceConfig.getContainerResponseFilters().add(new LoggingFilter());
        startServer(resourceConfig);

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 3000);
        ApacheHttpClient4 client = ApacheHttpClient4.create(config);
        String entity = getVeryLongString();
        ClientResponse response = client.resource(getUri().path("resource").build())
                .post(ClientResponse.class, entity);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("chunked", response.getEntity(String.class));
    }

    /**
     * Tests that {@link ApacheHttpClient4Config#PROPERTY_ENABLE_BUFFERING} can be defined
     * per request with different values.
     */
    public void testWithChunkEncodingPerRequest() {
        DefaultResourceConfig resourceConfig = new DefaultResourceConfig(MyResource.class);
        resourceConfig.getContainerRequestFilters().add(new LoggingFilter());
        resourceConfig.getContainerResponseFilters().add(new LoggingFilter());
        startServer(resourceConfig);

        DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getProperties().put(ClientConfig.PROPERTY_CHUNKED_ENCODING_SIZE, 3000);
        ApacheHttpClient4 client = ApacheHttpClient4.create(config);
        String entity = getVeryLongString();
        ClientResponse response = client.resource(getUri().path("resource").build())
                .post(ClientResponse.class, entity);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("chunked", response.getEntity(String.class));

        client.getProperties().put(ApacheHttpClient4Config.PROPERTY_ENABLE_BUFFERING, true);
        response = client.resource(getUri().path("resource").build())
                .post(ClientResponse.class, entity);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals(String.valueOf(entity.length()), response.getEntity(String.class));
    }


    public String getVeryLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("helllllloooooooooooooooooooooooooooooouuuuuuuuuuu.");
        }
        return sb.toString();
    }
}
