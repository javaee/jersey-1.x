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

package com.sun.ws.rest.impl.provider.header;

import javax.ws.rs.core.MediaType;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReaderImpl;
import com.sun.ws.rest.impl.http.header.writer.WriterUtil;
import javax.ws.rs.ext.HeaderProvider;
import java.text.ParseException;
import java.util.Map;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class MediaTypeProvider implements HeaderProvider<MediaType> {
    
    public boolean supports(Class<?> type) {
        return type == MediaType.class;
    }

    public String toString(MediaType header) {
        StringBuilder b = new StringBuilder();
        b.append(header.getType());
        b.append('/');
        b.append(header.getSubtype());
        for (String key: header.getParameters().keySet()) {
            b.append(';');
            b.append(key);
            b.append('=');
            WriterUtil.appendQuotedIfWhitespace(b, header.getParameters().get(key));
        }
        return b.toString();
    }

    public MediaType fromString(String header) throws ParseException {
        if (header==null)
            return new MediaType();
        HttpHeaderReader reader = new HttpHeaderReaderImpl(header);
        // Skip any white space
        reader.hasNext();
        
        // Get the type
        String type = reader.nextToken();
        reader.nextSeparator('/');
        // Get the subtype
        String subType = reader.nextToken();
        
        Map<String, String> params = null;
        
        if (reader.hasNext())
            params = HttpHeaderReader.readParameters(reader);
        
        return new MediaType(type,subType,params);
    }

}
