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

package com.sun.jersey.multipart.impl;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

/**
 * <p>{@link Provider} {@link MessageBodyWriter} implementation for
 * {@link MultiPart} entities.</p>
 */
@Produces("multipart/*")
public class MultiPartWriter implements MessageBodyWriter<MultiPart> {

    /**
     * <P>Injectable helper to look up appropriate {@link Provider}s
     * for our body parts.</p>
     */
    @Context
    private Providers providers;

    public long getSize(MultiPart entity, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return MultiPart.class.isAssignableFrom(type);
    }

    /**
     * <p>Write the entire list of body parts to the output stream, using the
     * appropriate provider implementation to serialize each body part's entity.</p>
     *
     * @param entity The {@link MultiPart} instance to write
     * @param type The class of the object to be written (i.e. {@link MultiPart}.class)
     * @param genericType The type of object to be written
     * @param annotations Annotations on the resource method that returned this object
     * @param mediaType Media type (<code>multipart/*</code>) of this entity
     * @param headers Mutable map of HTTP headers for the entire response
     * @param stream Output stream to which the entity should be written
     *
     * @throws java.io.IOException if an I/O error occurs
     * @throws javax.ws.rs.WebApplicationException if an HTTP error response
     *  needs to be produced (only effective if the response is not committed yet)
     */
    public void writeTo(MultiPart entity, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> headers,
                        OutputStream stream) throws IOException, WebApplicationException {

        // If our entity is not nested, make sure the MIME-Version header is set
        if (entity.getParent() == null) {
            Object value = headers.getFirst("MIME-Version");
            if (value == null) {
                headers.putSingle("MIME-Version", "1.0");
            }
        }

        // Initialize local variables we need
        Annotation[] emptyAnnotations = new Annotation[0];
        Writer writer = new BufferedWriter(new OutputStreamWriter(stream)); // FIXME - charset???

        // Determine the boundary string to be used, creating one if needed
        MediaType entityMediaType = (MediaType) headers.getFirst("Content-Type");
        if (entityMediaType == null) {
            Map<String,String> parameters = new HashMap<String,String>();
            parameters.put("boundary", createBoundary());
            entityMediaType = new MediaType("multipart", "mixed", parameters);
            headers.putSingle("Content-Type", entityMediaType);
        }
        String boundaryString = entityMediaType.getParameters().get("boundary");
        if (boundaryString == null) {
            boundaryString = createBoundary();
            Map<String,String> parameters = new HashMap<String,String>();
            parameters.putAll(entityMediaType.getParameters());
            parameters.put("boundary", boundaryString);
            entityMediaType = new MediaType(entityMediaType.getType(),
                                            entityMediaType.getSubtype(),
                                            parameters);
            headers.putSingle("Content-Type", entityMediaType);
        }

        // Iterate through the body parts for this message
        for (BodyPart bodyPart : entity.getBodyParts()) {

            // Write the leading boundary string
            writer.write("\r\n--");
            writer.write(boundaryString);
            writer.write("\r\n");

            // Write the headers for this body part
            MediaType bodyMediaType = bodyPart.getMediaType();
            if (bodyMediaType == null) {
                throw new WebApplicationException
                        (new IllegalArgumentException("Missing body part media type"));
            }
            MultivaluedMap<String,String> bodyHeaders = bodyPart.getHeaders();
            bodyHeaders.putSingle("Content-Type", bodyMediaType.toString());

            // Iterate for the nested body parts
            for (Map.Entry<String,List<String>> entry : bodyHeaders.entrySet()) {

                // Only headers that match "Content-*" are allowed on body parts
                if (!entry.getKey().toLowerCase().startsWith("content-")) {
                    throw new WebApplicationException
                            (new IllegalArgumentException("Invalid body part header '" + entry.getKey() + "', only Content-* allowed"));
                }

                // Write this header and its value(s)
                writer.write(entry.getKey());
                writer.write(':');
                boolean first = true;
                for (Object value : entry.getValue()) {
                    if (first) {
                        writer.write(' ');
                        first = false;
                    } else {
                        writer.write(',');
                    }
                    writer.write(value.toString());
                }
                writer.write("\r\n");
            }

            // Mark the end of the headers for this body part
            writer.write("\r\n");
            writer.flush();

            // Write the entity for this body part
            Object bodyEntity = bodyPart.getEntity();
            if (bodyEntity == null) {
                throw new WebApplicationException(
                        new IllegalArgumentException("Missing body part entity of type '" + bodyMediaType + "'"));
            }
            MessageBodyWriter bodyWriter =
              providers.getMessageBodyWriter(bodyEntity.getClass(),
                                             bodyEntity.getClass(),
                                             emptyAnnotations,
                                             bodyMediaType);
            if (bodyWriter == null) {
                throw new WebApplicationException(
                        new IllegalArgumentException("No MessageBodyWriter for body part of type '" + bodyEntity.getClass().getName() + "' and media type '" + bodyMediaType + "'"));
            }
            bodyWriter.writeTo(bodyEntity, bodyEntity.getClass(), bodyEntity.getClass(),
                               emptyAnnotations, bodyMediaType, bodyHeaders, stream);

        }

        // Write the final boundary string
        writer.write("\r\n--");
        writer.write(boundaryString);
        writer.write("--\r\n");
        writer.flush();
    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Counter used to generate unique boundary values.  Access to this
     * value SHOULD be synchronized to ensure unique boundary values in a
     * multithreaded environment.</p>
     */
    private static int boundaryCounter = 0;


    /**
     * <p>Create and return a unique value for the <code>boundary</code>
     * parameter of our <code>Content-Type</code> header field.</p>
     */
    private synchronized static String createBoundary() {
        StringBuilder sb = new StringBuilder();
        return "Boundary_" + (++boundaryCounter) + "_" + sb.hashCode() +
               "_" + System.currentTimeMillis();
    }


}
