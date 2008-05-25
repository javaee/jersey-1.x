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

package com.sun.jersey.impl.provider.header;

import com.sun.jersey.impl.http.header.HttpHeaderFactory;
import com.sun.jersey.impl.http.header.writer.WriterUtil;
import com.sun.jersey.spi.HeaderDelegateProvider;
import javax.ws.rs.core.Cookie;

public class CookieProvider implements HeaderDelegateProvider<Cookie> {
    
    public boolean supports(Class<?> type) {
        return type == Cookie.class;
    }

    public String toString(Cookie cookie) {
        StringBuilder b = new StringBuilder();
        
        b.append("$Version=").append(cookie.getVersion()).append(';');
        
        b.append(cookie.getName()).append('=');
        WriterUtil.appendQuotedIfWhitespace(b, cookie.getValue());
        
        if (cookie.getDomain() != null) {
            b.append(";$Domain=");
            WriterUtil.appendQuotedIfWhitespace(b, cookie.getDomain());
        }
        if (cookie.getPath() != null) {
            b.append(";$Path=");
            WriterUtil.appendQuotedIfWhitespace(b, cookie.getPath());
        }
        return b.toString();
    }

    public Cookie fromString(String header) {
        return HttpHeaderFactory.createCookie(header);
    }
}