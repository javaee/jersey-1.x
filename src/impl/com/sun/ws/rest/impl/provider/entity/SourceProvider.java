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

package com.sun.ws.rest.impl.provider.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class SourceProvider {
    
    public static final class StreamSourceReader implements 
            MessageBodyReader<StreamSource> {
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as) {
            return StreamSource.class == t;
        }

        public StreamSource readFrom(
                Class<StreamSource> t, 
                Type gt, 
                Annotation[] as, 
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            return new StreamSource(entityStream);
        }
    }
    
    public static final class SAXSourceReader implements 
            MessageBodyReader<SAXSource> {
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as) {
            return SAXSource.class == t;
        }

        public SAXSource readFrom(
                Class<SAXSource> t, 
                Type gt, 
                Annotation[] as, 
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            return new SAXSource(new InputSource(entityStream));
        }
    }
    
    public static final class DOMSourceReader implements 
            MessageBodyReader<DOMSource> {
        private final DocumentBuilderFactory dbf;
        
        public DOMSourceReader() {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
        }
        
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as) {
            return DOMSource.class == t;
        }

        public DOMSource readFrom(
                Class<DOMSource> t, 
                Type gt, 
                Annotation[] as, 
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            try {
                Document d = dbf.newDocumentBuilder().parse(entityStream);
                return new DOMSource(d);
            } catch (SAXException ex) {
                throw getIOException(ex);
            } catch (ParserConfigurationException ex) {
                throw getIOException(ex);
            }
        }
    }
    
    public static final class SourceWriter implements 
            MessageBodyWriter<Source> {

        private final TransformerFactory tf;
        
        public SourceWriter() {
            tf = TransformerFactory.newInstance();
        }
        
        public boolean isWriteable(Class<?> t, Type gt, Annotation[] as) {
            return Source.class.isAssignableFrom(t);
        }

        public long getSize(Source o) {
            return -1;
        }

        public void writeTo(Source o, Class<?> t, Type gt, Annotation[] as, 
                MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, 
                OutputStream entityStream) throws IOException {
            try {
                StreamResult sr = new StreamResult(entityStream);
                tf.newTransformer().transform(o, sr);
            } catch (TransformerException ex) {
                throw getIOException(ex);
            }
        }        
    }
    
    private static IOException getIOException(Exception cause) throws IOException {
        IOException e = new IOException();
        e.initCause(cause);
        return e;
    }    
}