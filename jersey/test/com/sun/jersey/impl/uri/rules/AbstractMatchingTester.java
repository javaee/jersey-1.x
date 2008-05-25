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

package com.sun.jersey.impl.uri.rules;

import com.sun.jersey.impl.model.RulesMap;
import com.sun.jersey.impl.uri.PathPattern;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractMatchingTester extends TestCase {
    
    protected UriRules<String> rules;

    final List<String> groupValues = new ArrayList<String>();
    
    public AbstractMatchingTester(String testName) {
        super(testName);
    }

    protected abstract class RulesBuilder {
        protected RulesMap<String> rulesMap = new RulesMap<String>();
        
        public RulesBuilder add(UriTemplate t, String s) {
            return add(t, true, s);
        }
        
        public RulesBuilder add(UriTemplate t, boolean limited, String s) {
            rulesMap.put(new PathPattern(t, limited), s);
            return this;
        }
        
        public RulesBuilder add(PathPattern p, String s) {
            rulesMap.put(p, s);
            return this;
        }

        public void build() {
            rules = _build();
        }
        
        protected abstract UriRules<String> _build();
    }
    
    public final RulesBuilder add(UriTemplate t, String s) {
        return create().add(t, s);
    }
    
    public final RulesBuilder add(UriTemplate t, boolean limited, String s) {
        return create().add(t, limited, s);
    }
    
    public final RulesBuilder add(PathPattern p, String s) {
        return create().add(p, s);
    }
    
    protected abstract RulesBuilder create();
    
    protected String match(CharSequence path) {
        Iterator<String> i = rules.match(path, groupValues);
        if (!i.hasNext())
            return null;
        return i.next();
    }
    
    public void testNull() {
        add(PathPattern.EMPTY_PATH, "MATCH").
                build();
        
        String s = match("");
        
        assertEquals("MATCH", s);
        assertEquals(0, groupValues.size());
    }
    
    public void testSlash() {
        add(new UriTemplate("/"), false, "MATCH").
                build();
        
        String s = match("/");
        
        assertEquals("MATCH", s);
        assertEquals(1, groupValues.size());
        assertEquals("/", groupValues.get(0));
    }
    
    public void testSlashWithMorePath() {
        add(new UriTemplate("/"), "MATCH").
                build();
        
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(1, groupValues.size());
        assertEquals("/a/b/c/d", groupValues.get(0));
    }
    
    public void testLiteralTemplateWithMorePath() {
        add(new UriTemplate("/a"), "MATCH").
                build();
        
        StringBuilder path = new StringBuilder("/a/b/c/d");
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(1, groupValues.size());
        assertEquals("/b/c/d", groupValues.get(0));
    }
    
    public void testSingleTemplate() {
        add(new UriTemplate("/{t}"), "MATCH").
                build();
        
        String s = match("/a");
        
        assertEquals("MATCH", s);
        assertEquals(2, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals(null, groupValues.get(1));
    }
    
    public void testSingleTemplateWithMorePath() {
        add(new UriTemplate("/{t}"), "MATCH").
                build();
        
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(2, groupValues.size());
        assertEquals("a", groupValues.get(0));
        assertEquals("/b/c/d", groupValues.get(1));
    }
    
    public void testMultipleTemplates() {
        add(new UriTemplate("/-{p1}-/-{p2}-/-{p3}-"), "/-{p1}-/-{p2}-/-{p3}-")     
        .add(new UriTemplate("/{p1}/{p2}/{p3}"), "/{p1}/{p2}/{p3}")
        .add(new UriTemplate("/{p1}/{p2}"), "/{p1}/{p2}")
        .add(new UriTemplate("/{p1}"), "/{p1}").
                build();

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
        add(new UriTemplate("/{p1}"), "/{p1}")  
        .add(new UriTemplate("/edit"), "/edit")
        .add(new UriTemplate("/edit/{p1}"), "/edit/{p1}")  
        .add(new UriTemplate("/edit/a{p1}"), "/edit/a{p1}").
                build();
        
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
        add(new UriTemplate("/edit/"), "/edit/")      
        .add(new UriTemplate("/edit/{p1}/"), "/edit/{p1}/").
                build();
        
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
        add(new UriTemplate("/a/{p1}/b"), "/a/{p1}/b")        
        .add(new UriTemplate("/a/{p1}/c"), "/a/{p1}/c").
                build();
      
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