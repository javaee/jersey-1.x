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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.Set;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class QueryParamAsSetStringTest extends AbstractResourceTester {

    public QueryParamAsSetStringTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ResourceStringSet {
        @GET
        @Produces("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
        
        @GET
        @Produces("application/Set")
        public String doGet(@QueryParam("args") Set args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSetEmpty {
        @GET
        @Produces("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertEquals(1, args.size());
            assertTrue(args.contains(""));
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringSetAbsent {
        @GET
        @Produces("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertEquals(null, args);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringSetNullDefault {
        @GET
        @Produces("application/stringSet")
        public String doGetString(
                @QueryParam("args") Set<String> args) {
            assertEquals(null, args);
            return "content";
        }
        
        @GET
        @Produces("application/Set")
        public String doGet(
                @QueryParam("args") Set args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSetDefault {
        @GET
        @Produces("application/stringSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") Set<String> args) {
            assertTrue(args.contains("a"));
            return "content";
        }
        
        @GET
        @Produces("application/Set")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") Set args) {
            assertTrue(args.contains("a"));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSetDefaultOverride {
        @GET
        @Produces("application/stringSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") Set<String> args) {
            assertTrue(args.contains("b"));
            return "content";
        }
        
        @GET
        @Produces("application/Set")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") Set args) {
            assertTrue(args.contains("b"));
            return "content";
        }
    }
    
    
    public void testStringSetGet() {
        initiateWebApplication(ResourceStringSet.class);
        
        resource("/?args=a&args=b&args=c").
            accept("application/stringSet").
            get(String.class);
    }
    
    public void testStringSetEmptyGet() {
        initiateWebApplication(ResourceStringSetEmpty.class);
        
        resource("/?args&args&args").
            accept("application/stringSet").
            get(String.class);
    }
    
    public void testStringSetAbsentGet() {
        initiateWebApplication(ResourceStringSetAbsent.class);
        
        resource("/").
            accept("application/stringSet").
            get(String.class);
    }
    
    public void testSetGet() {
         initiateWebApplication(ResourceStringSet.class);
        
        resource("/?args=a&args=b&args=c").
            accept("application/Set").
            get(String.class);
    }
        
    public void testStringSetNullDefault() {
        initiateWebApplication(ResourceStringSetNullDefault.class);
        
        resource("/").
            accept("application/stringSet").
            get(String.class);
    }
    
    public void testSetNullDefault() {
        initiateWebApplication(ResourceStringSetNullDefault.class);
        
        resource("/").
            accept("application/Set").
            get(String.class);
    }
    
    public void testStringSetDefault() {
        initiateWebApplication(ResourceStringSetDefault.class);
        
        resource("/").
            accept("application/stringSet").
            get(String.class);
    }
    
    public void testSetDefault() {
        initiateWebApplication(ResourceStringSetDefault.class);
        
        resource("/").
            accept("application/Set").
            get(String.class);
    }
    
    public void testSetDefaultOverride() {
        initiateWebApplication(ResourceStringSetDefaultOverride.class);
        
        resource("/?args=b").
            accept("application/Set").
            get(String.class);
    }
}
