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

import java.io.CharArrayReader;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpHeadersTest extends TestCase {
    
    public HttpHeadersTest(String testName) {
        super(testName);
    }
    
    
    public void testGet() {
        RequestHttpHeadersImpl h = new RequestHttpHeadersImpl();
        
        h.add("Content-Type", "value");
        
        String s = h.getFirst("content-type");
        assertEquals("value", s);
        s = h.getFirst("cONTENT-tYPE");
        assertEquals("value", s);
    }
    
    public void testPut() {
        RequestHttpHeadersImpl h = new RequestHttpHeadersImpl();
        
        h.add("Content-Type", "value");

        h.get("CONTENT-TYPE").set(0, "value1");
        
        String s = h.getFirst("content-type");
        assertEquals("value1", s);
        s = h.getFirst("cONTENT-tYPE");
        assertEquals("value1", s);
    }
    
    public void testMoreGet() {
        RequestHttpHeadersImpl h = new RequestHttpHeadersImpl();
        
        for (int i = 0; i < 100; i++) {
            String key = generate(i);
            String value = key;
            
            h.add(key, value);
            assertEquals(value, h.getFirst(key));

            value = value + "NEW";
            
            h.get(key).set(0, value);
            assertEquals(value, h.getFirst(key));
        }
    }
    
    private String generate(int size) {
        StringBuilder b = new StringBuilder();
        char c = 'A';
        while (size-- > 0) {
            b.append(Character.toLowerCase(c));
            b.append(Character.toUpperCase(c));
            c++;
        }
        
        return b.toString();
    }
    
}
