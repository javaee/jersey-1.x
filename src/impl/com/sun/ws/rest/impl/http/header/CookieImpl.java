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

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class CookieImpl {
    
    public static List<Cookie> createCookies(String header) {
        String bites[] = header.split("[;,]");
        List<Cookie> cookies = new ArrayList<Cookie>();
        int version = 0;
        NewCookie cookie = null;
        for (String bite: bites) {
            String crumbs[] = bite.split("=", 2);
            String name = crumbs.length>0 ? crumbs[0].trim() : "";
            String value = crumbs.length>1 ? crumbs[1].trim() : "";
            if (value.startsWith("\"") && value.endsWith("\"") && value.length()>1)
                value = value.substring(1,value.length()-1);
            if (!name.startsWith("$")) {
                if (cookie != null)
                    cookies.add(cookie);
                cookie = new NewCookie(name, value);
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
            cookies.add(cookie);
        return cookies;
    }    
}
