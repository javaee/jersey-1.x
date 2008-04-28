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

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceClassDynamicTest extends AbstractResourceTester {
    
    public SubResourceClassDynamicTest(String testName) {
        super(testName);
    }

    @Path("/parent")
    static public class Parent { 
        @GET
        public String getMe() {
            return "parent";
        }
        
        @Path("child")
        public Class<Child> getChild() {
            return Child.class;
        }
    }
    
    static public class Child { 
        @GET
        public String getMe() {
            return "child";
        }
    }
    
    public void testSubResourceDynamic() {
        initiateWebApplication(Parent.class);
        
        assertEquals("parent", resource("/parent").get(String.class));
        assertEquals("child", resource("/parent/child").get(String.class));
    }    
    
    @Path("/{p}")
    static public class ParentWithTemplates { 
        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }
        
        @Path("child/{c}")
        public Class<ChildWithTemplates> getChildWithTemplates() {
            return ChildWithTemplates.class;
        }
    }
    
    static public class ChildWithTemplates { 
        @GET
        public String getMe(@PathParam("c") String c) {
            return c;
        }
    }
    
    public void testSubResourceDynamicWithTemplates() {
        initiateWebApplication(ParentWithTemplates.class);
        
        assertEquals("parent", resource("/parent").get(String.class));
        assertEquals("first", resource("/parent/child/first").get(String.class));
    }    
    
    @Path("/{p}")
    static public class ParentWithTemplatesLifecycle { 
        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }
        
        @Path("child/{c}")
        public Class<ChildWithTemplatesPerRequest> getChildWithTemplates() {
            return ChildWithTemplatesPerRequest.class;
        }
        
        @Path("child/singleton/{c}")
        public Class<ChildWithTemplatesSingleton> getChildWithTemplatesSingleton() {
            return ChildWithTemplatesSingleton.class;
        }
    }
    
    static public class ChildWithTemplatesPerRequest {
        private int i = 0;
        private String c;
        
        public ChildWithTemplatesPerRequest(@PathParam("c") String c) {
            this.c = c;
        }
        
        @GET
        public String getMe() {
            i++;
            return c + i;
        }
    }
    
    @Singleton
    static public class ChildWithTemplatesSingleton {
        private int i = 0;
        
        @GET
        public String getMe(@PathParam("c") String c) {
            i++;
            return c + i;
        }
    }
    
    public void testSubResourceDynamicWithTemplatesLifecycle() {
        initiateWebApplication(ParentWithTemplatesLifecycle.class);
        
        assertEquals("parent", resource("/parent").get(String.class));
        assertEquals("x1", resource("/parent/child/x").get(String.class));
        assertEquals("x1", resource("/parent/child/x").get(String.class));
        assertEquals("x1", resource("/parent/child/singleton/x").get(String.class));
        assertEquals("x2", resource("/parent/child/singleton/x").get(String.class));
    }    
    
}
