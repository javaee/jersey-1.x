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

import com.sun.ws.rest.spi.HeaderDelegateProvider;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.CacheControl;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class CacheControlProvider implements HeaderDelegateProvider<CacheControl> {
    private static Pattern WHITESPACE = Pattern.compile("\\s");
    
    public boolean supports(Class<?> type) {
        return type == CacheControl.class;
    }

    public String toString(CacheControl header) {
        StringBuffer b = new StringBuffer();
        if (header.isPublic())
            appendWithSeparator(b, "public");
        if (header.isPrivate())
            appendQuotedWithSeparator(b, "private", buildListValue(header.getPrivateFields()));
        if (header.isNoCache())
            appendQuotedWithSeparator(b, "no-cache", buildListValue(header.getNoCacheFields()));
        if (header.isNoStore())
            appendWithSeparator(b, "no-store");
        if (header.isNoTransform())
            appendWithSeparator(b, "no-transform");
        if (header.isMustRevalidate())
            appendWithSeparator(b, "must-revalidate");
        if (header.isProxyRevalidate())
            appendWithSeparator(b, "proxy-revalidate");
        if (header.getMaxAge() != -1)
            appendWithSeparator(b, "max-age", header.getMaxAge());
        if (header.getSMaxAge() != -1)
            appendWithSeparator(b, "s-maxage", header.getSMaxAge());
        
        for (Map.Entry<String, String> e : header.getCacheExtension().entrySet()) {
            appendWithSeparator(b, e.getKey(), quoteIfWhitespace(e.getValue()));
        }
                    
        return b.toString();        
    }

    public CacheControl fromString(String header) {
        throw new UnsupportedOperationException();
    }
    
    private void appendWithSeparator(StringBuffer b, String field) {
        if (b.length()>0)
            b.append(", ");
        b.append(field);
    }
    
    private void appendQuotedWithSeparator(StringBuffer b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && value.length() > 0) {
            b.append("=\"");
            b.append(value);
            b.append("\"");
        }
    }

    private void appendWithSeparator(StringBuffer b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && value.length() > 0) {
        b.append("=");
            b.append(value);
        }
    }

    private void appendWithSeparator(StringBuffer b, String field, int value) {
        appendWithSeparator(b, field);
        b.append("=");
        b.append(value);
    }

    private String buildListValue(List<String> values) {
        StringBuffer b = new StringBuffer();
        for (String value: values)
            appendWithSeparator(b, value);
        return b.toString();
    }
    
    private String quoteIfWhitespace(String value) {
        if (value==null)
            return null;
        Matcher m = WHITESPACE.matcher(value);
        if (m.find()) {
            return "\""+value+"\"";
        }
        return value;
    }    
}
