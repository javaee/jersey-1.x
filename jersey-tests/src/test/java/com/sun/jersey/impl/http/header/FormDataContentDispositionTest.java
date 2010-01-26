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
package com.sun.jersey.impl.http.header;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.header.HttpDateFormat;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author Imran@SmartITEngineering.Com
 */
public class FormDataContentDispositionTest
    extends ContentDispositionTest {

    public FormDataContentDispositionTest(String testName) {
        super(testName);
        contentDispositionType = "form-data";
    }

    @Override
    public void testCreate() {
        System.out.println("create");
        Date date = new Date();
        FormDataContentDisposition contentDisposition;
        contentDisposition = FormDataContentDisposition.name("testData").
            fileName("test.file").creationDate(date).modificationDate(date).
            readDate(date).size(1222).build();
        assertFormDataContentDisposition(contentDisposition, date);
        try {
            String dateString = HttpDateFormat.getPreferedDateFormat().format(
                date);
            String header = new StringBuilder(contentDispositionType).append(
                ";filename=\"test.file\";creation-date=\"").append(
                dateString).append("\";modification-date=\"").append(dateString).
                append("\";read-date=\"").append(dateString).append(
                "\";size=1222").append(";name=\"testData\"").toString();
            contentDisposition = new FormDataContentDisposition(
                contentDisposition.toString());
            assertFormDataContentDisposition(contentDisposition, date);
            contentDisposition = new FormDataContentDisposition(header);
            assertFormDataContentDisposition(contentDisposition, date);
            contentDisposition = new FormDataContentDisposition(
                HttpHeaderReader.newInstance(header));
            assertFormDataContentDisposition(contentDisposition, date);
        }
        catch (ParseException ex) {
            fail(ex.getMessage());
        }
        try {
            contentDisposition = new FormDataContentDisposition(
                (HttpHeaderReader) null);
        }
        catch (ParseException exception) {
            fail(exception.getMessage());
        }
        catch (NullPointerException exception) {
            //expected
        }
        try {
            contentDisposition = new FormDataContentDisposition(
                "form-data;filename=\"test.file\"");
        }
        catch (ParseException exception) {
            fail(exception.getMessage());
        }
        catch (IllegalArgumentException exception) {
            //expected
        }
        try {
            contentDisposition = FormDataContentDisposition.name(null).build();
        }
        catch (IllegalArgumentException exception) {
            //expected
        }
        catch (Exception exception) {
            fail(exception.getMessage());
        }
    }

    @Override
    public void testToString() {
        Date date = new Date();
        FormDataContentDisposition contentDisposition;
        contentDisposition = FormDataContentDisposition.name("testData").
            fileName("test.file").creationDate(date).modificationDate(date).
            readDate(date).size(1222).build();
        String dateString = HttpDateFormat.getPreferedDateFormat().format(
            date);
        String header = new StringBuilder(contentDispositionType).append(
            ";filename=\"test.file\";creation-date=\"").append(
            dateString).append("\";modification-date=\"").append(dateString).
            append("\";read-date=\"").append(dateString).append(
            "\";size=1222").append(";name=\"testData\"").toString();
        assertEquals(header, contentDisposition.toString());
    }

    protected void assertFormDataContentDisposition(
        FormDataContentDisposition contentDisposition,
        Date date) {
        super.assertContentDisposition(contentDisposition, date);
        assertEquals("testData", contentDisposition.getName());
    }
}