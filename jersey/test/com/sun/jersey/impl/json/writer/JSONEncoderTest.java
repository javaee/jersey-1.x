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

package com.sun.jersey.impl.json.writer;

import com.sun.jersey.impl.json.writer.JsonEncoder;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class JSONEncoderTest extends TestCase {
    
    public void testSimpleText() {
        assertEquals("one two three", JsonEncoder.encode("one two three"));
        assertEquals("", JsonEncoder.encode(""));
    }

    public void testBackslashEncodedChars() {
        assertEquals("one \\\"two\\\" three", JsonEncoder.encode("one \"two\" three"));
        assertEquals("one\\\\two\\\\three", JsonEncoder.encode("one\\two\\three"));
        assertEquals("onee\\btwoo\\bthreee\\b", JsonEncoder.encode("onee\btwoo\bthreee\b"));
        assertEquals("one\\ftwo\\fthree", JsonEncoder.encode("one\ftwo\fthree"));
        assertEquals("one\\ntwo\\nthree", JsonEncoder.encode("one\ntwo\nthree"));
        assertEquals("one\\rtwo\\rthree", JsonEncoder.encode("one\rtwo\rthree"));
        assertEquals("one\\ttwo\\tthree", JsonEncoder.encode("one\ttwo\tthree"));
    }
    
    public void testUnicodeValEncodedChars() {
        // TODO: do we want to encode such chars (code>255) ?
//        assertEquals("\\u010Ce", JsonEncoder.encode("\u010Ce"));
//        assertEquals("\\u1401e", JsonEncoder.encode("\u1401e"));
        assertEquals("\\u0000e", JsonEncoder.encode("\u0000e"));
        assertEquals("\\u0001e", JsonEncoder.encode("\u0001e"));
    }
    
    public void testEncodeNull() {
        assertNull(JsonEncoder.encode(null));
    }
}
