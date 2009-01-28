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

package com.sun.jersey.impl.client;

import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.impl.container.grizzly.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.concurrent.Future;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriBuildingTest extends AbstractGrizzlyServerTester {
    public UriBuildingTest(String testName) {
        super(testName);
    }
    
    @Path("/x/y/z")
    public static class Resource {
        @GET
        public String get() {
            return "GET";
        }
    }

    public void testPathGet() throws Exception {
        startServer(Resource.class);
        WebResource r = Client.create().resource(getUri().build());
        String s = r.path("x").path("y").path("z").get(String.class);
        assertEquals("GET", s);
    }

    public void testPathGetAsync() throws Exception {
        startServer(Resource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().build());
        Future<String> s = r.path("x").path("y").path("z").get(String.class);
        assertEquals("GET", s.get());
    }

    @Path("/")
    public static class QueryResource {
        @GET
        public String get(@QueryParam("a") String a, @QueryParam("b") String b) {
            return a + b;
        }
    }

    public void testQueryGet() throws Exception {
        startServer(QueryResource.class);
        WebResource r = Client.create().resource(getUri().build());
        MultivaluedMap<String, String> qps = new MultivaluedMapImpl();
        qps.add("a", "foo");
        qps.add("b", "bar");
        String s = r.path("/").queryParams(qps).get(String.class);
        assertEquals("foobar", s);
    }

    public void testQueryGetAsync() throws Exception {
        startServer(QueryResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().build());
        MultivaluedMap<String, String> qps = new MultivaluedMapImpl();
        qps.add("a", "foo");
        qps.add("b", "bar");
        Future<String> s = r.path("/").queryParams(qps).get(String.class);
        assertEquals("foobar", s.get());
    }

    public void testQueryGet2() throws Exception {
        startServer(QueryResource.class);
        WebResource r = Client.create().resource(getUri().build());
        String s = r.path("/").queryParam("a", "foo").queryParam("b", "bar").get(String.class);
        assertEquals("foobar", s);
    }

    public void testQueryGetAsync2() throws Exception {
        startServer(QueryResource.class);
        AsyncWebResource r = Client.create().asyncResource(getUri().build());
        Future<String> s = r.path("/").queryParam("a", "foo").queryParam("b", "bar").get(String.class);
        assertEquals("foobar", s.get());
    }
}