/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl.uri.conneg;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.UriConnegFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Produces;
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
        @Produces("application/foo")
        public String doGetFoo() {
            return "foo";
        }
        
        @GET
        @Produces("application/foot")
        public String doGetFoot() {
            return "foot";
        }

        @GET
        @Produces("application/bar")
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
    
    public void testSingleSegmentWithFilter() throws IOException {
        _initWithFilter(SingleSegment.class, false);

        _test("/", "abc");
    }

    public void testSingleSegmentWithFilterOverrideResourceConfig() throws IOException {
        _initWithFilter(SingleSegment.class, true);

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
    
    @Path("/foo_bar_foot")
    public static class PathWithSuffixSegment extends Base {
    }

    public void testXXXSegment() throws IOException {
        _init(PathWithSuffixSegment.class);

        _test("/", "foo_bar_foot");
    }

    @Path("/")
    public static class SubResourceMethods {
        @Path("abc")
        @GET
        @Produces("application/foo")
        public String doGetFoo() {
            return "foo";
        }
        
        @Path("abc")
        @GET
        @Produces("application/foot")
        public String doGetFoot() {
            return "foot";
        }

        @Path("abc")
        @GET
        @Produces("application/bar")
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
        rc.getMediaTypeMappings().put("foot", MediaType.valueOf("application/foot"));
        initiateWebApplication(rc);        
    }
    
    private void _initWithFilter(Class<?> r, boolean useMappings) {
        ResourceConfig rc = new DefaultResourceConfig(r);

        Map<String, MediaType> m = new HashMap<String, MediaType>();
        m.put("foo", MediaType.valueOf("application/foo"));
        m.put("bar", MediaType.valueOf("application/bar"));
        m.put("foot", MediaType.valueOf("application/foot"));

        ContainerRequestFilter f = new UriConnegFilter(m) {};
        List<ContainerRequestFilter> lf = Collections.singletonList(f);
        rc.getProperties().put(rc.PROPERTY_CONTAINER_REQUEST_FILTERS, lf);

        if (useMappings) {
            rc.getMediaTypeMappings().putAll(m);
        }

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
        
        s = r.path(path + ".foot" + terminate).get(String.class);
        assertEquals("foot", s);

        s = r.path(path + ".bar" + terminate).get(String.class);
        assertEquals("bar", s);  
        
        s = r.path(path + terminate).accept("application/foo").get(String.class);
        assertEquals("foo", s);
        
        s = r.path(path + terminate).accept("application/foot").get(String.class);
        assertEquals("foot", s);

        s = r.path(path + terminate).accept("application/foo;q=0.1").get(String.class);
        assertEquals("foo", s);        
    }
}