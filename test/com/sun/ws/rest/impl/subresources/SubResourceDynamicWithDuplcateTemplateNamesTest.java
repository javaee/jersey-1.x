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

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.impl.AbstractResourceTester;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class SubResourceDynamicWithDuplcateTemplateNamesTest extends AbstractResourceTester {
    
    public SubResourceDynamicWithDuplcateTemplateNamesTest(String testName) {
        super(testName);
    }
    
    @UriTemplate("/{v}")
    static public class ParentWithTemplates { 
        @UriTemplate("child/")
        public ChildWithTemplates getChildWithTemplates(@UriParam("v") String v) {
            return new ChildWithTemplates();
        }
    }
    
    static public class ChildWithTemplates { 
        @HttpMethod
        public String getMe(@UriParam("v") String v) {
            return v;
        }
        
        @UriTemplate("child/{v}")
        public ChildWithTemplates getChildWithTemplates() {
            return new ChildWithTemplates();
        }
    }
    
    public void testSubResourceDynamicWithTemplates() {
        initiateWebApplication(ParentWithTemplates.class);
        
        assertEquals("first", resourceProxy("/parent/child/child/first").get(String.class));
    }    
}
