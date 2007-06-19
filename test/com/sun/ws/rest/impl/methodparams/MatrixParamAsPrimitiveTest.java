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
import javax.ws.rs.MatrixParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpResponseContext;
import com.sun.ws.rest.impl.bean.*;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MatrixParamAsPrimitiveTest extends AbstractBeanTester {

    public MatrixParamAsPrimitiveTest(String testName) {
        super(testName);
    }

    @UriTemplate("/")
    public static class ResourceQueryPrimitives {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitivesNullDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") boolean v) {
            assertEquals(false, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") byte v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") short v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") int v) {
            assertEquals(0, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") long v) {
            assertEquals(0l, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") float v) {
            assertEquals(0.0f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") double v) {
            assertEquals(0.0d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitivesDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") @DefaultValue("true") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") @DefaultValue("127") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") @DefaultValue("32767") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") @DefaultValue("2147483647") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") @DefaultValue("9223372036854775807") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") @DefaultValue("3.14159265") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") @DefaultValue("3.14159265358979") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitivesDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") @DefaultValue("false") boolean v) {
            assertEquals(true, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") @DefaultValue("1") byte v) {
            assertEquals(127, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") @DefaultValue("1") short v) {
            assertEquals(32767, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") @DefaultValue("1") int v) {
            assertEquals(2147483647, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") @DefaultValue("1") long v) {
            assertEquals(9223372036854775807L, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") @DefaultValue("0.0") float v) {
            assertEquals(3.14159265f, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") @DefaultValue("0.0") double v) {
            assertEquals(3.14159265358979d, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappers {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappersNullDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") Boolean v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") Byte v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") Short v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") Integer v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") Long v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") Float v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") Double v) {
            assertEquals(null, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappersDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") @DefaultValue("true") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") @DefaultValue("127") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") @DefaultValue("32767") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") @DefaultValue("2147483647") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") @DefaultValue("9223372036854775807") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") @DefaultValue("3.14159265") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") @DefaultValue("3.14159265358979") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveWrappersDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGet(@MatrixParam("boolean") @DefaultValue("false") Boolean v) {
            assertEquals(true, v.booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGet(@MatrixParam("byte") @DefaultValue("1") Byte v) {
            assertEquals(127, v.byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGet(@MatrixParam("short") @DefaultValue("1") Short v) {
            assertEquals(32767, v.shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGet(@MatrixParam("int") @DefaultValue("1") Integer v) {
            assertEquals(2147483647, v.intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGet(@MatrixParam("long") @DefaultValue("1") Long v) {
            assertEquals(9223372036854775807L, v.longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGet(@MatrixParam("float") @DefaultValue("0.0") Float v) {
            assertEquals(3.14159265f, v.floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGet(@MatrixParam("double") @DefaultValue("0.0") Double v) {
            assertEquals(3.14159265358979d, v.doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveList {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@MatrixParam("boolean") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            assertEquals(true, v.get(1).booleanValue());
            assertEquals(true, v.get(2).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@MatrixParam("byte") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            assertEquals(127, v.get(1).byteValue());
            assertEquals(127, v.get(2).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@MatrixParam("short") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            assertEquals(32767, v.get(1).shortValue());
            assertEquals(32767, v.get(2).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@MatrixParam("int") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            assertEquals(2147483647, v.get(1).intValue());
            assertEquals(2147483647, v.get(2).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@MatrixParam("long") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            assertEquals(9223372036854775807L, v.get(1).longValue());
            assertEquals(9223372036854775807L, v.get(2).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@MatrixParam("float") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            assertEquals(3.14159265f, v.get(1).floatValue());
            assertEquals(3.14159265f, v.get(2).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@MatrixParam("double") List<Double> v) {
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
        public String doGetBoolean(@MatrixParam("boolean") List<Boolean> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@MatrixParam("byte") List<Byte> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@MatrixParam("short") List<Short> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@MatrixParam("int") List<Integer> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@MatrixParam("long") List<Long> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@MatrixParam("float") List<Float> v) {
            assertEquals(null, v);
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@MatrixParam("double") List<Double> v) {
            assertEquals(null, v);
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveListDefault {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@MatrixParam("boolean") @DefaultValue("true") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@MatrixParam("byte") @DefaultValue("127") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@MatrixParam("short") @DefaultValue("32767") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@MatrixParam("int") @DefaultValue("2147483647") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@MatrixParam("long") @DefaultValue("9223372036854775807") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@MatrixParam("float") @DefaultValue("3.14159265") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@MatrixParam("double") @DefaultValue("3.14159265358979") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            return "content";
        }        
    }
    
    @UriTemplate("/")
    public static class ResourceQueryPrimitiveListDefaultOverride {
        @HttpMethod("GET")
        @ProduceMime("application/boolean")
        public String doGetBoolean(@MatrixParam("boolean") @DefaultValue("false") List<Boolean> v) {
            assertEquals(true, v.get(0).booleanValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/byte")
        public String doGetByte(@MatrixParam("byte") @DefaultValue("0") List<Byte> v) {
            assertEquals(127, v.get(0).byteValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/short")
        public String doGetShort(@MatrixParam("short") @DefaultValue("0") List<Short> v) {
            assertEquals(32767, v.get(0).shortValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/int")
        public String doGetInteger(@MatrixParam("int") @DefaultValue("0") List<Integer> v) {
            assertEquals(2147483647, v.get(0).intValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/long")
        public String doGetLong(@MatrixParam("long") @DefaultValue("0") List<Long> v) {
            assertEquals(9223372036854775807L, v.get(0).longValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/float")
        public String doGetFloat(@MatrixParam("float") @DefaultValue("0.0") List<Float> v) {
            assertEquals(3.14159265f, v.get(0).floatValue());
            return "content";
        }        
        
        @HttpMethod("GET")
        @ProduceMime("application/double")
        public String doGetDouble(@MatrixParam("double") @DefaultValue("0.0") List<Double> v) {
            assertEquals(3.14159265358979d, v.get(0).doubleValue());
            return "content";
        }        
    }
    
    
    public void testGetBoolean() {
        callGet(ResourceQueryPrimitives.class, 
                "/;boolean=true", "application/boolean");
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/;boolean=true", "application/boolean");
        callGet(ResourceQueryPrimitiveList.class, 
                "/;boolean=true;boolean=true;boolean=true", "application/boolean");
    }
    
    public void testGetBooleanPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class,
                "/", "application/boolean");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/boolean");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/;boolean=true", "application/boolean");
    }
    
    public void testGetBooleanPrimitiveWrapperDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class,
                "/", "application/boolean");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/boolean");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/;boolean=true", "application/boolean");
    }
    
    public void testGetBooleanPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class,
                "/", "application/boolean");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/boolean");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/;boolean=true", "application/boolean");
    }    
    
    public void testGetByte() {
        callGet(ResourceQueryPrimitives.class, 
                "/;byte=127", "application/byte");
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/;byte=127", "application/byte");
        callGet(ResourceQueryPrimitiveList.class, 
                "/;byte=127;byte=127;byte=127", "application/byte");
    }
    
    public void testGetBytePrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/;byte=127", "application/byte");
    }
    
    public void testGetBytePrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/;byte=127", "application/byte");
    }
    
    public void testGetBytePrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/byte");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/;byte=127", "application/byte");
    }    
    
    public void testGetShort() {
        callGet(ResourceQueryPrimitives.class,
                "/;short=32767", "application/short");
        callGet(ResourceQueryPrimitiveWrappers.class,
                "/;short=32767", "application/short");
        callGet(ResourceQueryPrimitiveList.class,
                "/;short=32767;short=32767;short=32767", "application/short");
    }
    
    public void testGetShortPrimtivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/;short=32767", "application/short");
    }
    
    public void testGetShortPrimtiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/;short=32767", "application/short");
    }
    
    public void testGetShortPrimtiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/short");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/;short=32767", "application/short");
    }    
    
    public void testGetInt() {
        callGet(ResourceQueryPrimitives.class,
                "/;int=2147483647", "application/int");
        callGet(ResourceQueryPrimitiveWrappers.class,
                "/;int=2147483647", "application/int");
        callGet(ResourceQueryPrimitiveList.class,
                "/;int=2147483647;int=2147483647;int=2147483647", "application/int");
    }
    
    public void testGetIntPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/;int=2147483647", "application/int");
    }
    
    public void testGetIntPrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/;int=2147483647", "application/int");
    }
    
    public void testGetIntPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/int");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/;int=2147483647", "application/int");
    }    
    
    public void testGetLong() {
        callGet(ResourceQueryPrimitives.class, 
                "/;long=9223372036854775807", "application/long");
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/;long=9223372036854775807", "application/long");
        callGet(ResourceQueryPrimitiveList.class, 
                "/;long=9223372036854775807;long=9223372036854775807;long=9223372036854775807", "application/long");
    }
    
    public void testGetLongPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/;long=9223372036854775807", "application/long");
    }
    
    public void testGetLongPrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/;long=9223372036854775807", "application/long");
    }
    
    public void testGetLongPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/long");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/;long=9223372036854775807", "application/long");
    }    
    
    public void testGetFloat() {
        callGet(ResourceQueryPrimitives.class, 
                "/;float=3.14159265", "application/float");
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/;float=3.14159265", "application/float");
        callGet(ResourceQueryPrimitiveList.class, 
                "/;float=3.14159265;float=3.14159265;float=3.14159265", "application/float");
    }
    
    public void testGetFloatPrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/;float=3.14159265", "application/float");
    }
    
    public void testGetFloatPrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/;float=3.14159265", "application/float");
    }
    
    public void testGetFloatPrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/float");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/;float=3.14159265", "application/float");
    }    
    
    public void testGetDouble() {
        callGet(ResourceQueryPrimitives.class, 
                "/;double=3.14159265358979", "application/double");
        callGet(ResourceQueryPrimitiveWrappers.class, 
                "/;double=3.14159265358979", "application/double");
        callGet(ResourceQueryPrimitiveList.class, 
                "/;double=3.14159265358979;double=3.14159265358979;double=3.14159265358979", "application/double");
    }
    
    public void testGetDoublePrimitivesDefault() {
        callGet(ResourceQueryPrimitivesNullDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitivesDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitivesDefaultOverride.class, 
                "/;double=3.14159265358979", "application/double");
    }
    
    public void testGetDoublePrimitiveWrappersDefault() {
        callGet(ResourceQueryPrimitiveWrappersNullDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitiveWrappersDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitiveWrappersDefaultOverride.class, 
                "/;double=3.14159265358979", "application/double");
    }
    
    public void testGetDoublePrimitiveListDefault() {
        callGet(ResourceQueryPrimitiveListNullDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitiveListDefault.class, 
                "/", "application/double");
        callGet(ResourceQueryPrimitiveListDefaultOverride.class, 
                "/;double=3.14159265358979", "application/double");
    }
    
    public void testBadPrimitiveValue() {
        HttpResponseContext response = callNoStatusCheck(ResourceQueryPrimitives.class, "GET",
                "/;int=abcdef", null, "application/int", "");
        assertEquals(400, response.getResponse().getStatus());
    }
    
    public void testBadPrimitiveWrapperValue() {
        HttpResponseContext response = callNoStatusCheck(ResourceQueryPrimitiveWrappers.class, "GET",
                "/;int=abcdef", null, "application/int", "");
        assertEquals(400, response.getResponse().getStatus());
    }
    
    public void testBadPrimitiveListValue() {
        HttpResponseContext response = callNoStatusCheck(ResourceQueryPrimitiveWrappers.class, "GET",
                "/;int=abcdef;int=abcdef", null, "application/int", "");
        assertEquals(400, response.getResponse().getStatus());
    }
}
