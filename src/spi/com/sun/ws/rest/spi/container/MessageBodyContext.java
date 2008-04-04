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

package com.sun.ws.rest.spi.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * An injectable context class to obtain a message body reader
 * or writer given a Java type and a media type.
 * 
 * This will be replaced by MessageBodyWorkers for the 0.8 API.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface MessageBodyContext {
    
    /**
     * Get a MessageBodyReader. This is a place holder method equivalent
     * to the on the MesageBodyWorkers of the 0.8 API.
     * 
     */
    <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType, 
            Annotation annotations[], MediaType mediaType);    
    
    
    /**
     * Get a MessageBodyWriter. This is a place holder method equivalent
     * to the on the MesageBodyWorkers of the 0.8 API.
     * 
     */
    <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType, 
            Annotation annotations[],MediaType mediaType);
}