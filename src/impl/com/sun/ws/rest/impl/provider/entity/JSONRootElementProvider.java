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
import com.sun.ws.rest.impl.json.JSONJAXBContext;
import com.sun.ws.rest.impl.json.JSONMarshaller;
import com.sun.ws.rest.impl.json.JSONUnmarshaller;
import com.sun.ws.rest.impl.json.reader.JsonXmlStreamReader;
import com.sun.ws.rest.impl.json.writer.JsonXmlStreamWriter;
import com.sun.ws.rest.impl.util.ThrowHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@ProduceMime("application/json")
@ConsumeMime("application/json")
public final class JSONRootElementProvider extends AbstractRootElementProvider {
    
    public JSONRootElementProvider() {
        Class<?> c = JAXBContext.class;
    }
    
    public Object readFrom(Class<Object> type, MediaType mediaType,
            MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
        try {
            JAXBContext context = getJAXBContext(type);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            if (unmarshaller instanceof JSONUnmarshaller) {
                unmarshaller.setProperty(JSONJAXBContext.JSON_ENABLED, Boolean.TRUE);
                JAXBElement jaxbElem = (JAXBElement)((JSONUnmarshaller)unmarshaller).
                        unmarshal(new InputStreamReader(entityStream, getCharset(mediaType)),
                        type);
                return jaxbElem.getValue();
            } else {
                return unmarshaller.unmarshal(new JsonXmlStreamReader(
                        new InputStreamReader(entityStream, getCharset(mediaType))));
            }
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(type))
                    );
        }
    }
    
    public void writeTo(Object t, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException {
        try {
            JAXBContext context = getJAXBContext(t.getClass());
            Marshaller marshaller = context.createMarshaller();
            if (marshaller instanceof JSONMarshaller) {
                marshaller.setProperty(JSONJAXBContext.JSON_ENABLED, Boolean.TRUE);
                marshaller.marshal(t, 
                        new OutputStreamWriter(entityStream, getCharset(mediaType, UTF8)));
            } else {
                marshaller.marshal(t, new JsonXmlStreamWriter(
                        new OutputStreamWriter(entityStream, getCharset(mediaType, UTF8))));
            }
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(t.getClass()))
                    );
        }
    }
}
