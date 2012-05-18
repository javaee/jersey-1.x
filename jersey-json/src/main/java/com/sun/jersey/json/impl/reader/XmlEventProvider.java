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
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import com.sun.jersey.api.json.JSONConfiguration;

import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * Abstract provider for creating {@code JsonXmlEvent} instances from {@code JsonParser}. Extensions of this class should
 * adjust their behaviour according to the JSON notation they are supporting.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public abstract class XmlEventProvider {

    private static class ProcessingInfo {

        QName name;
        boolean isArray;
        boolean isFirstElement;

        ProcessingInfo(QName name, boolean isArray, boolean isFirstElement) {
            this.name = name;
            this.isArray = isArray;
            this.isFirstElement = isFirstElement;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(XmlEventProvider.class.getName());

    private final JSONConfiguration configuration;

    /**
     * JSON parser.
     */
    private final JsonParser parser;

    /**
     * Queue of unprocessed events.
     */
    private final Queue<JsonXmlEvent> eventQueue = new LinkedList<JsonXmlEvent>();

    final Stack<ProcessingInfo> processingStack = new Stack<ProcessingInfo>();
    
    protected XmlEventProvider(final JsonParser parser, final JSONConfiguration configuration) throws XMLStreamException {
        this.parser = parser;
        this.configuration = configuration;

        try {
            readNext();
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new XMLStreamException(ex);
        }
    }

    /**
     * @see com.sun.jersey.json.impl.reader.JsonXmlStreamReader#close()
     */
    void close() throws XMLStreamException {
        eventQueue.clear();
        processingStack.empty();

        try {
            parser.close();
        } catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }

    /**
     * Creates an {@code EndElementEvent}.
     *
     * @param elementName name of element for which the end element should be created.
     * @param location location of this end element in the JSON stream.
     * @return end element event.
     */
    protected JsonXmlEvent createEndElementEvent(final QName elementName, final Location location) {
        return new EndElementEvent(elementName, location);
    }

    /**
     * Creates an {@code StartElementEvent}.
     *
     * @param elementName name of element for which the start element should be created.
     * @param location location of this start element in the JSON stream.
     * @return start element event.
     */
    protected JsonXmlEvent createStartElementEvent(final QName elementName, final Location location) {
        return new StartElementEvent(elementName, location);
    }

    /**
     * Returns the name of an attribute from the given field name without the leading '@' character if present.
     *
     * @param jsonFieldName field name representing the attribute name.
     * @return the name of an attribute.
     */
    protected String getAttributeName(final String jsonFieldName) {
        return '@' == jsonFieldName.charAt(0) ? jsonFieldName.substring(1) : jsonFieldName;
    }

    /**
     * Returns the attribute qualified name which is determined from the given {@code jsonFieldName} and configuration of the
     * underlying implementation.
     *
     * @param jsonFieldName name of the json field to obtain the qualified name for.
     * @return qualified name of the attribute.
     */
    protected abstract QName getAttributeQName(final String jsonFieldName);
    
    JsonXmlEvent getCurrentNode() {
        return eventQueue.peek();
    }

    /**
     * Returns the element qualified name which is determined from the given {@code jsonFieldName} and configuration of the
     * underlying implementation.
     *
     * @param jsonFieldName name of the json field to obtain the qualified name for.
     * @return qualified name of the element.
     */
    protected abstract QName getElementQName(final String jsonFieldName);

    protected JSONConfiguration getJsonConfiguration() {
        return configuration;
    }

    /**
     * Checks whether the {@code jsonToken} belongs to the group of primitive json values (not an object or an array) and returns
     * the given {@code jsonFieldValue} if the conditions are met. If the {@code jsonFieldValue} does not represents primitive
     * json value an {@code IOException} is thrown.
     *
     * @param jsonToken token to determine whether the given value is of simple json type.
     * @param jsonFieldValue value to be returned if it's one of the simple json types.
     * @return simple json type value.
     * @throws IOException if the given value doesn't belong to the group of primitive json values.
     */
    private String getPrimitiveFieldValue(final JsonToken jsonToken, final String jsonFieldValue) throws IOException {
        if (jsonToken == JsonToken.VALUE_FALSE
                || jsonToken == JsonToken.VALUE_TRUE
                || jsonToken == JsonToken.VALUE_STRING
                || jsonToken == JsonToken.VALUE_NUMBER_FLOAT
                || jsonToken == JsonToken.VALUE_NUMBER_INT
                || jsonToken == JsonToken.VALUE_NULL) {

            return jsonFieldValue;
        }
        
        throw new IOException("Not an XML value, expected primitive value!");
    }

    /**
     * Determines whether the given json field name represents an attribute name.
     *
     * @param jsonFieldName json field name to be examined.
     * @return {@code true} if the given name represents an attribute, {@code false} otherwise.
     */
    protected abstract boolean isAttribute(final String jsonFieldName);

    /**
     * Retrieves and sets attributes for the current element from JSON stream.
     *
     * @throws XMLStreamException if an error occurred during the processing of the JSON stream.
     * @see #processTokens(boolean)
     */
    void processAttributesOfCurrentElement() throws XMLStreamException {
        eventQueue.peek().setAttributes(new LinkedList<JsonXmlEvent.Attribute>());

        processTokens(true);
    }
    
    private JsonXmlEvent processTokens(boolean processAttributes) throws XMLStreamException {
        if (!processAttributes) {
            // get rid of the current event
            eventQueue.poll();
        }

        try {
            while (eventQueue.isEmpty() || processAttributes) {
                while (true) {
                    final JsonToken jsonToken = parser.nextToken();
                    final ProcessingInfo pi = processingStack.isEmpty() ? null : processingStack.peek();

                    switch (jsonToken) {
                        case FIELD_NAME:
                            final String fieldName = parser.getCurrentName();

                            if (isAttribute(fieldName)) {
                                // attribute
                                final QName attributeName = getAttributeQName(fieldName);
                                final String attributeValue = getPrimitiveFieldValue(parser.nextToken(), parser.getText());

                                eventQueue.peek().getAttributes().add(new JsonXmlEvent.Attribute(attributeName, attributeValue));
                            } else {
                                processAttributes = false;

                                // child event
                                if ("$".equals(fieldName)) {
                                    // character event
                                    final String value = getPrimitiveFieldValue(parser.nextToken(), parser.getText());
                                    eventQueue.add(new CharactersEvent(value, new StaxLocation(parser.getCurrentLocation())));
                                } else {
                                    // element event
                                    final QName elementName = getElementQName(fieldName);
                                    final JsonLocation currentLocation = parser.getCurrentLocation();
                                    eventQueue.add(createStartElementEvent(elementName, new StaxLocation(currentLocation)));
                                    processingStack.add(new ProcessingInfo(elementName, false, true));

                                    return getCurrentNode();
                                }
                            }
                            break;
                        case START_OBJECT:
                            if (pi == null) {
                                eventQueue.add(new StartDocumentEvent(new StaxLocation(0, 0, 0)));
                                return getCurrentNode();
                            }
                            if (pi.isArray && !pi.isFirstElement) {
                                eventQueue.add(createStartElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                                return getCurrentNode();
                            } else {
                                pi.isFirstElement = false;
                            }
                            break;
                        case END_OBJECT:
                            processAttributes = false;

                            // end tag
                            eventQueue.add(createEndElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                            if (!pi.isArray) {
                                processingStack.pop();
                            }
                            if (processingStack.isEmpty()) {
                                eventQueue.add(new EndDocumentEvent(new StaxLocation(parser.getCurrentLocation())));
                            }
                            return getCurrentNode();
                        case VALUE_FALSE:
                        case VALUE_NULL:
                        case VALUE_NUMBER_FLOAT:
                        case VALUE_NUMBER_INT:
                        case VALUE_TRUE:
                        case VALUE_STRING:
                            if (!pi.isFirstElement) {
                                eventQueue.add(createStartElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                            } else {
                                pi.isFirstElement = false;
                            }
                            if (jsonToken != JsonToken.VALUE_NULL) {
                                eventQueue.add(new CharactersEvent(parser.getText(), new StaxLocation(parser.getCurrentLocation())));
                            }
                            eventQueue.add(new EndElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                            if (!pi.isArray) {
                                processingStack.pop();
                            }
                            if (processingStack.isEmpty()) {
                                eventQueue.add(new EndDocumentEvent(new StaxLocation(parser.getCurrentLocation())));
                            }
                            processAttributes = false;
                            return getCurrentNode();
                        case START_ARRAY:
                            processingStack.peek().isArray = true;
                            break;
                        case END_ARRAY:
                            processingStack.pop();
                            processAttributes = false;
                            break;
                        default:
                            throw new IllegalStateException("Unknown JSON token: " + jsonToken);
                    }
                }
            }

            return eventQueue.peek();
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Reads and returns next {@code JsonXmlEvent}.
     *
     * @return an instance of {@code JsonXmlEvent}.
     * @throws XMLStreamException if something went wrong.
     */
    JsonXmlEvent readNext() throws XMLStreamException {
        return processTokens(false);
    }
    
}
