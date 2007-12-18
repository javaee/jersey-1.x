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

package com.sun.ws.rest.impl.http.header;

import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReaderImpl;
import java.text.ParseException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class LanguageTag {
    
    protected String tag;
    
    protected String primaryTag;
    
    protected String subTags;
    
    protected LanguageTag() {    
    }
    
    public LanguageTag(String primaryTag, String subTags) {
        if (subTags != null && subTags.length() > 0)
            this.tag = primaryTag + "-" + subTags;
        else 
            this.tag = primaryTag;
        
        this.primaryTag = primaryTag;
        this.subTags = subTags;
    }
    
    public LanguageTag(String header) throws ParseException {
        this(new HttpHeaderReaderImpl(header));
    }
    
    public LanguageTag(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();
        
        tag = reader.nextToken();
        
        if (reader.hasNext())
            throw new ParseException("Invalid Language tag", reader.getIndex());
        
        parse(tag);
    }
    
    public final boolean isCompatible(String tag) {
        if (this.tag.equals("*"))
            return true;
        
        return this.tag.equals(tag);
    }
    
    protected final void parse(String languageTag) throws ParseException {
        if (!isValid(languageTag)) {
            throw new ParseException("String, " + languageTag + ", is not a valid language tag", 0);
        }
        
        int index = languageTag.indexOf('-');
        if (index == -1) {
            primaryTag = languageTag;
            subTags = null;
        } else {
            primaryTag = languageTag.substring(0, index);
            subTags = languageTag.substring(index + 1, languageTag.length());
        }        
    }
    
    private boolean isValid(String tag) {
        int alphaCount = 0;
        for (int i = 0; i < tag.length(); i++) {
            final char c = tag.charAt(i);
            if (c == '-') {
                if (alphaCount == 0)
                    return false;
                alphaCount = 0;
            } else if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
                alphaCount++;
                if (alphaCount > 8)
                    return false;
            } else {
                return false;
            }
        }
        return (alphaCount != 0);
    }

    public final String getTag() {
        return tag;
    }

    public final String getPrimaryTag() {
        return primaryTag;
    }

    public final String getSubTags() {
        return subTags;
    }
}
