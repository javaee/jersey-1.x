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
package com.sun.jersey.impl.json;

import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.impl.json.writer.JsonXmlStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.validation.Schema;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 *
 * @author japod
 */
public final class JSONMarshaller implements Marshaller {

    private final JAXBContext jaxbContext;
    private final Marshaller jaxbMarshaller;
    private JSONJAXBContext.JSONNotation jsonNotation;
    private boolean jsonEnabled;
    private boolean jsonRootUnwrapping;
    private Collection<String> arrays;
    private Collection<String> nonStrings;
    private Collection<String> attrAsElemNames;
    private Map<String, String> xml2jsonNamespace;

    public JSONMarshaller(JAXBContext jaxbContext, Map<String, Object> properties) throws JAXBException {
        try {
            this.jaxbContext = jaxbContext;
            this.jaxbMarshaller = jaxbContext.createMarshaller();
            setProperties(properties);
        } catch (PropertyException ex) {
            Logger.getLogger(JSONMarshaller.class.getName()).log(Level.SEVERE, null, ex);
            throw new JAXBException(ex);
        }
    }

    public void marshal(Object jaxbObject, Result result) throws JAXBException {
        jaxbMarshaller.marshal(jaxbObject, result);
    }

    public void marshal(Object jaxbObject, OutputStream os) throws JAXBException {
        if (jsonEnabled) {
            try {
                XMLStreamWriter xsw = createXmlStreamWriter(new OutputStreamWriter(os, getCharset()));
                jaxbMarshaller.marshal(jaxbObject, xsw);
                xsw.flush();
            } catch (XMLStreamException ex) {
                throw new JAXBException(ex.getMessage(), ex);
            }
        } else {
            jaxbMarshaller.marshal(jaxbObject, os);
        }
    }

    public void marshal(Object jaxbObject, File file) throws JAXBException {
        if (jsonEnabled) {
            try {
                XMLStreamWriter xsw = createXmlStreamWriter(
                        new OutputStreamWriter(new FileOutputStream(file), getCharset()));
                jaxbMarshaller.marshal(jaxbObject, xsw);
                xsw.flush();
            } catch (Exception ex) {
                Logger.getLogger(JSONMarshaller.class.getName()).log(
                        Level.SEVERE, "IOException caught when marshalling into a file.", ex);
                throw new JAXBException(ex.getMessage(), ex);
            }
        } else {
            jaxbMarshaller.marshal(jaxbObject, file);
        }
    }

    public void marshal(Object jaxbObject, Writer writer) throws JAXBException {
        if (jsonEnabled) {
            XMLStreamWriter xsw = createXmlStreamWriter(writer);
            jaxbMarshaller.marshal(jaxbObject, xsw);
            try {
                xsw.flush();
            } catch (XMLStreamException ex) {
                throw new JAXBException(ex.getMessage(), ex);
            }
        } else {
            jaxbMarshaller.marshal(jaxbObject, writer);
        }
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

    public void setProperty(String key, Object value) throws PropertyException {
        if (JSONJAXBContext.JSON_ENABLED.equals(key)) {
            this.jsonEnabled = (Boolean) value;
        } else if (JSONJAXBContext.JSON_NOTATION.equals(key)) {
            this.jsonNotation = JSONJAXBContext.JSONNotation.valueOf((String) value);
        } else if (JSONJAXBContext.JSON_ROOT_UNWRAPPING.equals(key)) {
            this.jsonRootUnwrapping = (Boolean) value;
        } else if (JSONJAXBContext.JSON_ARRAYS.equals(key)) {
            try {
                this.arrays = JSONTransformer.asCollection((String) value);
            } catch (JSONException e) {
                throw new PropertyException("JSON exception when trying to set " + JSONJAXBContext.JSON_ARRAYS + " property.", e);
            }
        } else if (JSONJAXBContext.JSON_NON_STRINGS.equals(key)) {
            try {
                this.nonStrings = JSONTransformer.asCollection((String) value);
            } catch (JSONException e) {
                throw new PropertyException("JSON exception when trying to set " + JSONJAXBContext.JSON_NON_STRINGS + " property.", e);
            }
        } else if (JSONJAXBContext.JSON_XML2JSON_NS.equals(key)) {
            try {
                this.xml2jsonNamespace = JSONTransformer.asMap((String) value);
            } catch (JSONException e) {
                throw new PropertyException("JSON exception when trying to set " + JSONJAXBContext.JSON_XML2JSON_NS + " property.", e);
            }
        } else if (JSONJAXBContext.JSON_ATTRS_AS_ELEMS.equals(key)) {
            try {
                this.attrAsElemNames = JSONTransformer.asCollection((String) value);
            } catch (JSONException e) {
                throw new PropertyException("JSON exception when trying to set " + JSONJAXBContext.JSON_ATTRS_AS_ELEMS + " property.", e);
            }
        } else {
            if (!key.startsWith(JSONJAXBContext.NAMESPACE)) {
                jaxbMarshaller.setProperty(key, value);
            }
        }
    }

    
    public Object getProperty(String key) throws PropertyException {
        if (JSONJAXBContext.JSON_ENABLED.equals(key)) {
            return this.jsonEnabled;
        } else if (JSONJAXBContext.JSON_NOTATION.equals(key)) {
            return this.jsonNotation.name();
        } else if (JSONJAXBContext.JSON_ROOT_UNWRAPPING.equals(key)) {
            return this.jsonRootUnwrapping;
        } else if (JSONJAXBContext.JSON_ARRAYS.equals(key)) {
            return JSONTransformer.asJsonArray(this.arrays);
        } else if (JSONJAXBContext.JSON_NON_STRINGS.equals(key)) {
            return JSONTransformer.asJsonArray(this.nonStrings);
        } else if (JSONJAXBContext.JSON_XML2JSON_NS.equals(key)) {
            return JSONTransformer.asJsonObject(this.xml2jsonNamespace);
        } else if (JSONJAXBContext.JSON_ATTRS_AS_ELEMS.equals(key)) {
            return JSONTransformer.asJsonArray(this.attrAsElemNames);
        } else {
            if (key.startsWith(JSONJAXBContext.NAMESPACE)) {
                return null;
            } else {
                return jaxbMarshaller.getProperty(key);
            }
        }
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

    private void setProperties(Map<String, Object> properties) throws PropertyException {
        if (null != properties) {
            for (Entry<String, Object> entry : properties.entrySet()) {
                setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private XMLStreamWriter createXmlStreamWriter(Writer writer) {
        XMLStreamWriter xmlStreamWriter;
        if (JSONJAXBContext.JSONNotation.MAPPED == this.jsonNotation) {
            xmlStreamWriter = JsonXmlStreamWriter.createWriter(writer, this.jsonRootUnwrapping, this.arrays, this.nonStrings, this.attrAsElemNames);
        } else if (JSONJAXBContext.JSONNotation.MAPPED_JETTISON == this.jsonNotation) {
                Configuration jmConfig;
                if (null == this.xml2jsonNamespace) {
                    jmConfig = new Configuration();
                } else {
                    jmConfig = new Configuration(this.xml2jsonNamespace);
                }
            xmlStreamWriter = new MappedXMLStreamWriter(
                    new MappedNamespaceConvention(jmConfig), writer);
        } else {
            xmlStreamWriter = new BadgerFishXMLStreamWriter(writer);
        }
        return xmlStreamWriter;
    }
    
    private Charset getCharset() throws JAXBException {
        String charset = (String)jaxbMarshaller.getProperty(Marshaller.JAXB_ENCODING);
        return (charset == null) ? Charset.forName("UTF-8") : Charset.forName(charset);
    }
}
