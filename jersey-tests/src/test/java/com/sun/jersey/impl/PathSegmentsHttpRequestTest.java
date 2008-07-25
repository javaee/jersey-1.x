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

package com.sun.jersey.impl;

import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.impl.application.WebApplicationContext;
import com.sun.jersey.spi.container.ContainerRequest;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
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
        ContainerRequest r = new TestHttpRequestContext(new DummyWebApplication(), 
                "GET", null,
                "/context/p1;x=1;y=1/p2;x=2;y=2/p3;x=3;y=3", "/context");
        UriInfo ui = new WebApplicationContext(null, r, null);
        List<PathSegment> segments = ui.getPathSegments();
        
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
        ContainerRequest r = new TestHttpRequestContext(new DummyWebApplication(),
                "GET", null,
                "/context/p//p//p//", "/context");
        UriInfo ui = new WebApplicationContext(null, r, null);
        List<PathSegment> segments = ui.getPathSegments();
        
        assertEquals(3, segments.size());
        for (PathSegment segment : segments) {
            assertEquals("p", segment.getPath());
            assertEquals(0, segment.getMatrixParameters().size());
        }
    }
    
    public void testMultipleMatrixParams() throws Exception {
        ContainerRequest r = new TestHttpRequestContext(new DummyWebApplication(),
                "GET", null,
                "/context/p;x=1;x=2;x=3", "/context");
        UriInfo ui = new WebApplicationContext(null, r, null);
        List<PathSegment> segments = ui.getPathSegments();
        
        MultivaluedMap<String, String> m = segments.get(0).getMatrixParameters();        

        List<String> values = m.get("x");
        for (int i = 0; i < m.size(); i++) {
            assertEquals(Integer.valueOf(i+1).toString(), values.get(i));
        }        
    }

    public void testEmptyPathSegmentsWithMultipleMatrixParams() throws Exception {
        ContainerRequest r = new TestHttpRequestContext(new DummyWebApplication(),
                "GET", null,
                "/context/;x=1;y=1/;x=2;y=2/;x=3;y=3", "/context");
        UriInfo ui = new WebApplicationContext(null, r, null);
        List<PathSegment> segments = ui.getPathSegments();
        
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
