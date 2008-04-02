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

import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

/**
 * TODO use the HttpHeaderReader
 * 
 * @author Marc.Hadley@Sun.Com
 */
/* protected */ class CookiesParser {
    private static class MutableCookie {
        String name;
        String value;
        int version = Cookie.DEFAULT_VERSION;
        String path = null;
        String domain = null;

        public MutableCookie(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Cookie getImmutableCookie() {
            return new Cookie(name, value, path, domain, version);
        }        
    }
    
    public static Map<String, Cookie> parseCookies(String header) {
        String bites[] = header.split("[;,]");
        Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
        int version = 0;
        MutableCookie cookie = null;
        for (String bite: bites) {
            String crumbs[] = bite.split("=", 2);
            String name = crumbs.length>0 ? crumbs[0].trim() : "";
            String value = crumbs.length>1 ? crumbs[1].trim() : "";
            if (value.startsWith("\"") && value.endsWith("\"") && value.length()>1)
                value = value.substring(1,value.length()-1);
            if (!name.startsWith("$")) {
                if (cookie != null)
                    cookies.put(cookie.name, cookie.getImmutableCookie());
                
                cookie = new MutableCookie(name, value);
                cookie.version = version;
            }
            else if (name.startsWith("$Version"))
                version = Integer.parseInt(value);
            else if (name.startsWith("$Path") && cookie!=null)
                cookie.path = value;
            else if (name.startsWith("$Domain") && cookie!=null)
                cookie.domain = value;
        }
        if (cookie != null)
            cookies.put(cookie.name, cookie.getImmutableCookie());
        return cookies;
    }
    
    public static Cookie parseCookie(String header) {
        Map<String, Cookie> cookies = parseCookies(header);
        return cookies.entrySet().iterator().next().getValue();
    }
    
    private static class MutableNewCookie {
        String name = null;
        String value = null;
        String path = null;
        String domain = null;
        int version = Cookie.DEFAULT_VERSION;
        String comment = null;
        int maxAge = NewCookie.DEFAULT_MAX_AGE;
        boolean secure = false;

        public MutableNewCookie(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public NewCookie getImmutableNewCookie() {
            return new NewCookie(name, value, path, domain, version, comment, maxAge, secure);
        }        
    }
    
    public static NewCookie parseNewCookie(String header) {
        String bites[] = header.split("[;,]");
        
        MutableNewCookie cookie = null;
        for (String bite: bites) {
            String crumbs[] = bite.split("=", 2);
            String name = crumbs.length>0 ? crumbs[0].trim() : "";
            String value = crumbs.length>1 ? crumbs[1].trim() : "";
            if (value.startsWith("\"") && value.endsWith("\"") && value.length()>1)
                value = value.substring(1,value.length()-1);
            
            if (cookie == null)
                cookie = new MutableNewCookie(name, value);
            else if (name.startsWith("Comment"))
                cookie.comment = value;
            else if (name.startsWith("Domain"))
                cookie.domain = value;
            else if (name.startsWith("Max-Age"))
                cookie.maxAge = Integer.parseInt(value);
            else if (name.startsWith("Path"))
                cookie.path = value;
            else if (name.startsWith("Secure"))
                cookie.secure = true;
            else if (name.startsWith("Version"))
                cookie.version = Integer.parseInt(value);
            else if (name.startsWith("Domain"))
                cookie.domain = value;
        }
        
        return cookie.getImmutableNewCookie();
    }
}