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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.bean.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriParamAsPrimitiveTest extends AbstractBeanTester {

    public UriParamAsPrimitiveTest(String testName) {
        super(testName);
    }

    @UriTemplate("/boolean/{arg}")
    public static class ResourceUriBoolean {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
    }
    
    @UriTemplate("/byte/{arg}")
    public static class ResourceUriByte {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") byte v) {
            assertEquals(127, v);
            return "content";
        }        
    }
    
    @UriTemplate("/short/{arg}")
    public static class ResourceUriShort {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") short v) {
            assertEquals(32767, v);
            return "content";
        }        
    }
    
    @UriTemplate("/int/{arg}")
    public static class ResourceUriInt {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
    }
    
    @UriTemplate("/long/{arg}")
    public static class ResourceUriLong {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
    }
    
    @UriTemplate("/float/{arg}")
    public static class ResourceUriFloat {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
    }
    
    @UriTemplate("/double/{arg}")
    public static class ResourceUriDouble {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    
    @UriTemplate("/boolean/wrapper/{arg}")
    public static class ResourceUriBooleanWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
    }
    
    @UriTemplate("/byte/wrapper/{arg}")
    public static class ResourceUriByteWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
    }
    
    @UriTemplate("/short/wrapper/{arg}")
    public static class ResourceUriShortWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
    }
    
    @UriTemplate("/int/wrapper/{arg}")
    public static class ResourceUriIntWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
    }
    
    @UriTemplate("/long/wrapper/{arg}")
    public static class ResourceUriLongWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
    }
    
    @UriTemplate("/float/wrapper/{arg}")
    public static class ResourceUriFloatWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
    }
    
    @UriTemplate("/double/wrapper/{arg}")
    public static class ResourceUriDoubleWrapper {
        @HttpMethod("GET")
        public String doGet(@UriParam("arg") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    
    
    public void testGetBoolean() {
        callGet(ResourceUriBoolean.class, 
                "/boolean/true", "text/plain");
        callGet(ResourceUriBooleanWrapper.class, 
                "/boolean/wrapper/true", "text/plain");        
    }    
    
    public void testGetByte() {
        callGet(ResourceUriByte.class, 
                "/byte/127", "text/plain");
        callGet(ResourceUriByteWrapper.class, 
                "/byte/wrapper/127", "text/plain");
    }    
    
    public void testGetShort() {
        callGet(ResourceUriShort.class, 
                "/short/32767", "text/plain");
        callGet(ResourceUriShortWrapper.class, 
                "/short/wrapper/32767", "text/plain");
    }    
    
    public void testGetInt() {
        callGet(ResourceUriInt.class, 
                "/int/2147483647", "text/plain");
        callGet(ResourceUriIntWrapper.class, 
                "/int/wrapper/2147483647", "text/plain");
    }    
    
    public void testGetLong() {
        callGet(ResourceUriLong.class, 
                "/long/9223372036854775807", "text/plain");
        callGet(ResourceUriLongWrapper.class, 
                "/long/wrapper/9223372036854775807", "text/plain");
    }    
    
    public void testGetFloat() {
        callGet(ResourceUriFloat.class, 
                "/float/3.14159265", "text/plain");
        callGet(ResourceUriFloatWrapper.class, 
                "/float/wrapper/3.14159265", "text/plain");
    }    
    
    public void testGetDouble() {
        callGet(ResourceUriDouble.class, 
                "/double/3.14159265358979", "text/plain");
        callGet(ResourceUriDoubleWrapper.class, 
                "/double/wrapper/3.14159265358979", "text/plain");
    }
    
    public void testBadPrimitiveValue() {
        HttpResponseContext response = callNoStatusCheck(ResourceUriInt.class, "GET",
                "/int/abcdef", null, "text/plain", "");
        assertEquals(400, response.getResponse().getStatus());
    }
    
    public void testBadPrimitiveWrapperValue() {
        HttpResponseContext response = callNoStatusCheck(ResourceUriIntWrapper.class, "GET",
                "/int/wrapper/abcdef", null, "text/plain", "");
        assertEquals(400, response.getResponse().getStatus());
    }    
}
