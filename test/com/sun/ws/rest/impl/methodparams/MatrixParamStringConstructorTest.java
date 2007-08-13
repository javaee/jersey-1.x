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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.bean.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MatrixParamStringConstructorTest extends AbstractBeanTester {

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
        Class r = ResourceString.class;
        callGet(r, "/;arg1=3.145;arg2=3145;arg3=http:%2F%2Ftest", 
                "text/plain");
    }
    
    public void testStringConstructorListGet() {
        Class r = ResourceStringList.class;
        callGet(r, "/;args=3.145;args=2.718;args=1.618", 
                "application/stringlist");
    }
    
    public void testStringConstructorListEmptyGet() {
        Class r = ResourceStringListEmpty.class;
        callGet(r, "/;args;args;args", 
                "application/stringlist");
    }
    
    public void testStringConstructorListAbsentGet() {
        Class r = ResourceStringListAbsent.class;
        callGet(r, "/", 
                "application/stringlist");
    }
    
    public void testStringConstructorNullDefault() {
        Class r = ResourceStringNullDefault.class;
        callGet(r, "/", 
                "text/plain");
    }
    
    public void testStringConstructorDefault() {
        Class r = ResourceStringDefault.class;
        callGet(r, "/", 
                "text/plain");
    }
    
    public void testStringConstructorDefaultOverride() {
        Class r = ResourceStringDefault.class;
        callGet(r, "/;args=2.718", 
                "text/plain");
    }
    
    public void testStringConstructorListNullDefault() {
        Class r = ResourceStringListNullDefault.class;
        callGet(r, "/", 
                "text/plain");
    }
    
    public void testStringConstructorListDefault() {
        Class r = ResourceStringListDefault.class;
        callGet(r, "/", 
                "text/plain");
    }
    
    public void testStringConstructorListDefaultOverride() {
        Class r = ResourceStringListDefaultOverride.class;
        callGet(r, "/;args=2.718", 
                "text/plain");
    }
    
    public void testBadStringConstructorValue() {
        Class r = ResourceString.class;
        HttpResponseContext response = callNoStatusCheck(r, "GET",
                "/;arg1=ABCDEF;arg2=3145;arg3=http:%2F%2Ftest", null, "text/plain", "");
        assertEquals(400, response.getStatus());
    }
}
