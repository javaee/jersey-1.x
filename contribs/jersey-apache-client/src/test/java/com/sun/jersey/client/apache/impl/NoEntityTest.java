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

package com.sun.jersey.client.apache.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class NoEntityTest extends AbstractGrizzlyServerTester {
    @Path("/test")
    public static class HttpMethodResource {
        @GET
        public Response get() {
            return Response.status(Status.CONFLICT).build();
        }

        @POST
        public void post(String entity) {
        }
    }

    public NoEntityTest(String testName) {
        super(testName);
    }

    protected ApacheHttpClient createClient() {
        return ApacheHttpClient.create();
    }

    protected ApacheHttpClient createClient(ApacheHttpClientConfig cc) {
        return ApacheHttpClient.create(cc);
    }

    public void testGet() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.get(ClientResponse.class);
        }
    }

    public void testGetWithClose() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.get(ClientResponse.class);
            cr.close();
        }
    }

    public void testPost() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.post(ClientResponse.class);
        }
    }

    public void testPostWithClose() {
        startServer(HttpMethodResource.class);
        WebResource r = createClient().resource(getUri().path("test").build());

        for (int i = 0; i < 5; i++) {
            ClientResponse cr = r.post(ClientResponse.class);
            cr.close();
        }
    }
}