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
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderListAdapter;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReaderImpl;
import com.sun.ws.rest.impl.http.header.AcceptMediaType;
import com.sun.ws.rest.impl.model.MimeHelper;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AcceptMediaTypeProvider {
    private static final String QUALITY_FACTOR = "q";
    
    private static final int DEFAULT_QUALITY_FACTOR = 1000;
    
    @SuppressWarnings("unchecked")
    public static List<MediaType> fromString(String header) throws ParseException {
        List l = new ArrayList<AcceptMediaType>();
        HttpHeaderReader reader = new HttpHeaderReaderImpl(header);
        HttpHeaderListAdapter adapter = new HttpHeaderListAdapter(reader);
        while(reader.hasNext()) {
            l.add(create(adapter));
            adapter.reset();
            if (reader.hasNext())
                reader.next();
        }
    
        Collections.sort(l, MimeHelper.ACCEPT_MEDIA_TYPE_COMPARATOR);
        return l;
    }    
    
    private static AcceptMediaType create(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();
        
        // Get the type
        String type = reader.nextToken();
        String subType = "*";
        // Some HTTP implements use "*" to mean "*/*"
        if (reader.hasNextSeparator('/', false)) {
            reader.next(false);
            // Get the subtype
            subType = reader.nextToken();
        }
        
        Map<String, String> parameters = null;
        int quality = DEFAULT_QUALITY_FACTOR;
        if (reader.hasNext()) {
            parameters = HttpHeaderReader.readParameters(reader);
            if (parameters != null) {
                String v = parameters.get(QUALITY_FACTOR);
                if (v != null)
                    quality = HttpHeaderReader.readQualityFactor(v);
            }
        }
        
        return new AcceptMediaType(type, subType, quality, parameters);
    }
}
