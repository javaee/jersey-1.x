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

package com.sun.ws.rest.impl.bean;

import com.sun.ws.rest.impl.TestResourceProxy;
import com.sun.ws.rest.impl.client.ResponseInBound;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
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

    @UriTemplate("/project/")
    static public class Project { 
        @HttpMethod
        public String getMe() {
            return "project";
        }
    }
    
    public void testRedirect() {
        initiateWebApplication(Project.class);
        
        ResponseInBound response = resourceProxy("/project", false).get(ResponseInBound.class);
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());

        String s = resourceProxy("/project/", false).get(String.class);
        assertEquals("project", s);
    }    
    
    @UriTemplate("/project/")
    static public class ProjectWithSubMethods { 
        @HttpMethod
        public String getMe() {
            return "project";
        }

        @UriTemplate("/details")
        @HttpMethod
        public String getDetails() {
            return "details";
        }
        
        @UriTemplate("/moreDetails/")
        @HttpMethod
        public String getMoreDetails() {
            return "moreDetails";
        }
    }
    
    public void testRedirectWithSubMethods() {
        initiateWebApplication(ProjectWithSubMethods.class);
        
        ResponseInBound response = resourceProxy("/project", false).get(ResponseInBound.class);
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());        
        
        String s = resourceProxy("/project/", false).get(String.class);
        assertEquals("project", s);        
        
        s = resourceProxy("/project/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resourceProxy("/project/details/", false).get(ResponseInBound.class);
        assertEquals(404, response.getStatus());
        
        response = resourceProxy("/project/moreDetails", false).get(ResponseInBound.class);
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/moreDetails/").build(), 
                response.getLocation());        

        s = resourceProxy("/project/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);
    }    
    
    @UriTemplate("/project/")
    static public class ProjectWithSubResource { 
        @HttpMethod
        public String getMe() {
            return "project";
        }

        @UriTemplate("build/")
        public BuildWithSubMethods getBuildResource() {
            return new BuildWithSubMethods();
        }
        
        @UriTemplate("/details")
        @HttpMethod
        public String getDetails() {
            return "details";
        }
        
        @UriTemplate("/moreDetails/")
        @HttpMethod
        public String getMoreDetails() {
            return "moreDetails";
        }
    }
    
    @UriTemplate("build/")
    static public class BuildWithSubMethods { 
        @HttpMethod
        public String getMe() {
            return "build";
        }
        
        @UriTemplate("/details")
        @HttpMethod
        public String getDetails() {
            return "details";
        }
        
        @UriTemplate("/moreDetails/")
        @HttpMethod
        public String getMoreDetails() {
            return "moreDetails";
        }
    }
    
    public void testRedirectWithSubResource() {
        initiateWebApplication(ProjectWithSubResource.class);
        
        ResponseInBound response = resourceProxy("/project", false).get(ResponseInBound.class);
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/").build(), 
                response.getLocation());        
        
        String s = resourceProxy("/project/", false).get(String.class);
        assertEquals("project", s);        
        
        s = resourceProxy("/project/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resourceProxy("/project/details/", false).get(ResponseInBound.class);
        assertEquals(404, response.getStatus());
        
        response = resourceProxy("/project/moreDetails", false).get(ResponseInBound.class);
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/moreDetails/").build(), 
                response.getLocation());        

        s = resourceProxy("/project/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);        

        
        response = resourceProxy("/project/build", false).get(ResponseInBound.class);
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("/project/build/").build(), 
                response.getLocation());        
        
        s = resourceProxy("/project/build/", false).get(String.class);
        assertEquals("build", s);        
        
        s = resourceProxy("/project/build/details", false).get(String.class);
        assertEquals("details", s);
        
        response = resourceProxy("/project/build/details/", false).get(ResponseInBound.class);
        assertEquals(404, response.getStatus());
        
        response = resourceProxy("/project/build/moreDetails", false).get(ResponseInBound.class);
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), 
                response.getStatus());
        assertEquals(UriBuilder.fromUri(BASE_URI).path("project/build/moreDetails/").build(), 
                response.getLocation());        

        s = resourceProxy("/project/build/moreDetails/", false).get(String.class);
        assertEquals("moreDetails", s);
    }    
}
