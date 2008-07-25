/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl.http.header;

import com.sun.jersey.impl.http.header.reader.HttpHeaderReader;
import com.sun.jersey.impl.http.header.reader.HttpHeaderReaderImpl;
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
