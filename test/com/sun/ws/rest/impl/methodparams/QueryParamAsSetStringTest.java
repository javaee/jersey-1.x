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

package com.sun.ws.rest.impl.methodparams;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
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
        @ProduceMime("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
        
        @GET
        @ProduceMime("application/Set")
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
        @ProduceMime("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertEquals(1, args.size());
            assertTrue(args.contains(""));
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringSetAbsent {
        @GET
        @ProduceMime("application/stringSet")
        public String doGetString(@QueryParam("args") Set<String> args) {
            assertEquals(null, args);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringSetNullDefault {
        @GET
        @ProduceMime("application/stringSet")
        public String doGetString(
                @QueryParam("args") Set<String> args) {
            assertEquals(null, args);
            return "content";
        }
        
        @GET
        @ProduceMime("application/Set")
        public String doGet(
                @QueryParam("args") Set args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSetDefault {
        @GET
        @ProduceMime("application/stringSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") Set<String> args) {
            assertTrue(args.contains("a"));
            return "content";
        }
        
        @GET
        @ProduceMime("application/Set")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") Set args) {
            assertTrue(args.contains("a"));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSetDefaultOverride {
        @GET
        @ProduceMime("application/stringSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") Set<String> args) {
            assertTrue(args.contains("b"));
            return "content";
        }
        
        @GET
        @ProduceMime("application/Set")
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
