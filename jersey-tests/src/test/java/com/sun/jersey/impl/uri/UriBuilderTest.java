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
package com.sun.jersey.impl.uri;

import com.sun.jersey.api.uri.UriComponent;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriBuilderTest extends TestCase {

    public UriBuilderTest(String testName) {
        super(testName);
    }

    public void testOpaqueUri() {
        URI bu = UriBuilder.fromUri("mailto:a@b").build();
        assertEquals(URI.create("mailto:a@b"), bu);
    }

    public void testOpaqueUriReplaceSchemeSpecificPart() {
        URI bu = UriBuilder.fromUri("mailto:a@b").schemeSpecificPart("c@d").build();
        assertEquals(URI.create("mailto:c@d"), bu);
    }

    public void testReplaceScheme() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                scheme("https").build();
        assertEquals(URI.create("https://localhost:8080/a/b/c"), bu);
    }

    public void testReplaceSchemeSpecificPart() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                schemeSpecificPart("//localhost:8080/a/b/c/d").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/d"), bu);
    }

    public void testReplaceUserInfo() {
        URI bu = UriBuilder.fromUri("http://bob@localhost:8080/a/b/c").
                userInfo("sue").build();
        assertEquals(URI.create("http://sue@localhost:8080/a/b/c"), bu);
    }

    public void testReplaceHost() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                host("a.com").build();
        assertEquals(URI.create("http://a.com:8080/a/b/c"), bu);
    }

    public void testReplacePort() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                port(9090).build();
        assertEquals(URI.create("http://localhost:9090/a/b/c"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                port(-1).build();
        assertEquals(URI.create("http://localhost/a/b/c"), bu);
    }

    public void testReplacePath() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                replacePath("/x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/x/y/z"), bu);
    }

    public void testReplaceMatrix() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").
                replaceMatrix("x=a;y=b").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), bu);
    }

    public void testReplaceMatrixParams() {
        UriBuilder ubu = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").
                replaceMatrixParam("a", "z", "zz");

        {
        URI bu = ubu.build();
        List<PathSegment> ps = UriComponent.decodePath(bu, true);
        MultivaluedMap<String, String> mps = ps.get(2).getMatrixParameters();
        List<String> a = mps.get("a");
        assertEquals(2, a.size());
        assertEquals("z", a.get(0));
        assertEquals("zz", a.get(1));
        List<String> b = mps.get("b");
        assertEquals(1, b.size());
        assertEquals("y", b.get(0));
        }

        {
        URI bu = ubu.replaceMatrixParam("a", "_z_", "_zz_").build();
        List<PathSegment> ps = UriComponent.decodePath(bu, true);
        MultivaluedMap<String, String> mps = ps.get(2).getMatrixParameters();
        List<String> a = mps.get("a");
        assertEquals(2, a.size());
        assertEquals("_z_", a.get(0));
        assertEquals("_zz_", a.get(1));
        List<String> b = mps.get("b");
        assertEquals(1, b.size());
        assertEquals("y", b.get(0));
        }

        {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").
                replaceMatrixParam("a", "z", "zz").matrixParam("c", "c").
                path("d").build();

        List<PathSegment> ps = UriComponent.decodePath(bu, true);
        MultivaluedMap<String, String> mps = ps.get(2).getMatrixParameters();
        List<String> a = mps.get("a");
        assertEquals(2, a.size());
        assertEquals("z", a.get(0));
        assertEquals("zz", a.get(1));
        List<String> b = mps.get("b");
        assertEquals(1, b.size());
        assertEquals("y", b.get(0));
        List<String> c = mps.get("c");
        assertEquals(1, c.size());
        assertEquals("c", c.get(0));
        }
    }

    public void testReplaceMatrixParamsEmpty() {
        UriBuilder ubu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                replaceMatrixParam("a", "z", "zz");
        {
        URI bu = ubu.build();
        List<PathSegment> ps = UriComponent.decodePath(bu, true);
        MultivaluedMap<String, String> mps = ps.get(2).getMatrixParameters();
        List<String> a = mps.get("a");
        assertEquals(2, a.size());
        assertEquals("z", a.get(0));
        assertEquals("zz", a.get(1));
        }
    }

    public void testReplaceQuery() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").
                replaceQuery("x=a&y=b").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&y=b"), bu);
    }

    public void testBuildEncodedQuery() {
        URI u = UriBuilder.fromPath("").
                queryParam("y", "1 %2B 2").build();
        assertEquals(URI.create("?y=1+%2B+2"), u);
    }

    public void testReplaceQueryParams() {
        UriBuilder ubu = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").
                replaceQueryParam("a", "z", "zz").queryParam("c", "c");

        {
        URI bu = ubu.build();

        MultivaluedMap<String, String> qps = UriComponent.decodeQuery(bu, true);
        List<String> a = qps.get("a");
        assertEquals(2, a.size());
        assertEquals("z", a.get(0));
        assertEquals("zz", a.get(1));
        List<String> b = qps.get("b");
        assertEquals(1, b.size());
        assertEquals("y", b.get(0));
        List<String> c = qps.get("c");
        assertEquals(1, c.size());
        assertEquals("c", c.get(0));
        }

        {
        URI bu = ubu.replaceQueryParam("a", "_z_", "_zz_").build();

        MultivaluedMap<String, String> qps = UriComponent.decodeQuery(bu, true);
        List<String> a = qps.get("a");
        assertEquals(2, a.size());
        assertEquals("_z_", a.get(0));
        assertEquals("_zz_", a.get(1));
        List<String> b = qps.get("b");
        assertEquals(1, b.size());
        assertEquals("y", b.get(0));
        List<String> c = qps.get("c");
        assertEquals(1, c.size());
        assertEquals("c", c.get(0));
        }
    }

    public void testReplaceQueryParamsEmpty() {
        UriBuilder ubu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                replaceQueryParam("a", "z", "zz").queryParam("c", "c");

        {
        URI bu = ubu.build();

        MultivaluedMap<String, String> qps = UriComponent.decodeQuery(bu, true);
        List<String> a = qps.get("a");
        assertEquals(2, a.size());
        assertEquals("z", a.get(0));
        assertEquals("zz", a.get(1));
        List<String> c = qps.get("c");
        assertEquals(1, c.size());
        assertEquals("c", c.get(0));
        }
    }

    public void testReplaceFragment() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y#frag").
                fragment("ment").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y#ment"), bu);
    }

    public void testReplaceUri() {
        URI u = URI.create("http://bob@localhost:8080/a/b/c?a=x&b=y#frag");

        URI bu = UriBuilder.fromUri(u).
                uri(URI.create("https://bob@localhost:8080")).build();
        assertEquals(URI.create("https://bob@localhost:8080/a/b/c?a=x&b=y#frag"), bu);

        bu = UriBuilder.fromUri(u).
                uri(URI.create("https://sue@localhost:8080")).build();
        assertEquals(URI.create("https://sue@localhost:8080/a/b/c?a=x&b=y#frag"), bu);

        bu = UriBuilder.fromUri(u).
                uri(URI.create("https://sue@localhost:9090")).build();
        assertEquals(URI.create("https://sue@localhost:9090/a/b/c?a=x&b=y#frag"), bu);

        bu = UriBuilder.fromUri(u).
                uri(URI.create("/x/y/z")).build();
        assertEquals(URI.create("http://bob@localhost:8080/x/y/z?a=x&b=y#frag"), bu);

        bu = UriBuilder.fromUri(u).
                uri(URI.create("?x=a&b=y")).build();
        assertEquals(URI.create("http://bob@localhost:8080/a/b/c?x=a&b=y#frag"), bu);

        bu = UriBuilder.fromUri(u).
                uri(URI.create("#ment")).build();
        assertEquals(URI.create("http://bob@localhost:8080/a/b/c?a=x&b=y#ment"), bu);
    }

    public void testSchemeSpecificPart() {
        URI u = URI.create("http://bob@localhost:8080/a/b/c?a=x&b=y#frag");

        URI bu = UriBuilder.fromUri(u).
                schemeSpecificPart("//sue@remotehost:9090/x/y/z?x=a&y=b").build();
        assertEquals(URI.create("http://sue@remotehost:9090/x/y/z?x=a&y=b#frag"), bu);
    }

    public void testAppendPath() {
        URI bu = UriBuilder.fromUri("http://localhost:8080").
                path("a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/").
                path("a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080").
                path("/a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c/").
                path("/").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c/").
                path("/x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("/x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("x/y/z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("/").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c/"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a%20/b%20/c%20").
                path("/x /y /z ").build();
        assertEquals(URI.create("http://localhost:8080/a%20/b%20/c%20/x%20/y%20/z%20"), bu);
    }

    public void testAppendSegment() {
        URI bu = UriBuilder.fromUri("http://localhost:8080").
                segment("a/b/c").build();
        assertEquals(URI.create("http://localhost:8080/a%2Fb%2Fc"), bu);
    }

    public void testRelativefromUri() {
        URI bu = UriBuilder.fromUri("a/b/c").
            build();
        assertEquals(URI.create("a/b/c"), bu);

        bu = UriBuilder.fromUri("a/b/c").path("d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromUri("a/b/c/").path("d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromUri("a/b/c").path("/d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromUri("a/b/c/").path("/d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromUri("").queryParam("x", "y").
            build();
        assertEquals(URI.create("?x=y"), bu);

    }

    public void testRelativefromPath() {
        URI bu = UriBuilder.fromPath("a/b/c").
            build();
        assertEquals(URI.create("a/b/c"), bu);

        bu = UriBuilder.fromPath("a/b/c").path("d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromPath("a/b/c/").path("d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromPath("a/b/c").path("/d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromPath("a/b/c/").path("/d").
            build();
        assertEquals(URI.create("a/b/c/d"), bu);

        bu = UriBuilder.fromPath("").queryParam("x", "y").
            build();
        assertEquals(URI.create("?x=y"), bu);
    }

    public void testAppendQueryParams() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").
                queryParam("c", "z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y&c=z"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").
                queryParam("c= ", "z= ").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y&c%3D+=z%3D+"), bu);

    }

    public void testAppendMatrixParams() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").
                matrixParam("c", "z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;a=x;b=y;c=z"), bu);

        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").
                matrixParam("c=/ ", "z=/ ").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;a=x;b=y;c%3D%2F%20=z%3D%2F%20"), bu);
    }

    public void testAppendPathAndMatrixParams() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/").
                path("a").matrixParam("x", "foo").matrixParam("y", "bar").
                path("b").matrixParam("x", "foo").matrixParam("y", "bar").build();
        assertEquals(URI.create("http://localhost:8080/a;x=foo;y=bar/b;x=foo;y=bar"), bu);
    }

    @Path("resource")
    class Resource {
        @Path("method")
        public @GET String get() { return ""; }

        @Path("locator")
        public Object locator() { return null; }
    }

    public void testResourceAppendPath() throws NoSuchMethodException {
        URI ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(Resource.class).build();
        assertEquals(URI.create("http://localhost:8080/base/resource"), ub);

        ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(Resource.class, "get").build();
        assertEquals(URI.create("http://localhost:8080/base/method"), ub);

        Method get = Resource.class.getMethod("get");
        Method locator = Resource.class.getMethod("locator");
        ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(get).path(locator).build();
        assertEquals(URI.create("http://localhost:8080/base/method/locator"), ub);
    }

    @Path("resource/{id}")
    class ResourceWithTemplate {
        @Path("method/{id1}")
        public @GET String get() { return ""; }

        @Path("locator/{id2}")
        public Object locator() { return null; }
    }

    public void testResourceWithTemplateAppendPath() throws NoSuchMethodException {
        URI ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(ResourceWithTemplate.class).build("foo");
        assertEquals(URI.create("http://localhost:8080/base/resource/foo"), ub);

        ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(ResourceWithTemplate.class, "get").build("foo");
        assertEquals(URI.create("http://localhost:8080/base/method/foo"), ub);

        Method get = ResourceWithTemplate.class.getMethod("get");
        Method locator = ResourceWithTemplate.class.getMethod("locator");
        ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(get).path(locator).build("foo", "bar");
        assertEquals(URI.create("http://localhost:8080/base/method/foo/locator/bar"), ub);
    }

    @Path("resource/{id: .+}")
    class ResourceWithTemplateRegex {
        @Path("method/{id1: .+}")
        public @GET String get() { return ""; }

        @Path("locator/{id2: .+}")
        public Object locator() { return null; }
    }

    public void testResourceWithTemplateRegexAppendPath() throws NoSuchMethodException {
        URI ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(ResourceWithTemplateRegex.class).build("foo");
        assertEquals(URI.create("http://localhost:8080/base/resource/foo"), ub);

        ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(ResourceWithTemplateRegex.class, "get").build("foo");
        assertEquals(URI.create("http://localhost:8080/base/method/foo"), ub);

        Method get = ResourceWithTemplateRegex.class.getMethod("get");
        Method locator = ResourceWithTemplateRegex.class.getMethod("locator");
        ub = UriBuilder.fromUri("http://localhost:8080/base").
                path(get).path(locator).build("foo", "bar");
        assertEquals(URI.create("http://localhost:8080/base/method/foo/locator/bar"), ub);
    }

    public void testBuildTemplates() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("/{foo}/{bar}/{baz}/{foo}").build("x", "y", "z");
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x"), bu);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "x");
        m.put("bar", "y");
        m.put("baz", "z");
        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("/{foo}/{bar}/{baz}/{foo}").buildFromMap(m);
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x"), bu);
    }

    public void testBuildQueryTemplates() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                queryParam("a", "{b}").build("=+&%xx%20");
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%2520"), bu);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("b", "=+&%xx%20");
        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                queryParam("a", "{b}").buildFromMap(m);
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%2520"), bu);
    }

    public void testBuildFromEncodedQueryTemplates() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                queryParam("a", "{b}").buildFromEncoded("=+&%xx%20");
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%20"), bu);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("b", "=+&%xx%20");
        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                queryParam("a", "{b}").buildFromEncodedMap(m);
        assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%20"), bu);
    }

    public void testTemplatesDefaultPort() {
        URI bu = UriBuilder.fromUri("http://localhost/a/b/c").
                path("/{foo}/{bar}/{baz}/{foo}").build("x", "y", "z");
        assertEquals(URI.create("http://localhost/a/b/c/x/y/z/x"), bu);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "x");
        m.put("bar", "y");
        m.put("baz", "z");
        bu = UriBuilder.fromUri("http://localhost/a/b/c").
                path("/{foo}/{bar}/{baz}/{foo}").buildFromMap(m);
        assertEquals(URI.create("http://localhost/a/b/c/x/y/z/x"), bu);
    }

    public void testClone() {
        UriBuilder ub = UriBuilder.fromUri("http://user@localhost:8080/?query#fragment").path("a");
        URI full = ub.clone().path("b").build();
        URI base = ub.build();

        assertEquals(URI.create("http://user@localhost:8080/a?query#fragment"), base);
        assertEquals(URI.create("http://user@localhost:8080/a/b?query#fragment"), full);
    }

    public void testIllegalArgumentException() {
        boolean caught = false;
        try {
            UriBuilder.fromPath(null);
        } catch(IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);

        caught = false;
        try {
            UriBuilder.fromUri((URI)null);
        } catch(IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);

        caught = false;
        try {
            UriBuilder.fromUri((String)null);
        } catch(IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);
    }
}
