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

package com.sun.jersey.impl.http.header;

import com.sun.jersey.impl.http.header.reader.HttpHeaderReader;
import com.sun.jersey.impl.http.header.reader.HttpHeaderReader.ListElementCreator;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpHeaderFactory {
    private static final ListElementCreator<AcceptableMediaType> ACCEPTABLE_MEDIA_TYPE_CREATOR = 
            new ListElementCreator<AcceptableMediaType>() {
        public AcceptableMediaType create(HttpHeaderReader reader) throws ParseException {
            return AcceptableMediaType.valueOf(reader);
        }
    };
    
    public static List<AcceptableMediaType> createAcceptMediaType(String header) throws ParseException {
        return HttpHeaderReader.readAcceptableList(ACCEPTABLE_MEDIA_TYPE_CREATOR, header);
    }
    
    public static final ListElementCreator<Token> TOKEN_CREATOR = 
            new ListElementCreator<Token>() {
        public Token create(HttpHeaderReader reader) throws ParseException {
            return new Token(reader);
        }
    };
    
    public static final ListElementCreator<AcceptableToken> ACCEPTABLE_TOKEN_CREATOR = 
            new ListElementCreator<AcceptableToken>() {
        public AcceptableToken create(HttpHeaderReader reader) throws ParseException {
            return new AcceptableToken(reader);
        }
    };
    
    public static List<AcceptableToken> createAcceptCharset(String header) throws ParseException {
        return HttpHeaderReader.readAcceptableList(ACCEPTABLE_TOKEN_CREATOR, header);
    }
    
    public static List<AcceptableToken> createAcceptEncoding(String header) throws ParseException {
        return HttpHeaderReader.readAcceptableList(ACCEPTABLE_TOKEN_CREATOR, header);
    }
    
    private static final ListElementCreator<AcceptableLanguageTag> LANGUAGE_CREATOR = 
            new ListElementCreator<AcceptableLanguageTag>() {
        public AcceptableLanguageTag create(HttpHeaderReader reader) throws ParseException {
            return new AcceptableLanguageTag(reader);
        }
    };
    
    public static List<AcceptableLanguageTag> createAcceptLanguage(String header) throws ParseException {
        return HttpHeaderReader.readAcceptableList(LANGUAGE_CREATOR, header);
    }

    public static Map<String, Cookie> createCookies(String header) {
        return CookiesParser.parseCookies(header);
    }
    
    public static Cookie createCookie(String header) {
        return CookiesParser.parseCookie(header);
    }
    
    public static NewCookie createNewCookie(String header) {
        return CookiesParser.parseNewCookie(header);
    }
    
    public static void createAllow(String header) {
        throw new UnsupportedOperationException();
    }
    
    public static void createCacheControl(String header) {
        throw new UnsupportedOperationException();
    }
    
    public static Token createContentEncoding(String header) throws ParseException {
        return new Token(header);
    }
    
    public static Token createContentLangauge(String header) throws ParseException {
        return new Token(header);
    }
    
    public static URI createContentLocation(String header) throws URISyntaxException {
        return new URI(header);
    }
    
    public static void createContentMD5(String header) throws ParseException {
        throw new UnsupportedOperationException();
    }
    
    public static MediaType createContentType(String header) throws ParseException {
        return MediaType.valueOf(header);
    }
    
    public static Date createDate(String header) throws ParseException {
        return HttpHeaderReader.readDate(header);
    }
    
    public static EntityTag createETag(String header) throws ParseException {
        return new EntityTag(header);
    }
    
    public static Date createExpires(String header) throws ParseException {
        return HttpHeaderReader.readDate(header);
    }
    
    public static void createExpect(String header) throws ParseException {
        throw new UnsupportedOperationException();
    }
    
    public static void createFrom(String header) throws ParseException {
        throw new UnsupportedOperationException();
    }
    
    public static List<String> createIfMatch(String header) throws ParseException, 
            NoSuchMethodException, InstantiationException, 
            IllegalAccessException, InvocationTargetException {
        if (header.equals("*"))
            return Collections.emptyList();
        
        // TODO support list of etags
        throw new UnsupportedOperationException();
    }
    
    public static Date createIfModifiedSince(String header) throws ParseException {
        return HttpHeaderReader.readDate(header);
    }
    
    public static List<String> createIfNoneMatch(String header) throws ParseException, 
            NoSuchMethodException, InstantiationException, 
            IllegalAccessException, InvocationTargetException {
        if (header.equals("*"))
            return Collections.emptyList();
        
        // TODO support list of etags
        throw new UnsupportedOperationException();
    }
    
    public static Date createIfUnmodifiedSince(String header) throws ParseException {
        return HttpHeaderReader.readDate(header);
    }
    
    public static Date createLastModified(String header) throws ParseException {
        return HttpHeaderReader.readDate(header);
    }
    
    public static URI createLocation(String header) throws URISyntaxException {
        URI u = new URI(header);
        if (!u.isAbsolute())
            throw new URISyntaxException(header, "URI is not absolute");
        
        return u;
    }
    
    public static URI createReferer(String header) throws URISyntaxException {
        return new URI(header);
    }
    
    public static void createUserAgent(String header) {
        throw new UnsupportedOperationException();
    }
    
    public static List<Token> createVary(String header) throws ParseException, 
            NoSuchMethodException, InstantiationException, 
            IllegalAccessException, InvocationTargetException {
        if (header.equals("*"))
            return null;
        return HttpHeaderReader.readList(TOKEN_CREATOR, header);
    }
}
