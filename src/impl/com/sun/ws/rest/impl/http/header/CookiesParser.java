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

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Cookie;

/**
 * TODO use the HttpHeaderReader
 * 
 * @author Marc.Hadley@Sun.Com
 */
/* protected */ class CookiesParser {
    private static class MutableCookie {
        private String name;
        private String value;
        private int version = -1;
        private String path = null;
        private String domain = null;

        public MutableCookie(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Cookie getImmutableCookie() {
            return new Cookie(name, value, path, domain, version);
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }


        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }


        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }


        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }
    
    public static Map<String, Cookie> createCookies(String header) {
        String bites[] = header.split("[;,]");
        Map<String, Cookie> cookies = new HashMap<String, Cookie>();
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
                    cookies.put(cookie.getName(), cookie.getImmutableCookie());
                
                cookie = new MutableCookie(name, value);
                cookie.setVersion(version);
            }
            else if (name.startsWith("$Version"))
                version = Integer.parseInt(value);
            else if (name.startsWith("$Path") && cookie!=null)
                cookie.setPath(value);
            else if (name.startsWith("$Domain") && cookie!=null)
                cookie.setDomain(value);
        }
        if (cookie != null)
            cookies.put(cookie.getName(), cookie.getImmutableCookie());
        return cookies;
    }

}
