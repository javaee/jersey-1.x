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
package com.sun.jersey.impl;

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

    public void testPath() {
        assertEquals("/a/b/c", UriComponent.encode("/a/b/c", UriComponent.Type.PATH));
        assertEquals("/a%20/b%20/c%20", UriComponent.encode("/a /b /c ", UriComponent.Type.PATH));
        assertEquals("/copyright%C2%A9", UriComponent.encode("/copyright\u00a9", UriComponent.Type.PATH));
    }
    
}
