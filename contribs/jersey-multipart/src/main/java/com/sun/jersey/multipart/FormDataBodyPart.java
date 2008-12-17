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

import java.io.IOException;
import java.io.InputStreamReader;
import javax.ws.rs.core.MediaType;

/**
 * <p>Subclass of {@link BodyPart} with specialized support for media type
 * <code>multipart/form-data</code>.  See
 * <a href="http://www.ietf.org/rfc/rfc2388.txt">RFC 2388</a>
 * for the formal definition of this media type.</p>
 *
 * <p>For a server side application wishing to process an incoming
 * <code>multipart/form-data</code> message, the following features
 * are provided:</p>
 * <ul>
 * <li>Property accessor to retrieve the field name.</li>
 * <li>Property accessor to retrieve the field value for a simple
 *     String field.</li>
 * <li>Convenience accessor to retrieve the field value after conversion
 *     through an appropriate <code>MessageBodyReader</code>.</li>
 * </ul>
 *
 * <p>For a client side application wishing to construct an outgoing
 * <code>multipart/form-data</code> message, the following features
 * are provided:</p>
 * <ul>
 * <li>Convenience constructors for named fields with either
 *     simple string values, or arbitrary entities and media types.</li>
 * <li>Property accessor to set the field name.</li>
 * <li>Property accessor to set the field value for a simple
 *     String field.</li>
 * <li>Convenience accessor to set the media type and value of a
 *     "file" field.</li>
 * </ul>
 */
public class FormDataBodyPart extends BodyPart {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Instantiate an unnamed new {@link FormDataBodyPart} with a
     * <code>mediaType</code> of <code>text/plain</code>.</p>
     */
    public FormDataBodyPart() {
        this(new MediaType("text", "plain"));
    }


    /**
     * <p>Instantiate an unnamed {@link FormDataBodyPart} with the
     * specified characteristics.</p>
     *
     * @param mediaType The {@link MediaType} for this body part
     */
    public FormDataBodyPart(MediaType mediaType) {
        super(mediaType);
    }


    /**
     * <p>Instantiate an unnamed {@link FormDataBodyPart} with the
     * specified characteristics.</p>
     *
     * @param entity The entity for this body part
     * @param mediaType The {@link MediaType} for this body part
     */
    public FormDataBodyPart(Object entity, MediaType mediaType) {
        super(entity, mediaType);
    }


    /**
     * <p>Instantiate a named {@link FormDataBodyPart} with a
     * media type of <code>text/plain</code> and String value.</p>
     *
     * @param name Field name for this body part
     * @param value Field value for this body part
     */
    public FormDataBodyPart(String name, String value) {
        super(value, MediaType.TEXT_PLAIN_TYPE);
        setName(name);
    }


    /**
     * <p>Instantiate a named {@link FormDataBodyPart} with the
     * specified characteristics.</p>
     *
     * @param name Field name for this body part
     * @param entity The entity for this body part
     * @param mediaType The {@link MediaType} for this body part
     */
    public FormDataBodyPart(String name, Object entity, MediaType mediaType) {
        super(entity, mediaType);
        setName(name);
    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The field name, saved to avoid the overhead of parsing a header
     * when <code>getName()</code> is called.</p>
     */
    private String name = null;


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Return the field name for this body part, or <code>null</code>
     * if no name has been established yet.</p>
     */
    public String getName() {
        return this.name;
    }


    /**
     * <p>Return the field value for this body part.  This should be called
     * only on body parts representing simple field values.</p>
     *
     * @exception IllegalStateException if called on a body part with a
     *  media type other than <code>text/plain</code>
     */
    public String getValue() {
        if (!MediaType.TEXT_PLAIN_TYPE.equals(getMediaType())) {
            throw new IllegalStateException("Media type is not text/plain");
        }
        if (getEntity() instanceof BodyPartEntity) {
            StringBuilder sb = new StringBuilder();
            try {
                InputStreamReader reader = new InputStreamReader(((BodyPartEntity) getEntity()).getInputStream());
                int ch = 0;
                while (true) {
                    ch = reader.read();
                    if (ch < 0) {
                        break;
                    }
                    sb.append((char) ch);
                }
                reader.close();
            } catch (IOException e) {
            }
            return sb.toString();
        } else {
            return (String) getEntity();
        }
    }


    /**
     * <p>Return the field value after appropriate conversion to the requested
     * type.  This is useful only when the containing {@link FormDataMultiPart}
     * instance has been received, which causes the <code>providers</code> property
     * to have been set.</p>
     *
     * @param clazz Desired class into which the field value should be converted
     *
     * @exception IllegalArgumentException if no <code>MessageBodyReader</code> can
     *  be found to perform the requested conversion
     * @exception IllegalStateException if this method is called when the
     *  <code>providers</code> property has not been set or when the
     *  entity instance is not the unconverted content of the body part entity
     */
    public <T> T getValueAs(Class<T> clazz) {
        return getEntityAs(clazz);
    }


    /**
     * <p>Return a flag indicating whether this {@link FormDataBodyPart}
     * represents a simple String-valued field.</p>
     */
    public boolean isSimple() {
        return MediaType.TEXT_PLAIN_TYPE.equals(getMediaType());
    }


    /**
     * <p>Set the field name for this body part.</p>
     *
     * @param name New field name for this body part
     */
    public void setName(String name) {
        this.name = name;
        if (getHeaders().get("Content-Disposition") == null) {
            getHeaders().putSingle("Content-Disposition", "form-data; name=\"" + name + "\"");
        }
    }


    /**
     * <p>Set the field value for this body part.  This should be called
     * only on body parts representing simple field values.</p>
     *
     * @param value The new field value
     *
     * @exception IllegalStateException if called on a body part with a
     *  media type other than <code>text/plain</code>
     */
    public void setValue(String value) {
        if (!MediaType.TEXT_PLAIN_TYPE.equals(getMediaType())) {
            throw new IllegalStateException("Media type is not text/plain");
        }
        setEntity(value);
    }


    /**
     * <p>Set the field media type and value for this body part.</p>
     *
     * @param mediaType Media type for this field value
     * @param value Field value as a Java object
     */
    public void setValue(MediaType mediaType, Object value) {
        setMediaType(mediaType);
        setEntity(value);
    }


}
