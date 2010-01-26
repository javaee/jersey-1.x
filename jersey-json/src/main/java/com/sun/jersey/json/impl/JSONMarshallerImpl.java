/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.json.impl;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONMarshaller;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import javax.xml.bind.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;
import java.io.File;
import java.io.OutputStream;
import java.io.Writer;

/**
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public final class JSONMarshallerImpl extends BaseJSONMarshaller implements Marshaller {

    public JSONMarshallerImpl(JAXBContext jaxbContext, JSONConfiguration jsonConfig) throws JAXBException {
        super(jaxbContext, jsonConfig);
    }

    // Marshaller
    public void marshal(Object jaxbObject, Result result) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, result);
    }

    public void marshal(Object jaxbObject, OutputStream os) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, os);
    }

    public void marshal(Object jaxbObject, File file) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, file);
    }

    public void marshal(Object jaxbObject, Writer writer) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, writer);
    }

    public void marshal(Object jaxbObject, ContentHandler handler) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, handler);
    }

    public void marshal(Object jaxbObject, Node node) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, node);
    }

    public void marshal(Object jaxbObject, XMLStreamWriter writer) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, writer);
    }

    public void marshal(Object jaxbObject, XMLEventWriter writer) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, writer);
    }

    public Node getNode(Object jaxbObject) throws JAXBException {
        return jaxbMarshaller.getNode(jaxbObject);
    }

    public void setProperty(String name, Object value) throws PropertyException {
        if(name == null)
            throw new IllegalArgumentException("Name can't be null.");

        if(name.equals(JSONMarshaller.FORMATTED)) {
            if(!(value instanceof Boolean)) {
                throw new PropertyException("property " + name + " must be an instance of type " +
                "boolean, not " + value.getClass().getName());
            }

            jsonConfig = JSONConfiguration.createJSONConfigurationWithFormatted(jsonConfig, (Boolean)value);
        } else {
            jaxbMarshaller.setProperty(name, value);
        }
    }

    public Object getProperty(String key) throws PropertyException {
        return jaxbMarshaller.getProperty(key);
    }

    public void setEventHandler(ValidationEventHandler handler) throws JAXBException {
        jaxbMarshaller.setEventHandler(handler);
    }

    public ValidationEventHandler getEventHandler() throws JAXBException {
        return jaxbMarshaller.getEventHandler();
    }

    public void setAdapter(XmlAdapter adapter) {
        jaxbMarshaller.setAdapter(adapter);
    }

    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        jaxbMarshaller.setAdapter(type, adapter);
    }

    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        return jaxbMarshaller.getAdapter(type);
    }

    public void setAttachmentMarshaller(AttachmentMarshaller marshaller) {
        jaxbMarshaller.setAttachmentMarshaller(marshaller);
    }

    public AttachmentMarshaller getAttachmentMarshaller() {
        return jaxbMarshaller.getAttachmentMarshaller();
    }

    public void setSchema(Schema schema) {
        jaxbMarshaller.setSchema(schema);
    }

    public Schema getSchema() {
        return jaxbMarshaller.getSchema();
    }

    public void setListener(Listener listener) {
        jaxbMarshaller.setListener(listener);
    }

    public Listener getListener() {
        return jaxbMarshaller.getListener();
    }
}
