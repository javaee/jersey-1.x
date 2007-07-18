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

import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.MultivaluedMapImpl;
import com.sun.ws.rest.impl.bean.*;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HeaderParamAsPrimitiveTest extends AbstractBeanTester {

    public HeaderParamAsPrimitiveTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    public static class ResourceQueryPrimitives {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitivesNullDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") boolean v) {
            assertEquals(false, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") byte v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") short v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") int v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") long v) {
            assertEquals(0l, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") float v) {
            assertEquals(0.0f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") double v) {
            assertEquals(0.0d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitivesDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") @DefaultValue("true") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") @DefaultValue("127") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") @DefaultValue("32767") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") @DefaultValue("2147483647") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") @DefaultValue("9223372036854775807") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") @DefaultValue("3.14159265") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") @DefaultValue("3.14159265358979") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitivesDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") @DefaultValue("false") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") @DefaultValue("1") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") @DefaultValue("1") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") @DefaultValue("1") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") @DefaultValue("1") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") @DefaultValue("0.0") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") @DefaultValue("0.0") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappers {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappersNullDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") Boolean v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") Byte v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") Short v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") Integer v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") Long v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") Float v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") Double v) {
            assertEquals(null, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappersDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") @DefaultValue("true") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") @DefaultValue("127") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") @DefaultValue("32767") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") @DefaultValue("2147483647") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") @DefaultValue("9223372036854775807") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") @DefaultValue("3.14159265") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") @DefaultValue("3.14159265358979") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappersDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@HeaderParam("boolean") @DefaultValue("false") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@HeaderParam("byte") @DefaultValue("1") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@HeaderParam("short") @DefaultValue("1") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@HeaderParam("int") @DefaultValue("1") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@HeaderParam("long") @DefaultValue("1") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@HeaderParam("float") @DefaultValue("0.0") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@HeaderParam("double") @DefaultValue("0.0") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveList {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@HeaderParam("boolean") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            assertEquals(true, v.get(1).booleanValue());
            assertEquals(true, v.get(2).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@HeaderParam("byte") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            assertEquals(127, v.get(1).byteValue());
            assertEquals(127, v.get(2).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@HeaderParam("short") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            assertEquals(32767, v.get(1).shortValue());
            assertEquals(32767, v.get(2).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@HeaderParam("int") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            assertEquals(2147483647, v.get(1).intValue());
            assertEquals(2147483647, v.get(2).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@HeaderParam("long") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            assertEquals(9223372036854775807L, v.get(1).longValue());
            assertEquals(9223372036854775807L, v.get(2).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@HeaderParam("float") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            assertEquals(3.14159265f, v.get(1).floatValue());
            assertEquals(3.14159265f, v.get(2).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@HeaderParam("double") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            assertEquals(3.14159265358979d, v.get(1).doubleValue());
            assertEquals(3.14159265358979d, v.get(2).doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveListNullDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@HeaderParam("boolean") List<Boolean> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@HeaderParam("byte") List<Byte> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@HeaderParam("short") List<Short> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@HeaderParam("int") List<Integer> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@HeaderParam("long") List<Long> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@HeaderParam("float") List<Float> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@HeaderParam("double") List<Double> v) {
            assertEquals(null, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveListDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@HeaderParam("boolean") @DefaultValue("true") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@HeaderParam("byte") @DefaultValue("127") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@HeaderParam("short") @DefaultValue("32767") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@HeaderParam("int") @DefaultValue("2147483647") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@HeaderParam("long") @DefaultValue("9223372036854775807") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@HeaderParam("float") @DefaultValue("3.14159265") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@HeaderParam("double") @DefaultValue("3.14159265358979") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveListDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@HeaderParam("boolean") @DefaultValue("false") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@HeaderParam("byte") @DefaultValue("0") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@HeaderParam("short") @DefaultValue("0") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@HeaderParam("int") @DefaultValue("0") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@HeaderParam("long") @DefaultValue("0") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@HeaderParam("float") @DefaultValue("0.0") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@HeaderParam("double") @DefaultValue("0.0") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            return "content";
        }        
    }
    
    
    public void testGetBoolean() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/boolean");
        m.add("boolean", "true");
        callGet(ResourceQueryPrimitives.class, 
                "/", m);
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/", m);
                
        m.add("boolean", "true");
        m.add("boolean", "true");
        callGet(ResourceQueryPrimitiveList.class, 
                "/", m);
    }
    
    public void testGetBooleanPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class,
                "/", "application/boolean");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/boolean");
                
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/boolean");
        m.add("boolean", "true");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetBooleanPrimitiveWrapperDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class,
                "/", "application/boolean");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/boolean");
        
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/boolean");
        m.add("boolean", "true");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetBooleanPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class,
                "/", "application/boolean");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/boolean");
        
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/boolean");
        m.add("boolean", "true");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/", m);
    }    
    
    public void testGetByte() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/byte");
        m.add("byte", "127");
        callGet(ResourceQueryPrimitives.class, 
                "/", m);
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/", m);
        
        m.add("byte", "127");
        m.add("byte", "127");        
        callGet(ResourceQueryPrimitiveList.class, 
                "/", m);
    }
    
    public void testGetBytePrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/byte");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/byte");
        m.add("byte", "127");        
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetBytePrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/byte");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/byte");
        m.add("byte", "127");        
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetBytePrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/byte");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/byte");
        m.add("byte", "127");        
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/", m);
    }    
    
    public void testGetShort() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/short");
        m.add("short", "32767");
        callGet(ResourceQueryPrimitives.class,
                "/", m);
        callGet(ResourceQueryPrimitiveWrappers.class,
                "/", m);

        m.add("short", "32767");
        m.add("short", "32767");
        callGet(ResourceQueryPrimitiveList.class,
                "/", m);
    }
    
    public void testGetShortPrimtivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/short");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/short");
        m.add("short", "32767");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetShortPrimtiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/short");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/short");
        m.add("short", "32767");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetShortPrimtiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/short");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/short");
        m.add("short", "32767");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/", m);
    }    
    
    public void testGetInt() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/int");
        m.add("int", "2147483647");
        callGet(ResourceQueryPrimitives.class,
                "/", m);
        callGet(ResourceQueryPrimitiveWrappers.class,
                "/", m);
        
        m.add("int", "2147483647");
        m.add("int", "2147483647");
        callGet(ResourceQueryPrimitiveList.class,
                "/", m);
    }
    
    public void testGetIntPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/int");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/int");
        m.add("int", "2147483647");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetIntPrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/int");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/int");
        m.add("int", "2147483647");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetIntPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/int");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/int");
        m.add("int", "2147483647");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/", m);
    }    
    
    public void testGetLong() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/long");
        m.add("long", "9223372036854775807");
        callGet(ResourceQueryPrimitives.class, 
                "/", m);
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/", m);

        m.add("long", "9223372036854775807");
        m.add("long", "9223372036854775807");
        callGet(ResourceQueryPrimitiveList.class, 
                "/", m);
    }
    
    public void testGetLongPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/long");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/long");
        m.add("long", "9223372036854775807");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetLongPrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/long");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/long");
        m.add("long", "9223372036854775807");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetLongPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/long");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/long");
        m.add("long", "9223372036854775807");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/", m);
    }    
    
    public void testGetFloat() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/float");
        m.add("float", "3.14159265");
        callGet(ResourceQueryPrimitives.class, 
                "/", m);
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/", m);
        
        m.add("float", "3.14159265");
        m.add("float", "3.14159265");
        callGet(ResourceQueryPrimitiveList.class, 
                "/", m);
    }
    
    public void testGetFloatPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/float");
        
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/float");
        m.add("float", "3.14159265");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetFloatPrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/float");
        
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/float");
        m.add("float", "3.14159265");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetFloatPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/float");
        
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/float");
        m.add("float", "3.14159265");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/", m);
    }    
    
    public void testGetDouble() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/double");
        m.add("double", "3.14159265358979");
        callGet(ResourceQueryPrimitives.class, 
                "/", m);
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/", m);

        m.add("double", "3.14159265358979");
        m.add("double", "3.14159265358979");
        callGet(ResourceQueryPrimitiveList.class, 
                "/", m);
    }
    
    public void testGetDoublePrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/double");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/double");
        m.add("double", "3.14159265358979");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetDoublePrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/double");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/double");
        m.add("double", "3.14159265358979");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/", m);
    }
    
    public void testGetDoublePrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/double");

        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/double");
        m.add("double", "3.14159265358979");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/", m);
    }
    
    public void testBadPrimitiveValue() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/int");
        m.add("int", "abcdef");
        HttpResponseContext response = callNoStatusCheck(ResourceQueryPrimitives.class, "GET",
                "/", m, "");
        assertEquals(400, response.getStatus());
    }
    
    public void testBadPrimitiveWrapperValue() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/int");
        m.add("int", "abcdef");
        HttpResponseContext response = callNoStatusCheck(ResourceQueryPrimitiveWrappers.class, "GET",
                "/", m, "");
        assertEquals(400, response.getStatus());
    }
    
    public void testBadPrimitiveListValue() {
        MultivaluedMap<String, String> m = new MultivaluedMapImpl();
        m.add("Accept", "application/int");
        m.add("int", "abcdef");
        m.add("int", "abcdef");
        m.add("int", "abcdef");
        HttpResponseContext response = callNoStatusCheck(ResourceQueryPrimitiveWrappers.class, "GET",
                "/", m, "");
        assertEquals(400, response.getStatus());
    }
}
