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
package com.sun.jersey.impl.json.writer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author japod
 */
public class A2EXmlStreamWriterProxy implements XMLStreamWriter {

    XMLStreamWriter underlyingWriter;
    List<String> attr2ElemNames;

    private class XmlAttribute {

        String prefix;
        String namespaceUri;
        String localName;
        String value;

        XmlAttribute(String prefix, String nsUri, String localName, String value) {
            this.prefix = prefix;
            this.namespaceUri = nsUri;
            this.localName = localName;
            this.value = value;
        }
    }
    List<XmlAttribute> unwrittenAttrs = null;

    public A2EXmlStreamWriterProxy(XMLStreamWriter writer, Collection<String> attr2ElemNames) {
        this.underlyingWriter = writer;
        this.attr2ElemNames = new LinkedList<String>();
        this.attr2ElemNames.addAll(attr2ElemNames);

    }

    private void flushUnwrittenAttrs() throws XMLStreamException {
        if (unwrittenAttrs != null) {
            for (XmlAttribute a : unwrittenAttrs) {
                underlyingWriter.writeStartElement(a.prefix, a.localName, a.namespaceUri);
                underlyingWriter.writeCharacters(a.value);
                underlyingWriter.writeEndElement();
            }
            unwrittenAttrs = null;
        }
    }

    public void writeStartElement(String arg0) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeStartElement(arg0);
    }

    public void writeStartElement(String arg0, String arg1) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeStartElement(arg0, arg1);
    }

    public void writeStartElement(String arg0, String arg1, String arg2) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeStartElement(arg0, arg1, arg2);
    }

    public void writeEmptyElement(String arg0, String arg1) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeEmptyElement(arg0, arg1);
    }

    public void writeEmptyElement(String arg0, String arg1, String arg2) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeEmptyElement(arg0, arg1, arg2);
    }

    public void writeEmptyElement(String arg0) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeEmptyElement(arg0);
    }

    public void writeEndElement() throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeEndElement();
    }

    public void writeEndDocument() throws XMLStreamException {
        underlyingWriter.writeEndDocument();
    }

    public void close() throws XMLStreamException {
        underlyingWriter.close();
    }

    public void flush() throws XMLStreamException {
        underlyingWriter.flush();
    }

    public void writeAttribute(String localName, String value) throws XMLStreamException {
        writeAttribute(null, null, localName, value);
    }

    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        writeAttribute(null, namespaceURI, localName, value);
    }

    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
        if (!this.attr2ElemNames.contains(localName)) {
            underlyingWriter.writeAttribute(prefix, namespaceURI, localName, value);
        } else {
            if (unwrittenAttrs == null) {
                unwrittenAttrs = new LinkedList<XmlAttribute>();
            }
            unwrittenAttrs.add(new XmlAttribute(prefix, namespaceURI, localName, value));
        }
    }

    public void writeNamespace(String arg0, String arg1) throws XMLStreamException {
        underlyingWriter.writeNamespace(arg0, arg1);
    }

    public void writeDefaultNamespace(String arg0) throws XMLStreamException {
        underlyingWriter.writeDefaultNamespace(arg0);
    }

    public void writeComment(String arg0) throws XMLStreamException {
        underlyingWriter.writeComment(arg0);
    }

    public void writeProcessingInstruction(String arg0) throws XMLStreamException {
        underlyingWriter.writeProcessingInstruction(arg0);
    }

    public void writeProcessingInstruction(String arg0, String arg1) throws XMLStreamException {
        underlyingWriter.writeProcessingInstruction(arg0, arg1);
    }

    public void writeCData(String arg0) throws XMLStreamException {
        underlyingWriter.writeCData(arg0);
    }

    public void writeDTD(String arg0) throws XMLStreamException {
        underlyingWriter.writeDTD(arg0);
    }

    public void writeEntityRef(String arg0) throws XMLStreamException {
        underlyingWriter.writeEntityRef(arg0);
    }

    public void writeStartDocument() throws XMLStreamException {
        underlyingWriter.writeStartDocument();
    }

    public void writeStartDocument(String arg0) throws XMLStreamException {
        underlyingWriter.writeStartDocument(arg0);
    }

    public void writeStartDocument(String arg0, String arg1) throws XMLStreamException {
        underlyingWriter.writeStartDocument(arg0, arg1);
    }

    public void writeCharacters(String arg0) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeCharacters(arg0);
    }

    public void writeCharacters(char[] arg0, int arg1, int arg2) throws XMLStreamException {
        flushUnwrittenAttrs();
        underlyingWriter.writeCharacters(arg0, arg1, arg2);
    }

    public String getPrefix(String arg0) throws XMLStreamException {
        return underlyingWriter.getPrefix(arg0);
    }

    public void setPrefix(String arg0, String arg1) throws XMLStreamException {
        underlyingWriter.setPrefix(arg0, arg1);
    }

    public void setDefaultNamespace(String arg0) throws XMLStreamException {
        underlyingWriter.setDefaultNamespace(arg0);
    }

    public void setNamespaceContext(NamespaceContext arg0) throws XMLStreamException {
        underlyingWriter.setNamespaceContext(arg0);
    }

    public NamespaceContext getNamespaceContext() {
        return underlyingWriter.getNamespaceContext();
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        return underlyingWriter.getProperty(arg0);
    }
}
