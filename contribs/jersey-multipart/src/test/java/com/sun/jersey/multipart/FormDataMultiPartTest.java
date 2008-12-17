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

import javax.ws.rs.core.MediaType;

/**
 * <p>Test case for {@link MultiPartImpl}.</p>
 */
public class FormDataMultiPartTest extends MultiPartTest {

    public FormDataMultiPartTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        multiPart = new FormDataMultiPart();
    }

    @Override
    protected void tearDown() throws Exception {
        multiPart = null;
        super.tearDown();
    }

    @SuppressWarnings("empty-statement")
    public void testCreateFDMP() {
    }

    @SuppressWarnings("empty-statement")
    public void testFieldsFDMP() {

        FormDataMultiPart fdmp = (FormDataMultiPart) multiPart;
        assertEquals(0, fdmp.getFields().size());
        fdmp = fdmp.field("foo", "bar").field("baz", "bop");
        assertEquals(2, fdmp.getFields().size());
        assertNotNull(fdmp.getField("foo"));
        assertEquals("bar", fdmp.getField("foo").getValue());
        assertNotNull(fdmp.getField("baz"));
        assertEquals("bop", fdmp.getField("baz").getValue());
        assertNotNull(fdmp.getFields().get("foo"));
        assertEquals("bar", fdmp.getFields().get("foo").getValue());
        assertNotNull(fdmp.getFields().get("baz"));
        assertEquals("bop", fdmp.getFields().get("baz").getValue());

    }


}
