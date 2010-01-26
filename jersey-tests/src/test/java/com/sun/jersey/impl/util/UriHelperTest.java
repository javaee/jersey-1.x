/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.jersey.impl.util;

import com.sun.jersey.server.impl.uri.UriHelper;
import junit.framework.*;
import java.net.URI;
import java.net.URISyntaxException;

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
