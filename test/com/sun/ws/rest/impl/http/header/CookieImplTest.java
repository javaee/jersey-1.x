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

package com.sun.ws.rest.impl.http.header;

import com.sun.ws.rest.impl.provider.header.NewCookieProvider;
import junit.framework.*;
import java.util.List;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class CookieImplTest extends TestCase {
    
    public CookieImplTest(String testName) {
        super(testName);
    }

    /**
     * Test of createCookies method, of class com.sun.ws.rest.impl.http.header.CookiesParser.
     */
    public void testCreateCookies() {
        System.out.println("createCookies");
        
        String cookieHeader = "fred=flintstone";
        List<Cookie> cookies = CookiesParser.createCookies(cookieHeader);
        assertEquals(cookies.size(), 1);
        Cookie c = cookies.get(0);
        assertEquals(c.getVersion(), 0);
        assertTrue(c.getName().equals("fred"));
        assertTrue(c.getValue().equals("flintstone"));
        
        cookieHeader = "fred=flintstone,barney=rubble";
        cookies = CookiesParser.createCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 0);
        assertTrue(c.getName().equals("fred"));
        assertTrue(c.getValue().equals("flintstone"));
        c = cookies.get(1);
        assertEquals(c.getVersion(), 0);
        assertTrue(c.getName().equals("barney"));
        assertTrue(c.getValue().equals("rubble"));

        cookieHeader = "fred=flintstone;barney=rubble";
        cookies = CookiesParser.createCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 0);
        assertTrue(c.getName().equals("fred"));
        assertTrue(c.getValue().equals("flintstone"));
        c = cookies.get(1);
        assertEquals(c.getVersion(), 0);
        assertTrue(c.getName().equals("barney"));
        assertTrue(c.getValue().equals("rubble"));
    
        cookieHeader = "$Version=1;fred=flintstone;$Path=/path;barney=rubble";
        cookies = CookiesParser.createCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 1);
        assertTrue(c.getName().equals("fred"));
        assertTrue(c.getValue().equals("flintstone"));
        assertTrue(c.getPath().equals("/path"));
        c = cookies.get(1);
        assertEquals(c.getVersion(), 1);
        assertTrue(c.getName().equals("barney"));
        assertTrue(c.getValue().equals("rubble"));

        cookieHeader = "$Version=1;fred=flintstone;$Path=/path,barney=rubble;$Domain=.sun.com";
        cookies = CookiesParser.createCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 1);
        assertTrue(c.getName().equals("fred"));
        assertTrue(c.getValue().equals("flintstone"));
        assertTrue(c.getPath().equals("/path"));
        c = cookies.get(1);
        assertEquals(c.getVersion(), 1);
        assertTrue(c.getName().equals("barney"));
        assertTrue(c.getValue().equals("rubble"));
        assertTrue(c.getDomain().equals(".sun.com"));

        cookieHeader = "$Version=1; fred = flintstone ; $Path=/path, barney=rubble ;$Domain=.sun.com";
        cookies = CookiesParser.createCookies(cookieHeader);
        assertEquals(cookies.size(), 2);
        c = cookies.get(0);
        assertEquals(c.getVersion(), 1);
        assertTrue(c.getName().equals("fred"));
        assertTrue(c.getValue().equals("flintstone"));
        assertTrue(c.getPath().equals("/path"));
        c = cookies.get(1);
        assertEquals(c.getVersion(), 1);
        assertTrue(c.getName().equals("barney"));
        assertTrue(c.getValue().equals("rubble"));
        assertTrue(c.getDomain().equals(".sun.com"));
    }
    
    /**
     * Test of toString method, of class com.sun.ws.rest.impl.http.header.CookiesParser.
     */
    public void testToStringWithProvider() {
        NewCookieProvider ncp = new NewCookieProvider();
        
        NewCookie cookie = new NewCookie("fred", "flintstone");
        
        String expResult = "fred=flintstone;Version=1";
        String result = ncp.toString(cookie);
        assertEquals(expResult, result);
        
        cookie = new NewCookie("fred", "flintstone", null, null, 
                null, 60, false);
        expResult = "fred=flintstone;Version=1;Max-Age=60";
        result = ncp.toString(cookie);
        assertEquals(expResult, result);
        
        cookie = new NewCookie("fred", "flintstone", null, null, 
                "a modern stonage family", 60, false);
        expResult = "fred=flintstone;Version=1;Comment=\"a modern stonage family\";Max-Age=60";
        result = ncp.toString(cookie);
        assertEquals(expResult, result);
    }
}
