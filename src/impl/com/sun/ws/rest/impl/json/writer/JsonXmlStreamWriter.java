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
package com.sun.ws.rest.impl.json.writer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author japod
 */
public class JsonXmlStreamWriter implements XMLStreamWriter {


    private static class WriterAdapter {

        Writer writer;
        boolean isEmpty = true;

        WriterAdapter() {
        }

        WriterAdapter(Writer w) {
            writer = w;
        }

        void write(String s) throws IOException {
            assert null != writer;
            writer.write(s);
            isEmpty = false;
        }

        String getContent() {
            return null;
        }
        }

    private static final class StringWriterAdapter extends WriterAdapter {

        StringWriterAdapter() {
            writer = new StringWriter();
        }

        @Override
        String getContent() {
            return writer.toString();
        }
    }

    private static final class DummyWriterAdapter extends WriterAdapter {

        DummyWriterAdapter() {
        }

        @Override
        void write(String s) throws IOException {
        }

        @Override
        String getContent() {
            return null;
        }
    }

    private static final class ProcessingState {

        String lastName;
        String currentName;
        WriterAdapter lastElementWriter;
        Boolean lastWasPrimitive;
        boolean lastIsArray;
        WriterAdapter writer;
        boolean hasNoElement = true;

        ProcessingState() {
            writer = new StringWriterAdapter();
        }

        ProcessingState(WriterAdapter w) {
            writer = w;
        }

        @Override
        public String toString() {
            return "PS(" + currentName 
                    + "|" + ((writer != null) ? writer.getContent() : null) 
                    + "|" + lastName 
                    + "|" + ((lastElementWriter != null) ? lastElementWriter.getContent() : null) 
                    + ")";
        }
    }
    
    Writer mainWriter;
    boolean stripRoot;
    List<ProcessingState> processingStack;
    int depth;
    
    final Collection<String> arrayElementNames = new LinkedList<String>();
    final Collection<String> nonStringElementNames = new LinkedList<String>();

    public JsonXmlStreamWriter(Writer writer) {
        this(writer, false, null, null);
    }

    public JsonXmlStreamWriter(Writer writer, boolean stripRoot) {
        this(writer, stripRoot, null, null);
    }
    
    public JsonXmlStreamWriter(Writer writer, boolean stripRoot, Collection<String> arrays, Collection<String> nonStrings) {
        this.mainWriter = writer;
        this.stripRoot = stripRoot;
        if (null != arrays) {
            this.arrayElementNames.addAll(arrays);
        }
        if (null != nonStrings) {
            this.nonStringElementNames.addAll(nonStrings);
        }
    }

