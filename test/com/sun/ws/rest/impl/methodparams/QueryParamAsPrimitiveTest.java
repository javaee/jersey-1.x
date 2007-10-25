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
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.bean.*;
import com.sun.ws.rest.impl.client.ResponseInBound;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class QueryParamAsPrimitiveTest extends AbstractBeanTester {

    public QueryParamAsPrimitiveTest(String testName) {
        super(testName);
        initiateWebApplication(
                ResourceQueryPrimitives.class,
                ResourceQueryPrimitivesDefaultNull.class,
                ResourceQueryPrimitivesDefault.class,
                ResourceQueryPrimitivesDefaultOverride.class,
                ResourceQueryPrimitiveWrappers.class,
                ResourceQueryPrimitiveWrappersDefaultNull.class,
                ResourceQueryPrimitiveWrappersDefault.class,
                ResourceQueryPrimitiveWrappersDefaultOverride.class,                
                ResourceQueryPrimitiveList.class,
                ResourceQueryPrimitiveListDefaultNull.class,
                ResourceQueryPrimitiveListDefault.class,
                ResourceQueryPrimitiveListDefaultOverride.class
                );
    }

    @UriTemplate("/")
    public static class ResourceQueryPrimitives {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/default/null")
    public static class ResourceQueryPrimitivesDefaultNull {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") boolean v) {
            assertEquals(false, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") byte v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") short v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") int v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") long v) {
            assertEquals(0l, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") float v) {
            assertEquals(0.0f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") double v) {
            assertEquals(0.0d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/default")
    public static class ResourceQueryPrimitivesDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") @DefaultValue("true") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") @DefaultValue("127") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") @DefaultValue("32767") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") @DefaultValue("2147483647") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") @DefaultValue("9223372036854775807") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") @DefaultValue("3.14159265") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") @DefaultValue("3.14159265358979") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/default/override")
    public static class ResourceQueryPrimitivesDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") @DefaultValue("false") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") @DefaultValue("1") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") @DefaultValue("1") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") @DefaultValue("1") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") @DefaultValue("1") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") @DefaultValue("0.0") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") @DefaultValue("0.0") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/wrappers")
    public static class ResourceQueryPrimitiveWrappers {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/wrappers/default/null")
    public static class ResourceQueryPrimitiveWrappersDefaultNull {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") Boolean v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") Byte v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") Short v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") Integer v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") Long v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") Float v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") Double v) {
            assertEquals(null, v);
            return "content";
        }        
    }
    
    @UriTemplate("/wrappers/default")
    public static class ResourceQueryPrimitiveWrappersDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") @DefaultValue("true") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") @DefaultValue("127") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") @DefaultValue("32767") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") @DefaultValue("2147483647") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") @DefaultValue("9223372036854775807") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") @DefaultValue("3.14159265") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") @DefaultValue("3.14159265358979") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/wrappers/default/override")
    public static class ResourceQueryPrimitiveWrappersDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@QueryParam("boolean") @DefaultValue("false") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@QueryParam("byte") @DefaultValue("1") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@QueryParam("short") @DefaultValue("1") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@QueryParam("int") @DefaultValue("1") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@QueryParam("long") @DefaultValue("1") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@QueryParam("float") @DefaultValue("0.0") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@QueryParam("double") @DefaultValue("0.0") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/list")
    public static class ResourceQueryPrimitiveList {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            assertEquals(true, v.get(1).booleanValue());
            assertEquals(true, v.get(2).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@QueryParam("byte") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            assertEquals(127, v.get(1).byteValue());
            assertEquals(127, v.get(2).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@QueryParam("short") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            assertEquals(32767, v.get(1).shortValue());
            assertEquals(32767, v.get(2).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@QueryParam("int") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            assertEquals(2147483647, v.get(1).intValue());
            assertEquals(2147483647, v.get(2).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@QueryParam("long") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            assertEquals(9223372036854775807L, v.get(1).longValue());
            assertEquals(9223372036854775807L, v.get(2).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@QueryParam("float") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            assertEquals(3.14159265f, v.get(1).floatValue());
            assertEquals(3.14159265f, v.get(2).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@QueryParam("double") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            assertEquals(3.14159265358979d, v.get(1).doubleValue());
            assertEquals(3.14159265358979d, v.get(2).doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/list/default/null")
    public static class ResourceQueryPrimitiveListDefaultNull {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") List<Boolean> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@QueryParam("byte") List<Byte> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@QueryParam("short") List<Short> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@QueryParam("int") List<Integer> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@QueryParam("long") List<Long> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@QueryParam("float") List<Float> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@QueryParam("double") List<Double> v) {
            assertEquals(null, v);
            return "content";
        }        
    }
    
    @UriTemplate("/list/default")
    public static class ResourceQueryPrimitiveListDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") @DefaultValue("true") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@QueryParam("byte") @DefaultValue("127") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@QueryParam("short") @DefaultValue("32767") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@QueryParam("int") @DefaultValue("2147483647") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@QueryParam("long") @DefaultValue("9223372036854775807") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@QueryParam("float") @DefaultValue("3.14159265") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@QueryParam("double") @DefaultValue("3.14159265358979") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/list/default/override")
    public static class ResourceQueryPrimitiveListDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@QueryParam("boolean") @DefaultValue("false") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@QueryParam("byte") @DefaultValue("0") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@QueryParam("short") @DefaultValue("0") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@QueryParam("int") @DefaultValue("0") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@QueryParam("long") @DefaultValue("0") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@QueryParam("float") @DefaultValue("0.0") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@QueryParam("double") @DefaultValue("0.0") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            return "content";
        }        
    }
    
    public void _test(String type, String value) {
        String param = type + "=" + value;
        
        resourceProxy("/?" + param).acceptable("application/" + type).
                get(String.class);
        
        resourceProxy("/wrappers?" + param).acceptable("application/" + type).
                get(String.class);    
        
        resourceProxy("/list?" + param + "&" + param + "&" + param).acceptable("application/" + type).
                get(String.class);
    }

     public void _testDefault(String base, String type, String value) {
        resourceProxy(base + "default/null").acceptable("application/" + type).
                get(String.class);
        
        resourceProxy(base + "default").acceptable("application/" + type).
                get(String.class);
        
        String param = type + "=" + value;
        resourceProxy(base + "default/override?" + param).acceptable("application/" + type).
                get(String.class);        
    }
     
    public void _testDefault(String type, String value) {
        _testDefault("/", type, value);
    }
    
    public void _testWrappersDefault(String type, String value) {
        _testDefault("/wrappers/", type, value);
    }

    public void _testListDefault(String type, String value) {
        _testDefault("/list/", type, value);
    }
    
    public void testGetBoolean() {
        _test("boolean", "true");
    }
    
    public void testGetBooleanPrimitivesDefault() {
        _testDefault("boolean", "true");
    }
    
    public void testGetBooleanPrimitiveWrapperDefault() {
        _testWrappersDefault("boolean", "true");
    }
    
    public void testGetBooleanPrimitiveListDefault() {
        _testListDefault("boolean", "true");
    }    
    
    public void testGetByte() {
        _test("byte", "127");
    }
    
    public void testGetBytePrimitivesDefault() {
        _testDefault("byte", "127");
    }
    
    public void testGetBytePrimitiveWrappersDefault() {
        _testWrappersDefault("byte", "127");
    }
    
    public void testGetBytePrimitiveListDefault() {
        _testListDefault("byte", "127");
    }    
    
    public void testGetShort() {
        _test("short", "32767");
    }
    
    public void testGetShortPrimtivesDefault() {
        _testDefault("short", "32767");
    }
    
    public void testGetShortPrimtiveWrappersDefault() {
        _testWrappersDefault("short", "32767");
    }
    
    public void testGetShortPrimtiveListDefault() {
        _testListDefault("short", "32767");
    }    
    
    public void testGetInt() {
        _test("int", "2147483647");
    }
    
    public void testGetIntPrimitivesDefault() {
        _testDefault("int", "2147483647");
    }
    
    public void testGetIntPrimitiveWrappersDefault() {
        _testWrappersDefault("int", "2147483647");
    }
    
    public void testGetIntPrimitiveListDefault() {
        _testListDefault("int", "2147483647");
    }    
    
    public void testGetLong() {
        _test("long", "9223372036854775807");
    }
    
    public void testGetLongPrimitivesDefault() {
        _testDefault("long", "9223372036854775807");
    }
    
    public void testGetLongPrimitiveWrappersDefault() {
        _testWrappersDefault("long", "9223372036854775807");
    }
    
    public void testGetLongPrimitiveListDefault() {
        _testListDefault("long", "9223372036854775807");
    }    
    
    public void testGetFloat() {
        _test("float", "3.14159265");
    }
    
    public void testGetFloatPrimitivesDefault() {
        _testDefault("float", "3.14159265");
    }
    
    public void testGetFloatPrimitiveWrappersDefault() {
        _testWrappersDefault("float", "3.14159265");
    }
    
    public void testGetFloatPrimitiveListDefault() {
        _testListDefault("float", "3.14159265");
    }    
    
    public void testGetDouble() {
        _test("double", "3.14159265358979");
    }
    
    public void testGetDoublePrimitivesDefault() {
        _testDefault("double", "3.14159265358979");
    }
    
    public void testGetDoublePrimitiveWrappersDefault() {
        _testWrappersDefault("double", "3.14159265358979");
    }
    
    public void testGetDoublePrimitiveListDefault() {
        _testListDefault("double", "3.14159265358979");
    }
    
    public void testBadPrimitiveValue() {
        ResponseInBound response = resourceProxy("/?int=abcdef", false).
                acceptable("application/int").
                get(ResponseInBound.class);
        
        assertEquals(400, response.getStatus());
    }
    
    public void testBadPrimitiveWrapperValue() {
        ResponseInBound response = resourceProxy("/wrappers?int=abcdef", false).
                acceptable("application/int").
                get(ResponseInBound.class);
        
        assertEquals(400, response.getStatus());
    }
    
    public void testBadPrimitiveListValue() {
        ResponseInBound response = resourceProxy("/list?int=abcdef&int=abcdef", false).
                acceptable("application/int").
                get(ResponseInBound.class);
        
        assertEquals(400, response.getStatus());
    }
}
