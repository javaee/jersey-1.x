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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
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
    
    private AtomicReference<Map<MediaType, List<MessageBodyReader>>> atomicReaderProviders = new AtomicReference<Map<MediaType, List<MessageBodyReader>>>();
    private AtomicReference<Map<MediaType, List<MessageBodyWriter>>> atomicWriterProviders = new AtomicReference<Map<MediaType, List<MessageBodyWriter>>>();
    
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
            headerProviders = cacheProviderList(atomicHeaderProviders, HeaderProvider.class);
        }
        
        for (HeaderProvider p: headerProviders) 
            if (p.supports(type))
                return p;

        throw new IllegalArgumentException("A header provider for type, " + type + ", is not supported");
    }
    
    private <T> Set<T> cacheProviderList(AtomicReference<Set<T>> atomicSet, Class<T> c) {
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

    private <T> Map<MediaType, List<T>> cacheProviderMap(AtomicReference<Map<MediaType, List<T>>> atomicMap, Class<T> c, Class<?> annotationClass) {
        synchronized(atomicMap) {
            Map<MediaType, List<T>> s = atomicMap.get();
            if (s == null) {
                s = new HashMap<MediaType, List<T>>();
                for (T p : ServiceFinder.find(c, true)) {
                    String values[] = getAnnotationValues(p.getClass(), annotationClass);
                    if (values==null)
                        cacheClassCapability(s, p, new MediaType());
                    else
                        for (String type: values)
                            cacheClassCapability(s, p, new MediaType(type));
                }     
                atomicMap.set(s);
            }
            return s;
        }
    }

    private <T> void cacheClassCapability(Map<MediaType, List<T>> capabilities, T provider, MediaType mediaType) {
        if (!capabilities.containsKey(mediaType))
            capabilities.put(mediaType, new ArrayList<T>());
        List<T> providers = capabilities.get(mediaType);
        providers.add(provider);
    }
    
    String[] getAnnotationValues(Class<?> clazz, Class<?> annotationClass) {
        String values[] = null;
        if (annotationClass.equals(ConsumeMime.class)) {
            ConsumeMime consumes = clazz.getAnnotation(ConsumeMime.class);
            if (consumes != null)
                values = consumes.value();
        } else if (annotationClass.equals(ProduceMime.class)) {
            ProduceMime produces = clazz.getAnnotation(ProduceMime.class);
            if (produces != null)
                values = produces.value();
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyReader<T> createMessageBodyReader(Class<T> type, MediaType mediaType) {
        Map<MediaType, List<MessageBodyReader>> entityProviders = atomicReaderProviders.get();
        if (entityProviders == null) {
            entityProviders = cacheProviderMap(atomicReaderProviders, MessageBodyReader.class, ConsumeMime.class);
        }
        
        List<MediaType> searchTypes = createSearchList(mediaType);
        for (MediaType t: searchTypes) {
            List<MessageBodyReader> readers = entityProviders.get(t);
            if (readers==null)
                continue;
            for (MessageBodyReader p: readers) {
                if (p.isReadable(type))
                    return p;
            }
        }
        
        throw new IllegalArgumentException("A message body reader for type, " + type + ", was not found");
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyWriter<T> createMessageBodyWriter(Class<T> type, MediaType mediaType) {
        Map<MediaType, List<MessageBodyWriter>> entityProviders = atomicWriterProviders.get();
        if (entityProviders == null) {
            entityProviders = cacheProviderMap(atomicWriterProviders, MessageBodyWriter.class, ProduceMime.class);
        }
        
        List<MediaType> searchTypes = createSearchList(mediaType);
        for (MediaType t: searchTypes) {
            List<MessageBodyWriter> writers = entityProviders.get(t);
            if (writers==null)
                continue;
            for (MessageBodyWriter p: writers) {
                if (p.isWriteable(type))
                    return p;
            }
        }
        
        throw new IllegalArgumentException("A message body writer for type, " + type + ", was not found");
    }

    private List<MediaType> createSearchList(MediaType mediaType) {
        if (mediaType==null)
            return Arrays.asList(new MediaType());
        else
            return Arrays.asList(mediaType, new MediaType(mediaType.getType(), MediaType.MEDIA_TYPE_WILDCARD), new MediaType());        
    }
}
