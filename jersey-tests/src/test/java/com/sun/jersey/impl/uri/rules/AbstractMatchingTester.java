/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.impl.uri.rules;

import com.sun.jersey.server.impl.model.RulesMap;
import com.sun.jersey.server.impl.uri.PathPattern;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.spi.uri.rules.UriMatchResultContext;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.Iterator;
import java.util.regex.MatchResult;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractMatchingTester extends TestCase implements UriMatchResultContext {
    
    protected UriRules<String> rules;

    MatchResult matchResult;

    public AbstractMatchingTester(String testName) {
        super(testName);
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public void setMatchResult(MatchResult matchResult) {
        this.matchResult = matchResult;
    }
    
    protected abstract class RulesBuilder {
        protected RulesMap<String> rulesMap = new RulesMap<String>();
        
        public RulesBuilder add(UriTemplate t, String s) {
            rulesMap.put(new PathPattern(t), s);
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
    
    public final RulesBuilder add(PathPattern p, String s) {
        return create().add(p, s);
    }
    
    protected abstract RulesBuilder create();
    
    protected String match(CharSequence path) {
        Iterator<String> i = rules.match(path, this);
        if (!i.hasNext())
            return null;
        return i.next();
    }
    
    public void testNull() {
        add(PathPattern.EMPTY_PATH, "MATCH").
                build();
        
        String s = match("");
        
        assertEquals("MATCH", s);
        assertEquals(0, matchResult.groupCount());
    }
    
    public void testSlash() {
        add(new UriTemplate("/"), "MATCH").
                build();
        
        String s = match("/");
        
        assertEquals("MATCH", s);
        assertEquals(1, matchResult.groupCount());
        assertEquals("/", matchResult.group(1));
    }
    
    public void testSlashWithMorePath() {
        add(new UriTemplate("/"), "MATCH").
                build();
        
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(1, matchResult.groupCount());
        assertEquals("/a/b/c/d", matchResult.group(1));
    }
    
    public void testLiteralTemplateWithMorePath() {
        add(new UriTemplate("/a"), "MATCH").
                build();
        
        StringBuilder path = new StringBuilder("/a/b/c/d");
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(1, matchResult.groupCount());
        assertEquals("/b/c/d", matchResult.group(1));
    }
    
    public void testSingleTemplate() {
        add(new UriTemplate("/{t}"), "MATCH").
                build();
        
        String s = match("/a");
        
        assertEquals("MATCH", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
    }
    
    public void testSingleTemplateWithMorePath() {
        add(new UriTemplate("/{t}"), "MATCH").
                build();
        
        String s = match("/a/b/c/d");
        
        assertEquals("MATCH", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals("/b/c/d", matchResult.group(2));
    }
    
    public void testMultipleTemplates() {
        add(new UriTemplate("/-{p1}-/-{p2}-/-{p3}-"), "/-{p1}-/-{p2}-/-{p3}-")     
        .add(new UriTemplate("/{p1}/{p2}/{p3}"), "/{p1}/{p2}/{p3}")
        .add(new UriTemplate("/{p1}/{p2}"), "/{p1}/{p2}")
        .add(new UriTemplate("/{p1}"), "/{p1}").
                build();

        String s = match("/-a-/-b-/-c-");
        assertEquals("/-{p1}-/-{p2}-/-{p3}-", s);
        assertEquals(4, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals("b", matchResult.group(2));
        assertEquals("c", matchResult.group(3));
        assertEquals(null, matchResult.group(4));
        
        s = match("/-a-/-b-/-c-/d");
        assertEquals("/-{p1}-/-{p2}-/-{p3}-", s);
        assertEquals(4, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals("b", matchResult.group(2));
        assertEquals("c", matchResult.group(3));
        assertEquals("/d", matchResult.group(4));
        
        s = match("/-a/b/c/d");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, matchResult.groupCount());
        assertEquals("-a", matchResult.group(1));
        assertEquals("b", matchResult.group(2));
        assertEquals("c", matchResult.group(3));
        assertEquals("/d", matchResult.group(4));
        
        s = match("/a/b/c/d");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals("b", matchResult.group(2));
        assertEquals("c", matchResult.group(3));
        assertEquals("/d", matchResult.group(4));
        
        s = match("/-a/b/c");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, matchResult.groupCount());
        assertEquals("-a", matchResult.group(1));
        assertEquals("b", matchResult.group(2));
        assertEquals("c", matchResult.group(3));
        assertEquals(null, matchResult.group(4));
        
        s = match("/a/b/c");
        assertEquals("/{p1}/{p2}/{p3}", s);
        assertEquals(4, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals("b", matchResult.group(2));
        assertEquals("c", matchResult.group(3));
        assertEquals(null, matchResult.group(4));

        
        s = match("/a/b");
        assertEquals("/{p1}/{p2}", s);
        assertEquals(3, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals("b", matchResult.group(2));
        assertEquals(null, matchResult.group(3));

        
        s = match("/a");
        assertEquals("/{p1}", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
    }
    
    public void testMultipleTemplatesWithExplicitPath() {
        add(new UriTemplate("/{p1}"), "/{p1}")  
        .add(new UriTemplate("/edit"), "/edit")
        .add(new UriTemplate("/edit/{p1}"), "/edit/{p1}")  
        .add(new UriTemplate("/edit/a{p1}"), "/edit/a{p1}").
                build();
        
        String s = match("/a");
        assertEquals("/{p1}", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
        
        s = match("/edit");
        assertEquals("/edit", s);
        assertEquals(1, matchResult.groupCount());
        assertEquals(null, matchResult.group(1));
        
        s = match("/edit/b");
        assertEquals("/edit/{p1}", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("b", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
                
        s = match("/edit/a");
        assertEquals("/edit/{p1}", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("a", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
        
        s = match("/edit/a_one");
        assertEquals("/edit/a{p1}", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("_one", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
    }
    
    public void ignoredTestTemplatesWithSlash() {
        add(new UriTemplate("/edit/"), "/edit/")      
        .add(new UriTemplate("/edit/{p1}/"), "/edit/{p1}/").
                build();
        
        String s = match("/edit");
        assertEquals("/edit/", s);
        assertEquals(1, matchResult.groupCount());
        assertEquals(null, matchResult.group(1));

        s = match("/edit/");
        assertEquals("/edit/", s);
        assertEquals(1, matchResult.groupCount());
        assertEquals("/", matchResult.group(1));
    }

    public void testTemplatesWithSameNumOfCharactersAndTemplates() {
        add(new UriTemplate("/a/{p1}/b"), "/a/{p1}/b")        
        .add(new UriTemplate("/a/{p1}/c"), "/a/{p1}/c").
                build();
      
        String s = match("/a/infix/b");
        assertEquals("/a/{p1}/b", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("infix", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
    
        s = match("/a/infix/c");
        assertEquals("/a/{p1}/c", s);
        assertEquals(2, matchResult.groupCount());
        assertEquals("infix", matchResult.group(1));
        assertEquals(null, matchResult.group(2));
    }
}
