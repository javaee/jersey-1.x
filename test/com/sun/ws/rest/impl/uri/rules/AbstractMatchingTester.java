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

package com.sun.ws.rest.impl.uri.rules;

import com.sun.ws.rest.spi.dispatch.UriTemplateType;
import com.sun.ws.rest.spi.uri.rules.UriRules;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractMatchingTester extends TestCase {
    
    protected UriRules<UriTemplateType, String> resolver;

    final List<String> groupValues = new ArrayList<String>();
    
    public AbstractMatchingTester(String testName) {
        super(testName);
        
        resolver = new LinearMatchingUriTemplateRules<String>();
    }

    protected String match(CharSequence path) {
        Iterator<String> i = resolver.match(path, groupValues);
        if (!i.hasNext())
            return null;
        return i.next();
    }
    
    public void testNull() {
        resolver.add(UriTemplateType.NULL, "MATCH");
        
        String s = match("");
        
        assertEquals("MATCH", s);
        assertEquals(0, groupValues.size());
    }
    
    public void testSlash() {
        resolver.add(new UriTemplateType("/", UriTemplateType.RIGHT_SLASHED_REGEX), "MATCH");
        
        String s = match("/");
        
        assertEquals("MATCH", s);
        assertEquals(1, groupValues.size());
        assertEquals("/", groupValues.get(0));
    }
    
    public void testSlashWithMorePath() {
        resolver.add(new UriTemplateType("/", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(1, groupValues.size());
        assertEquals("/a/b/c/d", groupValues.get(0));
    }
    
    public void testLiteralTemplateWithMorePath() {
        resolver.add(new UriTemplateType("/a", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        StringBuilder path = new StringBuilder("/a/b/c/d");
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(1, groupValues.size());
        assertEquals("/b/c/d", groupValues.get(0));
    }
    
    public void testSingleTemplate() {
        resolver.add(new UriTemplateType("/{t}", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        String s = match("/a");
        
        assertEquals("MATCH", s);
        assertEquals(2, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
    }
    
    public void testSingleTemplateWithMorePath() {
        resolver.add(new UriTemplateType("/{t}", UriTemplateType.RIGHT_HANDED_REGEX), "MATCH");
        
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(2, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals("/b/c/d", groupValues.get(1));
    }
    
    public void testMultipleTemplates() {
        resolver.add(new UriTemplateType("/-{p1}-/-{p2}-/-{p3}-", UriTemplateType.RIGHT_HANDED_REGEX), "/-{p1}-/-{p2}-/-{p3}-");        
        resolver.add(new UriTemplateType("/{p1}/{p2}/{p3}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}/{p2}/{p3}");        
        resolver.add(new UriTemplateType("/{p1}/{p2}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}/{p2}");        
        resolver.add(new UriTemplateType("/{p1}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}");        

        String s = match("/-a-/-b-/-c-");
        assertEquals("/-{p1}-/-{p2}-/-{p3}-", s);
        assertEquals(4, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals("b", groupValues.get(1));
        assertEquals("c", groupValues.get(2));
        assertEquals(null, groupValues.get(3));
        
        s = match("/-a-/-b-/-c-/d");
        assertEquals("/-{p1}-/-{p2}-/-{p3}-", s);
        assertEquals(4, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals("b", groupValues.get(1));
        assertEquals("c", groupValues.get(2));
        assertEquals("/d", groupValues.get(3));
        
        s = match("/-a/b/c/d");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, groupValues.size());
        assertEquals("-a", groupValues.get(0));
        assertEquals("b", groupValues.get(1));
        assertEquals("c", groupValues.get(2));
        assertEquals("/d", groupValues.get(3));
        
        s = match("/a/b/c/d");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals("b", groupValues.get(1));
        assertEquals("c", groupValues.get(2));
        assertEquals("/d", groupValues.get(3));
        
        s = match("/-a/b/c");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, groupValues.size());
        assertEquals("-a", groupValues.get(0));
        assertEquals("b", groupValues.get(1));
        assertEquals("c", groupValues.get(2));
        assertEquals(null, groupValues.get(3));
        
        s = match("/a/b/c");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals("b", groupValues.get(1));
        assertEquals("c", groupValues.get(2));
        assertEquals(null, groupValues.get(3));

        
        s = match("/a/b");
        assertEquals("/{p1}/{p2}", s);
        assertEquals(3, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals("b", groupValues.get(1));
        assertEquals(null, groupValues.get(2));

        
        s = match("/a");
        assertEquals("/{p1}", s);
        assertEquals(2, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
    }
    
    public void testMultipleTemplatesWithExplicitPath() {
        resolver.add(new UriTemplateType("/{p1}", UriTemplateType.RIGHT_HANDED_REGEX), "/{p1}");        
        resolver.add(new UriTemplateType("/edit", UriTemplateType.RIGHT_HANDED_REGEX), "/edit");        
        resolver.add(new UriTemplateType("/edit/{p1}", UriTemplateType.RIGHT_HANDED_REGEX), "/edit/{p1}");        
        resolver.add(new UriTemplateType("/edit/a{p1}", UriTemplateType.RIGHT_HANDED_REGEX), "/edit/a{p1}");        
        
        String s = match("/a");
        assertEquals("/{p1}", s);
        assertEquals(2, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
        
        s = match("/edit");
        assertEquals("/edit", s);
        assertEquals(1, groupValues.size());
        assertEquals(null, groupValues.get(0));
        
        s = match("/edit/b");
        assertEquals("/edit/{p1}", s);
        assertEquals(2, groupValues.size());
        assertEquals("b", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
                
        s = match("/edit/a");
        assertEquals("/edit/a{p1}", s);
        assertEquals(2, groupValues.size());
        assertEquals("", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
        
        s = match("/edit/a_one");
        assertEquals("/edit/a{p1}", s);
        assertEquals(2, groupValues.size());
        assertEquals("_one", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
    }
    
    public void testTemplatesWithSlash() {
        resolver.add(new UriTemplateType("/edit/", UriTemplateType.RIGHT_HANDED_REGEX), "/edit/");        
        resolver.add(new UriTemplateType("/edit/{p1}/", UriTemplateType.RIGHT_HANDED_REGEX), "/edit/{p1}/");        
        
        String s = match("/edit");
        assertEquals("/edit/", s);
        assertEquals(1, groupValues.size());
        assertEquals(null, groupValues.get(0));

        s = match("/edit/");
        assertEquals("/edit/", s);
        assertEquals(1, groupValues.size());
        assertEquals("/", groupValues.get(0));
    }

    public void testTemplatesWithSameNumOfCharactersAndTemplates() {
        resolver.add(new UriTemplateType("/a/{p1}/b", UriTemplateType.RIGHT_HANDED_REGEX), "/a/{p1}/b");        
        resolver.add(new UriTemplateType("/a/{p1}/c", UriTemplateType.RIGHT_HANDED_REGEX), "/a/{p1}/c");  
      
        String s = match("/a/infix/b");
        assertEquals("/a/{p1}/b", s);
        assertEquals(2, groupValues.size());
        assertEquals("infix", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
    
        s = match("/a/infix/c");
        assertEquals("/a/{p1}/c", s);
        assertEquals(2, groupValues.size());
        assertEquals("infix", groupValues.get(0));
        assertEquals(null, groupValues.get(1));    
    }
}