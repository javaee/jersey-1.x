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

import com.sun.ws.rest.api.core.HttpResponseContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RedirectToPathWithSlashTest extends AbstractBeanTester {
    
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
        String content;
        
        HttpResponseContext rc = callNoStatusCheck(Project.class, "GET", "/project", "", "", "");
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), rc.getResponse().getStatus());
        assertEquals(getBaseUri().resolve("project/"), rc.getHttpHeaders().getFirst("Location"));

        String s = (String)callGet(Project.class, "/project/", "").getResponse().getEntity();
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
        String content;
        
        HttpResponseContext rc = callNoStatusCheck(ProjectWithSubMethods.class, "GET", "/project", "", "", "");
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), rc.getResponse().getStatus());
        assertEquals(getBaseUri().resolve("project/"), rc.getHttpHeaders().getFirst("Location"));

        String s = (String)callGet(ProjectWithSubMethods.class, "/project/", "").getResponse().getEntity();
        assertEquals("project", s);
        
        s = (String)callGet(ProjectWithSubMethods.class, "/project/details", "").getResponse().getEntity();
        assertEquals("details", s);
        
        rc = callNoStatusCheck(ProjectWithSubMethods.class, "GET", "/project/details/", "", "", "");
        assertEquals(404, rc.getResponse().getStatus());
        
        rc = callNoStatusCheck(ProjectWithSubMethods.class, "GET", "/project/moreDetails", "", "", "");
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), rc.getResponse().getStatus());
        assertEquals(getBaseUri().resolve("project/moreDetails/"), rc.getHttpHeaders().getFirst("Location"));
        
        s = (String)callGet(ProjectWithSubMethods.class, "/project/moreDetails/", "").getResponse().getEntity();
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
        String content;
        
        HttpResponseContext rc = callNoStatusCheck(ProjectWithSubResource.class, "GET", "/project", "", "", "");
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), rc.getResponse().getStatus());
        assertEquals(getBaseUri().resolve("project/"), rc.getHttpHeaders().getFirst("Location"));

        String s = (String)callGet(ProjectWithSubResource.class, "/project/", "").getResponse().getEntity();
        assertEquals("project", s);
        
        s = (String)callGet(ProjectWithSubResource.class, "/project/details", "").getResponse().getEntity();
        assertEquals("details", s);
        
        rc = callNoStatusCheck(ProjectWithSubResource.class, "GET", "/project/details/", "", "", "");
        assertEquals(404, rc.getResponse().getStatus());
        
        rc = callNoStatusCheck(ProjectWithSubResource.class, "GET", "/project/moreDetails", "", "", "");
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), rc.getResponse().getStatus());
        assertEquals(getBaseUri().resolve("project/moreDetails/"), rc.getHttpHeaders().getFirst("Location"));
        
        s = (String)callGet(ProjectWithSubResource.class, "/project/moreDetails/", "").getResponse().getEntity();
        assertEquals("moreDetails", s);
        
        
        rc = callNoStatusCheck(ProjectWithSubResource.class, "GET", "/project/build", "", "", "");
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), rc.getResponse().getStatus());
        assertEquals(getBaseUri().resolve("project/build/"), rc.getHttpHeaders().getFirst("Location"));
        
        s = (String)callGet(ProjectWithSubResource.class, "/project/build/", "").getResponse().getEntity();
        assertEquals("build", s);
        
        s = (String)callGet(ProjectWithSubResource.class, "/project/build/details", "").getResponse().getEntity();
        assertEquals("details", s);
        
        rc = callNoStatusCheck(ProjectWithSubResource.class, "GET", "/project/build/details/", "", "", "");
        assertEquals(404, rc.getResponse().getStatus());
        
        rc = callNoStatusCheck(ProjectWithSubResource.class, "GET", "/project/build/moreDetails", "", "", "");
        assertEquals(Response.Builder.temporaryRedirect(null).build().getStatus(), rc.getResponse().getStatus());
        assertEquals(getBaseUri().resolve("project/build/moreDetails/"), rc.getHttpHeaders().getFirst("Location"));
        
        s = (String)callGet(ProjectWithSubResource.class, "/project/build/moreDetails/", "").getResponse().getEntity();
        assertEquals("moreDetails", s);
    }    
}
