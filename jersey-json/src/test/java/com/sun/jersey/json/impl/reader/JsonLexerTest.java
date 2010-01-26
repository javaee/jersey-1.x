/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.json.impl.reader;

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

    public void testEmpty() throws Exception {
        String testInput = "[]{}";
        JsonLexer lexer = new JsonLexer(new StringReader(testInput));
        JsonToken token;
        token = lexer.yylex();
        assertEquals(JsonToken.START_ARRAY, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_ARRAY, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.START_OBJECT, token.tokenType);
        token = lexer.yylex();
        assertEquals(JsonToken.END_OBJECT, token.tokenType);
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
