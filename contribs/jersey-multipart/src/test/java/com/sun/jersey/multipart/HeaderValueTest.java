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

package com.sun.jersey.multipart;

import java.util.List;
import junit.framework.TestCase;

/**
 * <p>Unit tests for {@link HeaderValue}.</p>
 */
public class HeaderValueTest extends TestCase {
    
    public HeaderValueTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCompoundValues() {
        HeaderValue hv = null;
        hv = new HeaderValue("text/html;q=0.5");
        assertEquals("text/html", hv.getValue());
        assertEquals(1, hv.getParameters().size());
        assertEquals("0.5", hv.getParameters().get("q"));
        hv = new HeaderValue("  text/html ; q=\"0.8\" ; \"r\" = 0.9");
        assertEquals("text/html", hv.getValue());
        assertEquals(2, hv.getParameters().size());
        assertEquals("0.8", hv.getParameters().get("q"));
        assertEquals("0.9", hv.getParameters().get("r"));
    }

    public void testInvalidArguments() {
        HeaderValue hv = null;
        try {
            hv = new HeaderValue(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        try {
            hv = new HeaderValue();
            hv.parseHeaderValue(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        try {
            hv = new HeaderValue();
            hv.setValue(null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        try {
            hv = new HeaderValue("foo,bar");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        try {
            hv = new HeaderValue();
            hv.parseHeaderValue("foo,bar");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        try {
            hv = new HeaderValue();
            hv.setValue("foo,bar");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
        try {
            hv = new HeaderValue();
            hv.setValue("foo;bar");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected result
        }
    }

    public void testMultipleValues() {
        List<HeaderValue> list =
          HeaderValue.parseHeaderValues("text/plain; q=0.5, text/html, text/x-dvi; q=0.8, text/x-c");
        assertEquals(4, list.size());
        HeaderValue hv = null;
        hv = list.get(0);
        assertEquals("text/plain", hv.getValue());
        assertEquals(1, hv.getParameters().size());
        assertEquals("0.5", hv.getParameters().get("q"));
        hv = list.get(1);
        assertEquals("text/html", hv.getValue());
        assertEquals(0, hv.getParameters().size());
        hv = list.get(2);
        assertEquals("text/x-dvi", hv.getValue());
        assertEquals(1, hv.getParameters().size());
        assertEquals("0.8", hv.getParameters().get("q"));
        hv = list.get(3);
        assertEquals("text/x-c", hv.getValue());
        assertEquals(0, hv.getParameters().size());
    }

    public void testSimpleValues() throws Exception {
        HeaderValue hv = null;
        hv = new HeaderValue("foobar");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue("  foobar  ");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue("\"foobar\"");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue("  \"  foobar  \"  ");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue();
        hv.setValue("foobar");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue();
        hv.setValue("   foobar   ");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue();
        hv.setValue("\"foobar\"");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue();
        hv.setValue("  \"foobar\"  ");
        assertEquals("foobar", hv.getValue());
        assertEquals("foobar", hv.toString());
    }


    public void testToString() {
        HeaderValue hv = null;
        hv = new HeaderValue("foobar");
        assertEquals("foobar", hv.toString());
        hv = new HeaderValue("\"foobar\"");
        assertEquals("foobar", hv.toString()); // FIXME - should we ever expect quotes to be added?
        hv = new HeaderValue(" foobar ; a = b ");
        assertEquals("foobar;a=b", hv.toString());
        hv = new HeaderValue("\"foobar\";\"a\"=\"b\";\"c\"=\"d\"");
        assertTrue(hv.toString().equals("foobar;a=b;c=d") || hv.toString().equals("foobar;c=d;a=b"));
    }


}
