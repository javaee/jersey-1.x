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
package com.sun.ws.rest.impl;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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

    public void testReplaceScheme() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                scheme("https").build();
        assertEquals(URI.create("https://localhost:8080/a/b/c"), bu);
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

    public void testReplaceMatrixParam() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").
                replaceMatrixParams("x=a;y=b").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), bu);
    }

    public void testReplaceQueryParams() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").
                replaceQueryParams("x=a&y=b").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&y=b"), bu);
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
    }

    public void testAppendMatrixParams() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").
                matrixParam("c", "z").build();
        assertEquals(URI.create("http://localhost:8080/a/b/c;a=x;b=y;c=z"), bu);
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
                path(get, locator).build();    
        assertEquals(URI.create("http://localhost:8080/base/method/locator"), ub);
    }
    
    public void testTemplates() {
        URI bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("/{foo}/{bar}/{baz}/{foo}").build("x", "y", "z");
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x"), bu);

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("foo", "x");
        m.put("bar", "y");
        m.put("baz", "z");
        bu = UriBuilder.fromUri("http://localhost:8080/a/b/c").
                path("/{foo}/{bar}/{baz}/{foo}").build(m);
        assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x"), bu);
    }

    public void testClone() {
        UriBuilder ub = UriBuilder.fromUri("http://user@localhost:8080/?query#fragment").path("a");
        URI full = ub.clone().path("b").build();
        URI base = ub.build();

        assertEquals(URI.create("http://user@localhost:8080/a?query#fragment"), base);
        assertEquals(URI.create("http://user@localhost:8080/a/b?query#fragment"), full);
    }
}
