/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.core.impl.provider.entity;

import com.sun.jersey.core.provider.jaxb.AbstractListElementProvider;
import com.sun.jersey.spi.inject.Injectable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class XMLListElementProvider extends AbstractListElementProvider {

    // Delay construction of factory
    private final Injectable<XMLInputFactory> xif;
    
    XMLListElementProvider(Injectable<XMLInputFactory> xif, Providers ps) {
        super(ps);

        this.xif = xif;
    }
    
    XMLListElementProvider(Injectable<XMLInputFactory> xif, Providers ps, MediaType mt) {
        super(ps, mt);

        this.xif = xif;
    }

    @Produces("application/xml")
    @Consumes("application/xml")
    public static final class App extends XMLListElementProvider {
        public App(@Context Injectable<XMLInputFactory> xif, @Context Providers ps) {
            super(xif, ps , MediaType.APPLICATION_XML_TYPE);
        }
    }
    
    @Produces("text/xml")
    @Consumes("text/xml")
    public static final class Text extends XMLListElementProvider {
        public Text(@Context Injectable<XMLInputFactory> xif, @Context Providers ps) {
            super(xif, ps , MediaType.TEXT_XML_TYPE);
        }
    }
    
    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends XMLListElementProvider {
        public General(@Context Injectable<XMLInputFactory> xif, @Context Providers ps) {
            super(xif, ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+xml");
        }
    }

    @Override
    protected final XMLStreamReader getXMLStreamReader(Class<?> elementType, 
            MediaType mediaType,
            Unmarshaller u,
            InputStream entityStream)
            throws XMLStreamException {
        return xif.getValue().createXMLStreamReader(entityStream);
    }
    
    public final void writeList(Class<?> elementType, Collection<?> t,
            MediaType mediaType, Charset c,
            Marshaller m, OutputStream entityStream)
            throws JAXBException, IOException {
        final String rootElement = getRootElementName(elementType);
        final String cName = c.name();

        entityStream.write(
                String.format("<?xml version=\"1.0\" encoding=\"%s\" standalone=\"yes\"?>", cName).getBytes(cName));
        entityStream.write(String.format("<%s>", rootElement).getBytes(cName));

        for (Object o : t)
            m.marshal(o, entityStream);

        entityStream.write(String.format("</%s>", rootElement).getBytes(cName));
    }
}