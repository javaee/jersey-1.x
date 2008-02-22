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

package com.sun.ws.rest.impl.json.reader;

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
    
    
    private enum LaState { START, END, O1, O2, O3, O4, O5, A1, A2, A3 };
    
    // TODO: can take the location info from JsonToken
    public static class DummyLocation implements Location {

        public int getCharacterOffset() {
            return 0;
        }

        public int getColumnNumber() {
            return -1;
        }

        public int getLineNumber() {
            return -1;
        }

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return null;
        }
        
    }
    
    Reader reader;
    boolean jsonRootUnwrapping;

    JsonLexer lexer;
    JsonToken lastToken;
    
    private static final class ProcessingState {
        String lastName;
        LaState state;
        
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
    }
    
    final Queue<JsonReaderXmlEvent> eventQueue = new LinkedList<JsonReaderXmlEvent>();
    
    List<ProcessingState> processingStack;
    int depth;

    // TODO: aux variable to detect infinite loop (to be removed later...)
    int counter = 0;
    
    public JsonXmlStreamReader(Reader reader) throws IOException {
        this(reader, false);
    }
    
    public JsonXmlStreamReader(Reader reader, boolean jsonRootUnwrapping) throws IOException {
        this.reader = reader;
        this.jsonRootUnwrapping = jsonRootUnwrapping;
        lexer = new JsonLexer(reader); 
        depth = 0;
        processingStack = new ArrayList<ProcessingState>();
        processingStack.add(new ProcessingState());
        readNextEvent();
    }
    
    void colon() throws IOException {
        JsonToken token = nextToken();
        if (token.tokenType != JsonToken.COLON) {
            throw new IOException("Colon expected instead of \"" + token.tokenText + "\"");
        }
    }
    
    JsonToken nextToken() throws IOException {
        JsonToken result = lexer.yylex();
        //System.out.println("token=" + result + " depth=" + depth + " state=" + processingStack.elementAt(depth).state);
        counter++;
        if (counter > 60) {
            throw new NullPointerException("counter overflow error");
        }
        return result;
    }
    
    private void valueRead() {
        if (LaState.O4 == processingStack.get(depth).state) {
            processingStack.get(depth).state = LaState.O5;
        } else if (LaState.A2 == processingStack.get(depth).state) {
            processingStack.get(depth).state = LaState.A3;
        } else if (LaState.A1 == processingStack.get(depth).state) {
            processingStack.get(depth).state = LaState.A3;
        }
    }
    
    private void readNextEvent() throws IOException {
        eventQueue.poll();
        while (eventQueue.isEmpty()) {
            lastToken = nextToken();
            if (null == lastToken) {
                if (jsonRootUnwrapping) {
                    eventQueue.add(new EndElementEvent(processingStack.get(depth).lastName));
                }
                eventQueue.add(new EndDocumentEvent());
                break;
            }
            switch (processingStack.get(depth).state) {
                case START :
                    if (0 == depth) {
                        eventQueue.add(new StartDocumentEvent());
                        processingStack.get(depth).state = LaState.O1;
                        if (jsonRootUnwrapping) {
                            processingStack.get(depth).lastName = "rootObject";
                            eventQueue.add(new StartElementEvent(processingStack.get(depth).lastName));
                        }
                        processingStack.add(new ProcessingState());
                        depth++;
                    }
                    // TODO: if JsonToken.START_OBJECT != lastToken then problem
                    processingStack.get(depth).state = LaState.O1;
                    break;
                case O1 :
                    switch (lastToken.tokenType) {
                        case JsonToken.STRING :
                            eventQueue.add(new StartElementEvent(lastToken.tokenText));
                            processingStack.get(depth).lastName = lastToken.tokenText;
                            processingStack.get(depth).state = LaState.O4;
                            colon();
                            break;
                        case JsonToken.END_OBJECT : // empty object/element
                            eventQueue.add(new EndElementEvent(processingStack.get(depth).lastName));
                            processingStack.remove(depth);
                            depth--;
                            break;
                        default:
                            // TODO: handle problem
                    }
                    break;
                case O2 :
                    switch (lastToken.tokenType) {
                        case JsonToken.STRING :
                            eventQueue.add(new StartElementEvent(lastToken.tokenText));
                            processingStack.get(depth).lastName = lastToken.tokenText;
                            colon();
                            processingStack.get(depth).state = LaState.O4;
                            break;
                        default:
                            // TODO: handle problem
                    }
                    break;
                case O4 :
                    switch (lastToken.tokenType) {
                        case JsonToken.START_OBJECT :
                            processingStack.add(new ProcessingState(LaState.O1));
                            depth++;
                            break;
                        case JsonToken.START_ARRAY :
                            processingStack.add(new ProcessingState(LaState.A1));
                            depth++;
                            break;
                        case JsonToken.STRING :
                            eventQueue.add(new CharactersEvent(lastToken.tokenText));
                            processingStack.get(depth).state = LaState.O5;
                            break;
                        // TODO: could be also number, false, true, null !
                        default:
                            // TODO: handle problem
                    }
                    break; // O4
                case O5 :
                    switch (lastToken.tokenType) {
                        case JsonToken.COMMA :
                            processingStack.get(depth).state = LaState.O2;
                            eventQueue.add(new EndElementEvent(processingStack.get(depth).lastName));
                            break; // STRING
                        case JsonToken.END_OBJECT : // empty object/element
                            eventQueue.add(new EndElementEvent(processingStack.get(depth).lastName));
                            processingStack.remove(depth);
                            depth--;
                            valueRead();
                            break; // END_OBJECT
                        default:
                            // TODO: handle problem
                    }
                    break; // O5
                case A1 :
                    switch (lastToken.tokenType) {
                        case JsonToken.START_OBJECT :
                            processingStack.add(new ProcessingState(LaState.O1));
                            depth++;
                            break;
                        case JsonToken.START_ARRAY :
                            processingStack.add(new ProcessingState(LaState.A1));
                            depth++;
                            break;
                        case JsonToken.END_ARRAY :
                            processingStack.remove(depth);
                            depth--;
                            valueRead();
                            break;
                        case JsonToken.STRING :
                            eventQueue.add(new CharactersEvent(lastToken.tokenText));
                            processingStack.get(depth).state = LaState.A3;
                            break;
                        default:
                            // TODO: handle problem
                    }                
                    break; // A1
                case A2 :
                    eventQueue.add(new StartElementEvent(processingStack.get(depth-1).lastName));
                    switch (lastToken.tokenType) {
                        case JsonToken.START_OBJECT :
                            processingStack.add(new ProcessingState(LaState.O1));
                            depth++;
                            break;
                        case JsonToken.START_ARRAY :
                            processingStack.add(new ProcessingState(LaState.A1));
                            depth++;
                            break;
                        case JsonToken.STRING :
                            eventQueue.add(new CharactersEvent(lastToken.tokenText));
                            processingStack.get(depth).state = LaState.A3;
                            break;
                        default:
                            // TODO: handle problem
                    }
                    break; // A2
                case A3 :
                    switch (lastToken.tokenType) {
                        case JsonToken.END_ARRAY :
                            processingStack.remove(depth);
                            depth--;
                            valueRead();
                            break;
                        case JsonToken.COMMA :
                            processingStack.get(depth).state = LaState.A2;
                            eventQueue.add(new EndElementEvent(processingStack.get(depth-1).lastName));
                            break;
                        default:
                            // TODO: handle problem
                    }                
                    break; // A3
             }
        } // end while lastEvent null
        //System.out.println("Next event = " + eventQueue.peek());
    }

    public int getAttributeCount() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributeCount();
    }

    public int getEventType() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getEventType();
    }

    public int getNamespaceCount() {
        return 0;
    }

    public int getTextLength() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getTextLength();
    }

    public int getTextStart() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getTextStart();
    }

    public int next() throws XMLStreamException {
        try {
            readNextEvent();
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
        assert !eventQueue.isEmpty();
        return eventQueue.peek().isCharacters();
    }

    public boolean isEndElement() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().isEndElement();
    }

    public boolean isStandalone() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isStartElement() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().isStartElement();
    }

    public boolean isWhiteSpace() {
        return false; // white space processed by lexer
    }

    public boolean standaloneSet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public char[] getTextCharacters() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getTextCharacters();
    }

    public boolean isAttributeSpecified(int attribute) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().isAttributeSpecified(attribute);
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getTextCharacters(sourceStart, target, targetStart, length);
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
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getLocalName();
    }

    public String getNamespaceURI() {
        return null;
    }

    public String getPIData() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPITarget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrefix() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getPrefix();
    }

    public String getText() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getText();
    }

    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAttributeLocalName(int index) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributeLocalName(index);
    }

    public QName getAttributeName(int index) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributeName(index);
    }

    public String getAttributeNamespace(int index) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributeNamespace(index);
    }

    public String getAttributePrefix(int index) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributePrefix(index);
    }

    public String getAttributeType(int index) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributeType(index);
    }

    public String getAttributeValue(int index) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributeValue(index);
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getAttributeValue(namespaceURI, localName);
    }

    public String getNamespacePrefix(int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getNamespaceURI(int arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamespaceContext getNamespaceContext() {
        // TODO: put/take it to/from processing stack
        return new JsonNamespaceContext();
    }

    public QName getName() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getName();
    }

    public Location getLocation() {
        assert !eventQueue.isEmpty();
        return eventQueue.peek().getLocation();
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

}
