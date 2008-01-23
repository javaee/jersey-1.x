/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.application;

import com.sun.ws.rest.impl.model.MediaTypeHelper;
import com.sun.ws.rest.spi.container.MessageBodyContext;
import com.sun.ws.rest.spi.service.ComponentProvider;
import com.sun.ws.rest.spi.service.ServiceFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class MessageBodyFactory implements MessageBodyContext {
    private static final ComponentProvider DEFAULT_COMPONENT_PROVIDER = new ComponentProvider() {
        public Object provide(Class<?> c) throws InstantiationException, IllegalAccessException {
            return c.newInstance();
        }
    };
    
    private static final Logger LOGGER = Logger.getLogger(MessageBodyFactory.class.getName());
    
    private final ComponentProvider componentProvider;
    
    private final Map<MediaType, List<MessageBodyReader>> readerProviders;
    
    private final Map<MediaType, List<MessageBodyWriter>> writerProviders;
    
    public MessageBodyFactory() {
        this(DEFAULT_COMPONENT_PROVIDER);
    }
    
    public MessageBodyFactory(ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
        this.readerProviders = getProviderMap(MessageBodyReader.class, ConsumeMime.class);    
        this.writerProviders = getProviderMap(MessageBodyWriter.class, ProduceMime.class);
    }
                
    private <T> Map<MediaType, List<T>> getProviderMap(
            Class<T> c,
            Class<?> annotationClass) {
        LOGGER.log(Level.CONFIG, "Searching for providers that implement: " + c);
        Map<MediaType, List<T>> s = new HashMap<MediaType, List<T>>();
        for (T p : ServiceFinder.find(c, true, componentProvider)) {
            LOGGER.log(Level.CONFIG, "    Provider found: " + p.getClass());
            String values[] = getAnnotationValues(p.getClass(), annotationClass);
            if (values==null)
                getClassCapability(s, p, MediaTypeHelper.GENERAL_MEDIA_TYPE);
            else
                for (String type: values)
                    getClassCapability(s, p, MediaType.parse(type));
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
    
    private String[] getAnnotationValues(Class<?> clazz, Class<?> annotationClass) {
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
    
    // MessageBodyContext
    
    @SuppressWarnings("unchecked")
    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, MediaType mediaType) {
        List<MediaType> searchTypes = createSearchList(mediaType);
        for (MediaType t: searchTypes) {
            List<MessageBodyReader> readers = readerProviders.get(t);
            if (readers==null)
                continue;
            for (MessageBodyReader p: readers) {
                if (p.isReadable(type))
                    return p;
            }
        }
        
        throw new IllegalArgumentException("A message body reader for Java type, " + type + 
                ", and MIME media type, " + mediaType + ", was not found");    
    }

    @SuppressWarnings("unchecked")
    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, MediaType mediaType) {
        List<MediaType> searchTypes = createSearchList(mediaType);
        for (MediaType t: searchTypes) {
            List<MessageBodyWriter> writers = writerProviders.get(t);
            if (writers==null)
                continue;
            for (MessageBodyWriter p: writers) {
                if (p.isWriteable(type))
                    return p;
            }
        }
        
        throw new IllegalArgumentException("A message body writer for Java type, " + type + 
                ", and MIME media type, " + mediaType + ", was not found");
    }

    private List<MediaType> createSearchList(MediaType mediaType) {
        if (mediaType==null)
            return Arrays.asList(MediaTypeHelper.GENERAL_MEDIA_TYPE);
        else
            return Arrays.asList(mediaType, 
                    new MediaType(mediaType.getType(), MediaType.MEDIA_TYPE_WILDCARD), 
                    MediaTypeHelper.GENERAL_MEDIA_TYPE);
    }   
}