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
package com.sun.jersey.json.impl.writer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.json.impl.JSONHelper;
import com.sun.jersey.json.impl.JaxbXmlDocumentStructure;

import org.codehaus.jackson.JsonGenerator;

/**
 * Implementation of {@link XMLStreamWriter} for JSON streams in natural notation.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class Stax2JacksonWriter extends DefaultXmlStreamWriter implements XMLStreamWriter {

    private static class ProcessingInfo {

        boolean isArray;
        Type t;
        ProcessingInfo lastUnderlyingPI;
        boolean startObjectWritten = false;
        boolean afterFN = false;
        QName elementName;

        public ProcessingInfo(QName elementName, boolean isArray, Type t) {
            this.elementName = elementName;

            this.isArray = isArray;
            this.t = t;
        }

        public ProcessingInfo(ProcessingInfo pi) {
            this(pi.elementName, pi.isArray, pi.t);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProcessingInfo other = (ProcessingInfo) obj;
            if (this.isArray != other.isArray) {
                return false;
            }
            if (this.elementName != other.elementName
                    && (this.elementName == null || !this.elementName.equals(other.elementName))) {
                return false;
            }
            if (this.t != other.t && (this.t == null || !this.t.equals(other.t))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + (this.isArray ? 1 : 0);
            hash = 47 * hash + (this.elementName != null ?this.elementName.hashCode() : 0);
            hash = 47 * hash + (this.t != null ? this.t.hashCode() : 0);
            return hash;
        }
    }

    final private boolean attrsWithPrefix;

    final static String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

    JacksonStringMergingGenerator generator;
    final List<ProcessingInfo> processingStack = new ArrayList<ProcessingInfo>();
    boolean writingAttr = false;

    /**
     * Document structure to obtain the expected elements and attributes from.
     */
    private JaxbXmlDocumentStructure documentStructure;

    static <T> T pop(List<T> stack) {
        return stack.remove(stack.size() - 1);
    }

    static <T> T peek(List<T> stack) {
        return (stack.size() > 0) ? stack.get(stack.size() - 1) : null;
    }

    static <T> T peek2nd(List<T> stack) {
        return (stack.size() > 1) ? stack.get(stack.size() - 2) : null;
    }

    public Stax2JacksonWriter(final JsonGenerator generator, final Class<?> expectedType, final JAXBContext jaxbContext) {
        this(generator, JSONConfiguration.DEFAULT, expectedType, jaxbContext);
    }

    public Stax2JacksonWriter(final JsonGenerator generator,
                              final JSONConfiguration config,
                              final Class<?> expectedType,
                              JAXBContext jaxbContext) {
        this.attrsWithPrefix = config.isUsingPrefixesAtNaturalAttributes();
        this.generator = JacksonStringMergingGenerator.createGenerator(generator);
        this.documentStructure = JSONHelper.getXmlDocumentStructure(jaxbContext, expectedType, false);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement(null, localName, null);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(null, localName, namespaceURI);
    }

    private void ensureStartObjectBeforeFieldName(ProcessingInfo pi) throws IOException {
        if ((pi != null) && pi.afterFN) {
            generator.writeStartObject();
            peek2nd(processingStack).startObjectWritten = true;
            pi.afterFN = false;
        }
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        try {
            pushPropInfo(namespaceURI, localName);
            ProcessingInfo currentPI = peek(processingStack);
            ProcessingInfo parentPI = peek2nd(processingStack);
            if (!currentPI.isArray) {
                if ((parentPI != null) && (parentPI.lastUnderlyingPI != null) && (parentPI.lastUnderlyingPI.isArray)) {
                    generator.writeEndArray();
                    parentPI.afterFN = false;
                }
                ensureStartObjectBeforeFieldName(parentPI);
                generator.writeFieldName(localName);
                currentPI.afterFN = true;
            } else {
                if ((parentPI == null) || (!currentPI.equals(parentPI.lastUnderlyingPI))) {
                    // not the same array, need to close the last array off?
                    if ((parentPI != null) && (parentPI.lastUnderlyingPI != null) && (parentPI.lastUnderlyingPI.isArray)) {
                        generator.writeEndArray();
                        parentPI.afterFN = false;
                    }
                    // now start the new array
                    ensureStartObjectBeforeFieldName(parentPI);
                    generator.writeFieldName(localName);
                    generator.writeStartArray();
                    currentPI.afterFN = true;
                } else {
                    // next array element
                    currentPI.afterFN = true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }
    static final Type[] _pt = new Type[]{
        byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class,
        Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class, 
        String.class
    };
    static final Type[] _pta = new Type[]{
        byte[].class, short[].class, int[].class, long[].class, float[].class, double[].class, boolean[].class, char[].class,
        Byte[].class, Short[].class, Integer[].class, Long[].class, Float[].class, Double[].class, Boolean[].class, Character[].class, 
        String[].class
    };
    static final Type[] _nst = new Type[]{
        byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class,
        Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, BigInteger.class, BigDecimal.class,
    };
    static final Set<Type> primitiveTypes = new HashSet<Type>() {

        {
            addAll(Arrays.asList(_pt));
        }
    };
    static final Set<Type> nonStringTypes = new HashSet<Type>() {

        {
            addAll(Arrays.asList(_nst));
        }
    };

    private void pushPropInfo(String namespaceUri, String localName) {
        final QName qname = new QName(namespaceUri == null ? XMLConstants.NULL_NS_URI : namespaceUri, localName);
        documentStructure.startElement(qname);

        ProcessingInfo parentPI = peek(processingStack);
        // still the same array, no need to dig out runtime property info
        if ((localName != null) && (parentPI != null) && (parentPI.lastUnderlyingPI != null) && (localName.equals(parentPI.lastUnderlyingPI.elementName.getLocalPart()))) {
            processingStack.add(new ProcessingInfo(parentPI.lastUnderlyingPI));
            return;
        }

        final Type rt = documentStructure.getEntityType(qname, writingAttr);
        // rt is null for root elements
        if (null == rt) {
            processingStack.add(new ProcessingInfo(qname, false, null));
            return;
        }
        if (primitiveTypes.contains(rt)) {
            processingStack.add(new ProcessingInfo(qname, false, rt));
            return;
        }

        // TODO: wildcard could still simulate an array by adding several elements of the same name
        if (documentStructure.isArrayCollection()) { // another array
            if (!((parentPI != null) && (parentPI.isArray) && (documentStructure.isSameArrayCollection()))) {
                // another array
                processingStack.add(new ProcessingInfo(qname, true, rt));
                return;
            }
        }
        // something else
        processingStack.add(new ProcessingInfo(qname, false, rt));
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        writeEndElement();
    }

    private void cleanlyEndObject(ProcessingInfo pi) throws IOException {
        if (pi.startObjectWritten) {
            generator.writeEndObject();
        } else {
            if (pi.afterFN && pi.lastUnderlyingPI == null) {
                if(documentStructure.isArrayCollection()
                        || documentStructure.hasSubElements()) {
                    generator.writeStartObject();
                    generator.writeEndObject();
                } else {
                    generator.writeNull();
                }
            }
        }
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        try {
            ProcessingInfo removedPI = pop(processingStack);
            ProcessingInfo currentPI = peek(processingStack);
            if (currentPI != null) {
                currentPI.lastUnderlyingPI = removedPI;
            }
            // need to check first, if there was an array to be closed off
            if ((removedPI.lastUnderlyingPI != null) && (removedPI.lastUnderlyingPI.isArray)) {
                generator.writeEndArray();
            }
            cleanlyEndObject(removedPI);
            documentStructure.endElement(removedPI.elementName);
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        try {
            generator.writeEndObject();
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    @Override
    public void close() throws XMLStreamException {
        try {
            generator.close();
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    @Override
    public void flush() throws XMLStreamException {
        try {
            generator.flush();
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writeAttribute(null, null, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writeAttribute(null, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        writingAttr = true;
        writeStartElement(prefix, attrsWithPrefix ? ("@" + localName) : localName, namespaceURI);
        writingAttr = false;
        // a dirty hack, since jaxb ri is giving us wrong info on the actual attribute type in this case
        writeCharacters(value, "type".equals(localName) && XML_SCHEMA_INSTANCE.equals(namespaceURI));
        writeEndElement();
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        writeStartDocument(null, null);
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        writeStartDocument(null, version);
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        try {
            generator.writeStartObject();
        } catch (IOException ex) {
            //JRA-18973: Log Socket exceptions that can happen quite often when browsers close connections pre-maturely at DEBUG level.
            // Also don't need to throw the exception further up but just swallow it here.
            if (ex instanceof java.net.SocketTimeoutException || ex instanceof java.net.SocketException) {
                Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.FINE, "Socket excption", ex);
            } else {
                Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, "IO exception", ex);
                throw new XMLStreamException(ex);
            }
        }
    }

    private void writeCharacters(String text, boolean forceString) throws XMLStreamException {
        try {
            ProcessingInfo currentPI = peek(processingStack);
            if (currentPI.startObjectWritten && !currentPI.afterFN) {
                generator.writeFieldName("$");
            }
            currentPI.afterFN = false;
            if (forceString || !nonStringTypes.contains(currentPI.t)) {
                if (!currentPI.isArray) {
                    generator.writeStringToMerge(text);
                } else {
                    generator.writeString(text);
                }
            } else {
                if ((boolean.class == currentPI.t) || (Boolean.class == currentPI.t)) {
                    generator.writeBoolean(Boolean.parseBoolean(text));
                } else {
                    generator.writeNumber(text);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        writeCharacters(text, false);
    }

    @Override
    public void writeCharacters(char[] text, int start, int length) throws XMLStreamException {
        writeCharacters(new String(text, start, length));
    }

}
