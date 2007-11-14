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
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamReader;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@ProduceMime("application/json")
@ConsumeMime("application/json")
public final class JSONJAXBElementProvider extends AbstractJAXBElementProvider {
    
    public Object readFrom(Class<Object> type, MediaType mediaType,
            MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
        try {
            JAXBContext context = getJAXBContext(type);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return unmarshaller.unmarshal(
                    new BadgerFishXMLStreamReader(
                    new JSONObject(readFromAsString(entityStream))));
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(type))
                    );
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
    
    public void writeTo(Object t, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException {
        try {
            JAXBContext context = getJAXBContext(t.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(t, 
                    new BadgerFishXMLStreamWriter(
                    new OutputStreamWriter(entityStream)));
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(t.getClass()))
                    );
        }
    }
}
