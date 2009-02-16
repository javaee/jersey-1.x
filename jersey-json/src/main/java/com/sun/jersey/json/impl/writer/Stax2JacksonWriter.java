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
package com.sun.jersey.json.impl.writer;

import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.Property;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author japod
 */
public class Stax2JacksonWriter implements XMLStreamWriter {

    private static class ProcessingInfo {

        RuntimePropertyInfo rpi;
        boolean isArray;
        Type t;
        ProcessingInfo lastUnderlyingPI;
        boolean startObjectWritten = false;
        boolean afterFN = false;
        String elementName;

        public ProcessingInfo(String elementName, RuntimePropertyInfo rpi, boolean isArray, Type t) {
            this.elementName = elementName;
            this.rpi = rpi;
            this.isArray = isArray;
            this.t = t;
        }

        public ProcessingInfo(ProcessingInfo pi) {
            this(pi.elementName, pi.rpi, pi.isArray, pi.t);
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
            if (this.rpi != other.rpi && (this.rpi == null || !this.rpi.equals(other.rpi))) {
                return false;
            }
            if (this.isArray != other.isArray) {
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
            hash = 47 * hash + (this.rpi != null ? this.rpi.hashCode() : 0);
            hash = 47 * hash + (this.isArray ? 1 : 0);
            hash = 47 * hash + (this.t != null ? this.t.hashCode() : 0);
            return hash;
        }
    }
    JsonGenerator generator;
    final List<ProcessingInfo> processingStack = new ArrayList<ProcessingInfo>();
    boolean writingAttr = false;

    static <T> T pop(List<T> stack) {
        return stack.remove(stack.size() - 1);
    }

    static <T> T peek(List<T> stack) {
        return (stack.size() > 0) ? stack.get(stack.size() - 1) : null;
    }

    static <T> T peek2nd(List<T> stack) {
        return (stack.size() > 1) ? stack.get(stack.size() - 2) : null;
    }

    public Stax2JacksonWriter(JsonGenerator generator) {
        this.generator = generator;
    }

    public void writeStartElement(String localName) throws XMLStreamException {
        writeStartElement(null, localName, null);
    }

    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        writeStartElement(null, localName, namespaceURI);
    }

    private void ensureStartObjectBeforeFieldName(ProcessingInfo pi) throws JsonGenerationException, IOException {
        if ((pi != null) && pi.afterFN) {
            generator.writeStartObject();
            peek2nd(processingStack).startObjectWritten = true;
            pi.afterFN = false;
        }
    }

    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        try {
            pushPropInfo(localName);
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
    static final Set<Type> primitiveTypeArrays = new HashSet<Type>() {

        {
            addAll(Arrays.asList(_pta));
        }
    };
    static final Set<Type> nonStringTypes = new HashSet<Type>() {

        {
            addAll(Arrays.asList(_nst));
        }
    };
    static final Map<Type, Type> pta2pMap = new HashMap<Type, Type>() {

        {
            for (int i = 0; i < _pta.length; i++) {
                put(_pta[i], _pt[i]);
            }
        }
    };

    private void pushPropInfo(String elementName) {
        ProcessingInfo parentPI = peek(processingStack);
        // still the same array, no need to dig out runtime property info
        if ((elementName != null) && (parentPI != null) && (parentPI.lastUnderlyingPI != null) && (elementName.equals(parentPI.lastUnderlyingPI.elementName))) {
            processingStack.add(new ProcessingInfo(parentPI.lastUnderlyingPI));
            return;
        }
        final XMLSerializer xs = XMLSerializer.getInstance();
        final Property cp = (xs == null) ? null : xs.getCurrentProperty();
        final RuntimePropertyInfo ri = (cp == null) ? null : cp.getInfo();
        final Type rt = (ri == null) ? null : ri.getRawType();
        final String dn = (ri == null) ? null : ri.getName();
        // rt is null for root elements
        if (null == rt) {
            if (writingAttr) {
                // this should not happen:
                processingStack.add(new ProcessingInfo(elementName, ri, false, null));
                return;
            } else {
                processingStack.add(new ProcessingInfo(elementName, ri, false, null));
                return;
            }
        }
        if (primitiveTypes.contains(rt)) {
            processingStack.add(new ProcessingInfo(elementName, ri, false, rt));
            return;
        }
        if (ri.isCollection()) { // another array
            if (!((parentPI != null) && (parentPI.isArray) && (parentPI.rpi == ri))) {
                // another array
                processingStack.add(new ProcessingInfo(elementName, ri, true, rt));
                return;
            }
        }
        // something else
        processingStack.add(new ProcessingInfo(elementName, ri, false, rt));
        return;
    }

    public void writeEmptyElement(String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, null);
    }

    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        writeEmptyElement(null, localName, namespaceURI);
    }

    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        writeStartElement(prefix, localName, namespaceURI);
        writeEndElement();
    }

    private void cleanlyEndObject(ProcessingInfo pi) throws IOException {
        if (pi.startObjectWritten) {
            generator.writeEndObject();
        } else {
            if (pi.afterFN && pi.lastUnderlyingPI == null) {
                generator.writeNull();
            }
        }
    }

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
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    public void writeEndDocument() throws XMLStreamException {
        try {
            generator.writeEndObject();
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    public void close() throws XMLStreamException {
        try {
            generator.close();
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    public void flush() throws XMLStreamException {
        try {
            generator.flush();
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writeAttribute(null, null, localName, value);
    }

    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writeAttribute(null, namespaceURI, localName, value);
    }

    // TODO: need it parameterized
    final static boolean attrsWithPrefix = false;

    final static String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        writingAttr = true;
        writeStartElement(prefix, attrsWithPrefix ? ("@" + localName) : localName, namespaceURI);
        writingAttr = false;
        // a dirty hack, since jaxb ri is giving us wrong info on the actual attribute type in this case
        writeCharacters(value, "type".equals(localName) && XML_SCHEMA_INSTANCE.equals(namespaceURI));
        writeEndElement();
    }

    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        // we do not want to deal with namespaces
        // the main goal of this writer is keep the produced json as simple as possible
    }

    public void writeDefaultNamespace(String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeComment(String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeProcessingInstruction(String target) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeCData(String data) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeDTD(String dtd) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeEntityRef(String name) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeStartDocument() throws XMLStreamException {
        writeStartDocument(null, null);
    }

    public void writeStartDocument(String version) throws XMLStreamException {
        writeStartDocument(null, version);
    }

    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        try {
            generator.writeStartObject();
        } catch (IOException ex) {
            Logger.getLogger(Stax2JacksonWriter.class.getName()).log(Level.SEVERE, null, ex);
            throw new XMLStreamException(ex);
        }
    }

    private void writeCharacters(String text, boolean forceString) throws XMLStreamException {
        try {
            ProcessingInfo currentPI = peek(processingStack);
            ProcessingInfo parentPI = peek2nd(processingStack);
            if (currentPI.startObjectWritten && !currentPI.afterFN) {
                generator.writeFieldName("$");
            }
            currentPI.afterFN = false;
            if (forceString || !nonStringTypes.contains(currentPI.t)) {
                generator.writeString(text);
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

    public void writeCharacters(String text) throws XMLStreamException {
        writeCharacters(text, false);
    }

    public void writeCharacters(char[] text, int start, int length) throws XMLStreamException {
        writeCharacters(new String(text, start, length));
    }

    public String getPrefix(String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NamespaceContext getNamespaceContext() {
        return null;
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
