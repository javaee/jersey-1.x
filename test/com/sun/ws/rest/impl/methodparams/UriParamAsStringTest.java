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
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class UriParamAsStringTest extends AbstractResourceTester {

    public UriParamAsStringTest(String testName) {
        super(testName);
    }

    @Path("/{arg1}/{arg2}/{arg3}")
    public static class Resource {
        @GET
        public String doGet(@PathParam("arg1") String arg1, 
                @PathParam("arg2") String arg2, @PathParam("arg3") String arg3) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            return "content";
        }
        
        @POST
        public String doPost(@PathParam("arg1") String arg1, 
                @PathParam("arg2") String arg2, @PathParam("arg3") String arg3,
                String r) {
            assertEquals("a", arg1);
            assertEquals("b", arg2);
            assertEquals("c", arg3);
            assertEquals("content", r);
            return "content";
        }
    }
    
    public void testStringArgsGet() {
        initiateWebApplication(Resource.class);
        resourceProxy("/a/b/c").
                get(String.class);
    }
    
    public void testStringArgsPost() {
        initiateWebApplication(Resource.class);
        String s = resourceProxy("/a/b/c").
                post(String.class, "content");

        assertEquals("content", s);
    }
    
    @Path("/{id}")
    public static class Duplicate {
        @GET
        public String get(@PathParam("id") String id) {
            return id;
        }
        
        @GET
        @Path("/{id}")
        public String getSub(@PathParam("id") String id) {
            return id;
        }
    }
    
    public void testDuplicate() {
        initiateWebApplication(Duplicate.class);
        
        assertEquals("foo", resourceProxy("/foo").get(String.class));
        assertEquals("bar", resourceProxy("/foo/bar").get(String.class));
    }
    
    @Path("/{id}")
    public static class DuplicateList {
        @GET
        public String get(@PathParam("id") String id) {
            return id;
        }
        
        @GET
        @Path("/{id}")
        public String getSub(@PathParam("id") List<String> id) {
            assertEquals(2, id.size());
            return id.get(0) + id.get(1);
        }
    }
    
    public void testDuplicateList() {
        initiateWebApplication(DuplicateList.class);
        
        assertEquals("foo", resourceProxy("/foo").get(String.class));
        assertEquals("barfoo", resourceProxy("/foo/bar").get(String.class));
    }
}
