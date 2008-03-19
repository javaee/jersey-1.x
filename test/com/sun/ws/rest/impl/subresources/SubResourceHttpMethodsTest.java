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

package com.sun.ws.rest.impl.subresources;

import com.sun.ws.rest.api.client.ClientResponse;
import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceHttpMethodsTest extends AbstractResourceTester {
    
    public SubResourceHttpMethodsTest(String testName) {
        super(testName);
    }

    @Path("/")
    static public class SubResourceMethods { 
        @GET
        public String getMe() {
            return "/";
        }

        @Path("sub")
        @GET
        public String getMeSub() {
            return "/sub";
        }
        
        @Path("sub/sub")
        @GET
        public String getMeSubSub() {
            return "/sub/sub";
        }
    }
    
    public void testSubResourceMethods() {
        initiateWebApplication(SubResourceMethods.class);
        
        assertEquals("/", resource("/").get(String.class));
        assertEquals("/sub", resource("/sub").get(String.class));
        assertEquals("/sub/sub", resource("/sub/sub").get(String.class));
    }
    
    @Path("/")
    static public class SubResourceMethodsWithTemplates { 
        @GET
        public String getMe() {
            return "/";
        }

        @Path("sub{t}")
        @GET
        public String getMeSub(@PathParam("t") String t) {
            return t;
        }
        
        @Path("sub/{t}")
        @GET
        public String getMeSubSub(@PathParam("t") String t) {
            return t;
        }
        
        @Path(value = "subunlimited{t}", limited=false)
        @GET
        public String getMeSubUnlimited(@PathParam("t") String t) {
            return t;
        }
        
        @Path(value = "subunlimited/{t}", limited=false)
        @GET
        public String getMeSubSubUnlimited(@PathParam("t") String t) {
            return t;
        }
    }
    
    public void testSubResourceMethodsWithTemplates() {
        initiateWebApplication(SubResourceMethodsWithTemplates.class);
        
        assertEquals("/", resource("/").get(String.class));
        
        assertEquals("value", resource("/subvalue").get(String.class));
        assertEquals("a", resource("/sub/a").get(String.class));
        
        assertEquals("value/a", resource("/subunlimitedvalue/a").get(String.class));
        assertEquals("a/b/c/d", resource("/subunlimited/a/b/c/d").get(String.class));
    }
    
    @Path("/")
    static public class SubResourceMethodsWithDifferentTemplates { 
        @Path("{foo}")
        @GET
        public String getFoo(@PathParam("foo") String foo) {
            return foo;
        }
        
        @Path("{bar}")
        @POST
        public String postBar(@PathParam("bar") String bar) {
            return bar;
        }
    }
    
    public void testSubResourceMethodsWithDifferentTemplates() {
        initiateWebApplication(SubResourceMethodsWithDifferentTemplates.class);
        
        assertEquals("foo", resource("/foo").get(String.class));
        assertEquals("bar", resource("/bar").post(String.class));
    }
    
    @Path("/{p}/")
    static public class SubResourceMethodWithLimitedTemplate { 
        @GET
        public String getMe(@PathParam("p") String p, @QueryParam("id") String id) {
            return p + id;
        }
        
        @GET
        @Path(value="{id}", limited=false)
        public String getUnmatchedPath(
                @PathParam("p") String p,
                @PathParam("id") String path) {
          return path;
        }
    }
    
    public void testSubResourceMethodWithLimitedTemplate() {
        initiateWebApplication(SubResourceMethodWithLimitedTemplate.class);
        
        assertEquals("topone", resource("/top/?id=one").get(String.class));
        assertEquals("a/b/c/d", resource("/top/a/b/c/d").get(String.class));
    }
    
    @Path("/{p}")
    static public class SubResourceNoSlashMethodWithLimitedTemplate { 
        @GET
        public String getMe(@PathParam("p") String p, @QueryParam("id") String id) {
            System.out.println(id);
            return p + id;
        }
        
        @GET
        @Path(value="{id}", limited=false)
        public String getUnmatchedPath(
                @PathParam("p") String p,
                @PathParam("id") String path) {
          return path;
        }
    }
    
    public void testSubResourceNoSlashMethodWithLimitedTemplate() {
        initiateWebApplication(SubResourceNoSlashMethodWithLimitedTemplate.class);
        
        assertEquals("topone", resource("/top?id=one").get(String.class));
        assertEquals("a/b/c/d", resource("/top/a/b/c/d").get(String.class));
    }
    
    @Path("/")
    static public class SubResourceWithSameTemplate { 
        public static class SubResource {
            @GET
            @Path("bar")
            public String get() {
                return "BAR";
            }
        }
        
        @GET
        @Path("foo")
        public String get() {
            return "FOO";
        }
        
        @Path("foo")
        public SubResource getUnmatchedPath() {
            return new SubResource();
        }        
    }
    
    public void testSubResourceMethodWithSameTemplate() {
        initiateWebApplication(SubResourceWithSameTemplate.class);
        
        assertEquals("FOO", resource("/foo").get(String.class));        
        assertEquals(404, resource("/foo/bar", false).get(ClientResponse.class).getStatus());
    }
}
