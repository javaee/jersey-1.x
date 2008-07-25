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

package com.sun.jersey.impl.json.reader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author japod
 */
public class JsonXmlStreamReader implements XMLStreamReader {
    
    
    private enum LaState { 
        START, 
        END, 
        AFTER_OBJ_START_BRACE, 
        BEFORE_OBJ_NEXT_KV_PAIR, 
        BEFORE_COLON_IN_KV_PAIR, 
        BEFORE_VALUE_IN_KV_PAIR, 
        AFTER_OBJ_KV_PAIR, 
        AFTER_ARRAY_START_BRACE, 
        BEFORE_NEXT_ARRAY_ELEM, 
        AFTER_ARRAY_ELEM
    };
    
    static class MyLocation implements Location {
        
        int charOffset = -1;
        int column = -1;
        int line = -1;
        
        MyLocation(final int charOffset, final int column, final int line) {
            this.charOffset = charOffset;
            this.column = column;
            this.line = line;
        }
        
        MyLocation(final JsonLexer lexer) {
            this(lexer.getCharOffset(), lexer.getColumn(), lexer.getLineNumber());
        }

        public int getCharacterOffset() {
            return charOffset;
        }

        public int getColumnNumber() {
            return column;
        }

        public int getLineNumber() {
            return line;
        }

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return null;
        }
        
    }
    
    private static final Logger LOGGER = Logger.getLogger(JsonXmlStreamReader.class.getName());   
    
    boolean jsonRootUnwrapping;

    JsonLexer lexer;
    JsonToken lastToken;
    
    private static final class ProcessingState {
        String lastName;
        LaState state;
        JsonReaderXmlEvent eventToReadAttributesFor;
        
        ProcessingState() {
            this(LaState.START);
        }
        
        ProcessingState(LaState state) {
            this(state, null);
        }
        
        ProcessingState(LaState state, String name) {
            this.state = state;
            this.lastName = name;
        }
        
        @Override
        public String toString() {
            return String.format("{lastName:%s,laState:%s}", lastName, state);
        }
    }
    
    final Queue<JsonReaderXmlEvent> eventQueue = new LinkedList<JsonReaderXmlEvent>();
    
    List<ProcessingState> processingStack;
    int depth;

    public JsonXmlStreamReader(Reader reader) throws IOException {
        this(reader, false);
    }
    
    public JsonXmlStreamReader(Reader reader, boolean jsonRootUnwrapping) throws IOException {
        this.jsonRootUnwrapping = jsonRootUnwrapping;
        lexer = new JsonLexer(reader); 
        depth = 0;
        processingStack = new ArrayList<ProcessingState>();
        processingStack.add(new ProcessingState());
        readNext();
    }
    
    void colon() throws IOException {
        JsonToken token = nextToken();
        if (token.tokenType != JsonToken.COLON) {
            throw new IOException("Colon expected instead of \"" + token.tokenText + "\"");
        }
    }
    
    JsonToken nextToken() throws IOException {
        JsonToken result = lexer.yylex();
        return result;
    }
    
    private void valueRead() {
        if (LaState.BEFORE_VALUE_IN_KV_PAIR == processingStack.get(depth).state) {
            processingStack.get(depth).state = LaState.AFTER_OBJ_KV_PAIR;
        } else if (LaState.BEFORE_NEXT_ARRAY_ELEM == processingStack.get(depth).state) {
            processingStack.get(depth).state = LaState.AFTER_ARRAY_ELEM;
        } else if (LaState.AFTER_ARRAY_START_BRACE == processingStack.get(depth).state) {
            processingStack.get(depth).state = LaState.AFTER_ARRAY_ELEM;
        }
    }
    
    private void readNext() throws IOException {
        readNext(false);
    }
    
    private void readNext(boolean checkAttributesOnly) throws IOException {
        if (!checkAttributesOnly) {
            eventQueue.poll();
        }
        //boolean attributesStarted = false;
        while (eventQueue.isEmpty() || checkAttributesOnly) {
            lastToken = nextToken();
            if ((null == lastToken) || (LaState.END == processingStack.get(depth).state)){
                if (jsonRootUnwrapping) {
                    generateEEEvent(processingStack.get(depth).lastName);
                }
                eventQueue.add(new EndDocumentEvent(new MyLocation(lexer)));
                break;
            }
            switch (processingStack.get(depth).state) {
                case START:
                    if (0 == depth) {
                        eventQueue.add(new StartDocumentEvent(new MyLocation(lexer)));
                        processingStack.get(depth).state = LaState.AFTER_OBJ_START_BRACE;
                        if (jsonRootUnwrapping) {
                            processingStack.get(depth).lastName = "rootObject";
                            StartElementEvent event = generateSEEvent(processingStack.get(depth).lastName);
                            processingStack.get(depth).eventToReadAttributesFor = event;
                        }
                        switch (lastToken.tokenType) {
                            case JsonToken.START_OBJECT:
                                processingStack.add(new ProcessingState(LaState.AFTER_OBJ_START_BRACE));
                                depth++;
                                break;
                            case JsonToken.START_ARRAY:
                                processingStack.add(new ProcessingState(LaState.AFTER_ARRAY_START_BRACE));
                                depth++;
                                break;
                            case JsonToken.STRING:
                            case JsonToken.NUMBER:
                            case JsonToken.TRUE:
                            case JsonToken.FALSE:
                            case JsonToken.NULL:
                                eventQueue.add(new CharactersEvent(lastToken.tokenText, new MyLocation(lexer)));
                                processingStack.get(depth).state = LaState.END;
                                break;
                            default:
                            // TODO: handle problem
                        }
                    }
                    // TODO: if JsonToken.START_OBJECT != lastToken then problem
                    processingStack.get(depth).state = LaState.AFTER_OBJ_START_BRACE;
                    break;
                case AFTER_OBJ_START_BRACE:
                    switch (lastToken.tokenType) {
                        case JsonToken.STRING:
                            if (lastToken.tokenText.startsWith("@")) { // eat attributes
                                String attrName = lastToken.tokenText;
                                colon();
                                lastToken = nextToken();
                                // TODO process attr value
                                if (JsonToken.STRING != lastToken.tokenType) {
                                    throw new IOException("Attribute value expected instead of \"" + lastToken.tokenText + "\"");
                                }
                                if (null != processingStack.get(depth - 1).eventToReadAttributesFor) {
                                    processingStack.get(depth - 1).eventToReadAttributesFor.addAttribute(
                                            attrName.substring(1), lastToken.tokenText);
                                }
                                lastToken = nextToken();
                                switch (lastToken.tokenType) {
                                    case JsonToken.END_OBJECT:
                                        processingStack.remove(depth);
                                        depth--;
                                        valueRead();
                                        checkAttributesOnly = false;
                                        break;
                                    case JsonToken.COMMA:
                                        break;
                                    default:
                                        throw new IOException("\'\"\', or \'}\' expected instead of \"" + lastToken.tokenText + "\"");
                                }
                            } else { // non attribute
                                StartElementEvent event =
                                        generateSEEvent(lastToken.tokenText);
                                processingStack.get(depth).eventToReadAttributesFor = event;
                                checkAttributesOnly = false;
                                processingStack.get(depth).lastName = lastToken.tokenText;
                                colon();
                                processingStack.get(depth).state = LaState.BEFORE_VALUE_IN_KV_PAIR;
                            }
                            break;
                        case JsonToken.END_OBJECT: // empty object/element
                            generateEEEvent(processingStack.get(depth).lastName);
                            checkAttributesOnly = false;
                            processingStack.remove(depth);
                            depth--;
                            break;
                        default:
                        // TODO: handle problem
                    }
                    break;
                case BEFORE_OBJ_NEXT_KV_PAIR:
                    switch (lastToken.tokenType) {
                        case JsonToken.STRING:
                            StartElementEvent event =
                                    generateSEEvent(lastToken.tokenText);
                            processingStack.get(depth).eventToReadAttributesFor = event;
                            processingStack.get(depth).lastName = lastToken.tokenText;
                            colon();
                            processingStack.get(depth).state = LaState.BEFORE_VALUE_IN_KV_PAIR;
                            break;
                        default:
                        // TODO: handle problem
                    }
                    break;
                case BEFORE_VALUE_IN_KV_PAIR:
                    switch (lastToken.tokenType) {
                        case JsonToken.START_OBJECT:
                            processingStack.add(new ProcessingState(LaState.AFTER_OBJ_START_BRACE));
                            depth++;
                            break;
                        case JsonToken.START_ARRAY:
                            processingStack.add(new ProcessingState(LaState.AFTER_ARRAY_START_BRACE));
                            depth++;
                            break;
                        case JsonToken.STRING:
                        case JsonToken.NUMBER:
                        case JsonToken.TRUE:
                        case JsonToken.FALSE:
                        case JsonToken.NULL:
                            eventQueue.add(new CharactersEvent(lastToken.tokenText, new MyLocation(lexer)));
                            processingStack.get(depth).state = LaState.AFTER_OBJ_KV_PAIR;
                            break;
                        default:
                        // TODO: handle problem
                    }
                    break; // AFTER_ARRAY_ELEM
                case AFTER_OBJ_KV_PAIR:
                    switch (lastToken.tokenType) {
                        case JsonToken.COMMA:
                            processingStack.get(depth).state = LaState.BEFORE_OBJ_NEXT_KV_PAIR;
                            generateEEEvent(processingStack.get(depth).lastName);
                            break; // STRING
                        case JsonToken.END_OBJECT: // empty object/element
                            generateEEEvent(processingStack.get(depth).lastName);
                            processingStack.remove(depth);
                            depth--;
                            valueRead();
                            break; // END_OBJECT
                        default:
                        // TODO: handle problem
                    }
                    break; // AFTER_OBJ_KV_PAIR
                case AFTER_ARRAY_START_BRACE:
                    switch (lastToken.tokenType) {
                        case JsonToken.START_OBJECT:
                            processingStack.add(new ProcessingState(LaState.AFTER_OBJ_START_BRACE));
                            processingStack.get(depth).eventToReadAttributesFor = processingStack.get(depth - 1).eventToReadAttributesFor;
                            depth++;
                            break;
                        case JsonToken.START_ARRAY:
                            processingStack.add(new ProcessingState(LaState.AFTER_ARRAY_START_BRACE));
                            depth++;
                            break;
                        case JsonToken.END_ARRAY:
                            processingStack.remove(depth);
                            depth--;
                            valueRead();
                            break;
                        case JsonToken.STRING:
                            eventQueue.add(new CharactersEvent(lastToken.tokenText, new MyLocation(lexer)));
                            processingStack.get(depth).state = LaState.AFTER_ARRAY_ELEM;
                            break;
                        default:
                        // TODO: handle problem
                    }
                    break; // AFTER_ARRAY_ELEM
                case BEFORE_NEXT_ARRAY_ELEM:
                    StartElementEvent event =
                            generateSEEvent(processingStack.get(depth - 1).lastName);
                    switch (lastToken.tokenType) {
                        case JsonToken.START_OBJECT:
                            processingStack.add(new ProcessingState(LaState.AFTER_OBJ_START_BRACE));
                            processingStack.get(depth).eventToReadAttributesFor = event;
                            depth++;
                            break;
                        case JsonToken.START_ARRAY:
                            processingStack.add(new ProcessingState(LaState.AFTER_ARRAY_START_BRACE));
                            depth++;
                            break;
                        case JsonToken.STRING:
                            eventQueue.add(new CharactersEvent(lastToken.tokenText, new MyLocation(lexer)));
                            processingStack.get(depth).state = LaState.AFTER_ARRAY_ELEM;
                            break;
                        default:
                        // TODO: handle problem
                    }
                    break; // BEFORE_NEXT_ARRAY_ELEM
                case AFTER_ARRAY_ELEM:
                    switch (lastToken.tokenType) {
                        case JsonToken.END_ARRAY:
                            processingStack.remove(depth);
                            depth--;
                            valueRead();
                            break;
                        case JsonToken.COMMA:
                            processingStack.get(depth).state = LaState.BEFORE_NEXT_ARRAY_ELEM;
                            generateEEEvent(processingStack.get(depth - 1).lastName);
                            break;
                        default:
                        // TODO: handle problem
                    }
                    break; // AFTER_ARRAY_ELEM
            }
        } // end while lastEvent null
    }

    public int getAttributeCount() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributeCount");
        assert !eventQueue.isEmpty();
        if (!eventQueue.peek().attributesChecked) {
            try {
                readNext(true);
            } catch (IOException e) {
            // TODO: handle it!!!
            }
            eventQueue.peek().attributesChecked = true;
        }
        int result = eventQueue.peek().getAttributeCount();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributeCount", result);
        return result;
    }

    public int getEventType() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getEventType");
        assert !eventQueue.isEmpty();
        int result = eventQueue.peek().getEventType();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getEventType", result);
        return result;
    }

    public int getNamespaceCount() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getNamespaceCount");
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getNamespaceCount", 0);
        return 0;
    }

    public int getTextLength() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getTextLength");
        assert !eventQueue.isEmpty();
        int result = eventQueue.peek().getTextLength();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getTextLength", result);
        return result;
    }

    public int getTextStart() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getTextStart");
        assert !eventQueue.isEmpty();
        int result = eventQueue.peek().getTextStart();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getTextStart", result);
        return result;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasNext() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasText() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isCharacters() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "isCharacters");
        assert !eventQueue.isEmpty();
        boolean result = eventQueue.peek().isCharacters();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "isCharacters", result);
        return result;
    }

    public boolean isEndElement() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "isEndElement");
        assert !eventQueue.isEmpty();
        boolean result = eventQueue.peek().isEndElement();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "isEndElement", result);
        return result;
    }

    public boolean isStandalone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isStartElement() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "isStartElement");
        assert !eventQueue.isEmpty();
        boolean result = eventQueue.peek().isStartElement();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "isStartElement", result);
        return result;
    }

    public boolean isWhiteSpace() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "isWhiteSpace");
        boolean result = false; // white space processed by lexer
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "isWhiteSpace", result);
        return result;
    }

    public boolean standaloneSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public char[] getTextCharacters() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getTextCharacters");
        assert !eventQueue.isEmpty();
        char[] result = eventQueue.peek().getTextCharacters();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getTextCharacters", result);
        return result;
    }

    public boolean isAttributeSpecified(int attribute) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "isAttributeSpecified");
        assert !eventQueue.isEmpty();
        boolean result = eventQueue.peek().isAttributeSpecified(attribute);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "isAttributeSpecified", result);
        return result;
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getTextCharacters");
        assert !eventQueue.isEmpty();
        int result = eventQueue.peek().getTextCharacters(sourceStart, target, targetStart, length);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getTextCharacters", result);
        return result;
    }

    public String getCharacterEncodingScheme() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getElementText() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getEncoding() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLocalName() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getLocalName");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getLocalName();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getLocalName", result);
        return result;
    }

    public String getNamespaceURI() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getNamespaceURI");
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getNamespaceURI", null);
        return null;
    }

    public String getPIData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPITarget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrefix() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getPrefix");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getPrefix();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getPrefix", result);
        return result;
    }

    public String getText() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getText");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getText();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getText", result);
        return result;
    }

    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAttributeLocalName(int index) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributeLocalName");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getAttributeLocalName(index);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributeLocalName", result);
        return result;
    }

    public QName getAttributeName(int index) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributeName");
        assert !eventQueue.isEmpty();
        QName result = eventQueue.peek().getAttributeName(index);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributeName", result);
        return result;
    }

    public String getAttributeNamespace(int index) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributeNamespace");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getAttributeNamespace(index);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributeNamespace", result);
        return result;
    }

    public String getAttributePrefix(int index) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributePrefix");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getAttributePrefix(index);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributePrefix", result);
        return result;
    }

    public String getAttributeType(int index) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributeType");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getAttributeType(index);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributeType", result);
        return result;
    }

    public String getAttributeValue(int index) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributeValue");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getAttributeValue(index);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributeValue", result);
        return result;
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getAttributeValue");
        assert !eventQueue.isEmpty();
        String result = eventQueue.peek().getAttributeValue(namespaceURI, localName);
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getAttributeValue", result);
        return result;
    }

    public String getNamespacePrefix(int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNamespaceURI(int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamespaceContext getNamespaceContext() {
        // TODO: put/take it to/from processing stack
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getNamespaceContext");
        NamespaceContext result = new JsonNamespaceContext();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getNamespaceContext", result);
        return result;
    }

    public QName getName() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getName");
        assert !eventQueue.isEmpty();
        QName result = eventQueue.peek().getName();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getName");
        return result;
    }

    public Location getLocation() {
        LOGGER.entering(JsonXmlStreamReader.class.getName(), "getLocation");
        assert !eventQueue.isEmpty();
        Location result = eventQueue.peek().getLocation();
        LOGGER.exiting(JsonXmlStreamReader.class.getName(), "getLocation", result);
        return result;
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNamespaceURI(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private StartElementEvent generateSEEvent(String name) {
        StartElementEvent event = null;
        if (!"$".equals(name)) {
           event = new StartElementEvent(name, new MyLocation(lexer));
           eventQueue.add(event);
        }
        return event;
    }

    private void generateEEEvent(String name) {
       if (!"$".equals(name)) {
           eventQueue.add(new EndElementEvent(name, new MyLocation(lexer)));
       }
    }    
}
