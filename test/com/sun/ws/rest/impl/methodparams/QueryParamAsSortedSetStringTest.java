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
import java.util.SortedSet;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class QueryParamAsSortedSetStringTest extends AbstractResourceTester {

    public QueryParamAsSortedSetStringTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ResourceStringSortedSet {
        @GET
        @ProduceMime("application/stringSortedSet")
        public String doGetString(@QueryParam("args") SortedSet<String> args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
        
        @GET
        @ProduceMime("application/SortedSet")
        public String doGet(@QueryParam("args") SortedSet args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSortedSetEmpty {
        @GET
        @ProduceMime("application/stringSortedSet")
        public String doGetString(@QueryParam("args") SortedSet<String> args) {
            assertEquals(1, args.size());
            assertTrue(args.contains(""));
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringSortedSetAbsent {
        @GET
        @ProduceMime("application/stringSortedSet")
        public String doGetString(@QueryParam("args") SortedSet<String> args) {
            assertEquals(null, args);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringSortedSetNullDefault {
        @GET
        @ProduceMime("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") SortedSet<String> args) {
            assertEquals(null, args);
            return "content";
        }
        
        @GET
        @ProduceMime("application/SortedSet")
        public String doGet(
                @QueryParam("args") SortedSet args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSortedSetDefault {
        @GET
        @ProduceMime("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") SortedSet<String> args) {
            assertTrue(args.contains("a"));
            return "content";
        }
        
        @GET
        @ProduceMime("application/SortedSet")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") SortedSet args) {
            assertTrue(args.contains("a"));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSortedSetDefaultOverride {
        @GET
        @ProduceMime("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") SortedSet<String> args) {
            assertTrue(args.contains("b"));
            return "content";
        }
        
        @GET
        @ProduceMime("application/SortedSet")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") SortedSet args) {
            assertTrue(args.contains("b"));
            return "content";
        }
    }
    
    
    public void testStringSortedSetGet() {
        initiateWebApplication(ResourceStringSortedSet.class);
        
        resource("/?args=a&args=b&args=c").
            accept("application/stringSortedSet").
            get(String.class);
    }
    
    public void testStringSortedSetEmptyGet() {
        initiateWebApplication(ResourceStringSortedSetEmpty.class);
        
        resource("/?args&args&args").
            accept("application/stringSortedSet").
            get(String.class);
    }
    
    public void testStringSortedSetAbsentGet() {
        initiateWebApplication(ResourceStringSortedSetAbsent.class);
        
        resource("/").
            accept("application/stringSortedSet").
            get(String.class);
    }
    
    public void testSortedSetGet() {
         initiateWebApplication(ResourceStringSortedSet.class);
        
        resource("/?args=a&args=b&args=c").
            accept("application/SortedSet").
            get(String.class);
    }
        
    public void testStringSortedSetNullDefault() {
        initiateWebApplication(ResourceStringSortedSetNullDefault.class);
        
        resource("/").
            accept("application/stringSortedSet").
            get(String.class);
    }
    
    public void testSortedSetNullDefault() {
        initiateWebApplication(ResourceStringSortedSetNullDefault.class);
        
        resource("/").
            accept("application/SortedSet").
            get(String.class);
    }
    
    public void testStringSortedSetDefault() {
        initiateWebApplication(ResourceStringSortedSetDefault.class);
        
        resource("/").
            accept("application/stringSortedSet").
            get(String.class);
    }
    
    public void testSortedSetDefault() {
        initiateWebApplication(ResourceStringSortedSetDefault.class);
        
        resource("/").
            accept("application/SortedSet").
            get(String.class);
    }
    
    public void testSortedSetDefaultOverride() {
        initiateWebApplication(ResourceStringSortedSetDefaultOverride.class);
        
        resource("/?args=b").
            accept("application/SortedSet").
            get(String.class);
    }
}