    public void close() throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void flush() throws XMLStreamException {
        try {
            mainWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(JsonXmlStreamWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeEndDocument() throws XMLStreamException {
        try {
            if (null != processingStack.get(depth).lastElementWriter) {
                processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
            }
            if ((null == processingStack.get(depth).lastWasPrimitive) || !processingStack.get(depth).lastWasPrimitive) {
                processingStack.get(depth).writer.write("}");
            }
            processingStack.remove(depth);
            depth--;
        } catch (IOException ex) {
            Logger.getLogger(JsonXmlStreamWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeEndElement() throws XMLStreamException {
        try {

            if (null != processingStack.get(depth).lastElementWriter) {
                if (processingStack.get(depth).lastIsArray) {
                    processingStack.get(depth).writer.write(",");
                    processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
                    processingStack.get(depth).writer.write("]");
                } else {
                    if (isArrayElement(processingStack.get(depth).lastName)) { // one elem array
                        processingStack.get(depth).writer.write("[");
                        processingStack.get(depth).lastIsArray = true;
                        processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
                        processingStack.get(depth).writer.write("]");
                    } else {
                        processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
                    }
                }
            }
            if (processingStack.get (depth).writer.isEmpty)
                processingStack.get (depth).writer.write ("null");
            else if ((null == processingStack.get(depth).lastWasPrimitive) || !processingStack.get(depth).lastWasPrimitive) {
                processingStack.get(depth).writer.write("}");
            }
            processingStack.get(depth - 1).lastName = processingStack.get(depth - 1).currentName;
            processingStack.get(depth - 1).lastWasPrimitive = false;
            processingStack.get(depth - 1).lastElementWriter = processingStack.get(depth).writer;
            processingStack.remove(depth);
            depth--;
        } catch (IOException ex) {
            Logger.getLogger(JsonXmlStreamWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeStartDocument() throws XMLStreamException {
        depth = 0;
        processingStack = new ArrayList<ProcessingState>();
        processingStack.add(createProcessingState());
    }

    public void writeCharacters(char[] text, int start, int length) throws XMLStreamException {
        writeCharacters(new String(text, start, length));
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeCData(String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeCharacters(String text) throws XMLStreamException {
        try {
            //TODO: what about encoding?
            if (isNonString(processingStack.get(depth - 1).currentName)) {
                processingStack.get(depth).writer.write(text);
            } else {
                processingStack.get(depth).writer.write("\"" + text + "\"");
            }
            processingStack.get(depth).lastWasPrimitive = true;
        } catch (IOException ex) {
            Logger.getLogger(JsonXmlStreamWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeComment(String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeDTD(String dtd) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeDefaultNamespace(String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
    }

    public void writeEntityRef(String name) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeStartDocument(String version) throws XMLStreamException {
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement(null, localName, null);
    }

    public NamespaceContext getNamespaceContext() {
        return null;
    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPrefix(String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writeAttribute(null, null, localName, value);
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(null, localName, null);
    }

    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writeAttribute(null, namespaceURI, localName, value);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(localName);
        writeEndElement();
    }

    private void printStack(String localName) {
        try {
            for (int d = 0; d <= depth; d++) {
                mainWriter.write("\n**" + d + ":" + processingStack.get(d));
            }
            mainWriter.write("\n*** [" + localName + "]");
        } catch (IOException ex) {
            Logger.getLogger(JsonXmlStreamWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean isArrayElement(String name) {
        if (null == name) {
            return false;
        }
        return arrayElementNames.contains(name);
    }

    private boolean isNonString(String name) {
        if (null == name) {
            return false;
        }
        return nonStringElementNames.contains(name);
    }
    
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        processingStack.get(depth).currentName = localName;
        try {
            boolean isNextArrayElement = processingStack.get(depth).currentName.equals(processingStack.get(depth).lastName);
                    //"rows".equals(processingStack.get(depth).currentName);
            if (!isNextArrayElement) {
                if (isArrayElement(processingStack.get(depth).lastName) && processingStack.get (depth).hasNoElement) { // one elem array
                    processingStack.get(depth).writer.write("[");
                    processingStack.get(depth).lastIsArray = true;
                    processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
                } else {
                    // write previous elements from buffer
                    if (null != processingStack.get(depth).lastElementWriter) {
                        if (processingStack.get(depth).lastIsArray) {
                            processingStack.get(depth).writer.write(",");
                            processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
                            processingStack.get(depth).writer.write("]");
                        } else {
                            processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
                        }
                    }

                    processingStack.get(depth).lastIsArray = false;
                    if (null != processingStack.get(depth).lastName) {
                        if (processingStack.get(depth).lastIsArray) {
                            processingStack.get(depth).writer.write("]");
                            processingStack.get(depth).lastIsArray = false;
                        }
                        processingStack.get(depth).writer.write(",");  // next element at the same level
                    }
                    if (null == processingStack.get(depth).lastWasPrimitive) {
                        processingStack.get(depth).writer.write("{"); // first sub-element
                    }
                    processingStack.get(depth).writer.write("\"" + localName + "\":");
                }
            } else { // next array element
                processingStack.get (depth).hasNoElement = false;
                processingStack.get(depth).writer.write(processingStack.get(depth).lastIsArray ? "," : "[");  // next element at the same level
                processingStack.get(depth).lastIsArray = true;
                processingStack.get(depth).writer.write(processingStack.get(depth).lastElementWriter.getContent());
            }

            depth++;
            processingStack.add(depth,createProcessingState());
        } catch (IOException ex) {
            Logger.getLogger(JsonXmlStreamWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        writeStartElement(prefix, "@" + localName, namespaceURI);
        writeCharacters(value);
        writeEndElement();
    }
    
    private ProcessingState createProcessingState() {
        switch (depth) {
            case 0: 
                return new ProcessingState(
                        stripRoot ? new DummyWriterAdapter() : new WriterAdapter(mainWriter));
            case 1: 
                return stripRoot ? 
                    new ProcessingState(new WriterAdapter(mainWriter)) : new ProcessingState();
            default: 
                return new ProcessingState();
        }
    }
}