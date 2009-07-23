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
package com.sun.jersey.json.impl.reader;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.json.impl.ImplMessages;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.codehaus.jackson.JsonParser;

/**
 *
 * @author japod
 */
public class Jackson2StaxReader implements XMLStreamReader {

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
    JsonParser parser;
    final Queue<JsonReaderXmlEvent> eventQueue = new LinkedList<JsonReaderXmlEvent>();
    final List<ProcessingInfo> processingStack = new ArrayList<ProcessingInfo>();
    final JsonNamespaceContext namespaceContext = new JsonNamespaceContext();


    private boolean properJAXBVersion = true;
    private final boolean attrsWithPrefix;

    final Collection<String> elemsExpected = new HashSet<String>();
    final Map<String, QName> qNamesOfExpElems = new HashMap<String, QName>();

    final Collection<String> attrsExpected = new HashSet<String>();
    final Map<String, QName> qNamesOfExpAttrs = new HashMap<String, QName>();

    static <T> T pop(List<T> stack) {
        return stack.remove(stack.size() - 1);
    }

    static <T> T peek(List<T> stack) {
        return (stack.size() > 0) ? stack.get(stack.size() - 1) : null;
    }

    static <T> T peek2nd(List<T> stack) {
        return (stack.size() > 1) ? stack.get(stack.size() - 2) : null;
    }

    public Jackson2StaxReader(JsonParser parser) throws XMLStreamException {
        this(parser, JSONConfiguration.DEFAULT);
    }

