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

package com.sun.jersey.impl.http.header;

import com.sun.jersey.impl.http.header.reader.HttpHeaderReader;
import com.sun.jersey.impl.http.header.reader.HttpHeaderReaderImpl;
import java.text.ParseException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class Token {
    protected String token;
        
    protected Token() {    
    }
    
    public Token(String header) throws ParseException {
        this(new HttpHeaderReaderImpl(header));
    }
    
    public Token(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();
        
        token = reader.nextToken();        
        
        if (reader.hasNext())
            throw new ParseException("Invalid token", reader.getIndex());
    }
    
    public String getToken() {
        return token;
    }
    
    public final boolean isCompatible(String token) {
        if (this.token.equals("*"))
            return true;
        
        return this.token.equals(token);
    }    
}