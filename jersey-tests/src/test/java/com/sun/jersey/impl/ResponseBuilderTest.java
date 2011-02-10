/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ResponseBuilderTest extends TestCase {
    
    public ResponseBuilderTest(String testName) {
        super(testName);
    }
        
    public void testInvalidMediaType() {
        boolean caught = false;
        try {
            Response.ok(null, "abcd");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);
        
        caught = false;
        try {
            Response.ok(null).type("abcd");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught);
    }
    
    public void testMetadata() {
        Response r = Response.status(500).header(("X-TEST"), "test").build();
        
        MultivaluedMap<String, Object> m = r.getMetadata();
        assertEquals(1, m.size());
        List<Object> l = m.get("X-TEST");
        assertEquals(1, l.size());
    }
    
    public void testRemovedCookie() {
        Response.ResponseBuilder rb = Response.status(200).
                cookie(new NewCookie(new Cookie("a", "1"))).
                header("set-cookie", "name_1=value_2;version=2").
                header("X-TEST", "test");
        
        Response r = rb.cookie((NewCookie[])null).build();
        assertEquals(null, r.getMetadata().get("Set-Cookie"));
        assertEquals(1, r.getMetadata().get("X-TEST").size());
    }
    
    public void testRemovedHeaders() {
        Response.ResponseBuilder rb = Response.status(200).
                header("X-TEXT", "1").
                header("X-TEST", "2");
        
        Response r = rb.header("X-TEST", null).build();
        assertEquals(null, r.getMetadata().get("X-TEST"));
    }
    
    public void testCloneStatus() {
        Response.ResponseBuilder rb1 = Response.status(300);
        Response.ResponseBuilder rb2 = rb1.clone();
        
        assertEquals(rb1.build().getStatus(), rb2.build().getStatus());
    }
    
    public void testCloneHeaderEntity() {
        Response.ResponseBuilder rb1 = Response.status(200).
                type("text/plain").header("X", "Y").
                entity("foo");
        Response.ResponseBuilder rb2 = rb1.clone();
        Response r1 = rb1.build();
        Response r2 = rb2.build();
        
        assertEquals(r1.getStatus(), r2.getStatus());
        assertEquals(r1.getMetadata().getFirst("Content-Type"), 
                r2.getMetadata().getFirst("Content-Type"));
        assertEquals(r1.getMetadata().getFirst("X"), 
                r2.getMetadata().getFirst("X"));
        assertEquals(r1.getEntity(), r2.getEntity());
    }
    
    public void testCloneTwoHeaders() {
        Response.ResponseBuilder rb1 = Response.status(200).
                header("Set-Cookie", "name_1=value_1;version=1");
        Response.ResponseBuilder rb2 = rb1.clone();

        Response r2 = rb2.build();

        Response r1 = rb1.entity("content").header("Set-Cookie",
                "name_1=value_2;version=1").build();
        
        assertEquals(2, r1.getMetadata().get("Set-Cookie").size());
        assertEquals(1, r2.getMetadata().get("Set-Cookie").size());
    }
    
    public void testCloneCookie() {
        NewCookie nck1 = new NewCookie(new Cookie("a", "1"));        
        NewCookie nck2 = new NewCookie(new Cookie("b", "2"));        
        Response.ResponseBuilder rb = Response.status(200).cookie(nck1);
        Response r = rb.cookie(nck2).build();
        assertEquals(2, r.getMetadata().get("Set-Cookie").size());
    }    
    
    public void testCloneCookie2() {
        Response.ResponseBuilder respb1 = Response.status(200).
               header("Set-Cookie", "name_1=value_1;version=1");
        Response.ResponseBuilder respb2 = respb1.clone();

        Response resp2 = respb2.build();

        Response resp1 = respb1.entity("content").cookie((NewCookie[])null).build();         
        
        assertEquals(200, resp1.getStatus());
        assertEquals("content", resp1.getEntity());
        assertEquals(null, resp1.getMetadata().get("Set-Cookie"));

        assertEquals(200, resp2.getStatus());
        assertEquals(null, resp2.getEntity());
        assertEquals(1, resp2.getMetadata().get("Set-Cookie").size());
    }    
}