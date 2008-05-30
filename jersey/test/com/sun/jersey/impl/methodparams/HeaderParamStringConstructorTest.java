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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
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
public class HeaderParamStringConstructorTest extends AbstractResourceTester {

    public HeaderParamStringConstructorTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class ResourceString {
        @GET
        public String doGet(
                @HeaderParam("arg1") BigDecimal arg1, 
                @HeaderParam("arg2") BigInteger arg2,
                @HeaderParam("arg3") URI arg3) {
            assertEquals("3.145", arg1.toString());
            assertEquals("3145", arg2.toString());
            assertEquals("http://test", arg3.toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringList {
        @GET
        public String doGetString(@HeaderParam("args") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            assertEquals("2.718", args.get(1).toString());
            assertEquals("1.618", args.get(2).toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListEmpty {
        @GET
        public String doGetString(@HeaderParam("args") List<BigDecimal> args) {
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
        public String doGetString(@HeaderParam("args") List<BigDecimal> args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringNullDefault {
        @GET
        public String doGet(
                @HeaderParam("arg1") BigDecimal arg1) {
            assertEquals(null, arg1);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringDefault {
        @GET
        public String doGet(
                @HeaderParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("3.145", arg1.toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringDefaultOverride {
        @GET
        public String doGet(
                @HeaderParam("arg1") @DefaultValue("3.145") BigDecimal arg1) {
            assertEquals("2.718", arg1.toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListNullDefault {
        @GET
        public String doGetString(@HeaderParam("args") List<BigDecimal> args) {
            assertEquals(null, args);
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefault {
        @GET
        public String doGetString(
                @HeaderParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("3.145", args.get(0).toString());
            return "content";
        }
    }
    
    @Path("/")
    public static class ResourceStringListDefaultOverride {
        @GET
        public String doGetString(
                @HeaderParam("args") @DefaultValue("3.145") List<BigDecimal> args) {
            assertEquals("2.718", args.get(0).toString());
            return "content";
        }
    }
    
    public void testStringConstructorGet() {
        initiateWebApplication(ResourceString.class);
        
        resource("/").
                header("arg1", "3.145").
                header("arg2", "3145").
                header("arg3", "http://test").
                get(String.class);
    }
    
    public void testStringConstructorListGet() {
        initiateWebApplication(ResourceStringList.class);
        
        resource("/").
                accept("application/stringlist").
                header("args", "3.145").
                header("args", "2.718").
                header("args", "1.618").
                get(String.class);
    }
    
    public void testStringConstructorListEmptyGet() {
        initiateWebApplication(ResourceStringListEmpty.class);
        
        resource("/").
                accept("application/stringlist").
                header("args", "").
                header("args", "").
                header("args", "").
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
        
        resource("/").
                header("args", "2.718").
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
        
        resource("/").
                header("args", "2.718").
                get(String.class);
    }
    
    public void testBadStringConstructorValue() {
        initiateWebApplication(ResourceString.class);
        
        ClientResponse response = resource("/", false).
                header("arg1", "ABCDEF").
                header("arg2", "3145").
                header("arg3", "http://test").
                get(ClientResponse.class);
        assertEquals(400, response.getStatus());
    }
}
