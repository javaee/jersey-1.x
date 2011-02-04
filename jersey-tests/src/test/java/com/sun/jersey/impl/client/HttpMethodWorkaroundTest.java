/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import com.sun.jersey.impl.container.grizzly.AbstractGrizzlyServerTester;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author pavel.bucek@oracle.com
 */
public class HttpMethodWorkaroundTest extends AbstractGrizzlyServerTester {
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("GOTOSLEEP")
    public @interface GOTOSLEEP {
    }

    @Path("/test")
    public static class HttpMethodWorkaroundResource {
        @GOTOSLEEP
        public void test1() {
            return;
        }

        @GOTOSLEEP
        @Path("entity")
        public String test2() {
            return "GIMME 5 MINUTES";
        }
    }

    public HttpMethodWorkaroundTest(String testName) {
        super(testName);
    }

    public void testWithoutWorkaround() {
        startServer(HttpMethodWorkaroundResource.class);
        boolean caught = false;

        try {
            DefaultClientConfig config = new DefaultClientConfig();
            Client c = Client.create(config);

            WebResource r = c.resource(getUri().path("test").build());

            ClientResponse cr = r.method("GOTOSLEEP", ClientResponse.class);
            cr.close();
        }  catch(Exception pe) {
            caught = true;
        }

        assertTrue(caught);
        stopServer();
    }

    public void testWithWorkaround() {
        startServer(HttpMethodWorkaroundResource.class);

        DefaultClientConfig config = new DefaultClientConfig();
        config.getProperties().put(URLConnectionClientHandler.PROPERTY_HTTP_URL_CONNECTION_SET_METHOD_WORKAROUND, true);
        Client c = Client.create(config);

        WebResource r = c.resource(getUri().path("test").build());

        ClientResponse cr = r.method("GOTOSLEEP", ClientResponse.class);
        assertEquals(204, cr.getStatus());
        cr.close();
    }

    public void testWithWorkaroundWithEntity() {
        startServer(HttpMethodWorkaroundResource.class);

        DefaultClientConfig config = new DefaultClientConfig();
        config.getProperties().put(URLConnectionClientHandler.PROPERTY_HTTP_URL_CONNECTION_SET_METHOD_WORKAROUND, true);
        Client c = Client.create(config);

        WebResource r = c.resource(getUri().path("test/entity").build());

        ClientResponse cr = r.method("GOTOSLEEP", ClientResponse.class);
        assertEquals(200, cr.getStatus());
        assertTrue(cr.hasEntity());
        cr.close();
    }
}