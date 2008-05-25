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

package com.sun.jersey.impl.http.header.writer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class WriterUtil {
    
    private static Pattern whitespace = Pattern.compile("\\s");

    private static Pattern whitespaceOrQuote = Pattern.compile("[\\s\"]");
    
    public static void appendQuotedMediaType(StringBuilder b, String value) {
        if (value==null)
            return;
        Matcher m = whitespaceOrQuote.matcher(value);
        boolean quote = m.find();
        if (quote)
            b.append('"');
        appendEscapingQuotes(b, value);
        if (quote)
            b.append('"');        
    }
    
    public static void appendQuotedIfWhitespace(StringBuilder b, String value) {
        if (value==null)
            return;
        Matcher m = whitespace.matcher(value);
        boolean quote = m.find();
        if (quote)
            b.append('"');
        appendEscapingQuotes(b, value);
        if (quote)
            b.append('"');
    }
    
    public static void appendQuoted(StringBuilder b, String value) {
        b.append('"');
        appendEscapingQuotes(b, value);
        b.append('"');
    }
    
    public static void appendEscapingQuotes(StringBuilder b, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"')
                b.append('\\');
            b.append(c);
        }
    }
    
}
