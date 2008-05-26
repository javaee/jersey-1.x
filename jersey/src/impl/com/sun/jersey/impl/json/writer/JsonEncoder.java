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
package com.sun.jersey.impl.json.writer;

import java.util.Formatter;

/**
 *
 * @author japod
 */
public class JsonEncoder {

    public static String encode(String text) {
        if ((null == text) || (text.length() == 0)) {
            return text;
        }
        Formatter formatter = new java.util.Formatter();
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    if (c < ' ') { // TODO: what about c > 255 ???
                        result.append(formatter.format("\\u%04X", (int)c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.toString();
    }
}
