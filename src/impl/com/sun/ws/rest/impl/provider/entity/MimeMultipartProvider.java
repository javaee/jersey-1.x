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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MimeMultipartProvider extends AbstractMessageReaderWriterProvider<MimeMultipart> {
    
    public MimeMultipartProvider() {
        Class<?> c = MimeMultipart.class;
    }
    
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
        return type == MimeMultipart.class;        
    }
    
    public MimeMultipart readFrom(
            Class<MimeMultipart> type, 
            Type genericType, 
            MediaType mediaType, 
            Annotation annotations[],
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        if (mediaType == null)
            mediaType = new MediaType("multipart", "form-data");
        ByteArrayDataSource ds = new ByteArrayDataSource(entityStream, mediaType.toString());
        try {
            return new MimeMultipart(ds);
        } catch (MessagingException cause) {
            IOException effect = new IOException("Error reading entity as MimeMultipart");
            effect.initCause(cause);
            throw effect;
        }
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
        return type == MimeMultipart.class;        
    }
    
    
    public void writeTo(
            MimeMultipart t, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            // TODO put boundary string as parameter of media type?
            t.writeTo(entityStream);
        } catch (MessagingException e) {
            IOException io = new IOException("Error writing entity from MimeMultipart");
            throw (IOException)io.initCause(e);
        }
    }
}