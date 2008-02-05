/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ClientResponse;
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
    
    public void testRedirect() {
        initiateWebApplication(Project.class);
        
        ClientResponse response = resourceProxy("/project", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());

        String s = resourceProxy("/project/", false).get(String.class);
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
        initiateWebApplication(ProjectWithSubMethods.class);
        
        ClientResponse response = resourceProxy("/project", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());        
        
        String s = resourceProxy("/project/", false).get(String.class);
        assertEquals("project", s);        
        
        s = resourceProxy("/project/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resourceProxy("/project/details/", false).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
        
        response = resourceProxy("/project/moreDetails", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/moreDetails/").build(), 
                response.getLocation());        

        s = resourceProxy("/project/moreDetails/", false).get(String.class);
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
        initiateWebApplication(ProjectWithSubResource.class);
        
        ClientResponse response = resourceProxy("/project", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());        
        
        String s = resourceProxy("/project/", false).get(String.class);
        assertEquals("project", s);        
        
        s = resourceProxy("/project/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resourceProxy("/project/details/", false).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
        
        response = resourceProxy("/project/moreDetails", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/moreDetails/").build(), 
                response.getLocation());        

        s = resourceProxy("/project/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);        

        
        response = resourceProxy("/project/build", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("/project/build/").build(), 
                response.getLocation());        
        
        s = resourceProxy("/project/build/", false).get(String.class);
        assertEquals("build", s);        
        
        s = resourceProxy("/project/build/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resourceProxy("/project/build/details/", false).get(ClientResponse.class);
        assertEquals(404, response.getStatus());
        
        response = resourceProxy("/project/build/moreDetails", false).get(ClientResponse.class);
        assertEquals(Response.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/build/moreDetails/").build(), 
                response.getLocation());        

        s = resourceProxy("/project/build/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);
    }    
}
