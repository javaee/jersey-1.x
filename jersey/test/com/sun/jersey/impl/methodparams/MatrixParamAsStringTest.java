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

package com.sun.jersey.impl.methodparams;

import com.sun.jersey.impl.AbstractResourceTester;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class MatrixParamAsStringTest extends AbstractResourceTester {

    public MatrixParamAsStringTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ResourceString {
        @GET
        public String doGet(@MatrixParam("arg1") String arg1, 
                @MatrixParam("arg2") String arg2, @MatrixParam("arg3") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }
        
        @POST
        public String doPost(@MatrixParam("arg1") String arg1, 
                @MatrixParam("arg2") String arg2, @MatrixParam("arg3") String arg3,
                String r) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            assertEquals("content", r);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringEmpty {
        @GET
        public String doGet(@MatrixParam("arg1") String arg1) {
            assertEquals("", arg1);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringAbsent {
        @GET
        public String doGet(@MatrixParam("arg1") String arg1) {
            assertEquals(null, arg1);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringList {
        @GET
        @ProduceMime("application/stringlist")
        public String doGetString(@MatrixParam("args") List<String> args) {
            assertEquals("a", args.get(0));
            assertEquals("b", args.get(1));
            assertEquals("c", args.get(2));
            return "content";
        }
        
        @GET
        @ProduceMime("application/list")
        public String doGet(@MatrixParam("args") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("a", args.get(0));
            assertEquals(String.class, args.get(1).getClass());
            assertEquals("b", args.get(1));
            assertEquals(String.class, args.get(2).getClass());
            assertEquals("c", args.get(2));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListEmpty {
        @GET
        @ProduceMime("application/stringlist")
        public String doGetString(@MatrixParam("args") List<String> args) {
            assertEquals(3, args.size());
            assertEquals("", args.get(0));
            assertEquals("", args.get(1));
            assertEquals("", args.get(2));
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringListAbsent {
        @GET
        @ProduceMime("application/stringlist")
        public String doGetString(@MatrixParam("args") List<String> args) {
            assertEquals(null, args);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringNullDefault {
        @GET
        public String doGet(@MatrixParam("arg1") String arg1, 
                @MatrixParam("arg2") String arg2, @MatrixParam("arg3") String arg3) {
            assertEquals(null, arg1);
            assertEquals(null, arg2);
            assertEquals(null, arg3);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringDefault {
        @GET
        public String doGet(
                @MatrixParam("arg1") @DefaultValue("a") String arg1, 
                @MatrixParam("arg2") @DefaultValue("b") String arg2, 
                @MatrixParam("arg3") @DefaultValue("c") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringDefaultOverride {
        @GET
        public String doGet(
                @MatrixParam("arg1") @DefaultValue("a") String arg1, 
                @MatrixParam("arg2") @DefaultValue("b") String arg2, 
                @MatrixParam("arg3") @DefaultValue("c") String arg3) {
            assertEquals("d", arg1);
            assertEquals("e", arg2);
            assertEquals("f", arg3);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringListNullDefault {
        @GET
        @ProduceMime("application/stringlist")
        public String doGetString(
                @MatrixParam("args") List<String> args) {
            assertEquals(null, args);
            return "content";
        }
        
        @GET
        @ProduceMime("application/list")
        public String doGet(
                @MatrixParam("args") List args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefault {
        @GET
        @ProduceMime("application/stringlist")
        public String doGetString(
                @MatrixParam("args") @DefaultValue("a") List<String> args) {
            assertEquals("a", args.get(0));
            return "content";
        }
        
        @GET
        @ProduceMime("application/list")
        public String doGet(
                @MatrixParam("args") @DefaultValue("a") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("a", args.get(0));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefaultOverride {
        @GET
        @ProduceMime("application/stringlist")
        public String doGetString(
                @MatrixParam("args") @DefaultValue("a") List<String> args) {
            assertEquals("b", args.get(0));
            return "content";
        }
        
        @GET
        @ProduceMime("application/list")
        public String doGet(
                @MatrixParam("args") @DefaultValue("a") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("b", args.get(0));
            return "content";
        }
    }
    
    public void testStringGet() {
        initiateWebApplication(ResourceString.class);
        
        resource("/;arg1=a;arg2=b;arg3=c").
            get(String.class);
    }
    
    public void testStringEmptyGet() {
        initiateWebApplication(ResourceStringEmpty.class);
        
        resource("/;arg1").
            get(String.class);
    }
    
    public void testStringAbsentGet() {
        initiateWebApplication(ResourceStringAbsent.class);
        
        resource("/").
            get(String.class);
    }
    
    public void testStringPost() {
         initiateWebApplication(ResourceString.class);
        
        String s = resource("/;arg1=a;arg2=b;arg3=c").
            entity("content").
            post(String.class);
        
        assertEquals("content", s);
    }
    
    public void testStringListGet() {
        initiateWebApplication(ResourceStringList.class);
        
        resource("/;args=a;args=b;args=c").
            accept("application/stringlist").
            get(String.class);
    }
    
    public void testStringListEmptyGet() {
        initiateWebApplication(ResourceStringListEmpty.class);
        
        resource("/;args;args;args").
            accept("application/stringlist").
            get(String.class);
    }
    
    public void testStringListAbsentGet() {
        initiateWebApplication(ResourceStringListAbsent.class);
        
        resource("/").
            accept("application/stringlist").
            get(String.class);
    }
    
    public void testListGet() {
         initiateWebApplication(ResourceStringList.class);
        
        resource("/;args=a;args=b;args=c").
            accept("application/list").
            get(String.class);
   }
    
    public void testStringNullDefault() {
        initiateWebApplication(ResourceStringNullDefault.class);
        
        resource("/").get(String.class);
    }
    
    public void testStringDefault() {
        initiateWebApplication(ResourceStringDefault.class);
        
        resource("/").get(String.class);
    }
    
    public void testStringDefaultOverride() {
        initiateWebApplication(ResourceStringDefaultOverride.class);
        
        resource("/;arg1=d;arg2=e;arg3=f").
            get(String.class);
    }
    
    public void testStringListNullDefault() {
        initiateWebApplication(ResourceStringListNullDefault.class);
        
        resource("/").
            accept("application/stringlist").
            get(String.class);
    }
    
    public void testListNullDefault() {
        initiateWebApplication(ResourceStringListNullDefault.class);
        
        resource("/").
            accept("application/list").
            get(String.class);
    }
    
    public void testStringListDefault() {
        initiateWebApplication(ResourceStringListDefault.class);
        
        resource("/").
            accept("application/stringlist").
            get(String.class);
    }
    
    public void testListDefault() {
        initiateWebApplication(ResourceStringListDefault.class);
        
        resource("/").
            accept("application/list").
            get(String.class);
    }
    
    public void testListDefaultOverride() {
        initiateWebApplication(ResourceStringListDefaultOverride.class);
        
        resource("/;args=b").
            accept("application/list").
            get(String.class);
    }
}
