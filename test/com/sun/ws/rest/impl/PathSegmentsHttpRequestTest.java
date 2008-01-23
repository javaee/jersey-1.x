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

import com.sun.ws.rest.api.core.HttpRequestContext;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PathSegmentsHttpRequestTest extends TestCase {
    
    public PathSegmentsHttpRequestTest(String testName) {
        super(testName);
    }
    
    public void testGeneral() throws Exception {
        HttpRequestContext r = new TestHttpRequestContext(null, "GET", null,
                "/context/p1;x=1;y=1/p2;x=2;y=2/p3;x=3;y=3", "/context");
        List<PathSegment> segments = r.getPathSegments();
        
        assertEquals(3, segments.size());
        
        PathSegment segment = segments.get(0);
        assertEquals("p1", segment.getPath());
        MultivaluedMap<String, String> m = segment.getMatrixParameters();        
        assertEquals("1", m.getFirst("x"));
        assertEquals("1", m.getFirst("y"));
        
        segment = segments.get(1);
        assertEquals("p2", segment.getPath());
        m = segment.getMatrixParameters();        
        assertEquals("2", m.getFirst("x"));
        assertEquals("2", m.getFirst("y"));
        
        segment = segments.get(2);
        assertEquals("p3", segment.getPath());
        m = segment.getMatrixParameters();        
        assertEquals("3", m.getFirst("x"));
        assertEquals("3", m.getFirst("y"));
    }
    
    public void testMultipleSlash() throws Exception {
        HttpRequestContext r = new TestHttpRequestContext(null, "GET", null,
                "/context/p//p//p//", "/context");
        List<PathSegment> segments = r.getPathSegments();
        
        assertEquals(3, segments.size());
        for (PathSegment segment : segments) {
            assertEquals("p", segment.getPath());
            assertEquals(0, segment.getMatrixParameters().size());
        }
    }
    
    public void testMultipleMatrixParams() throws Exception {
        HttpRequestContext r = new TestHttpRequestContext(null, "GET", null,
                "/context/p;x=1;x=2;x=3", "/context");
        List<PathSegment> segments = r.getPathSegments();
        
        MultivaluedMap<String, String> m = segments.get(0).getMatrixParameters();        

        List<String> values = m.get("x");
        for (int i = 0; i < m.size(); i++) {
            assertEquals(Integer.valueOf(i+1).toString(), values.get(i));
        }        
    }

    public void testEmptyPathSegmentsWithMultipleMatrixParams() throws Exception {
        HttpRequestContext r = new TestHttpRequestContext(null, "GET", null,
                "/context/;x=1;y=1/;x=2;y=2/;x=3;y=3", "/context");
        List<PathSegment> segments = r.getPathSegments();
        
        assertEquals(3, segments.size());
        
        PathSegment segment = segments.get(0);
        MultivaluedMap<String, String> m = segment.getMatrixParameters();        
        assertEquals("1", m.getFirst("x"));
        assertEquals("1", m.getFirst("y"));
        
        segment = segments.get(1);
        m = segment.getMatrixParameters();        
        assertEquals("2", m.getFirst("x"));
        assertEquals("2", m.getFirst("y"));
        
        segment = segments.get(2);
        m = segment.getMatrixParameters();        
        assertEquals("3", m.getFirst("x"));
        assertEquals("3", m.getFirst("y"));
    }
}
