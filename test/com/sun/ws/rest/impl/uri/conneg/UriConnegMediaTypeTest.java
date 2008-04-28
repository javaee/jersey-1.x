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

package com.sun.ws.rest.impl.uri.conneg;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.ws.rest.impl.AbstractResourceTester;
import java.io.IOException;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriConnegMediaTypeTest extends AbstractResourceTester {
    
    public UriConnegMediaTypeTest(String testName) {
        super(testName);
        
    }

    public static abstract class Base {
        @GET
        @ProduceMime("application/foo")
        public String doGetFoo() {
            return "foo";
        }
        
        @GET
        @ProduceMime("application/bar")
        public String doGetBar() {
            return "bar";
        }
    }
    
    @Path("")
    public static class Slash extends Base {
    }
    
    public void testSlash() throws IOException {
        _init(Slash.class);

        _test("/");
    }    
    
    @Path("/abc")
    public static class SingleSegment extends Base {
    }
    
    public void testSingleSegment() throws IOException {
        _init(SingleSegment.class);
        
        _test("/", "abc");
    }    
    
    @Path("/abc/")
    public static class SingleSegmentSlash extends Base {
    }
    
    public void testSingleSegmentSlash() throws IOException {
        _init(SingleSegmentSlash.class);
        
        _test("/", "abc", "/");
    }    
    
    @Path("/xyz/abc")
    public static class MultipleSegments extends Base {
    }
    
    public void testMultipleSegments() throws IOException {
        _init(MultipleSegments.class);
        
        _test("/xyz", "abc");
    }    
    
    @Path("/xyz/abc/")
    public static class MultipleSegmentsSlash extends Base {
    }
    
    public void testMultipleSegmentsSlash() throws IOException {
        _init(MultipleSegmentsSlash.class);
        
        _test("/xyz", "abc", "/");
    }

    @Path("/xyz/abc.xml")
    public static class DotPrefixSegments extends Base {
    }
    
    public void testDotPrefixSegments() throws IOException {
        _init(DotPrefixSegments.class);
        
        _test("/xyz", "abc.xml");
        
        _test("/xyz", "abc", ".xml");
    }    
    
    @Path("/")
    public static class SubResourceMethods {
        @Path("abc")
        @GET
        @ProduceMime("application/foo")
        public String doGetFoo() {
            return "foo";
        }
        
        @Path("abc")
        @GET
        @ProduceMime("application/bar")
        public String doGetBar() {
            return "bar";
        }
    }
    
    public void testSubResourceMethods() throws IOException {
        _init(SubResourceMethods.class);
        
        _test("/", "abc");
    }
    
    private void _init(Class<?> r) {
        ResourceConfig rc = new DefaultResourceConfig(r);
        rc.getMediaTypeMappings().put("foo", MediaType.valueOf("application/foo"));
        rc.getMediaTypeMappings().put("bar", MediaType.valueOf("application/bar"));
        initiateWebApplication(rc);        
    }
    
    private void _test(String base) {
        _test(base, "", "");
    }
    
    private void _test(String base, String path) {
        _test(base, path, "");
    }
    
    private void _test(String base, String path, String terminate) {
        WebResource r = resource(base);
        
        String s = r.path(path + ".foo" + terminate).get(String.class);
        assertEquals("foo", s);
        
        s = r.path(path + ".bar" + terminate).get(String.class);
        assertEquals("bar", s);  
        
        s = r.path(path + terminate).accept("application/foo").get(String.class);
        assertEquals("foo", s);
        
        s = r.path(path + terminate).accept("application/foo;q=0.1").get(String.class);
        assertEquals("foo", s);        
    }
}