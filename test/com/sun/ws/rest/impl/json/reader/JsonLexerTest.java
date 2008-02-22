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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test of yylex method, of class JsonLexer.
     */
    public void testJsonWithoutWhitespace() throws Exception {
        System.out.println("yylex");
        String testInput = "[{\"name\":\"jakub\",\"age\":\"12\"}]";
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
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_OBJECT, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_ARRAY, token.tokenType);
    }
    
    public void testJsonWithWhitespace() throws Exception {
        System.out.println("yylex");
        String testInput = "[{ \"name\" : \"jakub\" ,\n\"age\" : \"12\"}]";
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
        assertEquals(JsonToken.STRING, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_OBJECT, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_ARRAY, token.tokenType);
    }
    

}
