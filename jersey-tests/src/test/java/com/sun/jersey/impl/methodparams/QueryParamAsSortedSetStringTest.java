/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
        @Produces("application/stringSortedSet")
        public String doGetString(@QueryParam("args") SortedSet<String> args) {
            assertTrue(args.contains("a"));
            assertTrue(args.contains("b"));
            assertTrue(args.contains("c"));
            return "content";
        }
        
        @GET
        @Produces("application/SortedSet")
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
        @Produces("application/stringSortedSet")
        public String doGetString(@QueryParam("args") SortedSet<String> args) {
            assertEquals(1, args.size());
            assertTrue(args.contains(""));
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringSortedSetEmptyDefault {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") SortedSet<String> args) {
            assertEquals(0, args.size());
            return "content";
        }
        
        @GET
        @Produces("application/SortedSet")
        public String doGet(
                @QueryParam("args") SortedSet args) {
            assertEquals(0, args.size());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSortedSetDefault {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") SortedSet<String> args) {
            assertTrue(args.contains("a"));
            return "content";
        }
        
        @GET
        @Produces("application/SortedSet")
        public String doGet(
                @QueryParam("args") @DefaultValue("a") SortedSet args) {
            assertTrue(args.contains("a"));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringSortedSetDefaultOverride {
        @GET
        @Produces("application/stringSortedSet")
        public String doGetString(
                @QueryParam("args") @DefaultValue("a") SortedSet<String> args) {
            assertTrue(args.contains("b"));
            return "content";
        }
        
        @GET
        @Produces("application/SortedSet")
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
    
    public void testSortedSetGet() {
         initiateWebApplication(ResourceStringSortedSet.class);
        
        resource("/?args=a&args=b&args=c").
            accept("application/SortedSet").
            get(String.class);
    }
        
    public void testStringSortedSetEmptyDefault() {
        initiateWebApplication(ResourceStringSortedSetEmptyDefault.class);
        
        resource("/").
            accept("application/stringSortedSet").
            get(String.class);
    }
    
    public void testSortedSetEmptyDefault() {
        initiateWebApplication(ResourceStringSortedSetEmptyDefault.class);
        
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
