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

package com.sun.jersey.core.spi.factory;

import com.sun.jersey.api.MediaTypes;
import com.sun.jersey.core.util.KeyComparator;
import com.sun.jersey.core.util.KeyComparatorHashMap;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.service.ComponentProviderCache;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MessageBodyFactory implements MessageBodyWorkers {
    private static final KeyComparator<MediaType> MEDIA_TYPE_COMPARATOR = 
            new KeyComparator<MediaType>() {
        public boolean equals(MediaType x, MediaType y) {
            return x.getType().equalsIgnoreCase(y.getType())
                    && x.getSubtype().equalsIgnoreCase(y.getSubtype());
        }

        public int hash(MediaType k) {
            return k.getType().toLowerCase().hashCode() + 
                    k.getSubtype().toLowerCase().hashCode();
        }

        public int compare(MediaType o1, MediaType o2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }        
    };
    
    private final ComponentProviderCache componentProviderCache;
    
    private Map<MediaType, List<MessageBodyReader>> readerProviders;
    
    private Map<MediaType, List<MessageBodyWriter>> writerProviders;
    
    private List<MessageBodyWriterPair> writerListProviders;
    
    private static class MessageBodyWriterPair {
        final MessageBodyWriter mbw;
        
        final MediaType[] types;
    
        MessageBodyWriterPair(MessageBodyWriter mbw, MediaType[] types) {
            this.mbw = mbw;
            this.types = types;
        }
    }
    
    public MessageBodyFactory(ComponentProviderCache componentProviderCache) {
        this.componentProviderCache = componentProviderCache;
    }
     
    public void init() {
        initReaders();
        initWriters();
    }
    
    private void initReaders() {
        this.readerProviders = getProviderMap(MessageBodyReader.class, Consumes.class);            
    }
    
    private void initWriters() {
        this.writerProviders = new KeyComparatorHashMap<MediaType, List<MessageBodyWriter>>(
                MEDIA_TYPE_COMPARATOR);
        this.writerListProviders = new ArrayList<MessageBodyWriterPair>();
        
        for (MessageBodyWriter provider : componentProviderCache.getProvidersAndServices(MessageBodyWriter.class)) {
            MediaType values[] = getAnnotationValues(provider.getClass(), Produces.class);
            if (values == null)
                getClassCapability(writerProviders, provider, MediaTypes.GENERAL_MEDIA_TYPE);
            else
                for (MediaType type : values)
                    getClassCapability(writerProviders, provider, type);            

            writerListProviders.add(new MessageBodyWriterPair(provider, values));
        }   
    }
    
    private <T> Map<MediaType, List<T>> getProviderMap(
            Class<T> serviceClass,
            Class<?> annotationClass) {
        Map<MediaType, List<T>> s = new KeyComparatorHashMap<MediaType, List<T>>(
                MEDIA_TYPE_COMPARATOR);
        
        for (T provider : componentProviderCache.getProvidersAndServices(serviceClass)) {
            MediaType values[] = getAnnotationValues(provider.getClass(), annotationClass);
            if (values == null)
                getClassCapability(s, provider, MediaTypes.GENERAL_MEDIA_TYPE);
            else
                for (MediaType type : values)
                    getClassCapability(s, provider, type);            
        }   
        
        return s;        
    }

    private <T> void getClassCapability(Map<MediaType, List<T>> capabilities, 
            T provider, MediaType mediaType) {
        if (!capabilities.containsKey(mediaType))
            capabilities.put(mediaType, new ArrayList<T>());
        
        List<T> providers = capabilities.get(mediaType);
        providers.add(provider);
    }
    
    private MediaType[] getAnnotationValues(Class<?> clazz, Class<?> annotationClass) {
        String[] mts = _getAnnotationValues(clazz, annotationClass);
        if (mts == null) {
            MediaType[] mt = new MediaType[1];
            mt[0] = MediaTypes.GENERAL_ACCEPT_MEDIA_TYPE;
            return mt;
        }
        
        MediaType[] mt = new MediaType[mts.length];
        for (int i = 0; i < mts.length; i++)
            mt[i] = MediaType.valueOf(mts[i]);
        
        return mt;
    }
    
    private String[] _getAnnotationValues(Class<?> clazz, Class<?> annotationClass) {
        String values[] = null;
        if (annotationClass.equals(Consumes.class)) {
            Consumes consumes = clazz.getAnnotation(Consumes.class);
            if (consumes != null)
                values = consumes.value();
        } else if (annotationClass.equals(Produces.class)) {
            Produces produces = clazz.getAnnotation(Produces.class);
            if (produces != null)
                values = produces.value();
        }
        return values;
    }
    
    // MessageBodyWorkers
    
    @SuppressWarnings("unchecked")
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t, 
            Annotation[] as, 
            MediaType mediaType) {        
        MessageBodyReader p = null;
        if (mediaType != null) {
            p = _getMessageBodyReader(c, t, as, mediaType);
            if (p == null)
                p = _getMessageBodyReader(c, t, as, 
                        new MediaType(mediaType.getType(), MediaType.MEDIA_TYPE_WILDCARD));
        }
        if (p == null)
            p = _getMessageBodyReader(c, t, as, MediaTypes.GENERAL_MEDIA_TYPE);
        
        return p;
    }

    
    @SuppressWarnings("unchecked")
    private <T> MessageBodyReader<T> _getMessageBodyReader(Class<T> c, Type t, 
            Annotation[] as, 
            MediaType mediaType) {
        List<MessageBodyReader> readers = readerProviders.get(mediaType);
        if (readers == null)
            return null;
        for (MessageBodyReader p : readers) {
            if (p.isReadable(c, t, as, mediaType))
                return p;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType) {        
        MessageBodyWriter p = null;
        if (mediaType != null) {
            p = _getMessageBodyWriter(c, t, as, mediaType);
            if (p == null)
                p = _getMessageBodyWriter(c, t, as, 
                        new MediaType(mediaType.getType(), MediaType.MEDIA_TYPE_WILDCARD));
        }
        if (p == null)
            p = _getMessageBodyWriter(c, t, as, MediaTypes.GENERAL_MEDIA_TYPE);
        
        return p;
    }
    
    @SuppressWarnings("unchecked")
    private <T> MessageBodyWriter<T> _getMessageBodyWriter(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType) {        
        List<MessageBodyWriter> writers = writerProviders.get(mediaType);
        if (writers == null)
            return null;
        for (MessageBodyWriter p : writers) {
            if (p.isWriteable(c, t, as, mediaType))
                return p;
        }

        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<MediaType> getMessageBodyWriterMediaTypes(Class<T> c, Type t,
            Annotation[] as) {
        List<MediaType> mtl = new ArrayList<MediaType>();
        for (MessageBodyWriterPair mbwp : writerListProviders) {
            if (mbwp.mbw.isWriteable(c, t, as, null)) {
                for (MediaType mt : mbwp.types) mtl.add(mt);
            }
        }
        
        Collections.sort(mtl, MediaTypes.MEDIA_TYPE_COMPARATOR);
        return mtl;
    }
}