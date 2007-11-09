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

package com.sun.ws.rest.impl.provider;

import com.sun.ws.rest.spi.service.ServiceFinder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ProviderFactoryImpl extends ProviderFactory {
    private AtomicReference<Set<HeaderProvider>> atomicHeaderProviders = 
            new AtomicReference<Set<HeaderProvider>>();
    
    private AtomicReference<Set<MessageBodyReader>> atomicReaderProviders = 
            new AtomicReference<Set<MessageBodyReader>>();
    private AtomicReference<Set<MessageBodyWriter>> atomicWriterProviders = 
            new AtomicReference<Set<MessageBodyWriter>>();
    
    public <T> T createInstance(Class<T> type) {
        for (T t : ServiceFinder.find(type)) {
            return t;
        }     
        
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> HeaderProvider<T> createHeaderProvider(Class<T> type) {
        Set<HeaderProvider> headerProviders = atomicHeaderProviders.get();
        if (headerProviders == null) {
            headerProviders = cacheProviders(atomicHeaderProviders, HeaderProvider.class);
        }
        
        for (HeaderProvider p: headerProviders) 
            if (p.supports(type))
                return p;

        throw new IllegalArgumentException("A header provider for type, " + type + ", is not supported");
    }
    
    private <T> Set<T> cacheProviders(AtomicReference<Set<T>> atomicSet, Class<T> c) {
        synchronized(atomicSet) {
            Set<T> s = atomicSet.get();
            if (s == null) {
                s = new HashSet<T>();
                for (T p : ServiceFinder.find(c, true)) {
                    s.add(p);
                }     
                atomicSet.set(s);
            }
            return s;
        }
    }

    // TODO: implement selection by mediaType filtering
    @SuppressWarnings("unchecked")
    public <T> MessageBodyReader<T> createMessageBodyReader(Class<T> type, MediaType mediaType) {
        Set<MessageBodyReader> entityProviders = atomicReaderProviders.get();
        if (entityProviders == null) {
            entityProviders = cacheProviders(atomicReaderProviders, MessageBodyReader.class);
        }
        
        for (MessageBodyReader p: entityProviders) 
            if (p.isReadable(type))
                return p;
        
        throw new IllegalArgumentException("A message body reader for type, " + type + ", is not supported");
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyWriter<T> createMessageBodyWriter(Class<T> type, MediaType mediaType) {
        Set<MessageBodyWriter> entityProviders = atomicWriterProviders.get();
        if (entityProviders == null) {
            entityProviders = cacheProviders(atomicWriterProviders, MessageBodyWriter.class);
        }
        
        for (MessageBodyWriter p: entityProviders) 
            if (p.isWriteable(type))
                return p;
        
        throw new IllegalArgumentException("A message body writer for type, " + type + ", is not supported");
    }
}
