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

package com.sun.ws.rest.impl.provider.header;

import com.sun.ws.rest.impl.http.header.HttpHeaderFactory;
import com.sun.ws.rest.impl.http.header.writer.WriterUtil;
import com.sun.ws.rest.spi.HeaderDelegateProvider;
import javax.ws.rs.core.NewCookie;

public class NewCookieProvider implements HeaderDelegateProvider<NewCookie> {
    
    public boolean supports(Class<?> type) {
        return type == NewCookie.class;
    }

    public String toString(NewCookie cookie) {
        StringBuilder b = new StringBuilder();
                
        b.append(cookie.getName()).append('=');
        WriterUtil.appendQuotedIfWhitespace(b, cookie.getValue());
        
        b.append(";").append("Version=").append(cookie.getVersion());
        
        if (cookie.getComment() != null) {
            b.append(";Comment=");
            WriterUtil.appendQuotedIfWhitespace(b, cookie.getComment());
        }
        if (cookie.getDomain() != null) {
            b.append(";Domain=");
            WriterUtil.appendQuotedIfWhitespace(b, cookie.getDomain());
        }
        if (cookie.getPath() != null) {
            b.append(";Path=");
            WriterUtil.appendQuotedIfWhitespace(b, cookie.getPath());
        }
        if (cookie.getMaxAge()!=-1) {
            b.append(";Max-Age=");
            b.append(cookie.getMaxAge());
        }
        if (cookie.isSecure())
            b.append(";Secure");
        return b.toString();        
    }

    public NewCookie fromString(String header) {
        return HttpHeaderFactory.createNewCookie(header);
    }
}