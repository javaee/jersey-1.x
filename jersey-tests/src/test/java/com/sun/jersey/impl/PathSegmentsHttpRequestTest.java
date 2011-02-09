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

package com.sun.jersey.impl;

import com.sun.jersey.server.impl.application.WebApplicationContext;
import com.sun.jersey.server.impl.application.WebApplicationImpl;
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
        WebApplicationImpl wai = new WebApplicationImpl();
        ContainerRequest r = new TestHttpRequestContext(wai,
                "GET", null,
                "/context/p1;x=1;y=1/p2;x=2;y=2/p3;x=3;y=3", "/context/");
        UriInfo ui = new WebApplicationContext(wai, r, null);
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
        WebApplicationImpl wai = new WebApplicationImpl();
        ContainerRequest r = new TestHttpRequestContext(wai,
                "GET", null,
                "/context/p//p//p//", "/context/");
        UriInfo ui = new WebApplicationContext(wai, r, null);
        List<PathSegment> segments = ui.getPathSegments();
        
        assertEquals(7, segments.size());
        
        assertEquals("p", segments.get(0).getPath());
        assertEquals(0, segments.get(0).getMatrixParameters().size());
        assertEquals("", segments.get(1).getPath());
        assertEquals(0, segments.get(1).getMatrixParameters().size());
        assertEquals("p", segments.get(2).getPath());
        assertEquals(0, segments.get(2).getMatrixParameters().size());
        assertEquals("", segments.get(3).getPath());
        assertEquals(0, segments.get(3).getMatrixParameters().size());
        assertEquals("p", segments.get(4).getPath());
        assertEquals(0, segments.get(4).getMatrixParameters().size());
        assertEquals("", segments.get(5).getPath());
        assertEquals(0, segments.get(5).getMatrixParameters().size());
        assertEquals("", segments.get(6).getPath());
        assertEquals(0, segments.get(6).getMatrixParameters().size());
    }
    
    public void testMultipleMatrixParams() throws Exception {
        WebApplicationImpl wai = new WebApplicationImpl();
        ContainerRequest r = new TestHttpRequestContext(wai,
                "GET", null,
                "/context/p;x=1;x=2;x=3", "/context/");
        UriInfo ui = new WebApplicationContext(wai, r, null);
        List<PathSegment> segments = ui.getPathSegments();
        
        MultivaluedMap<String, String> m = segments.get(0).getMatrixParameters();        

        List<String> values = m.get("x");
        for (int i = 0; i < m.size(); i++) {
            assertEquals(Integer.valueOf(i+1).toString(), values.get(i));
        }        
    }

    public void testEmptyPathSegmentsWithMultipleMatrixParams() throws Exception {
        WebApplicationImpl wai = new WebApplicationImpl();
        ContainerRequest r = new TestHttpRequestContext(wai,
                "GET", null,
                "/context/;x=1;y=1/;x=2;y=2/;x=3;y=3", "/context/");
        UriInfo ui = new WebApplicationContext(wai, r, null);
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
