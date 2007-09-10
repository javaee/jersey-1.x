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

import javax.ws.rs.ext.EntityProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractTypeEntityProvider<T> implements EntityProvider<T> {
    public final void writeTo(T t, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        Object mediaType = httpHeaders.getFirst("Content-Type");
        if (mediaType instanceof MediaType) {
            writeTo(t, (MediaType)mediaType, httpHeaders, entityStream);
        } else {
            writeTo(t, new MediaType(mediaType.toString()), httpHeaders, entityStream);
        }
    }
    
    public final void writeTo(InputStream in, OutputStream out) throws IOException {
        int read;
        final byte[] data = new byte[2048];
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
    }
    
    public final String readFromAsString(InputStream in) throws IOException {
        Reader reader = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        } 
        return sb.toString();
    }    
}
