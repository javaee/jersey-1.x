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

import com.sun.ws.rest.api.representation.Form;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class FormProvider extends AbstractMessageReaderWriterProvider<Form> {
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
        return type == Form.class;
    }
    
    public Form readFrom(
            Class<Form> type, 
            Type genericType, 
            MediaType mediaType, 
            Annotation annotations[],
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        String encoded = readFromAsString(entityStream, mediaType);
    
        Form map = new Form();
        StringTokenizer tokenizer = new StringTokenizer(encoded, "&");
        String token;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            int idx = token.indexOf('=');
            if (idx < 0) {
                map.add(URLDecoder.decode(token,"UTF-8"), null);
            } else if (idx > 0) {
                map.add(URLDecoder.decode(token.substring(0, idx),"UTF-8"), 
                        URLDecoder.decode(token.substring(idx+1),"UTF-8"));
            }
        }
        return map;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
        return type == Form.class;
    }
    
    public void writeTo(
            Form t,
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : t.entrySet()) {
            for (String value : e.getValue()) {
                if (sb.length() > 0)
                    sb.append('&');
                sb.append(URLEncoder.encode(e.getKey(), "UTF-8"));
                if (value != null) {
                    sb.append('=');
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                }
            }
        }
                
        writeToAsString(sb.toString(), entityStream, mediaType);
    }    
}