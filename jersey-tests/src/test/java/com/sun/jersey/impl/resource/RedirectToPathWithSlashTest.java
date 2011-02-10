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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RedirectToPathWithSlashTest extends AbstractResourceTester {
    
    public RedirectToPathWithSlashTest(String testName) {
        super(testName);
    }

    @Path("/project/")
    static public class Project { 
        @GET
        public String getMe() {
            return "project";
        }
    }
    
    public void _testRedirect() {
        ResourceConfig rc = new DefaultResourceConfig(Project.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_REDIRECT, true);
        initiateWebApplication(rc);
        
        ClientResponse response = resource("/project", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());

        String s = resource("/project/", false).get(String.class);
        assertEquals("project", s);
    }    
    
    @Path("/project/")
    static public class ProjectWithSubMethods { 
        @GET
        public String getMe() {
            return "project";
        }

        @Path("/details")
        @GET
        public String getDetails() {
            return "details";
        }
        
        @Path("/moreDetails/")
        @GET
        public String getMoreDetails() {
            return "moreDetails";
        }
    }
    
    public void testRedirectWithSubMethods() {
        ResourceConfig rc = new DefaultResourceConfig(ProjectWithSubMethods.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_REDIRECT, true);
        initiateWebApplication(rc);
        
        ClientResponse response = resource("/project", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());        
        
        String s = resource("/project/", false).get(String.class);
        assertEquals("project", s);        
        
        s = resource("/project/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resource("/project/details/", false).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
        
        response = resource("/project/moreDetails", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/moreDetails/").build(), 
                response.getLocation());        

        s = resource("/project/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);
    }    
    
    @Path("/project/")
    static public class ProjectWithSubResource { 
        @GET
        public String getMe() {
            return "project";
        }

        @Path("build/")
        public BuildWithSubMethods getBuildResource() {
            return new BuildWithSubMethods();
        }
        
        @Path("/details")
        @GET
        public String getDetails() {
            return "details";
        }
        
        @Path("/moreDetails/")
        @GET
        public String getMoreDetails() {
            return "moreDetails";
        }
    }
    
    @Path("build/")
    static public class BuildWithSubMethods { 
        @GET
        public String getMe() {
            return "build";
        }
        
        @Path("/details")
        @GET
        public String getDetails() {
            return "details";
        }
        
        @Path("/moreDetails/")
        @GET
        public String getMoreDetails() {
            return "moreDetails";
        }
    }
    
    public void testRedirectWithSubResource() {
        ResourceConfig rc = new DefaultResourceConfig(ProjectWithSubResource.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_REDIRECT, true);
        initiateWebApplication(rc);
                
        ClientResponse response = resource("/project", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());        
        
        String s = resource("/project/", false).get(String.class);
        assertEquals("project", s);        
        
        s = resource("/project/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resource("/project/details/", false).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
        
        response = resource("/project/moreDetails", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/moreDetails/").build(), 
                response.getLocation());        

        s = resource("/project/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);        

        
        response = resource("/project/build", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("/project/build/").build(), 
                response.getLocation());        
        
        s = resource("/project/build/", false).get(String.class);
        assertEquals("build", s);        
        
        s = resource("/project/build/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resource("/project/build/details/", false).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
        
        response = resource("/project/build/moreDetails", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/build/moreDetails/").build(), 
                response.getLocation());        

        s = resource("/project/build/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);
    }    
}
