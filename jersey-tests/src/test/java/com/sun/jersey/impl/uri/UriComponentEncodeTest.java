/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.impl.uri;

import com.sun.jersey.api.uri.UriComponent;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class UriComponentEncodeTest extends TestCase {
    
    public UriComponentEncodeTest(String testName) {
        super(testName);
    }

    public void testEncodePath() {
        assertEquals("/a/b/c", 
                UriComponent.encode("/a/b/c", UriComponent.Type.PATH));
        assertEquals("/a%20/b%20/c%20", 
                UriComponent.encode("/a /b /c ", UriComponent.Type.PATH));
        assertEquals("/copyright%C2%A9", 
                UriComponent.encode("/copyright\u00a9", UriComponent.Type.PATH));
    }
    
    public void testContextualEncodePath() {
        assertEquals("/a/b/c", 
                UriComponent.contextualEncode("/a/b/c", UriComponent.Type.PATH));
        assertEquals("/a%20/b%20/c%20", 
                UriComponent.contextualEncode("/a /b /c ", UriComponent.Type.PATH));
        assertEquals("/copyright%C2%A9", 
                UriComponent.contextualEncode("/copyright\u00a9", UriComponent.Type.PATH));
        
        assertEquals("/a%20/b%20/c%20", 
                UriComponent.contextualEncode("/a%20/b%20/c%20", UriComponent.Type.PATH));
        assertEquals("/copyright%C2%A9", 
                UriComponent.contextualEncode("/copyright%C2%A9", UriComponent.Type.PATH));        
    }
    
    public void testEncodeQuery() {
        assertEquals("a+b+c.-*_=+",
                UriComponent.encode("a b c.-*_=+", UriComponent.Type.QUERY));
        assertEquals("a+b+c.-*_%3D%2B",
                UriComponent.encode("a b c.-*_=+", UriComponent.Type.QUERY_PARAM));
    }
    
    public void testContextualEncodeQuery() {
        assertEquals("a+b+c.-*_=+",
                UriComponent.contextualEncode("a b c.-*_=+", UriComponent.Type.QUERY));
        System.out.println(UriComponent.contextualEncode("a b c.-*_=+", UriComponent.Type.QUERY_PARAM));
        assertEquals("a+b+c.-*_%3D+",
                UriComponent.contextualEncode("a b c.-*_=+", UriComponent.Type.QUERY_PARAM));
    }

    public void testContextualEncodeQueryParam() {
        assertEquals("a%3Db+c",
                UriComponent.contextualEncode("a=b c", UriComponent.Type.QUERY_PARAM));
    }

    public void testContextualEncodeMatrixParam() {
        assertEquals("a%3Db%20c",
                UriComponent.contextualEncode("a=b c", UriComponent.Type.MATRIX_PARAM));
    }    
    
    public void testContextualEncodePercent() {
        assertEquals("%25",
                UriComponent.contextualEncode("%", UriComponent.Type.PATH));
        assertEquals("a%25",
                UriComponent.contextualEncode("a%", UriComponent.Type.PATH));
        assertEquals("a%25x",
                UriComponent.contextualEncode("a%x", UriComponent.Type.PATH));
        assertEquals("a%25%20%20",
                UriComponent.contextualEncode("a%  ", UriComponent.Type.PATH));

        assertEquals("a%20a%20%20",
                UriComponent.contextualEncode("a a%20 ", UriComponent.Type.PATH));

    }
}
