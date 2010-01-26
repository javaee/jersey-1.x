/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * <p>A mutable model representing a MIME MultiPart entity.  This class extends
 * {@link BodyPart} because MultiPart entities can be nested inside other
 * MultiPart entities to an arbitrary depth.</p>
 */
public class MultiPart extends BodyPart implements Closeable {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Instantiate a new {@link MultiPart} with a <code>mediaType</code> of
     * <code>multipart/mixed</code>.</p>
     */
    public MultiPart() {
        super(new MediaType("multipart", "mixed"));
    }


    /**
     * <p>Instantiate a new {@link MultiPart} with the specified characteristics.</p>
     *
     * @param mediaType The {@link MediaType} for this multipart
     */
    public MultiPart(MediaType mediaType) {
        super(mediaType);
    }


    // -------------------------------------------------------------- Properties


    private BodyPartsList bodyParts = new BodyPartsList(this);


    /**
     * <p>Return a mutable list of {@link BodyPart}s nested in this
     * {@link MultiPart}.</p>
     */
    public List<BodyPart> getBodyParts() {
        return this.bodyParts;
    }


    /**
     * <p>Disable access to the entity for a {@link MultiPart}.  Use the list
     * returned by <code>getBodyParts()</code> to access the relevant
     * {@link BodyPart} instead.</p>
     *
     * @exception IllegalStateException thrown unconditionally
     */
    @Override
    public Object getEntity() {
        throw new IllegalStateException("Cannot get entity on a MultiPart instance");
    }


    /**
     * <p>Disable access to the entity for a {@link MultiPart}.  Use the list
     * returned by <code>getBodyParts()</code> to access the relevant
     * {@link BodyPart} instead.</p>
     * @param entity
     */
    @Override
    public void setEntity(Object entity) {
        throw new IllegalStateException("Cannot set entity on a MultiPart instance");
    }


    /**
     * <p>Set the {@link MediaType} for this {@link MultiPart}.  If never set,
     * the default {@link MediaType} MUST be <code>multipart/mixed</code>.</p>
     *
     * @param mediaType The new {@link MediaType}
     *
     * @exception IllegalArgumentException if the <code>type</code> property
     *  is not set to <code>multipart</code>
     */
    @Override
    public void setMediaType(MediaType mediaType) {
        if (!"multipart".equals(mediaType.getType())) {
            throw new IllegalArgumentException(mediaType.toString());
        }
        super.setMediaType(mediaType);
    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Builder pattern method to add the specified {@link BodyPart} to this
     * {@link MultiPart}.</p>
     *
     * @param bodyPart {@link BodyPart} to be added
     */
    public MultiPart bodyPart(BodyPart bodyPart) {
        getBodyParts().add(bodyPart);
        return this;
    }


    /**
     * <p>Builder pattern method to add a newly configured {@link BodyPart}
     * to this {@link MultiPart}.</p>
     *
     * @param entity Entity object for this body part
     * @param mediaType Content type for this body part
     */
    public MultiPart bodyPart(Object entity, MediaType mediaType) {
        BodyPart bodyPart = new BodyPart(entity, mediaType);
        return bodyPart(bodyPart);
    }


    /**
     * <p>Override the entity set operation on a {@link MultiPart} to throw
     * <code>IllegalArgumentException</code>.</p>
     *
     * @param entity Entity to set for this {@link BodyPart}
     */
    @Override
    public BodyPart entity(Object entity) {
        setEntity(entity);
        return this;
    }


    /**
     * <p>Builder pattern method to return this {@link MultiPart} after
     * additional configuration.</p>
     *
     * @param type Media type to set for this {@link MultiPart}
     */
    @Override
    public MultiPart type(MediaType type) {
        setMediaType(type);
        return this;
    }


    /**
     * <p>Perform any necessary cleanup at the end of processing this
     * {@link MultiPart}.</p>
     */
    @Override
    public void cleanup() {
        for (BodyPart bp : getBodyParts()) {
            bp.cleanup();
        }
    }


    // Closeable

    public void close() throws IOException {
        cleanup();
    }
}