/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.impl.uri;

import com.sun.jersey.api.uri.UriComponent;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriComponentDecodeTest extends TestCase {
    
    public UriComponentDecodeTest(String testName) {
        super(testName);
    }

    public void testNull() {
        decodeCatch(null);
    }
    
    public void testEmpty() {
        assertEquals("", UriComponent.decode("", null));
    }
    
    public void testNoPercentEscapedOctets() {
        assertEquals("xyz", UriComponent.decode("xyz", null));
    }
    
    public void testZeroValuePercentEscapedOctet() {
        assertEquals("\u0000", UriComponent.decode("%00", null));        
    }
    
    public void testASCIIPercentEscapedOctets() {
        assertEquals(" ", UriComponent.decode("%20", null));
        assertEquals("   ", UriComponent.decode("%20%20%20", null));
        assertEquals("a b c ", UriComponent.decode("a%20b%20c%20", null));
        assertEquals("a  b  c  ", UriComponent.decode("a%20%20b%20%20c%20%20", null));

        assertEquals("0123456789", UriComponent.decode("%30%31%32%33%34%35%36%37%38%39", null));
        assertEquals("00112233445566778899", UriComponent.decode("%300%311%322%333%344%355%366%377%388%399", null));
    
    }
    
    public void testPercentUnicodeEscapedOctets() {
        assertEquals("copyright\u00a9", UriComponent.decode("copyright%c2%a9", null));
        assertEquals("copyright\u00a9", UriComponent.decode("copyright%C2%A9", null));
    }
    
    public void testHost() {
        assertEquals("[fec0::abcd%251]", UriComponent.decode("[fec0::abcd%251]", 
                UriComponent.Type.HOST));        
    }
    
    public void testInvalidPercentEscapedOctets() {
        assertTrue(decodeCatch("%"));
        assertTrue(decodeCatch("%1"));
        assertTrue(decodeCatch(" %1"));
        assertTrue(decodeCatch("%z1"));
        assertTrue(decodeCatch("%1z"));
    }
    
    private boolean decodeCatch(String s) {
        try {
            UriComponent.decode(s, null);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    public void testDecodePathEmptySlash() {
        _testDecodePath("", "");
        _testDecodePath("/", "", "");
        _testDecodePath("//", "", "", "");
        _testDecodePath("///", "", "", "", "");        
    }
    
    public void testDecodePath() {
        _testDecodePath("a", "a");
        _testDecodePath("/a", "", "a");
        _testDecodePath("/a/", "", "a", "");
        _testDecodePath("/a//", "", "a", "", "");
        _testDecodePath("/a///", "", "a", "", "", "");
        
        _testDecodePath("a/b/c", "a", "b", "c");
        _testDecodePath("a//b//c//", "a", "", "b", "", "c", "", "");
        _testDecodePath("//a//b//c//", "", "", "a", "", "b", "", "c", "", "");
        _testDecodePath("///a///b///c///", "", "", "", "a", "", "", "b", "", "", "c", "", "", "");
    }
    
    private void _testDecodePath(String path, String... segments) {
        List<PathSegment> ps = UriComponent.decodePath(path, true);
        assertEquals(segments.length, ps.size());

        for (int i = 0; i < segments.length; i++) {
            assertEquals(segments[i], ps.get(i).getPath());
        }
    }
    
    public void testDecodeQuery() {
        _testDecodeQuery("");
        _testDecodeQuery("&");
        _testDecodeQuery("=");
        _testDecodeQuery("&=junk");
        _testDecodeQuery("&&");
        _testDecodeQuery("a", "a", "");
        _testDecodeQuery("a&", "a", "");
        _testDecodeQuery("&a&", "a", "");
        _testDecodeQuery("a&&", "a", "");
        _testDecodeQuery("a=", "a", "");
        _testDecodeQuery("a=&", "a", "");
        _testDecodeQuery("a=&&", "a", "");
        _testDecodeQuery("a=x", "a", "x");
        _testDecodeQuery("a==x", "a", "=x");
        _testDecodeQuery("a=x&", "a", "x");
        _testDecodeQuery("a=x&&", "a", "x");
        _testDecodeQuery("a=x&b=y", "a", "x", "b", "y");
        _testDecodeQuery("a=x&&b=y", "a", "x", "b", "y");
        
        _testDecodeQuery("+a+=+x+", true, " a ", " x ");
        _testDecodeQuery("%20a%20=%20x%20", true, " a ", " x ");
        _testDecodeQuery("+a+=+x+", false, " a ", "+x+");
        _testDecodeQuery("%20a%20=%20x%20", false, " a ", "%20x%20");        
    }
    
    public void testDecodeQueryParam() {
        assertEquals(" ",
                UriComponent.decode("+", UriComponent.Type.QUERY_PARAM));
        assertEquals("a b c ",
                UriComponent.decode("a+b+c+", UriComponent.Type.QUERY_PARAM));
    }

    private void _testDecodeQuery(String q, String... query) {
        _testDecodeQuery(q, true, query);
    }
    
    private void _testDecodeQuery(String q, boolean decode, String... query) {
        MultivaluedMap<String, String> queryParameters = UriComponent.decodeQuery(q, decode);

        assertEquals(query.length / 2, queryParameters.size());

        for (int i = 0; i < query.length; i += 2)
            assertEquals(query[i + 1], queryParameters.getFirst(query[i]));
    }
    
    public void testDecodeMatrix() {
        _testDecodeMatrix("path", "path");
        _testDecodeMatrix("path;", "path");
        _testDecodeMatrix("path;=", "path");
        _testDecodeMatrix("path;=junk", "path");
        _testDecodeMatrix("path;;", "path");
        _testDecodeMatrix("path;a", "path", "a", "");
        _testDecodeMatrix("path;;a", "path", "a", "");
        _testDecodeMatrix("path;a;", "path", "a", "");
        _testDecodeMatrix("path;a;;", "path", "a", "");
        _testDecodeMatrix("path;a=", "path", "a", "");
        _testDecodeMatrix("path;a=;", "path", "a", "");
        _testDecodeMatrix("path;a=;;", "path", "a", "");
        _testDecodeMatrix("path;a=x", "path", "a", "x");
        _testDecodeMatrix("path;a=x;", "path", "a", "x");
        _testDecodeMatrix("path;a=x;;", "path", "a", "x");
        _testDecodeMatrix("path;a=x;b=y", "path", "a", "x", "b", "y");
        _testDecodeMatrix("path;a=x;;b=y", "path", "a", "x", "b", "y");
        _testDecodeMatrix("path;a==x;", "path", "a", "=x");
        
        _testDecodeMatrix("", "");
        _testDecodeMatrix(";", "");
        _testDecodeMatrix(";=", "");
        _testDecodeMatrix(";=junk", "");
        _testDecodeMatrix(";;", "");
        _testDecodeMatrix(";a", "", "a", "");
        _testDecodeMatrix(";;a", "", "a", "");
        _testDecodeMatrix(";a;", "", "a", "");
        _testDecodeMatrix(";a;;", "", "a", "");
        _testDecodeMatrix(";a=", "", "a", "");
        _testDecodeMatrix(";a=;", "", "a", "");
        _testDecodeMatrix(";a=;;", "", "a", "");
        _testDecodeMatrix(";a=x", "", "a", "x");
        _testDecodeMatrix(";a==x", "", "a", "=x");
        _testDecodeMatrix(";a=x;", "", "a", "x");
        _testDecodeMatrix(";a=x;;", "", "a", "x");
        _testDecodeMatrix(";a=x;b=y", "", "a", "x", "b", "y");
        _testDecodeMatrix(";a=x;;b=y", "", "a", "x", "b", "y");
        
        _testDecodeMatrix(";%20a%20=%20x%20", "", true, " a ", " x ");
        _testDecodeMatrix(";%20a%20=%20x%20", "", false, " a ", "%20x%20");
        
    }
    
    private void _testDecodeMatrix(String path, String pathSegment, String... matrix) {
        _testDecodeMatrix(path, pathSegment, true, matrix);
    }
    
    private void _testDecodeMatrix(String path, String pathSegment, boolean decode, String... matrix) {
        List<PathSegment> ps = UriComponent.decodePath(path, decode);
        MultivaluedMap<String, String> matrixParameters = ps.get(0).getMatrixParameters();

        assertEquals(pathSegment, ps.get(0).getPath());
        assertEquals(matrix.length / 2, matrixParameters.size());

        for (int i = 0; i < matrix.length; i += 2)
            assertEquals(matrix[i + 1], matrixParameters.getFirst(matrix[i]));
    }
}