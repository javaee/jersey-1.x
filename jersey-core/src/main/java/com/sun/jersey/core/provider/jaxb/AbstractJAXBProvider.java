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

package com.sun.jersey.core.provider.jaxb;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.sun.jersey.core.util.FeaturesAndProperties;
import org.xml.sax.InputSource;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A base class for implementing JAXB-based readers and writers.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractJAXBProvider<T> extends AbstractMessageReaderWriterProvider<T> {    
    private static final Map<Class, JAXBContext> jaxbContexts = 
            new WeakHashMap<Class, JAXBContext>();

    private final Providers ps;
    
    private final boolean fixedMediaType;
    
    private final ContextResolver<JAXBContext> mtContext;

    private final ContextResolver<Unmarshaller> mtUnmarshaller;
    
    private final ContextResolver<Marshaller> mtMarshaller;

    private boolean formattedOutput = false;

    private boolean xmlRootElementProcessing = false;

    public AbstractJAXBProvider(Providers ps) {
        this(ps, null);
    }
    
    public AbstractJAXBProvider(Providers ps, MediaType mt) {
        this.ps = ps;
        
        fixedMediaType = mt != null;
        if (fixedMediaType) {
            this.mtContext = ps.getContextResolver(JAXBContext.class, mt);
            this.mtUnmarshaller = ps.getContextResolver(Unmarshaller.class, mt);
            this.mtMarshaller = ps.getContextResolver(Marshaller.class, mt);            
        } else {
            this.mtContext = null;
            this.mtUnmarshaller = null;
            this.mtMarshaller = null;
        }
    }

    @Context
    public void setConfiguration(FeaturesAndProperties fp) {
        formattedOutput = fp.getFeature(FeaturesAndProperties.FEATURE_FORMATTED);
        xmlRootElementProcessing = fp.getFeature(FeaturesAndProperties.FEATURE_XMLROOTELEMENT_PROCESSING);
    }

    protected boolean isSupported(MediaType m) {
        return true;
    }
    
    protected final Unmarshaller getUnmarshaller(Class type, MediaType mt) throws JAXBException {
        if (fixedMediaType)
            return getUnmarshaller(type);
        
        final ContextResolver<Unmarshaller> uncr = ps.getContextResolver(Unmarshaller.class, mt);            
        if (uncr != null) {
            Unmarshaller u = uncr.getContext(type);
            if (u != null) return u;
        }

        return getJAXBContext(type, mt).createUnmarshaller();
    }
    
    private final Unmarshaller getUnmarshaller(Class type) throws JAXBException {
        if (mtUnmarshaller != null) {
            Unmarshaller u = mtUnmarshaller.getContext(type);
            if (u != null) return u;
        }
        
        return getJAXBContext(type).createUnmarshaller();
    }
    
    protected final Marshaller getMarshaller(Class type, MediaType mt) throws JAXBException {
        if (fixedMediaType)
            return getMarshaller(type);
        
        final ContextResolver<Marshaller> mcr = ps.getContextResolver(Marshaller.class, mt);            
        if (mcr != null) {
            Marshaller m = mcr.getContext(type);
            if (m != null) return m;
        }

        Marshaller m = getJAXBContext(type, mt).createMarshaller();
        if(formattedOutput)
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
        return m;

    }
    
    private final Marshaller getMarshaller(Class type) throws JAXBException {
        if (mtMarshaller != null) {
            Marshaller u = mtMarshaller.getContext(type);
            if (u != null) return u;
        }

        Marshaller m = getJAXBContext(type).createMarshaller();
        if(formattedOutput)
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
        return m;
    }
    
    private final JAXBContext getJAXBContext(Class type, MediaType mt) throws JAXBException {
        final ContextResolver<JAXBContext> cr = ps.getContextResolver(JAXBContext.class, mt);
        if (cr != null) {
            JAXBContext c = cr.getContext(type);
            if (c != null) return c;
        }

        return getStoredJAXBContext(type);
    }

    private final JAXBContext getJAXBContext(Class type) throws JAXBException {
        if (mtContext != null) {
            JAXBContext c = mtContext.getContext(type);
            if (c != null) return c;
        }
        
        return getStoredJAXBContext(type);
    }
    
    protected JAXBContext getStoredJAXBContext(Class type) throws JAXBException {
        synchronized (jaxbContexts) {
            JAXBContext c = jaxbContexts.get(type);
            if (c == null) {
                c = JAXBContext.newInstance(type);
                jaxbContexts.put(type, c);
            }
            return c;
        }        
    }
    
    protected static SAXSource getSAXSource(SAXParserFactory spf,
            InputStream entityStream) throws JAXBException {
        try {
            return new SAXSource(
                    spf.newSAXParser().getXMLReader(),
                    new InputSource(entityStream));
        } catch (Exception ex) {
            throw new JAXBException("Error creating SAXSource", ex);
        }
    }

    protected boolean isFormattedOutput() {
        return formattedOutput;
    }

    protected boolean isXmlRootElementProcessing() {
        return xmlRootElementProcessing;
    }
}