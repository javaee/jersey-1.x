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

package com.sun.ws.rest.impl.http.header.provider;

import com.sun.ws.rest.impl.http.header.AcceptableMediaType;
import com.sun.ws.rest.impl.http.header.HttpHeaderFactory;
import junit.framework.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class AcceptMediaTypeProviderTest extends TestCase {
    
    public AcceptMediaTypeProviderTest(String testName) {
        super(testName);
    }

    public void testOneMediaType() throws Exception {
        String header = "application/xml";
        List<AcceptableMediaType> l = HttpHeaderFactory.createAcceptMediaType(header);
        
        assertEquals(1, l.size());
        
        MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }
    
    public void testOneMediaTypeWithParameters() throws Exception {
        String header = "application/xml;charset=utf8";
        List<AcceptableMediaType> l = HttpHeaderFactory.createAcceptMediaType(header);
        
        assertEquals(1, l.size());
        
        MediaType m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        assertTrue(m.getParameters().containsKey("charset"));
        assertEquals("utf8", m.getParameters().get("charset"));
    }
    
    public void testMultipleMediaType() throws Exception {
        String header = "application/xml, text/xml, text/html";
        List<AcceptableMediaType> l = HttpHeaderFactory.createAcceptMediaType(header);
        
        assertEquals(3, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
    }
    
    public void testMultipleMediaTypeWithQuality() throws Exception {
        String header = "application/xml;q=0.1, text/xml;q=0.2, text/html;q=0.3";
        List<AcceptableMediaType> l = HttpHeaderFactory.createAcceptMediaType(header);
        
        assertEquals(3, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
    
    public void testHttpURLConnectionAcceptHeader() throws Exception {
        String header = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
        List<AcceptableMediaType> l = HttpHeaderFactory.createAcceptMediaType(header);
        
        assertEquals(5, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("image", m.getType());
        assertEquals("gif", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("image", m.getType());
        assertEquals("jpeg", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(4);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }

    public void testFirefoxAcceptHeader() throws Exception {
        String header = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";
        List<AcceptableMediaType> l = HttpHeaderFactory.createAcceptMediaType(header);
        
        assertEquals(7, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(1);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xhtml+xml", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(3);
        assertEquals("image", m.getType());
        assertEquals("png", m.getSubtype());
        assertEquals(0, m.getParameters().size());
        m = l.get(4);
        assertEquals("text", m.getType());
        assertEquals("html", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(5);
        assertEquals("text", m.getType());
        assertEquals("plain", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(6);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
    
    public void testWithStarAcceptHeader() throws Exception {
        String header = "application/xml;q=0.1, text/xml;q=0.2, *;q=0.3";
        List<AcceptableMediaType> l = HttpHeaderFactory.createAcceptMediaType(header);
        
        assertEquals(3, l.size());
        
        MediaType m;
        m = l.get(0);
        assertEquals("*", m.getType());
        assertEquals("*", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(1);
        assertEquals("text", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
        m = l.get(2);
        assertEquals("application", m.getType());
        assertEquals("xml", m.getSubtype());
        assertEquals(1, m.getParameters().size());
    }
}
