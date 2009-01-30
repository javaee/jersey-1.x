/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.json.impl.provider.entity;

import com.sun.jersey.api.json.JSONConfigurated;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.provider.jaxb.AbstractListElementProvider;
import com.sun.jersey.json.impl.JSONMarshaller;
import com.sun.jersey.json.impl.Stax2JsonFactory;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author japod
 */
public class JSONListElementProvider extends AbstractListElementProvider {

    JSONListElementProvider(Providers ps) {
        super(ps);
    }

    JSONListElementProvider(Providers ps, MediaType mt) {
        super(ps, mt);
    }

    @Produces("application/json")
    @Consumes("application/json")
    public static final class App extends JSONListElementProvider {
        public App(@Context Providers ps) { super(ps , MediaType.APPLICATION_JSON_TYPE); }
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JSONListElementProvider {
        public General(@Context Providers ps) { super(ps); }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+json");
        }
    }

    @Override
    public final void writeList(Class<?> elementType, Collection<?> t, MediaType mediaType, Charset c, Marshaller m, OutputStream entityStream) throws JAXBException, IOException {
        final OutputStreamWriter osw = new OutputStreamWriter(entityStream, c);
        // TODO: should reuse customization options from the marshaller (if it is JSONMarshaller)
        // TODO: should force the elementType being treated as array (for 1-elem lists)
        JSONConfiguration jsonConfig = JSONConfiguration.DEFAULT;
        if (m instanceof JSONMarshaller) {
            JSONMarshaller jm = (JSONMarshaller)m;
            jsonConfig = jm.getJSONConfiguration();
        }
        final XMLStreamWriter jxsw = Stax2JsonFactory.createWriter(osw, jsonConfig, true);
        try {
            jxsw.writeStartElement(getRootElementName(elementType));
            for (Object o : t) {
                    m.marshal(o, jxsw);
            }
            jxsw.writeEndElement();
            jxsw.writeEndDocument();
            jxsw.flush();
            jxsw.close();
        } catch (XMLStreamException ex) {
            Logger.getLogger(JSONListElementProvider.class.getName()).log(Level.SEVERE, null, ex);
            throw new JAXBException(ex.getMessage(), ex);
        }
    }

    @Override
    protected final XMLStreamReader getXMLStreamReader(Class<?> elementType, MediaType mediaType, Unmarshaller u, InputStream entityStream) throws XMLStreamException {
        JSONConfiguration c = JSONConfiguration.DEFAULT;
        if (u instanceof JSONConfigurated) {
            c = ((JSONConfigurated) u).getJSONConfiguration();
        }
        return Stax2JsonFactory.createReader(new InputStreamReader(entityStream), c, getRootElementName(elementType));
    }
}
