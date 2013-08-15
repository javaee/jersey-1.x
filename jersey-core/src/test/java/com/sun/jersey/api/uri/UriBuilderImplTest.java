/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.api.uri;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import javax.ws.rs.core.UriBuilder;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author Martin Matula
 */
public class UriBuilderImplTest {

    public UriBuilderImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    // Reproducer for JERSEY-2036
    @Test
    public void testReplaceNonAsciiQueryParam()
            throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        URL url = new URL("http://example.com/getMyName?néme=t");
        String query = url.getQuery();

        UriBuilder builder = new UriBuilderImpl().path(url.getPath())
                .scheme(url.getProtocol())
                .host(url.getHost())
                .port(url.getPort())
                .replaceQuery(query)
                .fragment(url.getRef());

        // Replace QueryParam.
        String parmName = "néme";
        String value = "value";

        builder.replaceQueryParam(parmName, value);

        final URI result = builder.build();
        final URI expected = new URI("http://example.com/getMyName?néme=value");
        assertEquals(expected.toASCIIString(), result.toASCIIString());
    }

    @Test
    public void testReplaceMatrixParamWithNull() {
        UriBuilder builder = new UriBuilderImpl().matrixParam("matrix", "param1", "param2");
        builder.replaceMatrixParam("matrix", (Object[]) null);
        assertEquals(builder.build().toString(), "");
    }

    @Test
    public void testReplaceNullMatrixParam() {
        try {
            new UriBuilderImpl().replaceMatrixParam(null, "param");
        } catch (IllegalArgumentException e) {
            return;
        } catch (Exception e) {
            fail("Expected IllegalArgumentException but got " + e.toString());
        }
        fail("Expected IllegalArgumentException but no exception was thrown.");
    }

    // for completeness (added along with regression tests for JERSEY-1114)
    @Test
    public void testBuildNoSlashUri() {
        UriBuilder builder = new UriBuilderImpl().uri(URI.create("http://localhost:8080")).path("test");
        assertEquals("http://localhost:8080/test", builder.build().toString());
    }

    // regression test for JERSEY-1114
    @Test
    public void testBuildFromMapNoSlashInUri() {
        UriBuilder builder = new UriBuilderImpl().uri(URI.create("http://localhost:8080")).path("test");
        assertEquals("http://localhost:8080/test", builder.buildFromMap(new HashMap<String, Object>()).toString());
    }

    // regression test for JERSEY-1114
    @Test
    public void testBuildFromArrayNoSlashInUri() {
        UriBuilder builder = new UriBuilderImpl().uri(URI.create("http://localhost:8080")).path("test");
        assertEquals("http://localhost:8080/test", builder.build("testing").toString());
    }

    // regression test for JERSEY-1081
    @Test
    public void testReplaceQueryParam() {
        URI uri = new UriBuilderImpl().path("http://localhost/").replaceQueryParam("foo", "test").build();
        assertEquals("http://localhost/?foo=test", uri.toString());
    }

    // regression test for JERSEY-1081
    @Test
    public void testReplaceQueryParamAndClone() {
        URI uri = new UriBuilderImpl().path("http://localhost/").replaceQueryParam("foo", "test").clone().build();
        assertEquals("http://localhost/?foo=test", uri.toString());
    }

    // regression test for JERSEY-1341
    @Test
    public void testEmptyQueryParamValue() {
        URI uri = new UriBuilderImpl().path("http://localhost/").queryParam("test", "").build();
        assertEquals("http://localhost/?test=", uri.toString());
    }
}
