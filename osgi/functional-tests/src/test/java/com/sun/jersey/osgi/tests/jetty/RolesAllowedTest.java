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
package com.sun.jersey.osgi.tests.jetty;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RolesAllowedTest extends AbstractJettyWebContainerTester {

    public static class SecurityFilter implements ContainerRequestFilter {

        @Context UriInfo ui;
        
        public ContainerRequest filter(ContainerRequest request) {
            String user = request.getHeaderValue("X-USER");
            request.setSecurityContext(new Authenticator(user));
            return request;
        }

        //

        public class Authenticator implements SecurityContext {
            private Principal p;
            
            Authenticator(final String name) {
                p = new Principal() {
                    public String getName() {
                        return name;
                    }
                };
            }
            
            public Principal getUserPrincipal() {
                return p;
            }

            public boolean isUserInRole(String role) {
                if (role.equals("user")) {
                    if ("admin".equals(p.getName()))
                        return true;
                    
                    String user = ui.getPathParameters().getFirst(role);
                    return user.equals(p.getName());
                } else if (role.equals("admin")) {
                    return role.equals(p.getName());
                } else {
                    return false;
                }
            }

            public boolean isSecure() {
                return false;
            }

            public String getAuthenticationScheme() {
                return "";
            }
        }
    }

    @Path("/{user}")
    public static class Resource {
        @RolesAllowed("user")
        @GET
        public String get() { return "GET"; }
        
        @RolesAllowed("admin")
        @POST
        public String post(String content) { return content; }
    }
        
    WebResource r;

    @Override
    public void setUp() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                SecurityFilter.class.getName());
        initParams.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                RolesAllowedResourceFilterFactory.class.getName());
        startServer(initParams, Resource.class);
        
        Client c = Client.create();
        r = c.resource(getUri().build());
    }
    
    @Test
    public void testGetAsUser() {
        assertEquals("GET", r.path("foo").header("X-USER", "foo").get(String.class));
    }

    @Test
    public void testGetAsAdmin() {
        assertEquals("GET", r.path("foo").header("X-USER", "admin").get(String.class));
    }

    @Test
    public void testPostAsUser() {
        ClientResponse cr = r.path("foo").header("X-USER", "foo").post(ClientResponse.class, "POST");
        assertEquals(403, cr.getStatus());
    }

    @Test
    public void testPostAsAdmin() {
        assertEquals("POST", r.path("foo").header("X-USER", "admin").post(String.class, "POST"));
    }

}