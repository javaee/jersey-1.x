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

import java.io.StringReader;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class JsonLexerTest extends TestCase {
    
    public JsonLexerTest(String testName) {
        super(testName);
    }            

    public void testNumbers() throws Exception {
        String testInput = "1 -45 12.355 0.123 -0.14 10e91 0e-12 0.12E14 -123.88E+34";
        JsonLexer lexer = new JsonLexer(new StringReader(testInput));
        JsonToken token;
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("1", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("-45", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("12.355", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("0.123", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("-0.14", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("10e91", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("0e-12", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("0.12E14", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        assertEquals("-123.88E+34", token.tokenText);
    }
    
    public void testBooleans() throws Exception {
        String testInput = "true false";
        JsonLexer lexer = new JsonLexer(new StringReader(testInput));
        JsonToken token;
        token = lexer.yylex();
        assertEquals(JsonToken.TRUE, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.FALSE, token.tokenType);
    }

    public void testNull() throws Exception {
        String testInput = "null";
        JsonLexer lexer = new JsonLexer(new StringReader(testInput));
        JsonToken token;
        token = lexer.yylex();
        assertEquals(JsonToken.NULL, token.tokenType);
    }
    
    public void testStrings() throws Exception {
        String testInput = "\"one\" \"one big\" \"one big \\n tower\" \"\\/ is slash\" \"other \\\" \\u0065 \\\\ symbols \\b\\f\\n\\r\\t\"";
        JsonLexer lexer = new JsonLexer(new StringReader(testInput));
        JsonToken token;
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        assertEquals("one", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        assertEquals("one big", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        assertEquals("one big \n tower", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        assertEquals("/ is slash", token.tokenText);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        assertEquals("other \" \u0065 \\ symbols \b\f\n\r\t", token.tokenText);
    }
    

    public void testJsonExprWithoutWhitespace() throws Exception {
        String testInput = "[{\"name\":\"jakub\",\"age\":12}]";
        JsonLexer lexer = new JsonLexer(new StringReader(testInput));
        JsonToken token;
        token = lexer.yylex();
        assertEquals(JsonToken.START_ARRAY, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.START_OBJECT, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.COLON, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.COMMA, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.COLON, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_OBJECT, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_ARRAY, token.tokenType);
    }
    
    public void testJsonExprWithWhitespace() throws Exception {
        String testInput = "[{ \"name\" : \"jakub\" ,\n\"age\" : 12}]";
        JsonLexer lexer = new JsonLexer(new StringReader(testInput));
        JsonToken token;
        token = lexer.yylex();
        assertEquals(JsonToken.START_ARRAY, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.START_OBJECT, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.COLON, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.COMMA, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.COLON, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.NUMBER, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_OBJECT, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_ARRAY, token.tokenType);
    }
    

}
