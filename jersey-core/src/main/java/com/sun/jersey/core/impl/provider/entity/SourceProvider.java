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

package com.sun.jersey.core.impl.provider.entity;

import com.sun.jersey.core.util.ThrowHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
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
    
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class StreamSourceReader implements 
            MessageBodyReader<StreamSource> {
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
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
    
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class SAXSourceReader implements 
            MessageBodyReader<SAXSource> {
        private final SAXParserFactory spf;

        public SAXSourceReader(@Context SAXParserFactory spf) {
            this.spf = spf;
        }

        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return SAXSource.class == t;
        }

        public SAXSource readFrom(
                Class<SAXSource> t, 
                Type gt, 
                Annotation[] as, 
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            try {
                return new SAXSource(spf.newSAXParser().getXMLReader(),
                        new InputSource(entityStream));
            } catch (Exception ex) {
                throw ThrowHelper.withInitCause(ex,
                        new IOException("Error creating SAXSource")
                        );
            }
        }
    }
    
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class DOMSourceReader implements 
            MessageBodyReader<DOMSource> {
        private final DocumentBuilderFactory dbf;
        
        public DOMSourceReader(@Context DocumentBuilderFactory dbf) {
            this.dbf = dbf;
        }
        
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
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
    
    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class SourceWriter implements 
            MessageBodyWriter<Source> {

        private final TransformerFactory tf;
        
        public SourceWriter() {
            tf = TransformerFactory.newInstance();
        }
        
        public boolean isWriteable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return Source.class.isAssignableFrom(t);
        }

        public long getSize(Source o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
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