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

package com.sun.ws.rest.impl.provider.entity;

import com.sun.ws.rest.api.representation.FormURLEncodedProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class FormURLEncodedProvider extends AbstractTypeEntityProvider<FormURLEncodedProperties> {
    public boolean supports(Class type) {
        return type == FormURLEncodedProperties.class;
    }

    public FormURLEncodedProperties readFrom(Class<FormURLEncodedProperties> type, 
            String mediaType, MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
        String decoded = URLDecoder.decode(readFromAsString(entityStream), "UTF-8");
    
        FormURLEncodedProperties map = new FormURLEncodedProperties();
        StringTokenizer tokenizer = new StringTokenizer(decoded, "&");
        String token;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            int idx = token.indexOf('=');
            if (idx < 0) {
                map.put(token, null);
            } else if (idx > 0) {
                map.put(token.substring(0, idx), token.substring(idx+1));
            }
        }
        return map;
    }

    public void writeTo(FormURLEncodedProperties t, 
            MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (String key: t.keySet()) {
            if (cnt++ > 0)
                sb.append('&');
            sb.append(URLEncoder.encode(key, "UTF-8"));
            String value = t.get(key);
            if (value != null) {
                sb.append('=');
                sb.append(URLEncoder.encode(value, "UTF-8"));
            }
        }
        
        entityStream.write(sb.toString().getBytes());
    }    
}
