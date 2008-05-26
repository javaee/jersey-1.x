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

package com.sun.jersey.impl.provider.entity;

import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.impl.json.JSONMarshaller;
import com.sun.jersey.impl.json.JSONUnmarshaller;
import com.sun.jersey.impl.json.reader.JsonXmlStreamReader;
import com.sun.jersey.impl.json.writer.JsonXmlStreamWriter;
import com.sun.jersey.impl.util.ThrowHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
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
 * @author japod
 */
@ProduceMime("application/json")
@ConsumeMime("application/json")
public final class JSONJAXBElementProvider extends AbstractJAXBElementProvider {
    
    public JSONJAXBElementProvider() {
        Class<?> c = JAXBContext.class;
    }
    
    @SuppressWarnings("unchecked")
    public JAXBElement<?> readFrom(
            Class<JAXBElement<?>> type, 
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        ParameterizedType pt = (ParameterizedType)genericType;
        Class ta = (Class)pt.getActualTypeArguments()[0];
        
        try {
            StreamSource source = new StreamSource(entityStream);
            JAXBContext context = getJAXBContext(ta);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (unmarshaller instanceof JSONUnmarshaller) {
                unmarshaller.setProperty(JSONJAXBContext.JSON_ENABLED, Boolean.TRUE);
                return unmarshaller.unmarshal(source, ta);
            } else {
                return (JAXBElement) unmarshaller.unmarshal(new JsonXmlStreamReader(
                        new InputStreamReader(entityStream, getCharset(mediaType)), true), ta);
            }
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(type))
                    );
        }    
    }
    
    public void writeTo(
            JAXBElement<?> t, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            JAXBContext context = getJAXBContext(t.getDeclaredType());            
            Marshaller marshaller = context.createMarshaller();
            if (marshaller instanceof JSONMarshaller) {
                marshaller.setProperty(JSONJAXBContext.JSON_ENABLED, Boolean.TRUE);
                marshaller.marshal(t, 
                        new OutputStreamWriter(entityStream, getCharset(mediaType, UTF8)));
            } else {
                marshaller.marshal(t, new JsonXmlStreamWriter(
                        new OutputStreamWriter(entityStream, getCharset(mediaType, UTF8)), true));
            }
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(t.getClass()))
                    );
        }
    }
}