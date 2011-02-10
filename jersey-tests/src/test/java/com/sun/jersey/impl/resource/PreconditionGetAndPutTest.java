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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.resource.Singleton;
import java.util.Date;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PreconditionGetAndPutTest extends AbstractResourceTester {
    
    public PreconditionGetAndPutTest(String testName) {
        super(testName);
    }

    @Path("/")
    @Singleton
    public static class LastModifiedResource {
        Date lastModified;
        
        @Context Request request;

        public LastModifiedResource(long initialTime) {
            lastModified = new Date(0);
        }

        @GET
        public Response doGet() {
            ResponseBuilder rb = request.evaluatePreconditions(lastModified);
            if (rb != null)
                return rb.build();
            
            return Response.ok("foo").lastModified(lastModified).build();
        }

        @PUT
        public Response doPut(@DefaultValue("1000") @QueryParam("i") int i) {
            ResponseBuilder rb = request.evaluatePreconditions(lastModified);
            if (rb != null)
                return rb.build();

            lastModified = new Date(lastModified.getTime() + i);
            return Response.ok("foo").lastModified(lastModified).build();
        }
    }
    
    public void testGetAndPutInitialTime0() {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getSingletons().add(new LastModifiedResource(1000));
        initiateWebApplication(rc);

        _testGetAndPut();
    }

    public void testGetAndPutInitialTime999() {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getSingletons().add(new LastModifiedResource(1999));
        initiateWebApplication(rc);

        _testGetAndPut();
    }

    public void _testGetAndPut() {
        WebResource r = resource("/", false);

        // Get the last modified at time T1

        ClientResponse response = r.get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Date lastModifiedT1 = response.getLastModified();


        // GET and PUT for last modified at time T1

        response = r.header("If-Modified-Since", new Date(lastModifiedT1.getTime() - 1000)).get(ClientResponse.class);
        assertEquals(200, response.getStatus());

        // GET and PUT for last modified at time T1

        response = r.header("If-Modified-Since", lastModifiedT1).get(ClientResponse.class);
        assertEquals(304, response.getStatus());

        response = r.header("If-Modified-Since", new Date(lastModifiedT1.getTime() + 1000)).get(ClientResponse.class);
        assertEquals(304, response.getStatus());

        response = r.header("If-Unmodified-Since", lastModifiedT1).put(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Date lastModifiedT2 = response.getLastModified();


        // Redo GET and PUT for last modified at time T1

        response = r.header("If-Modified-Since", lastModifiedT1).get(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = r.header("If-Unmodified-Since", lastModifiedT1).put(ClientResponse.class);
        assertEquals(412, response.getStatus());


        // GET and PUT for last modified at time T2

        response = r.header("If-Modified-Since", lastModifiedT2).get(ClientResponse.class);
        assertEquals(304, response.getStatus());

        response = r.header("If-Unmodified-Since", lastModifiedT2).put(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }


    public void testGetAndPutUpdateInterval500() {
        ResourceConfig rc = new DefaultResourceConfig();
        rc.getSingletons().add(new LastModifiedResource(1000));
        initiateWebApplication(rc);


        WebResource r = resource("/", false);

        // Get the last modified at time T1

        ClientResponse response = r.get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Date lastModifiedT1 = response.getLastModified();


        // GET and PUT for last modified at time T1

        response = r.header("If-Modified-Since", new Date(lastModifiedT1.getTime() - 1000)).get(ClientResponse.class);
        assertEquals(200, response.getStatus());

        // GET and PUT for last modified at time T1

        response = r.header("If-Modified-Since", lastModifiedT1).get(ClientResponse.class);
        assertEquals(304, response.getStatus());

        response = r.header("If-Modified-Since", new Date(lastModifiedT1.getTime() + 1000)).get(ClientResponse.class);
        assertEquals(304, response.getStatus());

        // Lost updates will happen within a one second interval
        
        response = r.queryParam("i", "200").header("If-Unmodified-Since", lastModifiedT1).put(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = r.queryParam("i", "200").header("If-Unmodified-Since", lastModifiedT1).put(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = r.header("If-Modified-Since", lastModifiedT1).get(ClientResponse.class);
        assertEquals(304, response.getStatus());
        
        response = r.queryParam("i", "800").header("If-Unmodified-Since", lastModifiedT1).put(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Date lastModifiedT2 = response.getLastModified();

        // Redo GET and PUT for last modified at time T1

        response = r.header("If-Modified-Since", lastModifiedT1).get(ClientResponse.class);
        assertEquals(200, response.getStatus());

        response = r.header("If-Unmodified-Since", lastModifiedT1).put(ClientResponse.class);
        assertEquals(412, response.getStatus());


        // GET and PUT for last modified at time T2

        response = r.header("If-Modified-Since", lastModifiedT2).get(ClientResponse.class);
        assertEquals(304, response.getStatus());

        response = r.header("If-Unmodified-Since", lastModifiedT2).put(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }
}
