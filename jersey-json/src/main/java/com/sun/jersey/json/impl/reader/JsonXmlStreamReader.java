/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.json.impl.reader;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

/**
 * Implementation of {@link XMLStreamReader} for JSON streams in natural or mapped notation. This class contains a factory
 * method for an instance creation.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class JsonXmlStreamReader implements XMLStreamReader {

    /**
     * Provider of Xml events.
     */
    private final XmlEventProvider eventProvider;

    /**
     * Default namespace context for this class.
     */
    private final JsonNamespaceContext namespaceContext = new JsonNamespaceContext();

    /**
     * Exception that occurred during processing of the JSON stream. This property is supposed to be used in methods that are not
     * designed to throw the {@code XMLStreamException} directly (i.e. {@code #getAttributeXXX}).
     */
    private XMLStreamException validationException;

    /**
     * Factory method for creating instances of this class.
     *
     * @param reader JSON input.
     * @param configuration JSON configuration.
     * @param rootName if non-{@code null} then the {@code JsonXmlStreamReader} emulates presence of root element with this
     * name for JAXB provider.
     * @param expectedType expected type of JAXB element.
     * @param jaxbContext JAXB context.
     * @param readingList flag whether it is expected that root is an JSON array instead of an object.
     * @return an instance of JSON XML stream reader
     * @throws XMLStreamException if an {@link IOException} has been thrown during the creation of an {@code JsonParser} instance.
     */
    public static XMLStreamReader create(final Reader reader,
                                         final JSONConfiguration configuration,
                                         String rootName,
                                         final Class<?> expectedType,
                                         JAXBContext jaxbContext,
                                         final boolean readingList) throws XMLStreamException {
        try {
            if ((rootName == null || "".equals(rootName)) && (configuration.isRootUnwrapping())) {
                rootName = "rootElement";
            }
            
            final JsonParser rawParser = new JsonFactory().createJsonParser(reader);
            final JsonParser nonListParser = configuration.isRootUnwrapping() ? JacksonRootAddingParser.createRootAddingParser
                    (rawParser, rootName) : rawParser;

            XmlEventProvider eventStack = null;
            switch (configuration.getNotation()) {
                case MAPPED:
                    eventStack = new MappedNotationEventProvider(nonListParser, configuration);
                    break;
                case NATURAL:
                    if (jaxbContext instanceof JSONJAXBContext) {
                        jaxbContext = ((JSONJAXBContext) jaxbContext).getOriginalJaxbContext();
                    }

                    if (!readingList) {
                        eventStack = new NaturalNotationEventProvider(nonListParser, configuration, jaxbContext, expectedType);
                    } else {
                        eventStack = new NaturalNotationEventProvider(
                                JacksonRootAddingParser.createRootAddingParser(nonListParser, "jsonArrayRootElement"),
                                configuration,
                                jaxbContext,
                                expectedType);
                    }
                    break;
            }

            return new JsonXmlStreamReader(eventStack);
        } catch (IOException ex) {
            throw new XMLStreamException(ex);
        }
    }

    private JsonXmlStreamReader(final XmlEventProvider nodeStack) {
        this.eventProvider = nodeStack;
    }

    /**
     * Returns a list of attribute of the current element. This method also checks if the parser is in the proper state ({@code
     * XMLStreamConstants.START_ELEMENT} or {@code XMLStreamConstants.ATTRIBUTE}).
     *
     * @return list of the current elements attributes.
     */
    private List<JsonXmlEvent.Attribute> getAttributes() {
        if (getEventType() != XMLStreamConstants.START_ELEMENT
                && getEventType() != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalArgumentException("Parser must be on START_ELEMENT or ATTRIBUTE to read next attribute.");
        }

        final JsonXmlEvent currentNode = eventProvider.getCurrentNode();

        try {
            if (currentNode.getAttributes() == null) {
                eventProvider.processAttributesOfCurrentElement();
            }
            return currentNode.getAttributes();
        } catch (XMLStreamException xse) {
            // Cannot throw an exception from here - #getAttributeXXX methods doesn't support it - throw it when #next() method
            // is invoked.
            validationException = xse;

            return Collections.emptyList();
        }
    }

    /**
     * Returns an attribute of the current element at given index.
     *
     * @param index index of an attribute to retrieve.
     * @return attribute at given index or {@code null} if the index is outside of boundaries of the list of attributes.
     */
    private JsonXmlEvent.Attribute getAttribute(int index) {
        List<JsonXmlEvent.Attribute> attributes = getAttributes();
        if (index < 0 || index >= attributes.size()) {
            return null;
        }
        return attributes.get(index);
    }

    @Override
    public void close() throws XMLStreamException {
        eventProvider.close();
    }

    @Override
    public int getAttributeCount() {
        return getAttributes().size();
    }

    @Override
    public String getAttributeLocalName(int index) {
        JsonXmlEvent.Attribute attribute = getAttribute(index);
        return attribute == null ? null : attribute.getName().getLocalPart();
    }

    @Override
    public QName getAttributeName(int index) {
        JsonXmlEvent.Attribute attribute = getAttribute(index);
        return attribute == null ? null : attribute.getName();
    }

    @Override
    public String getAttributeNamespace(int index) {
        JsonXmlEvent.Attribute attribute = getAttribute(index);
        return attribute == null ? null : attribute.getName().getNamespaceURI();
    }

    @Override
    public String getAttributePrefix(int index) {
        JsonXmlEvent.Attribute attribute = getAttribute(index);
        return attribute == null ? null : attribute.getName().getPrefix();
    }

    @Override
    public String getAttributeType(int index) {
        return null;
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        if (localName == null || "".equals(localName)) {
            return null;
        }
        
        for (JsonXmlEvent.Attribute attribute : getAttributes()) {
            if (localName.equals(attribute.getName().getLocalPart())
                    && ((namespaceURI == null) || (namespaceURI.equals(attribute.getName().getNamespaceURI())))) {
                return attribute.getValue();
            }
        }

        return null;
    }

    @Override
    public String getAttributeValue(int index) {
        JsonXmlEvent.Attribute attribute = getAttribute(index);
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return "UTF-8";
    }

    @Override
    public String getElementText() throws XMLStreamException {
        if(getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException(
                    "Parser must be on START_ELEMENT to read next text.", getLocation());
        }

        int eventType = next();
        StringBuilder content = new StringBuilder();

        while (eventType != XMLStreamConstants.END_ELEMENT) {
            if(eventType == XMLStreamConstants.CHARACTERS
                    || eventType == XMLStreamConstants.CDATA
                    || eventType == XMLStreamConstants.SPACE
                    || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
                content.append(getText());
            } else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                    || eventType == XMLStreamConstants.COMMENT) {
                // skipping
            } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
                throw new XMLStreamException(
                        "Unexpected end of document when reading element text content.", getLocation());
            } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException(
                        "Element text content may not contain START_ELEMENT.", getLocation());
            } else {
                throw new XMLStreamException(
                        "Unexpected event type " + eventType + ".", getLocation());
            }
            eventType = next();
        }

        return content.toString();
    }

    @Override
    public String getEncoding() {
        return "UTF-8";
    }

    @Override
    public int getEventType() {
        return eventProvider.getCurrentNode().getEventType();
    }

    @Override
    public String getLocalName() {
        final int eventType = getEventType();

        if (eventType != XMLStreamReader.START_ELEMENT
                && eventType != XMLStreamReader.END_ELEMENT
                && eventType != XMLStreamReader.ENTITY_REFERENCE) {
            throw new IllegalArgumentException(
                    "Parser must be on START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE to read local name.");
        }
        
        return eventProvider.getCurrentNode().getName().getLocalPart();
    }

    @Override
    public Location getLocation() {
        return eventProvider.getCurrentNode().getLocation();
    }

    @Override
    public QName getName() {
        final int eventType = getEventType();

        if (eventType != XMLStreamReader.START_ELEMENT
                && eventType != XMLStreamReader.END_ELEMENT) {
            throw new IllegalArgumentException("Parser must be on START_ELEMENT or END_ELEMENT to read the name.");
        }
        
        return eventProvider.getCurrentNode().getName();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    @Override
    public int getNamespaceCount() {
        return this.namespaceContext.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(int index) {
        return null;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return null;
    }

    @Override
    public String getNamespaceURI(int index) {
        return null;
    }

    @Override
    public String getNamespaceURI() {
        final int eventType = getEventType();

        if (eventType != XMLStreamReader.START_ELEMENT
                && eventType != XMLStreamReader.END_ELEMENT) {
            throw new IllegalArgumentException("Parser must be on START_ELEMENT or END_ELEMENT to read the namespace URI.");
        }

        return eventProvider.getCurrentNode().getName().getNamespaceURI();
    }

    @Override
    public String getPIData() {
        return null;
    }

    @Override
    public String getPITarget() {
        return null;
    }

    @Override
    public String getPrefix() {
        return eventProvider.getCurrentNode().getPrefix();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("Name is null.");
        }
        return null;
    }

    @Override
    public String getText() {
        final int eventType = getEventType();
        
        if(eventType == XMLStreamConstants.CHARACTERS
                || eventType == XMLStreamConstants.CDATA
                || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.ENTITY_REFERENCE) {

            return eventProvider.getCurrentNode().getText();
        }
        
        throw new IllegalArgumentException(
                "Parser must be on CHARACTERS, CDATA, SPACE or ENTITY_REFERENCE to read text.");
    }

    @Override
    public char[] getTextCharacters() {
        final String text = getText();
        return text != null ? text.toCharArray() : new char[0];
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length)
            throws XMLStreamException {
        getText().getChars(sourceStart, sourceStart + length, target, targetStart);
        return length;
    }

    @Override
    public int getTextLength() {
        final String text = getText();
        return text == null ? 0 : text.length();
    }

    @Override
    public int getTextStart() {
        return 0;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean hasName() {
        final int eventType = getEventType();

        if (eventType != XMLStreamReader.START_ELEMENT
                && eventType != XMLStreamReader.END_ELEMENT) {
            throw new IllegalArgumentException("Parser must be on START_ELEMENT or END_ELEMENT to read the name.");
        }

        return eventProvider.getCurrentNode().getName() != null;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        // Failure in the previous state?
        if (validationException != null) {
            throw validationException;
        }

        return eventProvider.getCurrentNode().getEventType() != XMLStreamConstants.END_DOCUMENT;
    }

    @Override
    public boolean hasText() {
        final int eventType = getEventType();

        return eventType == XMLStreamConstants.CHARACTERS
                || eventType == XMLStreamConstants.CDATA
                || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.ENTITY_REFERENCE
                || eventType == XMLStreamConstants.COMMENT
                || eventType == XMLStreamConstants.DTD;
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        return false;
    }

    @Override
    public boolean isCharacters() {
        return eventProvider.getCurrentNode().getEventType() == XMLStreamConstants.CHARACTERS;
    }

    @Override
    public boolean isEndElement() {
        return eventProvider.getCurrentNode().getEventType() == XMLStreamConstants.END_ELEMENT;
    }

    @Override
    public boolean isStandalone() {
        return false;
    }

    @Override
    public boolean isStartElement() {
        return eventProvider.getCurrentNode().getEventType() == XMLStreamConstants.START_ELEMENT;
    }

    @Override
    public boolean isWhiteSpace() {
        return false;   // JsonParser does not return any whitespace element.
    }

    @Override
    public int next() throws XMLStreamException {
        if (!hasNext()) {
            throw new IllegalArgumentException("No more parsing elements.");
        }

        return eventProvider.readNext().getEventType();
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int eventType = next();

        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) // skip whitespace
                || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }

        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("Expected start or end tag.", getLocation());
        }

        return eventType;
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
    }

    @Override
    public boolean standaloneSet() {
        return false;
    }


}
