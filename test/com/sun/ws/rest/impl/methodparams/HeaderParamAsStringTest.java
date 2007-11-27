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

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class HeaderParamAsStringTest extends AbstractResourceTester {

    public HeaderParamAsStringTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ResourceString {
        @HttpMethod("GET")
        public String doGet(@HeaderParam("arg1") String arg1, 
                @HeaderParam("arg2") String arg2, @HeaderParam("arg3") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }
        
        @HttpMethod("POST")
        public String doPost(@HeaderParam("arg1") String arg1, 
                @HeaderParam("arg2") String arg2, @HeaderParam("arg3") String arg3,
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
        @HttpMethod("GET")
        public String doGet(@HeaderParam("arg1") String arg1) {
            assertEquals("", arg1);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringAbsent {
        @HttpMethod("GET")
        public String doGet(@HeaderParam("arg1") String arg1) {
            assertEquals(null, arg1);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringList {
        @HttpMethod("GET")
        @ProduceMime("application/stringlist")
        public String doGetString(@HeaderParam("args") List<String> args) {
            assertEquals("a", args.get(0));
            assertEquals("b", args.get(1));
            assertEquals("c", args.get(2));
            return "content";
        }
        
        @HttpMethod("GET")
        @ProduceMime("application/list")
        public String doGet(@HeaderParam("args") List args) {
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
        @HttpMethod("GET")
        @ProduceMime("application/stringlist")
        public String doGetString(@HeaderParam("args") List<String> args) {
            assertEquals(3, args.size());
            assertEquals("", args.get(0));
            assertEquals("", args.get(1));
            assertEquals("", args.get(2));
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringListAbsent {
        @HttpMethod("GET")
        @ProduceMime("application/stringlist")
        public String doGetString(@HeaderParam("args") List<String> args) {
            assertEquals(null, args);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringNullDefault {
        @HttpMethod("GET")
        public String doGet(@HeaderParam("arg1") String arg1, 
                @HeaderParam("arg2") String arg2, @HeaderParam("arg3") String arg3) {
            assertEquals(null, arg1);
            assertEquals(null, arg2);
            assertEquals(null, arg3);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringDefault {
        @HttpMethod("GET")
        public String doGet(
                @HeaderParam("arg1") @DefaultValue("a") String arg1, 
                @HeaderParam("arg2") @DefaultValue("b") String arg2, 
                @HeaderParam("arg3") @DefaultValue("c") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringDefaultOverride {
        @HttpMethod("GET")
        public String doGet(
                @HeaderParam("arg1") @DefaultValue("a") String arg1, 
                @HeaderParam("arg2") @DefaultValue("b") String arg2, 
                @HeaderParam("arg3") @DefaultValue("c") String arg3) {
            assertEquals("d", arg1);
            assertEquals("e", arg2);
            assertEquals("f", arg3);
            return "content";
        }        
    }
    
    @Path("/")
    public static class ResourceStringListNullDefault {
        @HttpMethod("GET")
        @ProduceMime("application/stringlist")
        public String doGetString(
                @HeaderParam("args") List<String> args) {
            assertEquals(null, args);
            return "content";
        }
        
        @HttpMethod("GET")
        @ProduceMime("application/list")
        public String doGet(
                @HeaderParam("args") List args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefault {
        @HttpMethod("GET")
        @ProduceMime("application/stringlist")
        public String doGetString(
                @HeaderParam("args") @DefaultValue("a") List<String> args) {
            assertEquals("a", args.get(0));
            return "content";
        }
        
        @HttpMethod("GET")
        @ProduceMime("application/list")
        public String doGet(
                @HeaderParam("args") @DefaultValue("a") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("a", args.get(0));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/stringlist")
        public String doGetString(
                @HeaderParam("args") @DefaultValue("a") List<String> args) {
            assertEquals("b", args.get(0));
            return "content";
        }
        
        @HttpMethod("GET")
        @ProduceMime("application/list")
        public String doGet(
                @HeaderParam("args") @DefaultValue("a") List args) {
            assertEquals(String.class, args.get(0).getClass());
            assertEquals("b", args.get(0));
            return "content";
        }
    }
    
    public void testStringGet() {
        initiateWebApplication(ResourceString.class);
        
        resourceProxy("/").
            request("arg1", "a").
            header("arg2", "b").
            header("arg3", "c").
            get(String.class);
    }
    
    public void testStringEmptyGet() {
        initiateWebApplication(ResourceStringEmpty.class);
        
        resourceProxy("/").
            request("arg1", "").
            get(String.class);
    }
    
    public void testStringAbsentGet() {
        initiateWebApplication(ResourceStringAbsent.class);
        
        resourceProxy("/").
            get(String.class);
    }
    
    public void testStringPost() {
        initiateWebApplication(ResourceString.class);
        
        String s = resourceProxy("/").
            content("content").
            header("arg1", "a").
            header("arg2", "b").
            header("arg3", "c").
            post(String.class);
        
        assertEquals("content", s);
    }
    
    public void testStringListGet() {
        initiateWebApplication(ResourceStringList.class);
        
        resourceProxy("/").
            acceptable("application/stringlist").
            header("args", "a").
            header("args", "b").
            header("args", "c").
            get(String.class);
    }
    
    public void testStringListEmptyGet() {
        initiateWebApplication(ResourceStringListEmpty.class);
        
        resourceProxy("/").
            acceptable("application/stringlist").
            header("args", "").
            header("args", "").
            header("args", "").
            get(String.class);
    }
    
    public void testStringListAbsentGet() {
        initiateWebApplication(ResourceStringListAbsent.class);
        
        resourceProxy("/").
            acceptable("application/stringlist").
            get(String.class);
    }
    
    public void testListGet() {
        initiateWebApplication(ResourceStringList.class);
        
        resourceProxy("/").
            acceptable("application/list").
            header("args", "a").
            header("args", "b").
            header("args", "c").
            get(String.class);
    }
    
    public void testStringNullDefault() {
        initiateWebApplication(ResourceStringNullDefault.class);
        
        resourceProxy("/").get(String.class);
    }
    
    public void testStringDefault() {
        initiateWebApplication(ResourceStringDefault.class);
        
        resourceProxy("/").get(String.class);
    }
    
    public void testStringDefaultOverride() {
        initiateWebApplication(ResourceStringDefaultOverride.class);
        
        resourceProxy("/").
            request("arg1", "d").
            header("arg2", "e").
            header("arg3", "f").
            get(String.class);
    }
    
    public void testStringListNullDefault() {
        initiateWebApplication(ResourceStringListNullDefault.class);
        
        resourceProxy("/").
            acceptable("application/stringlist").
            get(String.class);
    }
    
    public void testListNullDefault() {
        initiateWebApplication(ResourceStringListNullDefault.class);
        
        resourceProxy("/").
            acceptable("application/list").
            get(String.class);
    }
    
    public void testStringListDefault() {
        initiateWebApplication(ResourceStringListDefault.class);
        
        resourceProxy("/").
            acceptable("application/stringlist").
            get(String.class);
    }
    
    public void testListDefault() {
        initiateWebApplication(ResourceStringListDefault.class);
        
        resourceProxy("/").
            acceptable("application/list").
            get(String.class);
    }
    
    public void testListDefaultOverride() {
        initiateWebApplication(ResourceStringListDefaultOverride.class);
        
        resourceProxy("/").
            acceptable("application/list").
            header("args", "b").
            get(String.class);
    }
}
