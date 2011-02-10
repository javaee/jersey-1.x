/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.atom.abdera;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;

/**
 * Helper class to assist in serializing and deserializing Java entities
 * as XML that will be transferred through the <code>content</code> element
 * of an Atom <code>Entry</code> instance.  A configured instance of this
 * class can be made available in a resource class as follows:
 *
 * <blockquote><pre>
 *   &#064;Context
 *   private ContentHelper contentHelper;
 * </blockquote></pre>
 */
public class ContentHelper {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a configured instance of this helper.
     *
     * @param providers Providers for this application
     */
    public ContentHelper(Providers providers) {
        this.providers = providers;
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Singleton Abdera instance for this application.</p>
     */
    private Abdera abdera = Abdera.getInstance();


    /**
     * <p>Empty array of annotations for calls to message body readers and writers.</p>
     */
    private Annotation[] emptyAnnotations = new Annotation[0];


    /**
     * <p>Empty map of headers for calls to message body readers and writers.</p>
     */
    private MultivaluedMap<String,String> emptyHeaders = new MultivaluedMapImpl();


    /**
     * <p>The injected helper to look up appropriate <code>Provider</code>
     * instances.</p>
     */
    private Providers providers;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Deserialize the content element of the specified entry, and
     * transform it back into an appropriate Java object.  The media type
     * used for selecting an appropriate <code>Provider</code> will be
     * acquired from the <code>type</code> attribute of the <code>content</code>
     * element.</p>
     *
     * @param entry <code>Entry</code> whose content element is to be processed
     * @param clazz <code>Class</code> of the object to be returned
     *
     * @exception IllegalArgumentException if the specified entry does not
     *  contain a valid content element
     */
    public <T> T getContentEntity(Entry entry, Class<T> clazz) {
        String[] parts = entry.getContentMimeType().toString().split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid content type '" + entry.getContentMimeType().toString() + "'");
        }
        return getContentEntity(entry, new MediaType(parts[0], parts[1]), clazz);
    }


    /**
     * <p>Deserialize the content element of the specified entry, and
     * transform it back into an appropriate Java object.</p>
     *
     * @param entry <code>Entry</code> whose content element is to be processed
     * @param mediaType <code>MediaType</code> to use when selecting an
     *  appropriate provider
     * @param clazz <code>Class</code> of the object to be returned
     *
     * @exception IllegalArgumentException if the specified entry does not
     *  contain a valid content element
     */
    public <T> T getContentEntity(Entry entry, MediaType mediaType, Class<T> clazz) {

        // Select the MessageBodyReader we will use
        MessageBodyReader<T> reader =
          providers.getMessageBodyReader(clazz, clazz, emptyAnnotations, mediaType);
        if (reader == null) {
            throw new IllegalArgumentException
              ("No MessageBodyReader for class '" +
               clazz.getName() + "' and media type '" + mediaType + "'");
        }

        // Extract the content element as an XML byte stream
        if ((entry.getContentElement() == null) || (entry.getContentElement().getValueElement() == null)) {
            throw new IllegalArgumentException("Entry does not contain a valid content element");
        }
        Element element = entry.getContentElement().getValueElement();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            element.writeTo(baos);
        } catch (IOException e) {
            // Can not happen
        }

        // Transform the byte stream into an appropriate Java object
        try {
            return reader.readFrom(clazz, clazz, emptyAnnotations,
                                   mediaType, emptyHeaders,
                                   new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            // Can not happen
            return null;
        }

    }


    /**
     * <p>Serialize the specified entity as the <code>content</code> element
     * of the specified <code>entry</code>.  The selected provider <strong>MUST</strong>
     * produce an XML representation.</p>
     *
     * @param entry <code>Entry</code> whose content element is to be set
     * @param mediaType <code>MediaType</code> to pass as the <code>type</code>
     *  attribute of the <code>content</code> element (also used to select an
     *  appropriate <code>Provider</code>)
     * @param entity Entity to be serialized
     */
    public void setContentEntity(Entry entry, MediaType mediaType, Object entity) {

        // Select the MessageBodyWriter we will use
        MessageBodyWriter writer =
          providers.getMessageBodyWriter(entity.getClass(),
                                         entity.getClass(),
                                         emptyAnnotations,
                                         mediaType);
        if (writer == null) {
            throw new IllegalArgumentException
              ("No MessageBodyWriter for class '" +
               entity.getClass().getName() + "' and media type '" + mediaType + "'");
        }

        // Serialize this entity to a byte stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writer.writeTo(entity, entity.getClass(), entity.getClass(),
                           emptyAnnotations, mediaType, emptyHeaders, baos);
        } catch (IOException e) {
            // Can not happen
        }

        // Parse the XML into an Abdera Element (yes, this is pretty smelly)
        // and set it as the content element
        Parser parser = abdera.getParser();
        Document document = parser.parse(new ByteArrayInputStream(baos.toByteArray()));
        entry.setContent(document.getRoot(), mediaType.toString());

    }


}
