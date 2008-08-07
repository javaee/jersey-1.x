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

package com.sun.jersey.impl.provider.entity;

import java.util.Map;
import java.util.WeakHashMap;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractJAXBProvider<T> extends AbstractMessageReaderWriterProvider<T> {    
    private static Map<Class, JAXBContext> jaxbContexts = 
            new WeakHashMap<Class, JAXBContext>();

    private final Providers ps;
    
    private final boolean fixedMediaType;
    
    private final ContextResolver<JAXBContext> context;

    private final ContextResolver<Unmarshaller> unmarshaller;
    
    private final ContextResolver<Marshaller> marshaller;
    
    private final ContextResolver<JAXBContext> mtContext;

    private final ContextResolver<Unmarshaller> mtUnmarshaller;
    
    private final ContextResolver<Marshaller> mtMarshaller;
        
    public AbstractJAXBProvider(Providers ps) {
        this(ps, null);
    }
    
    public AbstractJAXBProvider(Providers ps, MediaType mt) {
        this.ps = ps;
        
        this.context = ps.getContextResolver(JAXBContext.class, null, null);
        this.unmarshaller = ps.getContextResolver(Unmarshaller.class, null, null);
        this.marshaller = ps.getContextResolver(Marshaller.class, null, null);
        
        fixedMediaType = mt != null;
        if (mt != null) {
            this.mtContext = ps.getContextResolver(JAXBContext.class, null, mt);
            this.mtUnmarshaller = ps.getContextResolver(Unmarshaller.class, null, mt);
            this.mtMarshaller = ps.getContextResolver(Marshaller.class, null, mt);            
        } else {
            this.mtContext = null;
            this.mtUnmarshaller = null;
            this.mtMarshaller = null;
        }
    }
    
    protected final Unmarshaller getUnmarshaller(Class type, MediaType mt) throws JAXBException {
        if (fixedMediaType)
            return getUnmarshaller(type);
        
        final ContextResolver<Unmarshaller> uncr = ps.getContextResolver(Unmarshaller.class, null, mt);            
        if (uncr != null) {
            Unmarshaller u = uncr.getContext(type);
            if (u != null) return u;
        }

        if (unmarshaller != null) {
            Unmarshaller u = unmarshaller.getContext(type);
            if (u != null) return u;
        }
        
        return getJAXBContext(type, mt).createUnmarshaller();
    }
    
    protected final Unmarshaller getUnmarshaller(Class type) throws JAXBException {
        if (mtUnmarshaller != null) {
            Unmarshaller u = mtUnmarshaller.getContext(type);
            if (u != null) return u;
        }

        if (unmarshaller != null) {
            Unmarshaller u = unmarshaller.getContext(type);
            if (u != null) return u;
        }
        
        return getJAXBContext(type).createUnmarshaller();
    }
    
    protected final Marshaller getMarshaller(Class type, MediaType mt) throws JAXBException {
        if (fixedMediaType)
            return getMarshaller(type);
        
        final ContextResolver<Marshaller> mcr = ps.getContextResolver(Marshaller.class, null, mt);            
        if (mcr != null) {
            Marshaller u = mcr.getContext(type);
            if (u != null) return u;
        }

        if (marshaller != null) {
            Marshaller u = marshaller.getContext(type);
            if (u != null) return u;
        }
        
        return getJAXBContext(type, mt).createMarshaller();
    }
    
    protected final Marshaller getMarshaller(Class type) throws JAXBException {
        if (mtMarshaller != null) {
            Marshaller u = mtMarshaller.getContext(type);
            if (u != null) return u;
        }

        if (marshaller != null) {
            Marshaller u = marshaller.getContext(type);
            if (u != null) return u;
        }
        
        return getJAXBContext(type).createMarshaller();
    }
    
    private final JAXBContext getJAXBContext(Class type) throws JAXBException {
        if (mtContext != null) {
            JAXBContext c = mtContext.getContext(type);
            if (c != null) return c;
        }
        
        if (context != null) {
            JAXBContext c = context.getContext(type);
            if (c != null) return c;
        }
        
        return getStoredJAXBContext(type);
    }
    
    private final JAXBContext getJAXBContext(Class type, MediaType mt) throws JAXBException {
        final ContextResolver<JAXBContext> cr = ps.getContextResolver(JAXBContext.class, null, mt);        
        if (cr != null) {
            JAXBContext c = cr.getContext(type);
            if (c != null) return c;
        }
        
        if (context != null) {
            JAXBContext c = context.getContext(type);
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
    
    public static final String getCharsetAsString(MediaType m) {
        return (m == null) ? null : m.getParameters().get("charset");
    }    
}