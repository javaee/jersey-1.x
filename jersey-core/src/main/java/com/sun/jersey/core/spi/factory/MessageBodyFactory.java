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

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.reflection.ReflectionHelper.DeclaringClassInterfacePair;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.core.util.KeyComparator;
import com.sun.jersey.core.util.KeyComparatorHashMap;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

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
    
    private Map<MediaType, List<MessageBodyReader>> readerProviders;
    
    private Map<MediaType, List<MessageBodyWriter>> writerProviders;
    
    private List<MessageBodyWriterPair> writerListProviders;
    
    private static class MessageBodyWriterPair {
        final MessageBodyWriter mbw;
        
        final List<MediaType> types;
    
        MessageBodyWriterPair(MessageBodyWriter mbw, List<MediaType> types) {
            this.mbw = mbw;
            this.types = types;
        }
    }
    
    public MessageBodyFactory(ProviderServices providerServices) {
        this.providerServices = providerServices;
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
        this.readerProviders = new KeyComparatorHashMap<MediaType, List<MessageBodyReader>>(
                MEDIA_TYPE_COMPARATOR);

        for (MessageBodyReader provider : providerServices.getProvidersAndServices(MessageBodyReader.class)) {
            List<MediaType> values = MediaTypes.createMediaTypes(
                    provider.getClass().getAnnotation(Consumes.class));
            for (MediaType type : values)
                getClassCapability(readerProviders, provider, type);
        }

//        DistanceComparator<MessageBodyReader> dc = new DistanceComparator<MessageBodyReader>(MessageBodyReader.class);
//        System.out.println("INITIATE READERS");
//        for (Map.Entry<MediaType, List<MessageBodyReader>> e : readerProviders.entrySet()) {
//            System.out.println("MEDIA: " + e.getKey());
//            for (MessageBodyReader r : e.getValue()) {
//                DeclaringClassInterfacePair p = ReflectionHelper.getClass(
//                        r.getClass(), MessageBodyReader.class);
//
//                Class[] as = ReflectionHelper.getParameterizedClassArguments(p);
//                System.out.println("  MBR: " + r.getClass() + "  " + as[0]);
//            }
//
//            Collections.sort(e.getValue(), dc);
//            System.out.println("SOIRTED MEDIA: " + e.getKey());
//            for (MessageBodyReader r : e.getValue()) {
//                DeclaringClassInterfacePair p = ReflectionHelper.getClass(
//                        r.getClass(), MessageBodyReader.class);
//
//                Class[] as = ReflectionHelper.getParameterizedClassArguments(p);
//                System.out.println("  MBR: " + r.getClass() + "  " + as[0]);
//            }
//
//        }
//        System.out.println(" ");
//        System.out.println(" ");

        DistanceComparator<MessageBodyReader> dc = new DistanceComparator<MessageBodyReader>(MessageBodyReader.class);
        for (Map.Entry<MediaType, List<MessageBodyReader>> e : readerProviders.entrySet()) {
            Collections.sort(e.getValue(), dc);
        }

    }
    
    private void initWriters() {
        this.writerProviders = new KeyComparatorHashMap<MediaType, List<MessageBodyWriter>>(
                MEDIA_TYPE_COMPARATOR);
        this.writerListProviders = new ArrayList<MessageBodyWriterPair>();
        
        for (MessageBodyWriter provider : providerServices.getProvidersAndServices(MessageBodyWriter.class)) {
            List<MediaType> values = MediaTypes.createMediaTypes(
                    provider.getClass().getAnnotation(Produces.class));
            for (MediaType type : values)
                getClassCapability(writerProviders, provider, type);

            writerListProviders.add(new MessageBodyWriterPair(provider, values));
        }


//        DistanceComparator<MessageBodyWriter> dc = new DistanceComparator<MessageBodyWriter>(MessageBodyWriter.class);
//        System.out.println("INITIATE WRITERS");
//        for (Map.Entry<MediaType, List<MessageBodyWriter>> e : writerProviders.entrySet()) {
//            System.out.println("MEDIA: " + e.getKey());
//            for (MessageBodyWriter w : e.getValue()) {
//                DeclaringClassInterfacePair p = ReflectionHelper.getClass(
//                        w.getClass(), MessageBodyWriter.class);
//
//                Class[] as = ReflectionHelper.getParameterizedClassArguments(p);
//                System.out.println("  MBW: " + w.getClass() + "  " + as[0]);
//            }
//
//            Collections.sort(e.getValue(), dc);
//            System.out.println("SOIRTED MEDIA: " + e.getKey());
//            for (MessageBodyWriter w : e.getValue()) {
//                DeclaringClassInterfacePair p = ReflectionHelper.getClass(
//                        w.getClass(), MessageBodyWriter.class);
//
//                Class[] as = ReflectionHelper.getParameterizedClassArguments(p);
//                System.out.println("  MBW: " + w.getClass() + "  " + as[0]);
//            }
//
//        }
//        System.out.println(" ");
//        System.out.println(" ");

        final DistanceComparator<MessageBodyWriter> dc = new DistanceComparator<MessageBodyWriter>(MessageBodyWriter.class);
        for (Map.Entry<MediaType, List<MessageBodyWriter>> e : writerProviders.entrySet()) {
            Collections.sort(e.getValue(), dc);
        }
        
        Collections.sort(writerListProviders, new Comparator<MessageBodyWriterPair>() {
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
    
    @SuppressWarnings("unchecked")
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t, 
            Annotation[] as, 
            MediaType mediaType) {        
        MessageBodyReader p = null;
        if (mediaType != null) {
            p = _getMessageBodyReader(c, t, as, mediaType, mediaType);
            if (p == null)
                p = _getMessageBodyReader(c, t, as, mediaType,
                        new MediaType(mediaType.getType(), MediaType.MEDIA_TYPE_WILDCARD));
        }
        if (p == null)
            p = _getMessageBodyReader(c, t, as, mediaType, MediaTypes.GENERAL_MEDIA_TYPE);
        
        return p;
    }

    
    @SuppressWarnings("unchecked")
    private <T> MessageBodyReader<T> _getMessageBodyReader(Class<T> c, Type t, 
            Annotation[] as, 
            MediaType mediaType, MediaType lookup) {
        List<MessageBodyReader> readers = readerProviders.get(lookup);
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
            p = _getMessageBodyWriter(c, t, as, mediaType, mediaType);
            if (p == null)
                p = _getMessageBodyWriter(c, t, as, mediaType,
                        new MediaType(mediaType.getType(), MediaType.MEDIA_TYPE_WILDCARD));
        }
        if (p == null)
            p = _getMessageBodyWriter(c, t, as, mediaType, MediaTypes.GENERAL_MEDIA_TYPE);
        
        return p;
    }
    
    @SuppressWarnings("unchecked")
    private <T> MessageBodyWriter<T> _getMessageBodyWriter(Class<T> c, Type t,
            Annotation[] as,
            MediaType mediaType, MediaType lookup) {        
        List<MessageBodyWriter> writers = writerProviders.get(lookup);
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