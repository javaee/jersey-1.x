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

package com.sun.jersey.core.impl.provider.entity;

import com.sun.jersey.core.provider.EntityHolder;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class EntityHolderReader implements MessageBodyReader<Object> {
    
    private static final Logger LOGGER = Logger.getLogger(EntityHolderReader.class.getName());

    private final MessageBodyWorkers bodyWorker;

    public EntityHolderReader(@Context MessageBodyWorkers bodyWorker) {
        this.bodyWorker = bodyWorker;
    }

    public boolean isReadable(
            Class<?> type,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType) {
        if (type != EntityHolder.class) return false;

        if (!(genericType instanceof ParameterizedType)) return false;

        final ParameterizedType pt = (ParameterizedType)genericType;

        final Type t = pt.getActualTypeArguments()[0];

        if (t instanceof Class || t instanceof ParameterizedType)
            return true;
        else
            return false;
    }
    
    public Object readFrom(
            Class<Object> type,
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {

        if (!entityStream.markSupported()) {
            entityStream = new BufferedInputStream(entityStream);
        }
        entityStream.mark(1);
        if (entityStream.read() == -1) {
            return new EntityHolder();
        }

        entityStream.reset();

        final ParameterizedType pt = (ParameterizedType)genericType;
        final Type t = pt.getActualTypeArguments()[0];
        final Class entityClass = (t instanceof Class) ? (Class)t : (Class)((ParameterizedType)t).getRawType();
        final Type entityGenericType = (t instanceof Class) ? entityClass : t;

        MessageBodyReader br = bodyWorker.getMessageBodyReader(entityClass, entityGenericType, annotations, mediaType);
        if (br == null) {
            LOGGER.severe("A message body reader for the type, " + type + ", could not be found");
            throw new WebApplicationException();
        }
        Object o = br.readFrom(entityClass, entityGenericType, annotations, mediaType, httpHeaders, entityStream);
        return new EntityHolder(o);
    }
}