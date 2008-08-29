/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl.methodparams;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.PathSegment;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PathParamAsPathSegmentTest extends AbstractResourceTester {

    public PathParamAsPathSegmentTest(String testName) {
        super(testName);
    }

    @Path("/{arg1}/{arg2}/{arg3}")
    public static class Resource {
        @GET
        public String doGet(
                @PathParam("arg1") PathSegment arg1, 
                @PathParam("arg2") PathSegment arg2, 
                @PathParam("arg3") PathSegment arg3) {
            assertEquals("a", arg1.getPath());
            assertEquals("b", arg2.getPath());
            assertEquals("c", arg3.getPath());
            return "content";
        }
    }
    
    public void testStringArgsGet() {
        initiateWebApplication(Resource.class);
        resource("/a/b/c").
                get(String.class);
    }
    
    @Path("/{id}")
    public static class Duplicate {
        @GET
        public String get(@PathParam("id") PathSegment id) {
            return id.getPath();
        }
        
        @GET
        @Path("/{id}")
        public String getSub(@PathParam("id") PathSegment id) {
            return id.getPath();
        }
    }
    
    public void testDuplicate() {
        initiateWebApplication(Duplicate.class);
        
        assertEquals("foo", resource("/foo").get(String.class));
        assertEquals("bar", resource("/foo/bar").get(String.class));
    }
    
    @Path("/{a}/{b}/{c}")
    public static class Root {
        @Path("/{x}/{y}/{z}")
        public Sub getSub() {
            return new Sub();
        }
    }
    
    public static class Sub {
        @Path("{foo}")
        @GET public String get(
                @PathParam("a") PathSegment a, 
                @PathParam("b") PathSegment b, 
                @PathParam("c") PathSegment c,
                @PathParam("x") PathSegment x, 
                @PathParam("y") PathSegment y, 
                @PathParam("z") PathSegment z,
                @PathParam("foo") PathSegment foo
                ) {
            return acc(a, b, c, x, y, z, foo);
        }
        
        String acc(PathSegment... ps) {
            String s = "";
            for (PathSegment p : ps)
                s += p.getPath();
            
            return s;
        }
    }
    
    public void testSubResources() {
        initiateWebApplication(Root.class);
        
        assertEquals("1234567", resource("/1/2/3/4/5/6/7").get(String.class));
    }
    
    @Path("/{a}-{b}/{c}-{d}")
    public static class PathSeg {
        @GET
        public String doGet(
                @PathParam("a") PathSegment a, 
                @PathParam("b") PathSegment b,
                @PathParam("c") PathSegment c, 
                @PathParam("d") PathSegment d) {
            assertEquals(a.getPath(), b.getPath());
            assertEquals(c.getPath(), d.getPath());
            return "content";
        }
        
        @Path("{e}-{f}")
        @GET
        public String doGetSub(
                @PathParam("a") PathSegment a, 
                @PathParam("b") PathSegment b,
                @PathParam("c") PathSegment c, 
                @PathParam("d") PathSegment d,
                @PathParam("e") PathSegment e,
                @PathParam("f") PathSegment f) {
            assertEquals(a.getPath(), b.getPath());
            assertEquals(c.getPath(), d.getPath());
            assertEquals(e.getPath(), f.getPath());
            return "sub-content";
        }
    }
    
    public void testPathSeg() {
        initiateWebApplication(PathSeg.class);
        
        assertEquals("content", resource("a-b/c-d").get(String.class));
        assertEquals("sub-content", resource("a-b/c-d/e-f").get(String.class));
    }
    
    @Path("/{a: .+}/edit/{b}")
    public static class PathSegs {
        @GET
        public String doGet(
                @PathParam("a") PathSegment a, 
                @PathParam("b") PathSegment b) {
            return a.getPath() + "-" + b.getPath();
        }
    }
    
    public void testPathSegs() {
        initiateWebApplication(PathSegs.class);
        
        assertEquals("z-b", resource("/x/y/z/edit/b").get(String.class));
        assertEquals("z-b", resource("///x/y/z/edit/b").get(String.class));
    }    
    
    @Path("/{a: .+}")
    public static class PathSegsEnd {
        @GET
        public String doGet(
                @PathParam("a") PathSegment a) {
            return a.getPath();
        }
    }
    
    public void testPathSegsEnd() {
        initiateWebApplication(PathSegsEnd.class);
        
        assertEquals("z", resource("/x/y/z").get(String.class));
        assertEquals("", resource("/x/y/z/").get(String.class));
    }    
}