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

package com.sun.jersey.impl.util;

import com.sun.jersey.impl.uri.UriHelper;
import junit.framework.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class UriHelperTest extends TestCase {
    
    public UriHelperTest(String testName) {
        super(testName);
    }

    /**
     * Test of removeDotSegments method, of class com.sun.jersey.impl.util.UriHelper.
     */
    public void testRemoveDotSegments() {
        
        String path, expResult, result;

        path = "/a/b/c/./../../g";
        expResult = "/a/g";
        result = UriHelper.removeDotSegments(path, true);
        assertEquals(expResult, result);

        path = "/a//b/c/./../../g";
        expResult = "/a//g";
        result = UriHelper.removeDotSegments(path, true);
        assertEquals(expResult, result);

        path = "/a//b/c/./../../g";
        expResult = "/a/g";
        result = UriHelper.removeDotSegments(path, false);
        assertEquals(expResult, result);
        
        path = "mid/content=5/../6";
        expResult = "mid/6";
        result = UriHelper.removeDotSegments(path, true);
        assertEquals(expResult, result);

        path = "./jenda";
        expResult = "jenda";
        result = UriHelper.removeDotSegments(path, true);
        assertEquals(expResult, result);
        
        path = "/./jenda";
        expResult = "/jenda";
        result = UriHelper.removeDotSegments(path, true);
        assertEquals(expResult, result);
        
        path = "/uri/http://jersey.dev.java.net";
        expResult = "/uri/http://jersey.dev.java.net";
        result = UriHelper.removeDotSegments(path, true);
        assertEquals(expResult, result);
    }

    /**
     * Test of normalize method, of class com.sun.jersey.impl.util.UriHelper.
     */
    public void testNormalize() {
        System.out.println("normalize");
        URI uri, expResult, result;

        try {
            uri = new URI("http://example.org/foo/../r%65source/uri/http://example.org");
            expResult = new URI("http://example.org/r%65source/uri/http://example.org");;
            result = UriHelper.normalize(uri, true);
            assertEquals(expResult, result);
            assertEquals("/resource/uri/http://example.org", result.getPath()); // double check

            uri = new URI("http://example.org/resource//customers//");
            expResult = new URI("http://example.org/resource//customers//");;
            result = UriHelper.normalize(uri, true);
            assertEquals(expResult, result);
            assertEquals("/resource//customers//", result.getPath()); // double check

            uri = new URI("http://example.org/resource//customers//");
            expResult = new URI("http://example.org/resource/customers/");;
            result = UriHelper.normalize(uri, false);
            assertEquals(expResult, result);
            assertEquals("/resource/customers/", result.getPath()); // double check
            
        } catch (URISyntaxException ex) {
            fail("Unexpected URI syntax exception!");
        }
    }
    
}
