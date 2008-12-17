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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;

/**
 * <p>Subclass of {@link MultiPart} with specialized support for media type
 * <code>multipart/form-data</code>.  See
 * <a href="http://www.ietf.org/rfc/rfc2388.txt">RFC 2388</a>
 * for the formal definition of this media type.</p>
 *
 * <p>For a server side application wishing to process an incoming
 * <code>multipart/form-data</code> message, the following features
 * are provided:</p>
 * <ul>
 * <li>Incoming entities will be of type {@link FormDataMultiPart},
 *     enabling access to the specialized methods.</li>
 * <li>Incoming body parts will be of type {@link FormDataBodyPart},
 *     enabling access to its specialized methods.</li>
 * <li>Convenience method to return the {@link FormDataBodyPart} for a
 *     specified field name.</li>
 * <li>Convenience method to return a <code>Map</code> of {@link FormDataBodyPart}s
 *     for all fields, keyed by field name.</li>
 * </ul>
 *
 * <p>For a client side application wishing to construct an outgoing
 * <code>multipart/form-data</code> message, the following features
 * are provided:</p>
 * <ul>
 * <li>Media type of the {@link FormDataMultiPart} instance will automatically
 *     set to <code>multipart/form-data</code>.</li>
 * <li>Builder pattern method to add simple field values as body parts of
 *     type <code>text/plain</code>.</li>
 * <li>Builder pattern method to add named "file" field values with arbitrary
 *     media types.</li>
 * </ul>
 *
 * <p><strong>FIXME</strong> - Consider supporting the use case of a nested
 * <code>multipart/mixed</code> body part to contain multiple uploaded files.</p>
 */
public class FormDataMultiPart extends MultiPart {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Instantiate a new {@link FormDataMultiPart} instance with
     * default characteristics.</p>
     */
    public FormDataMultiPart() {
        super(MediaType.MULTIPART_FORM_DATA_TYPE);
    }


    // ------------------------------------------------------ Instance Variables


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Builder pattern method to add a named field with a text value,
     * and return this instance.</p>
     */
    public FormDataMultiPart field(String name, String value) {
        getBodyParts().add(new FormDataBodyPart(name, value));
        return this;
    }


    /**
     * <p>Builder pattern method to add a named field with an arbitrary
     * media type and entity, and return this instance.</p>
     *
     * @param name Field name of the new field
     * @param entity Entity value for the new field
     * @param mediaType Media type for the new field
     */
    public FormDataMultiPart field(String name, Object entity, MediaType mediaType) {
        getBodyParts().add(new FormDataBodyPart(name, entity, mediaType));
        return this;
    }


    /**
     * <p>Return the {@link FormDataBodyPart} for the specified field name,
     * if any; otherwise, return <code>null</code>.</p>
     */
    public FormDataBodyPart getField(String name) {
        FormDataBodyPart result = null;
        for (BodyPart bodyPart : getBodyParts()) {
            if (!(bodyPart instanceof FormDataBodyPart)) {
                continue;
            }
            if (name.equals(((FormDataBodyPart) bodyPart).getName())) {
                result = (FormDataBodyPart) bodyPart;
            }
        }
        return result;
    }


    /**
     * <p>Return an immutable <code>Map</code> of the {@link FormDataBodyPart}s
     * for all fields, keyed by field name.</p>
     */
    public Map<String,FormDataBodyPart> getFields() {
        Map<String,FormDataBodyPart> map = new HashMap<String,FormDataBodyPart>();
        for (BodyPart bodyPart : getBodyParts()) {
            if (!(bodyPart instanceof FormDataBodyPart)) {
                continue;
            }
            map.put(((FormDataBodyPart) bodyPart).getName(), (FormDataBodyPart) bodyPart);
        }
        return Collections.unmodifiableMap(map);
    }


    /**
     * <p>Disable changing the media type to anything other than
     * <code>multipart/form-data</code>.</p>
     *
     * @param mediaType The proposed media type
     *
     * @exception IllegalArgumentException if the proposed media type is not
     *  <code>multipart/form-data</code>
     */
    @Override
    public void setMediaType(MediaType mediaType) {
        if (!"multipart".equals(mediaType.getType()) || !"form-data".equals(mediaType.getSubtype())) {
            throw new IllegalArgumentException("Cannot change media type of a FormDataMultiPart instance");
        }
        super.setMediaType(mediaType);
    }


    // --------------------------------------------------------- Private Methods


}
