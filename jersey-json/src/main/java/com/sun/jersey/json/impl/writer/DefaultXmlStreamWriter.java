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
package com.sun.jersey.json.impl.writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.jersey.json.impl.reader.JsonNamespaceContext;

/**
 * This class contains a default implementation of {@code XMLStreamWriter} methods that aren't expected of doing anything
 * (have empty body or return a {@code null} reference) in case of marshalling a {@code JAXBBean} into a JSON representation.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public abstract class DefaultXmlStreamWriter implements XMLStreamWriter {

    private NamespaceContext namespaceContext = null;

    @Override
    public NamespaceContext getNamespaceContext() {
        if (namespaceContext == null) {
            namespaceContext = new JsonNamespaceContext();
        }
        return namespaceContext;
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return getNamespaceContext().getPrefix(uri);
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        this.namespaceContext = context;
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        writeCharacters(data);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        // we do not want to deal with namespaces
        // the main goal of this writer is keep the produced json as simple as possible
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        // do nothing
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        // do nothing
    }

    public void writeStartDocument() throws XMLStreamException {
        // do nothing
    }
    
}
