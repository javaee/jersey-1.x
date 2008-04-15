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

import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.util.ThrowHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class XMLJAXBElementProvider extends AbstractJAXBElementProvider {
    
    public XMLJAXBElementProvider() {
        Class<?> c = JAXBContext.class;
    }
    
    @Override
    public JAXBElement<?> readFrom(
            Class<JAXBElement<?>> type, 
            MediaType mediaType, 
            MultivaluedMap<String, String> headers, 
            InputStream entityStream) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public JAXBElement<?> readFrom(
            Class<JAXBElement<?>> type, 
            Type genericType, 
            MediaType mediaType, 
            Annotation annotations[],
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        ParameterizedType pt = (ParameterizedType)genericType;
        Class ta = (Class)pt.getActualTypeArguments()[0];
        
        try {
            StreamSource source = new StreamSource(entityStream);
            JAXBContext context = getJAXBContext(ta);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller.unmarshal(source, ta);
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(type))
                    );
        }    }
    
    public void writeTo(
            JAXBElement<?> t, 
            MediaType mediaType,
            MultivaluedMap<String, Object> headers, 
            OutputStream entityStream) throws IOException {
        try {
            JAXBContext context = getJAXBContext(t.getDeclaredType());            
            Marshaller marshaller = context.createMarshaller();
            String name = getCharsetAsString(mediaType);
            if (name != null) {
                marshaller.setProperty(Marshaller.JAXB_ENCODING, name);
            }
            marshaller.marshal(t, entityStream);
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(t.getClass()))
                    );
        }
    }
}