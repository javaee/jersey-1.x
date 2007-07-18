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

package com.sun.ws.rest.impl.subresources;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.bean.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceDynamicWithParametersTest extends AbstractBeanTester {
    
    public SubResourceDynamicWithParametersTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{p}")
    static public class ParentWithTemplates { 
        @HttpMethod
        public String getMe(@UriParam("p") String p) {
            return p;
        }
        
        @UriTemplate("child/{c}")
        public ChildWithTemplates getChildWithTemplates(
                @UriParam("p") String p, @UriParam("c") String c,
                @QueryParam("a") int a, @QueryParam("b") int b) {
            assertEquals("parent", p);
            assertEquals("first", c);
            assertEquals(1, a);
            assertEquals(2, b);
            return new ChildWithTemplates();
        }
        
        @UriTemplate(value="unmatchedPath/{path}", limited=false)
        public UnmatchedPathResource getUnmatchedPath(
                @UriParam("p") String p,
                @UriParam("path") String path) {
            assertEquals("parent", p);
            return new UnmatchedPathResource(path);
        }
    }
    
    static public class ChildWithTemplates { 
        @HttpMethod
        public String getMe(@UriParam("c") String c) {
            return c;
        }
    }
    
    static public class UnmatchedPathResource { 
        String path;
        
        UnmatchedPathResource(String path) {
            this.path = path;
        }
        
        @HttpMethod
        public String getMe() {
            if (path == null) path = "";
            return path;
        }
    }
    
    public void testSubResourceDynamicWithTemplates() {
        String content;
        
        content = (String)callGet(ParentWithTemplates.class, "/parent", "").
                getEntity();
        assertEquals("parent", content);
        content = (String)callGet(ParentWithTemplates.class, "/parent/child/first?a=1&b=2", "").
                getEntity();
        assertEquals("first", content);
    }
    
    public void testSubResourceDynamicWithUnmatchedPath() {
        String content;
        
        content = (String)callGet(ParentWithTemplates.class, "/parent/unmatchedPath/", "").
                getEntity();
        assertEquals("", content);
        content = (String)callGet(ParentWithTemplates.class, "/parent/unmatchedPath/a/b/c/d", "").
                getEntity();
        assertEquals("a/b/c/d", content);
    }
}
