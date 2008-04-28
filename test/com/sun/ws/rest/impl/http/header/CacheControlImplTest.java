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

import junit.framework.*;
import com.sun.ws.rest.impl.provider.header.CacheControlProvider;
import javax.ws.rs.core.CacheControl;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class CacheControlImplTest extends TestCase {
    
    public CacheControlImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of toString method, of class com.sun.jersey.api.response.CacheControl.
     */
    public void testToString() {
        System.out.println("toString");
        
        CacheControlProvider p = new CacheControlProvider();
        CacheControl instance = new CacheControl();
        
        instance.setNoCache(true);
        String expResult = "public, no-cache, no-transform";
        String result = p.toString(instance);
        assertEquals(expResult, result);
        
        instance.setNoStore(true);
        expResult = "public, no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance.setPrivate(true);
        expResult = "public, private, no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance.getPrivateFields().add("Fred");
        expResult = "public, private=\"Fred\", no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);
        instance.getPrivateFields().add("Bob");
        expResult = "public, private=\"Fred, Bob\", no-cache, no-store, no-transform";
        result = p.toString(instance);
        assertEquals(expResult, result);
        
        instance = new CacheControl();
        instance.getCacheExtension().put("key1","value1");
        expResult = "public, no-transform, key1=value1";
        result = p.toString(instance);
        assertEquals(expResult, result);
        instance.getCacheExtension().put("key1","value1 with spaces");
        expResult = "public, no-transform, key1=\"value1 with spaces\"";
        result = p.toString(instance);
        assertEquals(expResult, result);
        
        instance.setNoStore(true);
        expResult = "public, no-store, no-transform, key1=\"value1 with spaces\"";
        result = p.toString(instance);
        assertEquals(expResult, result);

        instance = new CacheControl();
        instance.getCacheExtension().put("key1",null);
        expResult = "public, no-transform, key1";
        result = p.toString(instance);
        assertEquals(expResult, result);
        
    }
}
