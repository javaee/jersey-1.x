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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.Path;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ResponseInBound;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriParamAsPrimitiveTest extends AbstractResourceTester {

    public UriParamAsPrimitiveTest(String testName) {
        super(testName);
        initiateWebApplication(
                ResourceUriBoolean.class,
                ResourceUriByte.class,
                ResourceUriShort.class,
                ResourceUriInt.class,
                ResourceUriLong.class,
                ResourceUriFloat.class,
                ResourceUriDouble.class,
                ResourceUriBooleanWrapper.class,
                ResourceUriByteWrapper.class,
                ResourceUriShortWrapper.class,
                ResourceUriIntWrapper.class,
                ResourceUriLongWrapper.class,
                ResourceUriFloatWrapper.class,
                ResourceUriDoubleWrapper.class
        );
    }

    @Path("/boolean/{arg}")
    public static class ResourceUriBoolean {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
    }
    
    @Path("/byte/{arg}")
    public static class ResourceUriByte {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") byte v) {
            assertEquals(127, v);
            return "content";
        }        
    }
    
    @Path("/short/{arg}")
    public static class ResourceUriShort {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") short v) {
            assertEquals(32767, v);
            return "content";
        }        
    }
    
    @Path("/int/{arg}")
    public static class ResourceUriInt {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
    }
    
    @Path("/long/{arg}")
    public static class ResourceUriLong {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
    }
    
    @Path("/float/{arg}")
    public static class ResourceUriFloat {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
    }
    
    @Path("/double/{arg}")
    public static class ResourceUriDouble {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    
    @Path("/boolean/wrapper/{arg}")
    public static class ResourceUriBooleanWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
    }
    
    @Path("/byte/wrapper/{arg}")
    public static class ResourceUriByteWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
    }
    
    @Path("/short/wrapper/{arg}")
    public static class ResourceUriShortWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
    }
    
    @Path("/int/wrapper/{arg}")
    public static class ResourceUriIntWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
    }
    
    @Path("/long/wrapper/{arg}")
    public static class ResourceUriLongWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
    }
    
    @Path("/float/wrapper/{arg}")
    public static class ResourceUriFloatWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
    }
    
    @Path("/double/wrapper/{arg}")
    public static class ResourceUriDoubleWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    
    void _test(String type, String value) {
        resourceProxy("/"+ type + "/" + value).
                get(String.class);
        resourceProxy("/"+ type + "/wrapper/" + value).
                get(String.class);
    }
    
    public void testGetBoolean() {
        _test("boolean", "true");
    }    
    
    public void testGetByte() {
        _test("byte", "127");
    }    
    
    public void testGetShort() {
        _test("short", "32767");
    }    
    
    public void testGetInt() {
        _test("int", "2147483647");
    }    
    
    public void testGetLong() {
        _test("long", "9223372036854775807");
    }    
    
    public void testGetFloat() {
        _test("float", "3.14159265");
    }    
    
    public void testGetDouble() {
        _test("double", "3.14159265358979");
    }
    
    public void testBadPrimitiveValue() {
        ResponseInBound response = resourceProxy("/int/abcdef", false).
                get(ResponseInBound.class);
        
        assertEquals(400, response.getStatus());
    }
    
    public void testBadPrimitiveWrapperValue() {
        ResponseInBound response = resourceProxy("/int/wrapper/abcdef", false).
                get(ResponseInBound.class);
        
        assertEquals(400, response.getStatus());
    }    
}
