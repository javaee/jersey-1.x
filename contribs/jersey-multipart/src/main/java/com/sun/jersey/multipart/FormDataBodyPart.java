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

package com.sun.jersey.multipart;

import java.text.ParseException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.header.MediaTypes;

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
 * <li>Property accessor to retrieve the control name.</li>
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
 * <li>Property accessor to set the control name.</li>
 * <li>Property accessor to set the field value for a simple
 *     String field.</li>
 * <li>Convenience accessor to set the media type and value of a
 *     "file" field.</li>
 * </ul>
 *
 * @author Craig.McClanahan@Sun.COM
 * @author Paul.Sandoz@Sun.Com
 * @author imran@smartitengineering.com
 */
public class FormDataBodyPart extends BodyPart {

    private final boolean fileNameFix;

    /**
     * Instantiate an unnamed new {@link FormDataBodyPart} with a
     * <code>mediaType</code> of <code>text/plain</code>.
     */
    public FormDataBodyPart() {
        this(false);
    }

    /**
     * Instantiate an unnamed new {@link FormDataBodyPart} with <code>mediaType</code> of <code>text/plain</code>
     * and setting the flag for applying the fix for erroneous file name value if content disposition header of
     * messages coming from MS Internet Explorer (see <a href="http://java.net/jira/browse/JERSEY-759">JERSEY-759</a>).
     * @param fileNameFix If set to <code>true</code>, header parser will not treat backslash as an escape character
     *                    when retrieving the value of <code>filename</code> parameter of
     *                    <code>Content-Disposition</code> header.
     */
    public FormDataBodyPart(boolean fileNameFix) {
        super();
        this.fileNameFix = fileNameFix;
    }

    /**
     * Instantiate an unnamed {@link FormDataBodyPart} with the
     * specified characteristics.
     *
     * @param mediaType The {@link MediaType} for this body part
     */
    public FormDataBodyPart(MediaType mediaType) {
        super(mediaType);
        this.fileNameFix = false;
    }


    /**
     * Instantiate an unnamed {@link FormDataBodyPart} with the
     * specified characteristics.
     *
     * @param entity The entity for this body part
     * @param mediaType The {@link MediaType} for this body part
     */
    public FormDataBodyPart(Object entity, MediaType mediaType) {
        super(entity, mediaType);
        this.fileNameFix = false;
    }


    /**
     * Instantiate a named {@link FormDataBodyPart} with a
     * media type of <code>text/plain</code> and String value.
     *
     * @param name the control name for this body part
     * @param value the value for this body part
     */
    public FormDataBodyPart(String name, String value) {
        super(value, MediaType.TEXT_PLAIN_TYPE);
        this.fileNameFix = false;
        setName(name);
    }


    /**
     * Instantiate a named {@link FormDataBodyPart} with the
     * specified characteristics.
     *
     * @param name the control name for this body part
     * @param entity the entity for this body part
     * @param mediaType the {@link MediaType} for this body part
     */
    public FormDataBodyPart(String name, Object entity, MediaType mediaType) {
        super(entity, mediaType);
        this.fileNameFix = false;
        setName(name);
    }


    /**
     * Instantiate a named {@link FormDataBodyPart} with the
     * specified characteristics.
     *
     * @param fdcd the content disposition header for this body part.
     * @param value the value for this body part
     */
    public FormDataBodyPart(FormDataContentDisposition fdcd, String value) {
        super(value, MediaType.TEXT_PLAIN_TYPE);
        this.fileNameFix = false;
        setFormDataContentDisposition(fdcd);
    }


    /**
     * Instantiate a named {@link FormDataBodyPart} with the
     * specified characteristics.
     *
     * @param fdcd the content disposition header for this body part.
     * @param entity The entity for this body part
     * @param mediaType The {@link MediaType} for this body part
     */
    public FormDataBodyPart(FormDataContentDisposition fdcd, Object entity, MediaType mediaType) {
        super(entity, mediaType);
        this.fileNameFix = false;
        setFormDataContentDisposition(fdcd);
    }

    /**
     * Get the form data content disposition.
     *
     * @return the form data content disposition.
     */
    public FormDataContentDisposition getFormDataContentDisposition() {
        return (FormDataContentDisposition)getContentDisposition();
    }

    /**
     * Set the form data content disposition.
     *
     * @param cd the form data content disposition.
     */
    public void setFormDataContentDisposition(FormDataContentDisposition cd) {
        super.setContentDisposition(cd);
    }

