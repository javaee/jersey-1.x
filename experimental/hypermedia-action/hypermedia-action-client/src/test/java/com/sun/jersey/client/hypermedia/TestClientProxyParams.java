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

package com.sun.jersey.client.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ViewResource;
import javax.ws.rs.Path;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.core.hypermedia.Name;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.UriBuilder;

/**
 * Test Hypermedia.
 * 
 * @author Santiago.PericasGeertsen@Sun.Com
 */
public class TestClientProxyParams extends AbstractGrizzlyServerTester {

    final Map<String, Object> initParams = new HashMap<String, Object>();

    public TestClientProxyParams(String testName) {
        super(testName);
    }

    @Path("/strings/{id}")
    @HypermediaController(
        model=String.class,
        linkType=LinkType.LINK_HEADERS
    )
    public static class StringResource {

        protected String string = "string";

        @GET
        public String get(@PathParam("id") String id) {
            return "string" + id;
        }

        @PUT
        public void put(@PathParam("id") String id, String newString) {
            string = newString + id;
        }

        @PUT
        @Action("action1") @Path("action1")
        public String action1(@QueryParam("p") String p) {
            assertTrue(p != null);
            return p;
        }

        @PUT
        @Action("action2") @Path("action2")
        public String action2(@HeaderParam("p") String p) {
            assertTrue(p != null);
            return p;
        }

        @PUT
        @Action("action3") @Path("action3")
        public String action3(@CookieParam("p") String p) {
            assertTrue(p != null);
            return p;
        }
    }

    @HypermediaController(
        model=String.class,
        linkType=LinkType.LINK_HEADERS
    )
    public interface StringController {

        @Action("action1")
        public String action1(@QueryParam("p") String p);

        @Action("action2")
        public String action2(@HeaderParam("p") String p);
        
        @Action("action3")
        public String action3(@CookieParam("p") String p);

        @Action("action1")
        public String action11(@Name("p") String p);

        @Action("action2")
        public String action21(@Name("p") String p);

        @Action("action3")
        public String action31(@Name("p") String p);
    }

    public void test1() {
        DefaultResourceConfig drc = new DefaultResourceConfig(StringResource.class);
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                "com.sun.jersey.server.hypermedia.filter.HypermediaFilterFactory");
        drc.setPropertiesAndFeatures(initParams);
        startServer(drc);

        Client c = Client.create();
        ViewResource r = c.viewResource(getUri().path("strings").path("1").build());
        StringController sc = r.get(StringController.class);
        Cookie cookie = new Cookie("p", "foo");

        // Test static annotations @QueryParam, @HeaderParam and @CookieParam
        assert(sc.action1("foo").equals("foo"));
        assert(sc.action2("foo").equals("foo"));
        assert(sc.action3(cookie.toString()).equals("foo"));

        // Test @Name mapped dynamically using WADL
        assert(sc.action11("foo").equals("foo"));
        assert(sc.action21("foo").equals("foo"));
        assert(sc.action31(cookie.toString()).equals("foo"));
    }

    // Method used for manual testing (re-directing)
    public UriBuilder getClientUri() {
        return UriBuilder.fromUri("http://localhost").port(8080).path("/test").path("/");
    }

}