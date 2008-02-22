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
%%
%class JsonLexer
%unicode
%line
%char
%type JsonToken
//%state STRING
WHITE_SPACE_CHAR=[\n\r\ \t\b\012]
STRING_TEXT=(\\\"|[^\n\r\"]|\\{WHITE_SPACE_CHAR}+\\)*
%%
<YYINITIAL> {
  "," { return (new JsonToken(JsonToken.COMMA, yytext(), yyline, yychar, yychar+1)); }
  ":" { return (new JsonToken(JsonToken.COLON, yytext(), yyline, yychar, yychar+1)); }
  "[" { return (new JsonToken(JsonToken.START_ARRAY, yytext(), yyline, yychar, yychar+1)); }
  "]" { return (new JsonToken(JsonToken.END_ARRAY, yytext(), yyline, yychar, yychar+1)); }
  "{" { return (new JsonToken(JsonToken.START_OBJECT, yytext(), yyline, yychar, yychar+1)); }
  "}" { return (new JsonToken(JsonToken.END_OBJECT, yytext(), yyline, yychar, yychar+1)); }
  \"{STRING_TEXT}\" {
    String str =  yytext().substring(1, yylength()-1);
    return (new JsonToken(JsonToken.STRING, str, yyline, yychar, yychar+yylength()));
  }
  {WHITE_SPACE_CHAR} {}
}


