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
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.bean.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceDynamicTest extends AbstractBeanTester {
    
    public SubResourceDynamicTest(String testName) {
        super(testName);
    }

    @UriTemplate("/parent")
    static public class Parent { 
        @HttpMethod
        public String getMe() {
            return "parent";
        }
        
        @UriTemplate("child")
        public Child getChild() {
            return new Child();
        }
    }
    
    static public class Child { 
        @HttpMethod
        public String getMe() {
            return "child";
        }
    }
    
    public void testSubResourceDynamic() {
        initiateWebApplication(Parent.class);
        
        assertEquals("parent", resourceProxy("/parent").get(String.class));
        assertEquals("child", resourceProxy("/parent/child").get(String.class));
    }    
    
    @UriTemplate("/{p}")
    static public class ParentWithTemplates { 
        @HttpMethod
        public String getMe(@UriParam("p") String p) {
            return p;
        }
        
        @UriTemplate("child/{c}")
        public ChildWithTemplates getChildWithTemplates() {
            return new ChildWithTemplates();
        }
    }
    
    static public class ChildWithTemplates { 
        @HttpMethod
        public String getMe(@UriParam("c") String c) {
            return c;
        }
    }
    
    public void testSubResourceDynamicWithTemplates() {
        initiateWebApplication(ParentWithTemplates.class);
        
        assertEquals("parent", resourceProxy("/parent").get(String.class));
        assertEquals("first", resourceProxy("/parent/child/first").get(String.class));
    }    
}
