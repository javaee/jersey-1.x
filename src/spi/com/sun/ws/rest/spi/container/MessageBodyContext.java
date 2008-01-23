/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ws.rest.spi.container;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author ps23762
 */
public interface MessageBodyContext {
    /**
     * Get a MessageBodyReader for the specified class and media type.
     * 
     * @param type the type of value class used to represent the message body
     * @param mediaType the media type to be read
     * @return a message body reader
     */
    <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, MediaType mediaType);

    /**
     * Get a MessageBodyWriter for the specified class and media type.
     * 
     * @param type the type of value class used to represent the message body
     * @param mediaType the media type to be written
     * @return a message body writer
     */
    <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, MediaType mediaType);
}
