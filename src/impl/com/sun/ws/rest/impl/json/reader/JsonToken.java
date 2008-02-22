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

package com.sun.ws.rest.impl.json.reader;

/**
 *
 * @author japod
 */
class JsonToken {

    public static final int START_OBJECT = 1;
    public static final int END_OBJECT = 2;
    public static final int START_ARRAY = 3;
    public static final int END_ARRAY = 4;
    public static final int COLON = 5;
    public static final int COMMA = 6;
    public static final int STRING = 7;
    public static final int NUMBER = 8;
    public static final int TRUE = 9;
    public static final int FALSE = 10;
    public static final int NULL = 11;

    public int tokenType;
    public String tokenText;
    public int line;
    public int charBegin;
    public int charEnd;

    JsonToken(int tokenType, String text, int line, int charBegin, int charEnd) {
        this.tokenType = tokenType;
        this.tokenText = text;
        this.line = line;
        this.charBegin = charBegin;
        this.charEnd = charEnd;
    }

    @Override
    public String toString() {
        return "(token|" + tokenType + "|" + tokenText + ")";
    }
}

