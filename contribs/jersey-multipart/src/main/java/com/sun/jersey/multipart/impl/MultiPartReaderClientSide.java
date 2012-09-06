/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.multipart.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.jvnet.mimepull.Header;
import org.jvnet.mimepull.MIMEConfig;
import org.jvnet.mimepull.MIMEMessage;
import org.jvnet.mimepull.MIMEParsingException;
import org.jvnet.mimepull.MIMEPart;

import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.MultiPartConfig;
import com.sun.jersey.spi.inject.ClientSide;
import com.sun.jersey.spi.inject.ConstrainedTo;

/**
 * <p>{@link Provider} {@link MessageBodyReader} implementation for
 * {@link MultiPart} entities.</p>
 */
@ConstrainedTo(ClientSide.class)
@Consumes("multipart/*")
public class MultiPartReaderClientSide implements MessageBodyReader<MultiPart> {

    /**
     * <P>Injectable helper to look up appropriate {@link Provider}s
     * for our body parts.</p>
     */
    private final Providers providers;

    /**
     * <p>Injected configuration parameters for this application.</p>
     */
    private final MultiPartConfig config;

    private final MIMEConfig mimeConfig;

    /**
     * <p>Accept constructor injection of the configuration parameters for this
     * application.</p>
     */
    public MultiPartReaderClientSide(@Context Providers providers, @Context MultiPartConfig config) {
        this.providers = providers;

        if (config == null) {
            throw new IllegalArgumentException("The MultiPartConfig instance we expected is not present.  Have you registered the MultiPartConfigProvider class?");
        }
        this.config = config;

        mimeConfig = new MIMEConfig();
        mimeConfig.setMemoryThreshold(config.getBufferThreshold());
    }


    public boolean isReadable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return MultiPart.class.isAssignableFrom(type);
    }

    /**
     * <p>Read the entire list of body parts from the Input stream, using the
     * appropriate provider implementation to deserialize each body part's entity.</p>
     *
     * @param type The class of the object to be read (i.e. {@link MultiPart}.class)
     * @param genericType The type of object to be written
     * @param annotations Annotations on the resource method that returned this object
     * @param mediaType Media type (<code>multipart/*</code>) of this entity
     * @param headers Mutable map of HTTP headers for the entire response
     * @param stream Output stream to which the entity should be written
     *
     * @throws java.io.IOException if an I/O error occurs
     * @throws javax.ws.rs.WebApplicationException if an HTTP error response
     *  needs to be produced (only effective if the response is not committed yet)
     * @throws javax.ws.rs.WebApplicationException if the Content-Disposition
     *  header of a <code>multipart/form-data</code> body part cannot be parsed
     */
    public MultiPart readFrom(Class<MultiPart> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType,
                              MultivaluedMap<String, String> headers,
                              InputStream stream) throws IOException, WebApplicationException {

        try {
            return readMultiPart(type, genericType, annotations, mediaType, headers, stream);
        } catch (MIMEParsingException ex) {
            throw new WebApplicationException(ex, Status.BAD_REQUEST);
        }
    }


    protected MultiPart readMultiPart(Class<MultiPart> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType,
                              MultivaluedMap<String, String> headers,
                              InputStream stream) throws IOException, MIMEParsingException {
        MIMEMessage mm = new MIMEMessage(stream,
                mediaType.getParameters().get("boundary"),
                mimeConfig);

        boolean formData = false;
        MultiPart multiPart = null;
        if (MediaTypes.typeEquals(mediaType, MediaType.MULTIPART_FORM_DATA_TYPE)) {
            multiPart = new FormDataMultiPart();
            formData = true;
        } else {
            multiPart = new MultiPart();
        }

        multiPart.setProviders(providers);

        MultivaluedMap<String,String> mpHeaders = multiPart.getHeaders();
        for (Map.Entry<String,List<String>> entry : headers.entrySet()) {
            List<String> values = entry.getValue();
            for (String value : values) {
                mpHeaders.add(entry.getKey(), value);
            }
        }

        boolean fileNameFix;
        if (!formData) {
            multiPart.setMediaType(mediaType);
            fileNameFix = false;
        } else {
            // see if the User-Agent header corresponds to some version of MS Internet Explorer
            // if so, need to set fileNameFix to true to handle issue http://java.net/jira/browse/JERSEY-759
            String userAgent = headers.getFirst(HttpHeaders.USER_AGENT);
            fileNameFix = userAgent != null && userAgent.contains(" MSIE ");
        }

        for (MIMEPart mp : mm.getAttachments()) {
            BodyPart bodyPart = formData ? new FormDataBodyPart(fileNameFix) : new BodyPart();

            // Configure providers
            bodyPart.setProviders(providers);

            // Copy headers
            for (Header h : mp.getAllHeaders()) {
                bodyPart.getHeaders().add(h.getName(), h.getValue());
            }

            try {
                String contentType = bodyPart.getHeaders().getFirst("Content-Type");
                if (contentType != null)
                    bodyPart.setMediaType(MediaType.valueOf(contentType));

                bodyPart.getContentDisposition();
            } catch (IllegalArgumentException ex) {
                throw new WebApplicationException(ex, Status.BAD_REQUEST);
            }

            // Copy data into a BodyPartEntity structure
            bodyPart.setEntity(new BodyPartEntity(mp));
            // Add this BodyPart to our MultiPart
            multiPart.getBodyParts().add(bodyPart);
        }

        return multiPart;
    }
}
