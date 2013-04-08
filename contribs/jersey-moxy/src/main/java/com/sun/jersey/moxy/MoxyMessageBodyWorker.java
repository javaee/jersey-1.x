/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.moxy;

import com.sun.jersey.core.util.FeaturesAndProperties;

import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jakub Podlesak
 */
@Provider
public class MoxyMessageBodyWorker implements MessageBodyWriter, MessageBodyReader {

    // this is a fake JAXB bean class to be used when looking up JAXB msg body workers
    @XmlRootElement
    public static class JaxbDollType {};

    final MessageBodyWorkers msgBodyWorkers;
    final Set<String> moxyPackageNames = new HashSet<String>();

    private final static JaxbDollType JaxbDoll = new JaxbDollType();

    public MoxyMessageBodyWorker(@Context MessageBodyWorkers mbw, @Context FeaturesAndProperties fap) {
        this.msgBodyWorkers = mbw;
        moxyPackageNames.addAll(getPackageNames(fap.getProperty(MoxyContextResolver.PROPERTY_MOXY_OXM_PACKAGE_NAMES)));
    }

    /* package */ Class getJAXBDollClass() {
        return JaxbDoll.getClass();
    }

    static List<String> getPackageNames(Object p) {
        if (p == null) {
            return Collections.EMPTY_LIST;
        } else if (p instanceof String) {
            return Arrays.asList(Helper.getElements(new String[]{(String)p}));
        } else if (p instanceof String[]) {
            return Arrays.asList(Helper.getElements((String[])p));
        } else {
            throw new IllegalArgumentException(MoxyContextResolver.PROPERTY_MOXY_OXM_PACKAGE_NAMES + " must " +
                    "have a property value of type String or String[]");
        }
    }

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return typeIsKnown(type) && mediaTypeIsXml(mediaType);
    }

    /* package*/  boolean typeIsKnown(Class type) {
        final Package typePackage = type.getPackage();
        return typePackage != null && moxyPackageNames.contains(typePackage.getName());
    }

    /* package*/ boolean mediaTypeIsXml(MediaType mediaType) {
        final String subtype = mediaType.getSubtype();
        if (subtype == null){
            return false;
        }
        return "xml".equals(subtype) || subtype.endsWith("+xml");
    }

    @Override
    public long getSize(Object t, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object t, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        lookupXmlWriter(msgBodyWorkers, mediaType).writeTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return typeIsKnown(type) && mediaTypeIsXml(mediaType);
    }

    @Override
    public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        return lookupXmlReader(msgBodyWorkers, mediaType).readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    /* package */ MessageBodyWriter lookupXmlWriter(MessageBodyWorkers mbw, MediaType mt) {
        for (MessageBodyWriter writer : mbw.getWriters(mt).get(mt)) {
            if (writer instanceof MoxyMessageBodyWorker) {
                continue;
            } else if (!writer.isWriteable(getJAXBDollClass(), getJAXBDollClass(), null, mt)){
                continue;
            } else {
                return writer;
            }
        }

        return null;
    }

    /* package*/ MessageBodyReader lookupXmlReader(MessageBodyWorkers mbw, MediaType mt) {
        for (MessageBodyReader reader : mbw.getReaders(mt).get(mt)) {
            if (reader instanceof MoxyMessageBodyWorker) {
                continue;
            } else if (!reader.isReadable(getJAXBDollClass(), getJAXBDollClass(), null, mt)){
                continue;
            } else {
                return reader;
            }
        }

        return null;
    }
}
