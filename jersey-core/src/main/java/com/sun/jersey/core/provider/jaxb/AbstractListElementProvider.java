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

package com.sun.jersey.core.provider.jaxb;

import com.sun.jersey.core.impl.provider.entity.Inflector;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.core.util.ThrowHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractListElementProvider extends AbstractJAXBProvider<Collection<?>> {    

    public AbstractListElementProvider(Providers ps) {
        super(ps);
    }
    
    public AbstractListElementProvider(Providers ps, MediaType mt) {
        super(ps, mt);        
    }
    
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        if (type != List.class && type != Collection.class) return false;

        return verify(genericType);
    }
    
    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        if (!List.class.isAssignableFrom(type)) return false;

        return verify(genericType);
    }
    
    private boolean verify(Type genericType) {
        if (!(genericType instanceof ParameterizedType)) return false;

        final ParameterizedType pt = (ParameterizedType)genericType;

        if (pt.getActualTypeArguments().length > 1) return false;

        if (!(pt.getActualTypeArguments()[0] instanceof Class)) return false;

        final Class listClass = (Class)pt.getActualTypeArguments()[0];

        return listClass.isAnnotationPresent(XmlRootElement.class) || 
                listClass.isAnnotationPresent(XmlType.class);        
    }
    
    public final void writeTo(
            Collection<?> t, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            final Class elementType = getElementClass(type, genericType);
            final Charset c = getCharset(mediaType);
            final String cName = c.name();

            final Marshaller m = getMarshaller(elementType, mediaType);
            m.setProperty(Marshaller.JAXB_FRAGMENT, true);
            if (c != UTF8)
                m.setProperty(Marshaller.JAXB_ENCODING, cName);
            writeList(elementType, t, mediaType, c, m, entityStream);
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_MARSHALLING_JAXB(t.getClass()))
                    );
        }
    }

    /**
     * Write a collection of JAXB objects as child elements of the root element.
     * 
     * @param elementType the element type in the collection.
     * @param t the collecton to marshall
     * @param mediaType the media type
     * @param c the charset
     * @param m the marshaller
     * @param entityStream the output stream to marshall the collection
     * @throws javax.xml.bind.JAXBException
     * @throws IOException 
     */
    public abstract void writeList(Class<?> elementType, Collection<?> t,
            MediaType mediaType, Charset c,
            Marshaller m, OutputStream entityStream)
            throws JAXBException, IOException;
    
    public final List<?> readFrom(
            Class<Collection<?>> type, 
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException { 
        try {
            Class elementType = getElementClass(type, genericType);
                
            Unmarshaller u = getUnmarshaller(elementType, mediaType);
            
            XMLStreamReader r = getXMLStreamReader(mediaType, entityStream);
            
            List l = new ArrayList();
            
            // Move to root element
            r.nextTag();
            int event = r.next();
            // Move to first child (if any)
            while (event != XMLStreamReader.START_ELEMENT &&
                    event != XMLStreamReader.END_DOCUMENT)
                event = r.next();

            while (event != XMLStreamReader.END_DOCUMENT) {
                if (elementType.isAnnotationPresent(XmlRootElement.class))
                    l.add(u.unmarshal(r));
                else
                    l.add(u.unmarshal(r, elementType).getValue());

                // Move to next peer (if any)
                event = r.getEventType();
                while (event != XMLStreamReader.START_ELEMENT &&
                        event != XMLStreamReader.END_DOCUMENT)
                    event = r.next();
            }

            return l;
        } catch (UnmarshalException ex) {
            throw new WebApplicationException(ex, 400);
        } catch (XMLStreamException ex) {
            throw new WebApplicationException(ex, 400);
        } catch (JAXBException cause) {
            throw ThrowHelper.withInitCause(cause,
                    new IOException(ImplMessages.ERROR_UNMARSHALLING_JAXB(type))
                    );
        }
    }

    /**
     * Get the XMLStreamReader for unmarshalling.
     *
     * @param mediaType the media type.
     * @param entityStream the input stream.
     * @return the XMLStreamReader.
     * @throws javax.xml.stream.XMLStreamException
     */
    protected abstract XMLStreamReader getXMLStreamReader(MediaType mediaType,
            InputStream entityStream)
            throws XMLStreamException;

    protected Class getElementClass(Class<?> type, Type genericType) {
        ParameterizedType pt = (ParameterizedType)genericType;
        return (Class)pt.getActualTypeArguments()[0];
    }
    
    private final Inflector inflector = Inflector.getInstance();

    private final String convertToXmlName(final String name) {
        return name.replace("$", "_");
    }

    protected final String getRootElementName(Class<?> elementType) {
        return convertToXmlName(inflector.decapitalize(inflector.pluralize(inflector.demodulize(elementType.getName()))));
    }    
}