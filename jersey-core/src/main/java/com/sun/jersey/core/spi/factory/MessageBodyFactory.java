/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.core.spi.factory;

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.reflection.ReflectionHelper.DeclaringClassInterfacePair;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.core.util.KeyComparator;
import com.sun.jersey.core.util.KeyComparatorHashMap;
import com.sun.jersey.core.util.KeyComparatorLinkedHashMap;
import com.sun.jersey.spi.MessageBodyWorkers;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A factory for managing {@link MessageBodyReader} and {@link MessageBodyWriter}
 * instances.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MessageBodyFactory implements MessageBodyWorkers {
    /* package */ static final KeyComparator<MediaType> MEDIA_TYPE_COMPARATOR =
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
    
    private final ProviderServices providerServices;

    private final boolean deprecatedProviderPrecedence;
    
    private Map<MediaType, List<MessageBodyReader>> readerProviders;
    
    private Map<MediaType, List<MessageBodyWriter>> writerProviders;

    private List<MessageBodyWriterPair> writerListProviders;

    private Map<MediaType, List<MessageBodyReader>> customReaderProviders;

    private Map<MediaType, List<MessageBodyWriter>> customWriterProviders;

    private List<MessageBodyWriterPair> customWriterListProviders;

    private static class MessageBodyWriterPair {
        final MessageBodyWriter mbw;
        
        final List<MediaType> types;
    
        MessageBodyWriterPair(MessageBodyWriter mbw, List<MediaType> types) {
            this.mbw = mbw;
            this.types = types;
        }
    }
    
    public MessageBodyFactory(ProviderServices providerServices, boolean deprecatedProviderPrecedence) {
        this.providerServices = providerServices;
        this.deprecatedProviderPrecedence = deprecatedProviderPrecedence;
    }

    private static class DistanceComparator<T> implements Comparator<T> {
        private final Class<T> c;

        private final Map<Class, Integer> distanceMap = new HashMap<Class, Integer>();

        DistanceComparator(Class c) {
            this.c = c;
        }
        
        public int compare(T o1, T o2) {
            int d1 = getDistance(o1);
            int d2 = getDistance(o2);
            return d2 - d1;
        }

        int getDistance(T t) {
            Integer d = distanceMap.get(t.getClass());
            if (d != null)
                return d;

            DeclaringClassInterfacePair p = ReflectionHelper.getClass(
                    t.getClass(), c);

            Class[] as = ReflectionHelper.getParameterizedClassArguments(p);
            Class a = (as != null) ? as[0] : null;
            d = 0;
            while (a != null && a != Object.class) {
                d++;
                a = a.getSuperclass();
            }

            distanceMap.put(t.getClass(), d);
            return d;
        }
    }

    public void init() {
        initReaders();
        initWriters();
    }

    private void initReaders() {
        this.customReaderProviders = new KeyComparatorHashMap<MediaType, List<MessageBodyReader>>(
                MEDIA_TYPE_COMPARATOR);
        this.readerProviders = new KeyComparatorHashMap<MediaType, List<MessageBodyReader>>(
                MEDIA_TYPE_COMPARATOR);

        if(deprecatedProviderPrecedence) {
            initReaders(this.readerProviders, providerServices.getProvidersAndServices(MessageBodyReader.class));
        } else {
            initReaders(this.customReaderProviders, providerServices.getProviders(MessageBodyReader.class));
            initReaders(this.readerProviders, providerServices.getServices(MessageBodyReader.class));
        }
    }

    private void initReaders(Map<MediaType, List<MessageBodyReader>> providersMap, Set<MessageBodyReader> providersSet) {
        for (MessageBodyReader provider : providersSet) {
            List<MediaType> values = MediaTypes.createMediaTypes(
                    provider.getClass().getAnnotation(Consumes.class));
            for (MediaType type : values)
                getClassCapability(providersMap, provider, type);
        }

        DistanceComparator<MessageBodyReader> dc = new DistanceComparator<MessageBodyReader>(MessageBodyReader.class);
        for (Map.Entry<MediaType, List<MessageBodyReader>> e : providersMap.entrySet()) {
            Collections.sort(e.getValue(), dc);
        }
    }
    
    private void initWriters() {
        this.customWriterProviders = new KeyComparatorHashMap<MediaType, List<MessageBodyWriter>>(
                MEDIA_TYPE_COMPARATOR);
        this.customWriterListProviders = new ArrayList<MessageBodyWriterPair>();

        this.writerProviders = new KeyComparatorHashMap<MediaType, List<MessageBodyWriter>>(
                MEDIA_TYPE_COMPARATOR);
        this.writerListProviders = new ArrayList<MessageBodyWriterPair>();

        if(deprecatedProviderPrecedence) {
            initWriters(writerProviders,writerListProviders, providerServices.getProvidersAndServices(MessageBodyWriter.class));
        } else {
            initWriters(customWriterProviders, customWriterListProviders, providerServices.getProviders(MessageBodyWriter.class));
            initWriters(writerProviders, writerListProviders, providerServices.getServices(MessageBodyWriter.class));
        }
    }

    private void initWriters(Map<MediaType, List<MessageBodyWriter>> providersMap, List<MessageBodyWriterPair> listProviders, Set<MessageBodyWriter> providersSet) {
        for (MessageBodyWriter provider : providersSet) {
            List<MediaType> values = MediaTypes.createMediaTypes(
                    provider.getClass().getAnnotation(Produces.class));
            for (MediaType type : values)
                getClassCapability(providersMap, provider, type);

            listProviders.add(new MessageBodyWriterPair(provider, values));
        }

        final DistanceComparator<MessageBodyWriter> dc = new DistanceComparator<MessageBodyWriter>(MessageBodyWriter.class);
        for (Map.Entry<MediaType, List<MessageBodyWriter>> e : providersMap.entrySet()) {
            Collections.sort(e.getValue(), dc);
        }

        Collections.sort(listProviders, new Comparator<MessageBodyWriterPair>() {
            public int compare(MessageBodyWriterPair p1, MessageBodyWriterPair p2) {
                return dc.compare(p1.mbw, p2.mbw);
            }
        });
    }
    
    private <T> void getClassCapability(Map<MediaType, List<T>> capabilities, 
            T provider, MediaType mediaType) {
        if (!capabilities.containsKey(mediaType))
            capabilities.put(mediaType, new ArrayList<T>());
        
        List<T> providers = capabilities.get(mediaType);
        providers.add(provider);
    }
    
    // MessageBodyWorkers
    
    public Map<MediaType, List<MessageBodyReader>> getReaders(MediaType mediaType) {
        Map<MediaType, List<MessageBodyReader>> subSet =
                new KeyComparatorLinkedHashMap<MediaType, List<MessageBodyReader>>(
                MEDIA_TYPE_COMPARATOR);

        if(!customReaderProviders.isEmpty())
            getCompatibleReadersWritersMap(mediaType, customReaderProviders, subSet);
        getCompatibleReadersWritersMap(mediaType, readerProviders, subSet);
        return subSet;
    }

    public Map<MediaType, List<MessageBodyWriter>> getWriters(MediaType mediaType) {
        Map<MediaType, List<MessageBodyWriter>> subSet =
                new KeyComparatorLinkedHashMap<MediaType, List<MessageBodyWriter>>(
                MEDIA_TYPE_COMPARATOR);

        if(!customWriterProviders.isEmpty())
            getCompatibleReadersWritersMap(mediaType, customWriterProviders, subSet);
        getCompatibleReadersWritersMap(mediaType, writerProviders, subSet);
        return subSet;
    }

    public String readersToString(Map<MediaType, List<MessageBodyReader>> readers) {
        return toString(readers);
    }

    public String writersToString(Map<MediaType, List<MessageBodyWriter>> writers) {
        return toString(writers);
    }

    private <T> String toString(Map<MediaType, List<T>> set) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        for (Map.Entry<MediaType, List<T>> e : set.entrySet()) {
            pw.append(e.getKey().toString()).println(" ->");
            for (T t : e.getValue()) {
                pw.append("  ").println(t.getClass().getName());
            }
        }
        pw.flush();
        return sw.toString();
    }

    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t, 
            Annotation[] as, 
            MediaType mediaType) {

        MessageBodyReader reader;

        if(!customReaderProviders.isEmpty()) {
            reader = _getMessageBodyReader(c, t, as, mediaType, customReaderProviders);
            if(reader != null)
                return reader;
        }
        reader = _getMessageBodyReader(c, t, as, mediaType, readerProviders);

        return reader;
    }

    private <T> MessageBodyReader<T> _getMessageBodyReader(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType,
            Map<MediaType, List<MessageBodyReader>> providers) {
        MessageBodyReader p = null;
        if (mediaType != null) {
            p = _getMessageBodyReader(c, t, as, mediaType, mediaType, providers);
            if (p == null)
                p = _getMessageBodyReader(c, t, as, mediaType,
                        MediaTypes.getTypeWildCart(mediaType), providers);
        }
        if (p == null)
            p = _getMessageBodyReader(c, t, as, mediaType, MediaTypes.GENERAL_MEDIA_TYPE, providers);

        return p;
    }
    
    private <T> MessageBodyReader<T> _getMessageBodyReader(Class<T> c, Type t, 
            Annotation[] as, 
            MediaType mediaType, MediaType lookup) {

        MessageBodyReader reader;

        if(!customReaderProviders.isEmpty()) {
            reader = _getMessageBodyReader(c, t, as, mediaType, lookup, customReaderProviders);
            if(reader != null)
                return reader;
        }
        reader = _getMessageBodyReader(c, t, as, mediaType, lookup, readerProviders);

        return reader;
    }

    private <T> MessageBodyReader<T> _getMessageBodyReader(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType, MediaType lookup,
            Map<MediaType, List<MessageBodyReader>> providers) {

        List<MessageBodyReader> readers = providers.get(lookup);
        if (readers == null)
            return null;
        for (MessageBodyReader p : readers) {
            if (p.isReadable(c, t, as, mediaType)) {
                return p;
            }
        }
        return null;
    }
    
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType) {

        MessageBodyWriter p;

        if(!customWriterProviders.isEmpty()) {
            p = _getMessageBodyWriter(c, t, as, mediaType, customWriterProviders);
            if(p != null)
                return p;
        }
        p = _getMessageBodyWriter(c, t, as, mediaType, writerProviders);

        return p;
    }
    
    private <T> MessageBodyWriter<T> _getMessageBodyWriter(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType,
            Map<MediaType, List<MessageBodyWriter>> providers) {

        MessageBodyWriter p = null;

        if (mediaType != null) {
            p = _getMessageBodyWriter(c, t, as, mediaType, mediaType, providers);
            if (p == null)
                p = _getMessageBodyWriter(c, t, as, mediaType,
                        MediaTypes.getTypeWildCart(mediaType), providers);
        }
        if (p == null)
            p = _getMessageBodyWriter(c, t, as, mediaType, MediaTypes.GENERAL_MEDIA_TYPE, providers);

        return p;
    }

    private <T> MessageBodyWriter<T> _getMessageBodyWriter(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType, MediaType lookup,
            Map<MediaType, List<MessageBodyWriter>> providers) {
        List<MessageBodyWriter> writers = providers.get(lookup);
        if (writers == null)
            return null;
        for (MessageBodyWriter p : writers) {
            if (p.isWriteable(c, t, as, mediaType)) {
                return p;
            }
        }

        return null;
    }

    private <T> void getCompatibleReadersWritersMap(MediaType mediaType,
            Map<MediaType, List<T>> set,
            Map<MediaType, List<T>> subSet) {
        if (mediaType.isWildcardType()) {
            getCompatibleReadersWritersList(mediaType, set, subSet);
        } else if (mediaType.isWildcardSubtype()) {
            getCompatibleReadersWritersList(mediaType, set, subSet);
            getCompatibleReadersWritersList(MediaTypes.GENERAL_MEDIA_TYPE, set, subSet);
        } else {
            getCompatibleReadersWritersList(mediaType, set, subSet);
            getCompatibleReadersWritersList(
                    MediaTypes.getTypeWildCart(mediaType),
                    set, subSet);
            getCompatibleReadersWritersList(MediaTypes.GENERAL_MEDIA_TYPE, set, subSet);
        }

    }

    private <T> void getCompatibleReadersWritersList(MediaType mediaType,
            Map<MediaType, List<T>> set,
            Map<MediaType, List<T>> subSet) {
        List<T> readers = set.get(mediaType);
        if (readers != null) {
            subSet.put(mediaType, Collections.unmodifiableList(readers));
        }
    }

    public <T> List<MediaType> getMessageBodyWriterMediaTypes(Class<T> c, Type t,
            Annotation[] as) {
        List<MediaType> mtl = new ArrayList<MediaType>();
        for (MessageBodyWriterPair mbwp : customWriterListProviders) {
            if (mbwp.mbw.isWriteable(c, t, as, MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                mtl.addAll(mbwp.types);
            }
        }
        for (MessageBodyWriterPair mbwp : writerListProviders) {
            if (mbwp.mbw.isWriteable(c, t, as, MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                mtl.addAll(mbwp.types);
            }
        }
        
        Collections.sort(mtl, MediaTypes.MEDIA_TYPE_COMPARATOR);
        return mtl;
    }

    public <T> MediaType getMessageBodyWriterMediaType(Class<T> c, Type t,
			Annotation[] as, List<MediaType> acceptableMediaTypes) {
        for (MediaType acceptable : acceptableMediaTypes) {
            for (MessageBodyWriterPair mbwp : customWriterListProviders) {
                for (MediaType mt : mbwp.types) {
                    if (mt.isCompatible(acceptable) &&
                            mbwp.mbw.isWriteable(c, t, as, acceptable)) {
                        return MediaTypes.mostSpecific(mt, acceptable);
                    }
                }
            }
            for (MessageBodyWriterPair mbwp : writerListProviders) {
                for (MediaType mt : mbwp.types) {
                    if (mt.isCompatible(acceptable) &&
                            mbwp.mbw.isWriteable(c, t, as, acceptable)) {
                        return MediaTypes.mostSpecific(mt, acceptable);
                    }
                }
            }

        }
        return null;
    }
}