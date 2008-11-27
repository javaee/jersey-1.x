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

package com.sun.jersey.multipart;

import com.sun.jersey.core.header.ParameterizedHeader;
import com.sun.jersey.core.util.ImmutableMultivaluedMap;
import java.io.IOException;
import java.lang.String;
import java.lang.annotation.Annotation;
import java.text.ParseException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

/**
 * <p>A mutable model representing a body part nested inside a MIME MultiPart
 * entity.</p>
 */
public class BodyPart {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Instantiate a new {@link BodyPart} with a <code>mediaType</code> of
     * <code>text/plain</code>.</p>
     */
    public BodyPart() {
        this(new MediaType("text", "plain"));
    }


    /**
     * <p>Instantiate a new {@link BodyPart} with the specified characteristics.</p>
     *
     * @param mediaType The {@link MediaType} for this body part
     */
    public BodyPart(MediaType mediaType) {
        setMediaType(mediaType);
    }


    /**
     * <p>Instantiate a new {@link BodyPart} with the specified characteristics.</p>
     *
     * @param entity The entity for this body part
     * @param mediaType The {@link MediaType} for this body part
     */
    public BodyPart(Object entity, MediaType mediaType) {
        setEntity(entity);
        setMediaType(mediaType);
    }


    // -------------------------------------------------------------- Properties


    private Object entity;


    /**
     * <p>Return the entity object to be unmarshalled from a request, or to be
     * marshalled on a response.</p>
     *
     * @exception IllegalStateException if this method is called on a
     *  {@link MultiPart} instance; access the underlying {@link BodyPart}s instead
     */
    public Object getEntity() {
        return this.entity;
    }


    /**
     * <p>Set the entity object to be unmarshalled from a request, or to be
     * marshalled on a response.</p>
     *
     * @param entity The new entity object
     *
     * @exception IllegalStateException if this method is called on a
     *  {@link MultiPart} instance; access the underlying {@link BodyPart}s instead
     */
    public void setEntity(Object entity) {
        this.entity = entity;
    }


    private MultivaluedMap<String,String> headers = new HeadersMap();


    /**
     * <p>Return a mutable map of HTTP header value(s) for this {@link BodyPart},
     * keyed by the header name.  Key comparisons in the returned map must be
     * case-insensitive.</p>
     *
     * <p>Note that, per the MIME specifications, only headers that match
     * <code>Content-*</code> should be included on a {@link BodyPart}.</p>
     */
    public MultivaluedMap<String, String> getHeaders() {
        return this.headers;
    }


    /**
     * <p>Return an immutable map of parameterized HTTP header value(s) for this
     * {@link BodyPart}, keyed by header name.  Key comparisons in the
     * returned map must be case-insensitive.  If you wish to modify the
     * headers map for this {@link BodyPart}, modify the map returned by
     * <code>getHeaders()</code> instead.</p>
     */
    public MultivaluedMap<String, ParameterizedHeader> getParameterizedHeaders() throws ParseException {
        return new ImmutableMultivaluedMap<String,ParameterizedHeader>(
                new ParameterizedHeadersMap(headers));
    }


    private MediaType mediaType = null;


    /**
     * <p>Return the {@link MediaType} for this {@link BodyPart}.  If never
     * set, the default {@link MediaType} MUST be <code>text/plain</code>.</p>
     */
    public MediaType getMediaType() {
        return this.mediaType;
    }


    /**
     * <p>Set the {@link MediaType} for this {@link BodyPart}.
     *
     * @param mediaType The new {@link MediaType}
     */
    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }


    private MultiPart parent = null;


    /**
     * <p>Return the parent {@link MultiPart} (if any) for this {@link BodyPart}.
     */
    public MultiPart getParent() {
        return this.parent;
    }


    /**
     * <p>Set the parent {@link MultiPart} (if any) for this {@link BodyPart}.</p>
     *
     * @param parent The new parent
     */
    public void setParent(MultiPart parent) {
        this.parent = parent;
    }


    private Providers providers = null;


    /**
     * <p>Return the configured {@link Providers} for this {@link BodyPart}.</p>
     */
    public Providers getProviders() {
        return this.providers;
    }


    /**
     * <p>Set the configured {@link Providers} for this {@link BodyPart}.</p>
     *
     * @param providers The new {@link Providers}
     */
    public void setProviders(Providers providers) {
        this.providers = providers;
    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Perform any necessary cleanup at the end of processing this
     * {@link BodyPart}.</p>
     */
    public void cleanup() {
        if ((getEntity() != null) && (getEntity() instanceof BodyPartEntity)) {
            ((BodyPartEntity) getEntity()).cleanup();
        }
    }


    /**
     * <p>Builder pattern method to return this {@link BodyPart} after
     * additional configuration.</p>
     *
     * @param entity Entity to set for this {@link BodyPart}
     */
    public BodyPart entity(Object entity) {
        setEntity(entity);
        return this;
    }

    /**
     * <p>Return the entity after appropriate conversion to the requested
     * type.  This is useful only when the containing {@link MultiPart}
     * instance has been received, which causes the <code>providers</code> property
     * to have been set.</p>
     *
     * @param clazz Desired class into which the entity should be converted
     *
     * @exception IllegalArgumentException if no {@link MessageBodyReader} can
     *  be found to perform the requested conversion
     * @exception IllegalStateException if this method is called when the
     *  <code>providers</code> property has not been set or when the
     *  entity instance is not the unconverted content of the body part entity
     */
    public <T> T getEntityAs(Class<T> clazz) {
        if ((entity == null) || !(entity instanceof BodyPartEntity)) {
            throw new IllegalStateException("Entity instance does not contain the unconverted content");
        }
        if (getProviders() == null) {
            throw new IllegalStateException("The providers property has not been set, which is done automatically when a MultiPart entity is received");
        }
        Annotation annotations[] = new Annotation[0];
        MessageBodyReader<T> reader =
          getProviders().getMessageBodyReader(clazz, clazz, annotations, mediaType);
        if (reader == null) {
            throw new IllegalArgumentException("No available MessageBodyReader for class " + clazz.getName() + " and media type " + mediaType);
        }
        try {
            return reader.readFrom(clazz, clazz, annotations, mediaType, headers,
                    ((BodyPartEntity) entity).getInputStream());
        } catch (IOException e) {
            return null; // Can not happen
        }
    }

    /**
     * <p>Builder pattern method to return this {@link BodyPart} after
     * additional configuration.</p>
     *
     * @param type Media type to set for this {@link BodyPart}
     */
    public BodyPart type(MediaType type) {
        setMediaType(type);
        return this;
    }

}
