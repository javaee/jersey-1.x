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

package com.sun.jersey.impl.subresources;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceDynamicWithParametersTest extends AbstractResourceTester {
    
    public SubResourceDynamicWithParametersTest(String testName) {
        super(testName);
    }
    
    @Path("/{p}")
    static public class ParentWithTemplates { 
        @GET
        public String getMe(@PathParam("p") String p) {
            return p;
        }
        
        @Path("child/{c}")
        public ChildWithTemplates getChildWithTemplates(
                @PathParam("p") String p, @PathParam("c") String c,
                @QueryParam("a") int a, @QueryParam("b") int b) {
            assertEquals("parent", p);
            assertEquals("first", c);
            assertEquals(1, a);
            assertEquals(2, b);
            return new ChildWithTemplates();
        }
        
        @Path(value="unmatchedPath/{path}", limited=false)
        public UnmatchedPathResource getUnmatchedPath(
                @PathParam("p") String p,
                @PathParam("path") String path) {
            assertEquals("parent", p);
            return new UnmatchedPathResource(path);
        }
    }
    
    static public class ChildWithTemplates { 
        @GET
        public String getMe(@PathParam("c") String c) {
            return c;
        }
    }
    
    static public class UnmatchedPathResource { 
        String path;
        
        UnmatchedPathResource(String path) {
            this.path = path;
        }
        
        @GET
        public String getMe() {
            if (path == null) path = "";
            return path;
        }
    }
    
    public void testSubResourceDynamicWithTemplates() {
        initiateWebApplication(ParentWithTemplates.class);
        
        assertEquals("parent", resource("/parent").get(String.class));
        assertEquals("first", resource("/parent/child/first?a=1&b=2").get(String.class));
    }
    
    public void testSubResourceDynamicWithUnmatchedPath() {
        initiateWebApplication(ParentWithTemplates.class);
        
        assertEquals("", resource("/parent/unmatchedPath/").get(String.class));
        assertEquals("a/b/c/d", resource("/parent/unmatchedPath/a/b/c/d").get(String.class));
    }
}
