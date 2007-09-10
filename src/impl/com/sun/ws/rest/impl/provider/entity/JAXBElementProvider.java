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
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.WeakHashMap;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamReader;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class JAXBElementProvider extends AbstractTypeEntityProvider<Object> {
    
    static Map<Class, JAXBContext> jaxbContexts = new WeakHashMap<Class, JAXBContext>();
    
    static MediaType JSON = new MediaType("application/json");
    
    public boolean supports(Class<?> type) {
        return type.getAnnotation(XmlRootElement.class) != null;
    }
    
    public Object readFrom(Class<Object> type, MediaType mediaType,
            MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
        try {
            JAXBContext context = getJAXBContext(type);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            boolean jsonUsed = mediaType.equals(JSON);
            if (!jsonUsed) {
                return unmarshaller.unmarshal(entityStream);
            } else {
                try {
                    return unmarshaller.unmarshal(
                            new BadgerFishXMLStreamReader(new JSONObject(readFromAsString(entityStream))));
                } catch (XMLStreamException xmlStreamException) {
                    throw ThrowHelper.withInitCause(xmlStreamException,
                            new IOException(ImplMessages.ERROR_PARSING_JSON_OBJECT())
                            );
                } catch (JSONException jsonException) {
                    throw ThrowHelper.withInitCause(jsonException,
                            new IOException(ImplMessages.ERROR_PARSING_JSON_OBJECT())
                            );
                }
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
            if (JSON.equals(mediaType)) {
                marshaller.marshal(t, new BadgerFishXMLStreamWriter(new OutputStreamWriter(entityStream)));
            } else {
                marshaller.marshal(t, entityStream);
            }
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(t.getClass()))
                    );
        }
    }
    
    private JAXBContext getJAXBContext(Class type) throws JAXBException {
        synchronized (jaxbContexts) {
            JAXBContext context = jaxbContexts.get(type);
            if (context == null) {
                context = JAXBContext.newInstance(type);
            }
            return context;
        }
    }
}
