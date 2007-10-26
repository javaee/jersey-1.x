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
import javax.ws.rs.MatrixParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ResponseInBound;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MatrixParamStringConstructorTest extends AbstractResourceTester {

    public MatrixParamStringConstructorTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    public static class ResourceString {
        @HttpMethod("GET")
        public String doGet(
                @MatrixParam("arg1") BigDecimal arg1, 
                @MatrixParam("arg2") BigInteger arg2,
                @MatrixParam("arg3") URI arg3) {
            assertEquals("3.145", arg1.toString());
            assertEquals("3145", arg2.toString());
            assertEquals("http://test", arg3.toString());
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringList {
        @HttpMethod("GET")
        public String doGetString(@MatrixParam("args") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            assertEquals("2.718", args.get(1).toString());
            assertEquals("1.618", args.get(2).toString());
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringListEmpty {
        @HttpMethod("GET")
        public String doGetString(@MatrixParam("args") List<BigDecimal> args) {
            assertEquals(3, args.size());
            assertEquals(null, args.get(0));
            assertEquals(null, args.get(1));
            assertEquals(null, args.get(2));
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringListAbsent {
        @HttpMethod("GET")
        public String doGetString(@MatrixParam("args") List<BigDecimal> args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringNullDefault {
        @HttpMethod("GET")
        public String doGet(
                @MatrixParam("arg1") BigDecimal arg1) {
            assertEquals(null, arg1);
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringDefault {
        @HttpMethod("GET")
        public String doGet(
                @MatrixParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("3.145", arg1.toString());
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringDefaultOverride {
        @HttpMethod("GET")
        public String doGet(
                @MatrixParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("2.718", arg1.toString());
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringListNullDefault {
        @HttpMethod("GET")
        public String doGetString(@MatrixParam("args") List<BigDecimal> args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringListDefault {
        @HttpMethod("GET")
        public String doGetString(
                @MatrixParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            return "content";
        }
    }
    
    @UriTemplate("/")
    public static class ResourceStringListDefaultOverride {
        @HttpMethod("GET")
        public String doGetString(
                @MatrixParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("2.718", args.get(0).toString());
            return "content";
        }
    }
    
    public void testStringConstructorGet() {
        initiateWebApplication(ResourceString.class);
        
        resourceProxy("/;arg1=3.145;arg2=3145;arg3=http:%2F%2Ftest").
                get(String.class);
    }
    
    public void testStringConstructorListGet() {
        initiateWebApplication(ResourceStringList.class);
        
        resourceProxy("/;args=3.145;args=2.718;args=1.618").
                acceptable("application/stringlist").
                get(String.class);
    }
    
    public void testStringConstructorListEmptyGet() {
        initiateWebApplication(ResourceStringListEmpty.class);
        
        resourceProxy("/;args;args;args").
                acceptable("application/stringlist").
                get(String.class);
    }
    
    public void testStringConstructorListAbsentGet() {
        initiateWebApplication(ResourceStringListAbsent.class);
        
        resourceProxy("/").
            acceptable("application/stringlist").
            get(String.class);
    }
    
    public void testStringConstructorNullDefault() {
        initiateWebApplication(ResourceStringNullDefault.class);
        
        resourceProxy("/").get(String.class);
    }
    
    public void testStringConstructorDefault() {
        initiateWebApplication(ResourceStringDefault.class);
        
        resourceProxy("/").get(String.class);
    }
    
    public void testStringConstructorDefaultOverride() {
        initiateWebApplication(ResourceStringDefault.class);
        
        resourceProxy("/;args=2.718").
                get(String.class);
    }
    
    public void testStringConstructorListNullDefault() {
        initiateWebApplication(ResourceStringListNullDefault.class);
        
        resourceProxy("/").get(String.class);
    }
    
    public void testStringConstructorListDefault() {
        initiateWebApplication(ResourceStringListDefault.class);
        
        resourceProxy("/").get(String.class);
    }
    
    public void testStringConstructorListDefaultOverride() {
        initiateWebApplication(ResourceStringListDefaultOverride.class);
        
        resourceProxy("/;args=2.718").
                get(String.class);
    }
    
    public void testBadStringConstructorValue() {
        initiateWebApplication(ResourceString.class);
        
        ResponseInBound response = resourceProxy("/;arg1=ABCDEF;arg2=3145;arg3=http:%2F%2Ftest", false).
                get(ResponseInBound.class);
        assertEquals(400, response.getStatus());
    }
}