    /**
     * Override the behaviour on {@link BodyPart} to ensure that
     * only instances of {@link FormDataContentDisposition} can be obtained.
     *
     * @return the content disposition.
     * @throws IllegalArgumentException if the content disposition header
     *         cannot be parsed.
     */
    @Override
    public ContentDisposition getContentDisposition() {
        if (cd == null) {
            String scd = getHeaders().getFirst("Content-Disposition");
            if (scd != null) {
                try {
                    cd = new FormDataContentDisposition(scd, fileNameFix);
                } catch (ParseException ex) {
                    throw new IllegalArgumentException("Error parsing content disposition: " + scd, ex);
                }
            }
        }
        return cd;
    }

    /**
     * Override the behaviour on {@link BodyPart} to ensure that
     * only instances of {@link FormDataContentDisposition} can be set.
     *
     * @param cd the content disposition which must be an instance
     *        of {@link FormDataContentDisposition}.
     * @throws IllegalArgumentException if the content disposition is not an
     *         instance of {@link FormDataContentDisposition}.
     */
    @Override
    public void setContentDisposition(ContentDisposition cd) {
        if (cd instanceof FormDataContentDisposition) {
            super.setContentDisposition(cd);
        } else {
            throw new IllegalArgumentException();
        }
    }


    /**
     * Get the control name.
     *
     * @return the control name.
     */
    public String getName() {
        FormDataContentDisposition fdcd = getFormDataContentDisposition();
        if (fdcd == null)
            return null;

        return fdcd.getName();
    }

    /**
     * Set the control name.
     *
     * @param name the control name.
     */
    public void setName(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Name can not be null.");
        }
        if (getFormDataContentDisposition() == null) {
            FormDataContentDisposition contentDisposition;
            contentDisposition = FormDataContentDisposition.name(name)
                .build();
            super.setContentDisposition(contentDisposition);
        } else {
            FormDataContentDisposition _cd = FormDataContentDisposition.name(name).
                    fileName(cd.getFileName()).
                    creationDate(cd.getCreationDate()).
                    modificationDate(cd.getModificationDate()).
                    readDate(cd.getReadDate()).
                    size(cd.getSize()).
                    build();
            super.setContentDisposition(_cd);
        }
    }


    /**
     * Get the field value for this body part.  This should be called
     * only on body parts representing simple field values.
     *
     * @return the simple field value.
     * @throws IllegalStateException if called on a body part with a
     *         media type other than <code>text/plain</code>
     */
    public String getValue() {
        if (!MediaTypes.typeEquals(MediaType.TEXT_PLAIN_TYPE, getMediaType())) {
            throw new IllegalStateException("Media type is not text/plain");
        }
        if (getEntity() instanceof BodyPartEntity) {
            return getEntityAs(String.class);
        } else {
            return (String) getEntity();
        }
    }


    /**
     * Get the field value after appropriate conversion to the requested
     * type.  This is useful only when the containing {@link FormDataMultiPart}
     * instance has been received, which causes the <code>providers</code>
     * property to have been set.
     *
     * @param <T> the type of the field value.
     * @param clazz Desired class into which the field value should be converted
     * @return the field value
     * @throws IllegalArgumentException if no <code>MessageBodyReader</code> can
     *         be found to perform the requested conversion
     * @throws IllegalStateException if this method is called when the
     *         <code>providers</code> property has not been set or when the
     *         entity instance is not the unconverted content of the body part
     *         entity
     */
    public <T> T getValueAs(Class<T> clazz) {
        return getEntityAs(clazz);
    }

    /**
     * Set the field value for this body part.  This should be called
     * only on body parts representing simple field values.
     *
     * @param value the field value
     * @throws IllegalStateException if called on a body part with a
     *         media type other than <code>text/plain</code>
     */
    public void setValue(String value) {
        if (!MediaType.TEXT_PLAIN_TYPE.equals(getMediaType())) {
            throw new IllegalStateException("Media type is not text/plain");
        }
        setEntity(value);
    }

    /**
     * Set the field media type and value for this body part.
     *
     * @param mediaType the media type for this field value
     * @param value the field value as a Java object
     */
    public void setValue(MediaType mediaType, Object value) {
        setMediaType(mediaType);
        setEntity(value);
    }

    /**
     *
     * @return true if this body part represents a simple, string-based,
     *         field value, otherwise false.
     */
    public boolean isSimple() {
        return MediaType.TEXT_PLAIN_TYPE.equals(getMediaType());
    }
}
