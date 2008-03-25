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

import java.io.BufferedWriter;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractTypeEntityProvider<T> implements 
        MessageBodyReader<T>, MessageBodyWriter<T> {

    public static final Charset UTF8 = Charset.forName("UTF-8");
                  
    public static final Charset getCharset(MediaType m) {
        String name = (m == null) ? null : m.getParameters().get("charset");
        return (name == null) ? UTF8 : Charset.forName(name);
    }
        
    public static final Charset getCharset(MediaType m, Charset def) {
        String name = (m == null) ? null : m.getParameters().get("charset");
        try {
            return (name == null) ? UTF8 : Charset.forName(name);
        } catch (RuntimeException e) {
            return UTF8;
        }
    }
    
    public final void writeTo(InputStream in, OutputStream out) throws IOException {
        int read;
        final byte[] data = new byte[2048];
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
    }
    
    public final void writeTo(Reader in, Writer out) throws IOException {
        int read;
        final char[] data = new char[2048];
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
    }
    
    public final String readFromAsString(InputStream in, MediaType type) throws IOException {
        Reader reader = new InputStreamReader(in, getCharset(type));
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        } 
        return sb.toString();
    }
    
    public final void writeToAsString(String s, OutputStream out, MediaType type) throws IOException {
        Writer osw = new BufferedWriter(new OutputStreamWriter(out, 
                getCharset(type, UTF8)));        
        osw.write(s);
        osw.flush();        
    }
    
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
        return supports(type);
    }
    
    public T readFrom(Class<T> type, Type genericType, MediaType mediaType, 
            Annotation annotations[],
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {        
        return readFrom(type, mediaType, httpHeaders, entityStream);
    }
    
    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
        return supports(type);
    }
    
    public void writeTo(T t, Class<?> type, Type genericType, Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        writeTo(t, mediaType, httpHeaders, entityStream);
    }    
     
    public long getSize(T t) {
        return -1;
    }
    
    public abstract boolean supports(Class<?> type);
    
    public abstract T readFrom(Class<T> type, MediaType mediaType, 
            MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException;

    public abstract void writeTo(T t, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException;        
}
