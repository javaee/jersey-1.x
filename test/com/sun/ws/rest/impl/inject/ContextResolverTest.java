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

package com.sun.ws.rest.impl.inject;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.api.client.ResourceProxy;
import com.sun.ws.rest.spi.service.ContextResolver;
import java.io.IOException;
import java.math.BigInteger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContextResolverTest extends AbstractResourceTester {
    
    public ContextResolverTest(String testName) {
        super(testName);
    }

    @Provider
    public static class IntegerContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> objectType) {
            if (Integer.class == objectType)
                return objectType.getName();
            else 
                return null;
        }
        
    }
    
    @Provider
    public static class BigIntegerContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> objectType) {
            if (BigInteger.class == objectType)
                return objectType.getName();
            else 
                return null;
        }
        
    }
    
    @Path("/")
    public static class NullContextResource {
        @HttpContext ContextResolver<String> cr;
        
        @GET
        public String get() {
            return (cr == null) ? "null" : "value";
        }   
    }
    
    @Path("/")
    public static class ContextResource {
        
        @HttpContext ContextResolver<String> cr;
        
        @GET
        public String get() {
            return cr.getContext(Integer.class);
        }        
        
        @GET @Path("big")
        public String getBig() {
            return cr.getContext(BigInteger.class);
        }        
        
        @GET @Path("null")
        public String getNull() {
            String s = cr.getContext(Float.class);
            return (s != null) ? s : "null";
        }        
    }
    
    public void testZero() throws IOException {
        initiateWebApplication(NullContextResource.class);
        ResourceProxy r = resourceProxy("/");
        
        assertEquals("null", resourceProxy("/").get(String.class));
    }
    
    public void testOne() throws IOException {
        initiateWebApplication(ContextResource.class, IntegerContextResolver.class);
        
        assertEquals("java.lang.Integer", resourceProxy("/").get(String.class));        
        
        assertEquals("null", resourceProxy("/null").get(String.class));        
    }   
    
    public void testTwo() throws IOException {
        initiateWebApplication(ContextResource.class, IntegerContextResolver.class, 
                BigIntegerContextResolver.class);
        
        assertEquals("java.lang.Integer", resourceProxy("/").get(String.class));        
        
        assertEquals("java.math.BigInteger", resourceProxy("/big").get(String.class));        
        
        assertEquals("null", resourceProxy("/null").get(String.class));        
    }   
}