    public Jackson2StaxReader(JsonParser parser, JSONConfiguration config) throws XMLStreamException {
        this.attrsWithPrefix = config.isUsingPrefixesAtNaturalAttributes();
        this.parser = parser;
        try {
            readNext();
        } catch (IOException ex) {
            Logger.getLogger(Jackson2StaxReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void readNext() throws IOException {
        readNext(false);
    }
    final static Collection<org.codehaus.jackson.JsonToken> valueTokens = new HashSet<org.codehaus.jackson.JsonToken>() {

        {
            add(org.codehaus.jackson.JsonToken.VALUE_FALSE);
            add(org.codehaus.jackson.JsonToken.VALUE_TRUE);
            add(org.codehaus.jackson.JsonToken.VALUE_NULL);
            add(org.codehaus.jackson.JsonToken.VALUE_STRING);
            add(org.codehaus.jackson.JsonToken.VALUE_NUMBER_FLOAT);
            add(org.codehaus.jackson.JsonToken.VALUE_NUMBER_INT);
        }
    };

    private QName getQNameForTagLocName(final String localName) {
        return getQNameForLocName(localName, qNamesOfExpElems);
    }

    private QName getQNameForLocName(final String localName, final Map<String, QName> qNamesMap) {
        final QName result = qNamesMap.get(localName);
        if (result != null) {
            return result;
        } else {
            return new QName(localName);
        }
    }

    private void readNext(boolean lookingForAttributes) throws IOException {
        if (!lookingForAttributes) {
            eventQueue.poll();
        }
        while (eventQueue.isEmpty() || lookingForAttributes) {
            org.codehaus.jackson.JsonToken jtok;
            // TODO: ask Jackson to take care of DOS
            while (true) {
                parser.nextToken();
                jtok = parser.getCurrentToken();
                final ProcessingInfo pi = peek(processingStack);
                switch (jtok) {
                    case FIELD_NAME:
                        // start tag
                        String currentName = parser.getCurrentName();
                        if (attrsWithPrefix && currentName.startsWith("@")) {
                            currentName = currentName.substring(1);
                        }
                        boolean currentIsAttribute = !("$".equals(currentName)) && properJAXBVersion ? attrsExpected.contains(currentName) : !elemsExpected.contains(currentName);
                        if (lookingForAttributes && currentIsAttribute) {
                            parser.nextToken();
                            if (valueTokens.contains(parser.getCurrentToken())) {
                                eventQueue.peek().addAttribute(getQNameForLocName(currentName, qNamesOfExpAttrs), parser.getText());
                            } else {
                                System.out.println(String.format("CurrentName=%s", currentName));
                                throw new IOException("Not an attribute, expected primitive value!");
                            }
                        } else { // non attribute
                            lookingForAttributes = false; // stop seeking attributes
                            if (!("$".equals(currentName))) {
                                final QName currentQName = getQNameForTagLocName(currentName);
                                eventQueue.add(new StartElementEvent(currentQName, new StaxLocation(parser.getCurrentLocation())));
                                processingStack.add(new ProcessingInfo(currentQName, false, true));
                                return;
                            } else {
                                parser.nextToken();
                                if (valueTokens.contains(parser.getCurrentToken())) {
                                    eventQueue.add(new CharactersEvent(parser.getText(), new StaxLocation(parser.getCurrentLocation())));
                                    return;
                                } else {
                                    throw new IOException("Not a xml value, expected primitive value!");
                                }
                            }
                        }
                        break;
                    case START_OBJECT:
                        if (pi == null) {
                            eventQueue.add(new StartDocumentEvent(new StaxLocation(0, 0, 0)));
                            return;
                        }
                        if (pi.isArray && !pi.isFirstElement) {
                            eventQueue.add(new StartElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                            return;
                        } else {
                            pi.isFirstElement = false;
                        }
                        break;
                    case END_OBJECT:
                        lookingForAttributes = false;
                        // end tag
                        eventQueue.add(new EndElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                        if (!pi.isArray) {
                            pop(processingStack);
                        }
                        if (processingStack.isEmpty()) {
                            eventQueue.add(new EndDocumentEvent(new StaxLocation(parser.getCurrentLocation())));
                        }
                        return;
                    case VALUE_FALSE:
                    case VALUE_NULL:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NUMBER_INT:
                    case VALUE_TRUE:
                    case VALUE_STRING:
                        if (!pi.isFirstElement) {
                            eventQueue.add(new StartElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                        } else {
                            pi.isFirstElement = false;
                        }
                        if (jtok != jtok.VALUE_NULL) {
                            eventQueue.add(new CharactersEvent(parser.getText(), new StaxLocation(parser.getCurrentLocation())));
                        }
                        eventQueue.add(new EndElementEvent(pi.name, new StaxLocation(parser.getCurrentLocation())));
                        if (!pi.isArray) {
                            pop(processingStack);
                        }
                        if (processingStack.isEmpty()) {
                            eventQueue.add(new EndDocumentEvent(new StaxLocation(parser.getCurrentLocation())));
                        }
                        lookingForAttributes = false;
                        return;
                    case START_ARRAY:
                        peek(processingStack).isArray = true;
                        break;
                    case END_ARRAY :
                         pop(processingStack);
                         lookingForAttributes = false;
                }
            }
        }
    }

    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getElementText() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int next() throws XMLStreamException {
        try {
            readNext();
            return eventQueue.peek().getEventType();
        } catch (IOException ex) {
            Logger.getLogger(JsonXmlStreamReader.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) // skip whitespace
                || eventType == XMLStreamConstants.SPACE || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("expected start or end tag", getLocation());
        }
        return eventType;
    }

    public boolean hasNext() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNamespaceURI(
            String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isStartElement() {
        return eventQueue.peek().isStartElement();
    }

    public boolean isEndElement() {
        return eventQueue.peek().isEndElement();
    }

    public boolean isCharacters() {
        return eventQueue.peek().isCharacters();
    }

    public boolean isWhiteSpace() {
        return false;
    }

    public String getAttributeValue(
            String namespaceURI, String localName) {
        return eventQueue.peek().getAttributeValue(namespaceURI, localName);
    }

    public int getAttributeCount() {
        try {

            if (!eventQueue.peek().attributesChecked) {

                elemsExpected.clear();
                qNamesOfExpElems.clear();
                attrsExpected.clear();
                qNamesOfExpAttrs.clear();

                final UnmarshallingContext uctx = UnmarshallingContext.getInstance();

                if (uctx != null) {
                    try {
                        Collection<QName> currExpElems = uctx.getCurrentExpectedElements();
                        for (QName n : currExpElems) {
                            String nu = n.getNamespaceURI();
                            if (nu != null && (nu.equals("\u0000"))) {
                                elemsExpected.add("$");
                                qNamesOfExpElems.put("$", null);
                            } else {
                                elemsExpected.add(n.getLocalPart());
                                qNamesOfExpElems.put(n.getLocalPart(), n);
                            }
                        }
                    } catch (NullPointerException npe) {
                        // TODO: need to check what could be done in JAXB in order to prevent the npe
                        // thrown from com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext#1206
                    }

                    if (properJAXBVersion) {
                        try {
                            Collection<QName> currExpAttrs = uctx.getCurrentExpectedAttributes();
                            for (QName n : currExpAttrs) {
                                attrsExpected.add(n.getLocalPart());
                                qNamesOfExpAttrs.put(n.getLocalPart(), n);
                            }
                        } catch (NullPointerException npe) {
                                // thrown from com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext
                        } catch (NoSuchMethodError nsme) {
                            // thrown when JAXB version is less than 2.1.12
                            properJAXBVersion = false;
                            Logger.getLogger(Jackson2StaxReader.class.getName()).log(Level.SEVERE, ImplMessages.ERROR_JAXB_RI_2_1_12_MISSING(), nsme);
                        }
                    } 
                }

                readNext(true);
                eventQueue.peek().attributesChecked = true;
            }
        } catch (IOException ex) {
            Logger.getLogger(Jackson2StaxReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return eventQueue.peek().getAttributeCount();
    }

    public QName getAttributeName(
            int index) {
        return eventQueue.peek().getAttributeName(index);
    }

    public String getAttributeNamespace(
            int index) {
        return eventQueue.peek().getAttributeNamespace(index);
    }

    public String getAttributeLocalName(
            int index) {
        return eventQueue.peek().getAttributeLocalName(index);
    }

    public String getAttributePrefix(
            int index) {
        return eventQueue.peek().getAttributePrefix(index);
    }

    public String getAttributeType(
            int index) {
        return eventQueue.peek().getAttributeType(index);
    }

    public String getAttributeValue(
            int index) {
        return eventQueue.peek().getAttributeValue(index);
    }

    public boolean isAttributeSpecified(int index) {
        return eventQueue.peek().isAttributeSpecified(index);
    }

    public int getNamespaceCount() {
        return this.namespaceContext.getNamespaceCount();
    }

    public String getNamespacePrefix(int idx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNamespaceURI(int idx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    public int getEventType() {
        return eventQueue.peek().getEventType();
    }

    public String getText() {
        return eventQueue.peek().getText();
    }

    public char[] getTextCharacters() {
        return eventQueue.peek().getTextCharacters();
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        return eventQueue.peek().getTextCharacters(sourceStart, target, targetStart, length);
    }

    public int getTextStart() {
        return eventQueue.peek().getTextStart();
    }

    public int getTextLength() {
        return eventQueue.peek().getTextLength();
    }

    public String getEncoding() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasText() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getLocation() {
        return eventQueue.peek().getLocation();
    }

    public QName getName() {
        return eventQueue.peek().getName();
    }

    public String getLocalName() {
        return eventQueue.peek().getLocalName();
    }

    public boolean hasName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNamespaceURI() {
        return eventQueue.peek().getName().getNamespaceURI();
    }

    public String getPrefix() {
        return eventQueue.peek().getPrefix();
    }

    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isStandalone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean standaloneSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCharacterEncodingScheme() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPITarget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPIData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
