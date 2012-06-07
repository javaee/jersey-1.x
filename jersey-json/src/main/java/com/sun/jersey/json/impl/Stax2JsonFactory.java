/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.json.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.core.util.ReaderWriter;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;
import com.sun.jersey.json.impl.writer.JacksonArrayWrapperGenerator;
import com.sun.jersey.json.impl.writer.JacksonRootStrippingGenerator;
import com.sun.jersey.json.impl.writer.JsonXmlStreamWriter;
import com.sun.jersey.json.impl.writer.Stax2JacksonWriter;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamReader;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class Stax2JsonFactory {

    private Stax2JsonFactory() {
    }

    public static XMLStreamWriter createWriter(final Writer writer,
                                               final JSONConfiguration config,
                                               final Class<?> expectedType,
                                               final JAXBContext jaxbContext) throws IOException {
        return createWriter(writer, config, expectedType, jaxbContext, false);
    }

    public static XMLStreamWriter createWriter(final Writer writer,
                                               final JSONConfiguration config,
                                               final Class<?> expectedType,
                                               JAXBContext jaxbContext,
                                               final boolean writingList) throws IOException {
        if (jaxbContext instanceof JSONJAXBContext) {
            jaxbContext = ((JSONJAXBContext) jaxbContext).getOriginalJaxbContext();
        }

        switch (config.getNotation()) {
            case NATURAL:
                final JsonGenerator rawGenerator = new JsonFactory().createJsonGenerator(writer);
                if (config.isHumanReadableFormatting()) {
                    rawGenerator.useDefaultPrettyPrinter();
                }
                final JsonGenerator bodyGenerator = writingList ? JacksonArrayWrapperGenerator.createArrayWrapperGenerator(rawGenerator, config.isRootUnwrapping() ? 0 : 1) : rawGenerator;
                if (config.isRootUnwrapping()) {
                    return new Stax2JacksonWriter(JacksonRootStrippingGenerator.createRootStrippingGenerator(bodyGenerator, writingList ? 2 : 1), config, expectedType, jaxbContext);
                } else {
                    return new Stax2JacksonWriter(bodyGenerator, config, expectedType, jaxbContext);
                    }
            case MAPPED:
                return JsonXmlStreamWriter.createWriter(writer, config, JSONHelper.getRootElementName((Class<Object>) expectedType));
            case BADGERFISH:
                return new BadgerFishXMLStreamWriter(writer);
            case MAPPED_JETTISON:
                Configuration jmConfig;
                if (null == config.getXml2JsonNs()) {
                    jmConfig = new Configuration();
                } else {
                    jmConfig = new Configuration(config.getXml2JsonNs());
                }
                return new MappedXMLStreamWriter(new MappedNamespaceConvention(jmConfig), writer);
            default:
                return null;
        }
    }

    public static XMLStreamReader createReader(final Reader reader, final JSONConfiguration config, final String rootName, final Class<?> expectedType, final JAXBContext jaxbContext) throws XMLStreamException {
        return createReader(reader, config, rootName, expectedType, jaxbContext, false);
    }

    public static XMLStreamReader createReader(final Reader reader, final JSONConfiguration config, final String rootName, final Class<?> expectedType, final JAXBContext jaxbContext, final boolean readingList) throws XMLStreamException {

        Reader nonEmptyReader = ensureNonEmptyReader(reader);

        switch (config.getNotation()) {
            case NATURAL:
            case MAPPED:
                return JsonXmlStreamReader.create(nonEmptyReader, config, rootName, expectedType, jaxbContext, readingList);
            case MAPPED_JETTISON:
                try {
                    Configuration jmConfig;
                    if (null == config.getXml2JsonNs()) {
                        jmConfig = new Configuration();
                    } else {
                        jmConfig = new Configuration(config.getXml2JsonNs());
                    }
                    return new MappedXMLStreamReader(
                            new JSONObject(new JSONTokener(ReaderWriter.readFromAsString(nonEmptyReader))),
                            new MappedNamespaceConvention(jmConfig));
                } catch (Exception ex) {
                    throw new XMLStreamException(ex);
                }
            case BADGERFISH:
                try {
                    return new BadgerFishXMLStreamReader(new JSONObject(new JSONTokener(ReaderWriter.readFromAsString(nonEmptyReader))));
                } catch (Exception ex) {
                    throw new XMLStreamException(ex);
                }
        }
        // This should not occur
        throw new IllegalArgumentException("Unknown JSON config");
    }

    private static Reader ensureNonEmptyReader(Reader reader) throws XMLStreamException {
        try {
            Reader mr = reader.markSupported() ? reader : new BufferedReader(reader);
            mr.mark(1);
            if (mr.read() == -1) {
                throw new XMLStreamException("JSON expression can not be empty!");
            }
            mr.reset();
            return mr;
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
    }

}
