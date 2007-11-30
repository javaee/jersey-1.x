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

import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReaderImpl;
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
        
        HttpHeaderReaderImpl r = new HttpHeaderReaderImpl(header);
        while (r.hasNext()) {
            r.next();
            System.out.println(r.getEvent() + " '" + r.getEventValue() + "'");
        }
    }
    
    public void testMediaType() throws ParseException {
        String mimeType = "application/xml;charset=UTF-8";
        MediaType m = MediaType.parse(mimeType);
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
        String languageTags = "en-US;q=0.123, en-US;q=0.2, en-US;q=0.3, *;q=0.01";
        List<AcceptableLanguageTag> l = HttpHeaderReader.readList(AcceptableLanguageTag.class, languageTags);
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
        String tokens = "gzip;q=0.123, gzip;q=0.2, gzip;q=0.3, *;q=0.01";
        List<AcceptableToken> l = HttpHeaderReader.readList(AcceptableToken.class, tokens);
    }
    
    public void testEntityTag() throws ParseException {
        String entityTag = "W/\"a    b\"";
        EntityTag e = new EntityTag(entityTag);
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

}
