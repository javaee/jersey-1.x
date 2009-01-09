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
package com.sun.jersey.api.client;

import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * A terminating client handler that is invoked to produce an HTTP request
 * to send to a resource and process the HTTP response received from the resource.
 * <p>
 * This class can be extended to integrate HTTP protocol functionality with
 * the Client API. Utilitly methods are provided for converting header values
 * and writing a request entity.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class TerminatingClientHandler implements ClientHandler {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    
    @Context private MessageBodyWorkers workers;

    protected MessageBodyWorkers getMessageBodyWorkers() {
        return workers;
    }
    
    /**
     * Convert a header value to a String instance.
     * 
     * @param headerValue the header value.
     * @return the string instance.
     */
    protected String headerValueToString(Object headerValue) {
        HeaderDelegate hp = RuntimeDelegate.getInstance().
                createHeaderDelegate(headerValue.getClass());
        return hp.toString(headerValue);
    }

    /**
     * Write a request entity using an appropriate message body writer.
     * <p>
     * The method {@link WriteRequestEntityListener#onRequestEntitySize(long) } will be invoked
     * with the size of the request entity to be serialized.
     * The method {@link WriteRequestEntityListener#onGetOutputStream() } will be invoked
     * when the output stream is required to write the request entity.
     * 
     * @param ro the client request containing the request entity. If the
     *        request entity is null then the method will not write any entity.
     * @param listener the request entity listener.
     * @throws java.io.IOException
     */
    protected void writeRequestEntity(ClientRequest ro,
            WriteRequestEntityListener listener) throws IOException {
        Object entity = ro.getEntity();
        if (entity == null)
            return;

        Type entityType = null;
        if (entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity)entity;
            entityType = ge.getType();
            entity = ge.getEntity();
        } else {
            entityType = entity.getClass();
        }
        final Class entityClass = entity.getClass();


        MultivaluedMap<String, Object> metadata = ro.getMetadata();
        MediaType mediaType = null;
        final Object mediaTypeHeader = metadata.getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            mediaType = (MediaType)mediaTypeHeader;
        } else {
            if (mediaTypeHeader != null) {
                mediaType = MediaType.valueOf(mediaTypeHeader.toString());
            } else {
                // Content-Type is not present choose a default type
                List<MediaType> mediaTypes = workers.getMessageBodyWriterMediaTypes(
                        entityClass, entityType, EMPTY_ANNOTATIONS);
                mediaType = mediaTypes.get(0);
                if (mediaType.isWildcardType() || mediaType.isWildcardSubtype())
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
                
                metadata.putSingle("Content-Type", mediaType);
            }
        }


        final MessageBodyWriter bw = workers.getMessageBodyWriter(
                entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType);
        if (bw == null) {
            throw new ClientHandlerException(
                    "A message body writer for Java type, " + entity.getClass() +
                    ", and MIME media type, " + mediaType + ", was not found");
        }

        final long size = bw.getSize(
                entity, entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType);
        listener.onRequestEntitySize(size);

        final OutputStream out = ro.getAdapter().adapt(ro, listener.onGetOutputStream());
        bw.writeTo(entity, entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType, metadata, out);
        out.flush();
        out.close();
    }
}