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
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.ClientResponse;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import javax.ws.rs.GET;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class QueryParamStringConstructorTest extends AbstractResourceTester {

    public QueryParamStringConstructorTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ResourceString {
        @GET
        public String doGet(
                @QueryParam("arg1") BigDecimal arg1, 
                @QueryParam("arg2") BigInteger arg2,
                @QueryParam("arg3") URI arg3) {
            assertEquals("3.145", arg1.toString());
            assertEquals("3145", arg2.toString());
            assertEquals("http://test", arg3.toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringList {
        @GET
        public String doGetString(@QueryParam("args") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            assertEquals("2.718", args.get(1).toString());
            assertEquals("1.618", args.get(2).toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListEmpty {
        @GET
        public String doGetString(@QueryParam("args") List<BigDecimal> args) {
            assertEquals(3, args.size());
            assertEquals(null, args.get(0));
            assertEquals(null, args.get(1));
            assertEquals(null, args.get(2));
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListAbsent {
        @GET
        public String doGetString(@QueryParam("args") List<BigDecimal> args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringNullDefault {
        @GET
        public String doGet(
                @QueryParam("arg1") BigDecimal arg1) {
            assertEquals(null, arg1);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringDefault {
        @GET
        public String doGet(
                @QueryParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("3.145", arg1.toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringDefaultOverride {
        @GET
        public String doGet(
                @QueryParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("2.718", arg1.toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListNullDefault {
        @GET
        public String doGetString(@QueryParam("args") List<BigDecimal> args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefault {
        @GET
        public String doGetString(
                @QueryParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefaultOverride {
        @GET
        public String doGetString(
                @QueryParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("2.718", args.get(0).toString());
            return "content";
        }
    }
    
    public void testStringConstructorGet() {
        initiateWebApplication(ResourceString.class);
        
        resource("/?arg1=3.145&arg2=3145&arg3=http:%2F%2Ftest").
                get(String.class);
    }
    
    public void testStringConstructorListGet() {
        initiateWebApplication(ResourceStringList.class);
        
        resource("/?args=3.145&args=2.718&args=1.618").
                accept("application/stringlist").
                get(String.class);
    }
    
    public void testStringConstructorListEmptyGet() {
        initiateWebApplication(ResourceStringListEmpty.class);
        
        resource("/?args&args&args").
                accept("application/stringlist").
                get(String.class);
    }
    
    public void testStringConstructorListAbsentGet() {
        initiateWebApplication(ResourceStringListAbsent.class);
        
        resource("/").
            accept("application/stringlist").
            get(String.class);
    }
    
    public void testStringConstructorNullDefault() {
        initiateWebApplication(ResourceStringNullDefault.class);
        
        resource("/").get(String.class);
    }
    
    public void testStringConstructorDefault() {
        initiateWebApplication(ResourceStringDefault.class);
        
        resource("/").get(String.class);
    }
    
    public void testStringConstructorDefaultOverride() {
        initiateWebApplication(ResourceStringDefault.class);
        
        resource("/?args=2.718").
                get(String.class);
    }
    
    public void testStringConstructorListNullDefault() {
        initiateWebApplication(ResourceStringListNullDefault.class);
        
        resource("/").get(String.class);
    }
    
    public void testStringConstructorListDefault() {
        initiateWebApplication(ResourceStringListDefault.class);
        
        resource("/").get(String.class);
    }
    
    public void testStringConstructorListDefaultOverride() {
        initiateWebApplication(ResourceStringListDefaultOverride.class);
        
        resource("/?args=2.718").
                get(String.class);
    }
    
    public void testBadStringConstructorValue() {
        initiateWebApplication(ResourceString.class);
        
        ClientResponse response = resource("/?arg1=ABCDEF&arg2=3145&arg3=http:%2F%2Ftest", false).
                get(ClientResponse.class);
        assertEquals(400, response.getStatus());
    }
}
