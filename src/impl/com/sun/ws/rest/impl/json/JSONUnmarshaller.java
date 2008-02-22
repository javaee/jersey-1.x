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
package com.sun.ws.rest.impl.json;

import com.sun.ws.rest.impl.json.reader.JsonXmlStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamReader;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 *
 * @author japod
 */
public class JSONUnmarshaller implements Unmarshaller {

    private JAXBContext jaxbContext;
    private Unmarshaller jaxbUnmarshaller;
    private JSONJAXBContext.JSONNotation jsonNotation;
    private boolean jsonEnabled;
    private boolean jsonRootUnwrapping;

    public JSONUnmarshaller(JAXBContext jaxbContext, Map<String, Object> properties) throws JAXBException {
        try {
            this.jaxbContext = jaxbContext;
            this.jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            setProperties(properties);
        } catch (PropertyException ex) {
            Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Object unmarshal(File file) throws JAXBException {
        if (jsonEnabled) {
            try {
                return this.jaxbUnmarshaller.unmarshal(createXmlStreamReader(new FileReader(file)));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
                throw new JAXBException(ex);
            }
        } else {
            return this.jaxbUnmarshaller.unmarshal(file);
        }
    }

    public Object unmarshal(InputStream inputStream) throws JAXBException {
        return unmarshal(inputStream, null);
    }
    
    public Object unmarshal(InputStream inputStream, Class<Object> type) throws JAXBException {
        if (jsonEnabled) {
            if (null != type) {
                return this.jaxbUnmarshaller.unmarshal(
                    createXmlStreamReader(new InputStreamReader(inputStream)), type);
            } else {
                return this.jaxbUnmarshaller.unmarshal(
                    createXmlStreamReader(new InputStreamReader(inputStream)));                
            }
        } else {
            return this.jaxbUnmarshaller.unmarshal(inputStream);
        }
    }

    public Object unmarshal(Reader reader) throws JAXBException {
        return unmarshal(reader, null);
    }

    public Object unmarshal(Reader reader, Class<Object> type) throws JAXBException {
        if (jsonEnabled) {
            if (null != type) {
                return this.jaxbUnmarshaller.unmarshal(createXmlStreamReader(reader), type);
            } else {
                return this.jaxbUnmarshaller.unmarshal(createXmlStreamReader(reader));
            }
        } else {
            return this.jaxbUnmarshaller.unmarshal(reader);
        }
    }

    public Object unmarshal(URL url) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(url);
        }
    }

    public Object unmarshal(InputSource inputSource) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(inputSource);
        }
    }

    public Object unmarshal(Node node) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(node);
        }
    }

    public <T> JAXBElement<T> unmarshal(Node node, Class<T> type) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(node, type);
        }
    }

    public Object unmarshal(Source source) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(source);
        }
    }

    public <T> JAXBElement<T> unmarshal(Source source, Class<T> type) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(source, type);
        }
    }

    public Object unmarshal(XMLStreamReader xmlStreamReader) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(xmlStreamReader);
        }
    }

    public <T> JAXBElement<T> unmarshal(XMLStreamReader xmlStreamReader, Class<T> type) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(xmlStreamReader, type);
        }
    }

    public Object unmarshal(XMLEventReader xmlEventReader) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(xmlEventReader);
        }
    }

    public <T> JAXBElement<T> unmarshal(XMLEventReader xmlEventReader, Class<T> type) throws JAXBException {
        if (jsonEnabled) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            return this.jaxbUnmarshaller.unmarshal(xmlEventReader, type);
        }
    }

    public UnmarshallerHandler getUnmarshallerHandler() {
        return this.jaxbUnmarshaller.getUnmarshallerHandler();
    }

    public void setValidating(boolean validating) throws JAXBException {
        this.jaxbUnmarshaller.setValidating(validating);
    }

    public boolean isValidating() throws JAXBException {
        return this.jaxbUnmarshaller.isValidating();
    }

    public void setEventHandler(ValidationEventHandler validationEventHandler) throws JAXBException {
        this.jaxbUnmarshaller.setEventHandler(validationEventHandler);
    }

    public ValidationEventHandler getEventHandler() throws JAXBException {
        return this.jaxbUnmarshaller.getEventHandler();
    }

    public void setProperty(String key, Object value) throws PropertyException {
        if (JSONJAXBContext.JSON_ENABLED.equals(key)) {
            this.jsonEnabled = (Boolean) value;
        } else if (JSONJAXBContext.JSON_NOTATION.equals(key)) {
            this.jsonNotation = JSONJAXBContext.JSONNotation.valueOf((String) value);
        } else if (JSONJAXBContext.JSON_ROOT_UNWRAPPING.equals(key)) {
            this.jsonRootUnwrapping = (Boolean) value;
        } else {
            this.jaxbUnmarshaller.setProperty(key, value);
        }
    }

    public Object getProperty(String key) throws PropertyException {
        if (JSONJAXBContext.JSON_ENABLED.equals(key)) {
            return this.jsonEnabled;
        } else if (JSONJAXBContext.JSON_NOTATION.equals(key)) {
            return this.jsonNotation.name();
        } else if (JSONJAXBContext.JSON_ROOT_UNWRAPPING.equals(key)) {
            return this.jsonRootUnwrapping;
        } else {
            return this.jaxbUnmarshaller.getProperty(key);
        }
    }

    public void setSchema(Schema schema) {
        this.jaxbUnmarshaller.setSchema(schema);
    }

    public Schema getSchema() {
        return this.jaxbUnmarshaller.getSchema();
    }

    public void setAdapter(XmlAdapter xmlAdapter) {
        this.jaxbUnmarshaller.setAdapter(xmlAdapter);
    }

    public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter) {
        this.jaxbUnmarshaller.setAdapter(type, adapter);
    }

    public <A extends XmlAdapter> A getAdapter(Class<A> type) {
        return this.jaxbUnmarshaller.getAdapter(type);
    }

    public void setAttachmentUnmarshaller(AttachmentUnmarshaller attachmentUnmarshaller) {
        this.jaxbUnmarshaller.setAttachmentUnmarshaller(attachmentUnmarshaller);
    }

    public AttachmentUnmarshaller getAttachmentUnmarshaller() {
        return this.jaxbUnmarshaller.getAttachmentUnmarshaller();
    }

    public void setListener(Listener listener) {
        this.jaxbUnmarshaller.setListener(listener);
    }

    public Listener getListener() {
        return this.jaxbUnmarshaller.getListener();
    }

    private void setProperties(Map<String, Object> properties) throws PropertyException {
        if (null != properties) {
            for (Entry<String, Object> entry : properties.entrySet()) {
                setProperty(entry.getKey(), entry.getValue());
            }
        }
    }
    XMLStreamReader createXmlStreamReader(Reader reader) {
        if (JSONJAXBContext.JSONNotation.MAPPED == this.jsonNotation) {
            try {
                return new JsonXmlStreamReader(reader, this.jsonRootUnwrapping);
            } catch (IOException ex) {
                Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (JSONJAXBContext.JSONNotation.MAPPED_JETTISON == this.jsonNotation) {
            try {
                return new MappedXMLStreamReader(new JSONObject(new JSONTokener(readFromAsString(reader))));
            } catch (Exception ex) {
                Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                return new BadgerFishXMLStreamReader(new JSONObject(new JSONTokener(readFromAsString(reader))));
            } catch (Exception ex) {
                Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public final String readFromAsString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        } 
        return sb.toString();
    }    
}
