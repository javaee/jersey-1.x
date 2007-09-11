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

import com.sun.ws.rest.impl.dispatch.LinearOrderedUriPathResolver;
import com.sun.ws.rest.spi.dispatch.UriPathResolver;
import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import java.util.HashMap;
import java.util.Map;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriPathResolverTest extends TestCase {
    
    final UriPathResolver<String> resolver;

    final Map<String, String> templateValues = new HashMap<String, String>();
    
    public UriPathResolverTest(String testName) {
        super(testName);
        
        resolver = new LinearOrderedUriPathResolver<String>();
    }

    public void testNull() {
        resolver.add(UriTemplateType.NULL, "MATCH");
        
        StringBuilder path = new StringBuilder("");
        String s = resolver.resolve(path, path, templateValues);
        
        assertEquals("MATCH", s);
        assertEquals(0, templateValues.size());
        assertEquals("", path.toString());
    }
    
    public void testSlash() {
        resolver.add(new UriTemplateType("/", UriTemplateType.RIGHT_SLASHED_REGEX), "MATCH");
        
        StringBuilder path = new StringBuilder("/");
        String s = resolver.resolve(path, path, templateValues);
        
        assertEquals("MATCH", s);
        assertEquals(0, templateValues.size());
        assertEquals("/", path.toString());
    }
    
    public void testSlashWithMorePath() {
        resolver.add(new UriTemplateType("/", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        StringBuilder path = new StringBuilder("/a/b/c/d");
        String s = resolver.resolve(path, path, templateValues);
        
        assertEquals("MATCH", s);
        assertEquals(0, templateValues.size());
        assertEquals("/a/b/c/d", path.toString());
    }
    
    public void testLiteralTemplateWithMorePath() {
        resolver.add(new UriTemplateType("/a", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        StringBuilder path = new StringBuilder("/a/b/c/d");
        String s = resolver.resolve(path, path, templateValues);
        
        assertEquals("MATCH", s);
        assertEquals(0, templateValues.size());
        assertEquals("/b/c/d", path.toString());
    }
    
    public void testSingleTemplate() {
        resolver.add(new UriTemplateType("/{t}", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        StringBuilder path = new StringBuilder("/a");
        String s = resolver.resolve(path, path, templateValues);
        
        assertEquals("MATCH", s);
        assertEquals(1, templateValues.size());
        assertEquals("a", templateValues.get("t"));
        assertEquals("", path.toString());
    }
    
    public void testSingleTemplateWithMorePath() {
        resolver.add(new UriTemplateType("/{t}", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        StringBuilder path = new StringBuilder("/a/b/c/d");
        String s = resolver.resolve(path, path, templateValues);
        
        assertEquals("MATCH", s);
        assertEquals(1, templateValues.size());
        assertEquals("a", templateValues.get("t"));
        assertEquals("/b/c/d", path.toString());
    }
    
    public void testMultipleTemplates() {
        resolver.add(new UriTemplateType("/-{p1}-/-{p2}-/-{p3}-", UriTemplateType.RIGHT_HANDED_REGEX), "/-{p1}-/-{p2}-/-{p3}-");        
        resolver.add(new UriTemplateType("/{p1}/{p2}/{p3}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}/{p2}/{p3}");        
        resolver.add(new UriTemplateType("/{p1}/{p2}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}/{p2}");        
        resolver.add(new UriTemplateType("/{p1}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}");        

        StringBuilder path = new StringBuilder("/-a-/-b-/-c-");
        String s = resolver.resolve(path, path, templateValues);
        assertEquals("/-{p1}-/-{p2}-/-{p3}-", s);
        assertEquals(3, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("b", templateValues.get("p2"));
        assertEquals("c", templateValues.get("p3"));
        assertEquals("", path.toString());
        
        path = new StringBuilder("/-a-/-b-/-c-/d");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/-{p1}-/-{p2}-/-{p3}-", s);
        assertEquals(3, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("b", templateValues.get("p2"));
        assertEquals("c", templateValues.get("p3"));
        assertEquals("/d", path.toString());
        
        path = new StringBuilder("/-a/b/c/d");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(3, templateValues.size());
        assertEquals("-a", templateValues.get("p1"));
        assertEquals("b", templateValues.get("p2"));
        assertEquals("c", templateValues.get("p3"));
        assertEquals("/d", path.toString());
        
        path = new StringBuilder("/a/b/c/d");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(3, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("b", templateValues.get("p2"));
        assertEquals("c", templateValues.get("p3"));
        assertEquals("/d", path.toString());
        
        path = new StringBuilder("/-a/b/c");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(3, templateValues.size());
        assertEquals("-a", templateValues.get("p1"));
        assertEquals("b", templateValues.get("p2"));
        assertEquals("c", templateValues.get("p3"));
        assertEquals("", path.toString());
        
        path = new StringBuilder("/a/b/c");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(3, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("b", templateValues.get("p2"));
        assertEquals("c", templateValues.get("p3"));
        assertEquals("", path.toString());
        
        path = new StringBuilder("/a/b");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/{p1}/{p2}", s);
        assertEquals(2, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("b", templateValues.get("p2"));
        assertEquals("", path.toString());
        
        path = new StringBuilder("/a");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/{p1}", s);
        assertEquals(1, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("", path.toString());
    }
    
    public void testMultipleTemplatesWithExplicitPath() {
        resolver.add(new UriTemplateType("/{p1}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}");        
        resolver.add(new UriTemplateType("/edit", UriTemplateType.RIGHT_HANDED_REGEX), "/edit");        
        resolver.add(new UriTemplateType("/edit/{p1}", UriTemplateType.RIGHT_HANDED_REGEX), "/edit/{p1}");        
        
        StringBuilder path = new StringBuilder("/a");
        String s = resolver.resolve(path, path, templateValues);
        assertEquals("/{p1}", s);
        assertEquals(1, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("", path.toString());
        
        path = new StringBuilder("/edit/a");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/edit/{p1}", s);
        assertEquals(1, templateValues.size());
        assertEquals("a", templateValues.get("p1"));
        assertEquals("", path.toString());
        
        path = new StringBuilder("/edit");
        s = resolver.resolve(path, path, templateValues);
        assertEquals("/edit", s);
        assertEquals(0, templateValues.size());
        assertEquals("", path.toString());
    }
    
    protected UriPathResolver getResolver() {
        return new LinearOrderedUriPathResolver<String>();
    }
}