/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.jersey.core.header.AcceptableToken;
import com.sun.jersey.core.header.AcceptableLanguageTag;
import com.sun.jersey.core.header.Token;
import com.sun.jersey.core.header.LanguageTag;
import com.sun.jersey.core.header.HttpDateFormat;
import com.sun.jersey.core.header.ParameterizedHeader;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpHeaderTest extends TestCase {
    
    public HttpHeaderTest(String testName) {
        super(testName);
    }

    public void testTokens() throws ParseException {
        String header = "type  /  content; a = \"asdsd\"";
        
        HttpHeaderReader r =HttpHeaderReader.newInstance(header);
        while (r.hasNext()) {
            r.next();
            System.out.println(r.getEvent() + " '" + r.getEventValue() + "'");
        }
    }
    
    public void testMediaType() throws ParseException {
        String mimeType = "application/xml;charset=UTF-8";
        MediaType m = MediaType.valueOf(mimeType);
    }
        
    public void testLanguageTag() throws ParseException {
        String languageTag = "en-US";
        LanguageTag l = new LanguageTag(languageTag);
    }
    
    public void testAcceptableLanguageTag() throws ParseException {
        String languageTag = "en-US;q=0.123";
        AcceptableLanguageTag l = new AcceptableLanguageTag(languageTag);
    }
    
    public void testAcceptableLanguageTagList() throws Exception {
        String languageTags = "en-US;q=0.123, fr;q=0.2, en;q=0.3, *;q=0.01";
        List<AcceptableLanguageTag> l = HttpHeaderReader.readAcceptLanguage(languageTags);
        assertEquals("en", l.get(0).getTag());
        assertEquals("fr", l.get(1).getTag());
        assertEquals("en-US", l.get(2).getTag());
        assertEquals("*", l.get(3).getTag());        
    }
    
    public void testToken() throws ParseException {
        String token = "gzip";
        Token t = new Token(token);
    }
    
    public void testAcceptableToken() throws ParseException {
        String token = "gzip;q=0.123";
        AcceptableToken t = new AcceptableToken(token);
    }
    
    public void testAcceptableTokenList() throws Exception {
        String tokens = "gzip;q=0.123, compress;q=0.2, zlib;q=0.3, *;q=0.01";
        List<AcceptableToken> l = HttpHeaderReader.readAcceptToken(tokens);
        assertEquals("zlib", l.get(0).getToken());
        assertEquals("compress", l.get(1).getToken());
        assertEquals("gzip", l.get(2).getToken());
        assertEquals("*", l.get(3).getToken());        
    }
    
    public void testDateParsing() throws ParseException {
        String date_RFC1123 = "Sun, 06 Nov 1994 08:49:37 GMT";
        String date_RFC1036 = "Sunday, 06-Nov-94 08:49:37 GMT";
        String date_ANSI_C = "Sun Nov  6 08:49:37 1994";
        
        Date d;
        d = HttpHeaderReader.readDate(date_RFC1123);
        d = HttpHeaderReader.readDate(date_RFC1036);
        d = HttpHeaderReader.readDate(date_ANSI_C);
    }
    
    public void testDateFormatting() throws ParseException {
        String date_RFC1123 = "Sun, 06 Nov 1994 08:49:37 GMT";
        Date date = HttpHeaderReader.readDate(date_RFC1123);
        
        String date_formatted = HttpDateFormat.getPreferedDateFormat().format(date);
        assertEquals(date_RFC1123, date_formatted);
    }

    public void testParameterizedHeader() throws ParseException {
        ParameterizedHeader ph = new ParameterizedHeader("a");
        assertEquals("a", ph.getValue());

        ph = new ParameterizedHeader("a/b");
        assertEquals("a/b", ph.getValue());

        ph = new ParameterizedHeader("  a  /  b  ");
        assertEquals("a/b", ph.getValue());

        ph = new ParameterizedHeader("");
        assertEquals("", ph.getValue());

        ph = new ParameterizedHeader(";");
        assertEquals("", ph.getValue());
        assertEquals(0, ph.getParameters().size());

        ph = new ParameterizedHeader(";;;");
        assertEquals("", ph.getValue());
        assertEquals(0, ph.getParameters().size());

        ph = new ParameterizedHeader("  ;  ;  ;  ");
        assertEquals("", ph.getValue());
        assertEquals(0, ph.getParameters().size());

        ph = new ParameterizedHeader("a;x=1;y=2");
        assertEquals("a", ph.getValue());
        assertEquals(2, ph.getParameters().size());
        assertEquals("1", ph.getParameters().get("x"));
        assertEquals("2", ph.getParameters().get("y"));

        ph = new ParameterizedHeader("a ;  x=1  ;  y=2  ");
        assertEquals("a", ph.getValue());
        assertEquals(2, ph.getParameters().size());
        assertEquals("1", ph.getParameters().get("x"));
        assertEquals("2", ph.getParameters().get("y"));
    }
}
