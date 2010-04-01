/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.client.hypermedia;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.Path;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.hypermedia.Action;
import com.sun.jersey.core.hypermedia.HypermediaController;
import com.sun.jersey.core.hypermedia.HypermediaController.LinkType;
import com.sun.jersey.core.hypermedia.Name;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;

/**
 * Test Hypermedia.
 * 
 * @author Santiago.PericasGeertsen@Sun.Com
 */
public class TestClientProxyFormParams extends AbstractGrizzlyServerTester {

    final Map<String, Object> initParams = new HashMap<String, Object>();

    public TestClientProxyFormParams(String testName) {
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
        public String action1(@FormParam("p1") String p1,
                             @FormParam("p2") String p2) {
            assertTrue(p1 != null && p2 != null);
            return p1 + p2;
        }

        @PUT
        @Action("action2") @Path("action2")
        public String action2(@FormParam("p1") String p1,
                             @FormParam("p2") String p2,
                             Form f) {
            assertTrue(p1 != null && p2 != null && f != null);
            return p1 + p2;
        }
    }

    @HypermediaController(
        model=String.class,
        linkType=LinkType.LINK_HEADERS
    )
    public interface StringController {

        @Action("action1")
        public String action1(@FormParam("p1") String p1,
                @FormParam("p2") String p2);

        @Action("action1")
        public String action11(@Name("p1") String p1,
                @Name("p2") String p2);

        @Action("action2")
        public String action2(@FormParam("p1") String p1,
                @FormParam("p2") String p2, Form f);

        @Action("action2")
        public String action21(@Name("p1") String p1,
                @Name("p2") String p2, Form f);

    }

    // TODO fix failing test
    public void test1() {
        DefaultResourceConfig drc = new DefaultResourceConfig(StringResource.class);
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                "com.sun.jersey.server.hypermedia.filter.HypermediaFilterFactory");
        drc.setPropertiesAndFeatures(initParams);
        startServer(drc);

        Client c = Client.create();
        ViewResource r = c.viewResource(getUri().path("strings").path("1").build());
        StringController sc =r.get(StringController.class);

        // Test static annotation @FormParam
        Form f = new Form();
        f.add("p3", "boo");
        assert(sc.action1("foo", "bar").equals("foobar"));
        assert(sc.action2("foo", "bar", f).equals("foobar"));

//        // Test @Name mapped dynamically using WADL
//        f = new Form();
//        f.add("p3", "boo");
//        assert(sc.action11("foo", "bar").equals("foobar"));
//        assert(sc.action21("foo", "bar",f).equals("foobar"));
    }

    // Method used for manual testing (re-directing)
    public UriBuilder getClientUri() {
        return UriBuilder.fromUri("http://localhost").port(8080).path("/test").path("/");
    }

}