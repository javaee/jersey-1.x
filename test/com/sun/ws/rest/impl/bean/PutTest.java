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

package com.sun.ws.rest.impl.bean;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PutTest extends AbstractBeanTester {
    
    public PutTest(String testName) {
        super(testName);
    }

    @UriTemplate("/PutNoInputNoReturnResource")
    static public class PutNoInputNoReturnResource { 
        @HttpMethod("PUT")
        public void doPut() {
        }
    }
    
    @UriTemplate("/PutNoReturnResource")
    static public class PutNoReturnResource { 
        @HttpMethod("PUT")
        public void doPut(String in) {
            assertEquals("PutNoReturnResource", in);
        }
    }
    
    @UriTemplate("/PutNoInputResource")
    static public class PutNoInputResource { 
        @HttpMethod("PUT")
        public String doPut() {
            return "PutNoInputResource";
        }
    }
    
    @UriTemplate("/PutResource")
    static public class PutResource { 
        @HttpMethod("PUT")
        public String doPut(String in) {
            assertEquals("PutResource", in);
            return "PutResource";
        }
    }
    
    public void testPut() {
        Set<Class> s = new HashSet<Class>();
        s.add(PutNoInputNoReturnResource.class);
        s.add(PutNoReturnResource.class);
        s.add(PutNoInputResource.class);
        s.add(PutResource.class);
        
        call(s, "PUT", "/PutNoInputNoReturnResource", null, null, "PutNoInputNoReturnResource");
        call(s, "PUT", "/PutNoReturnResource", null, null, "PutNoReturnResource");
        call(s, "PUT", "/PutNoInputResource", null, null, "PutNoInputResource");
        call(s, "PUT", "/PutResource", null, null, "PutResource");
    }
}
