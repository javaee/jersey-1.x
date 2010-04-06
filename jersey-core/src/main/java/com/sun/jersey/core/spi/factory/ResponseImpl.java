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

package com.sun.jersey.core.spi.factory;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.lang.reflect.Type;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * An implementation of {@link Response}.
 * <p>
 * This implementation supports the declaration of an entity type that will be
 * utilized when a {@link MessageBodyWriter} is selected to write out the
 * entity.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ResponseImpl extends Response {
    private final int status;

    private final MultivaluedMap<String, Object> headers;

    private final Object entity;

    private final Type entityType;

    /**
     * Construct given a status, entity and metadata.
     *
     * @param status the status
     * @param headers the metadata, it is the callers responsibility to copy
     *        the metadata if necessary.
     * @param entity the entity
     * @param entityType the entity type, it is the callers responsiblity to
     *        ensure the entity type is compatible with the entity.
     */
    protected ResponseImpl(int status, OutBoundHeaders headers, Object entity, Type entityType) {
        this.status = status;
        this.headers = headers;
        this.entity = entity;
        this.entityType = entityType;
    }

    /**
     * Get the entity type.
     *
     * @return the entity type.
     */
    public Type getEntityType() {
        return entityType;
    }

    // Response 
    
    public int getStatus() {
        return status;
    }

    public MultivaluedMap<String, Object> getMetadata() {
        return headers;
    }

    public Object getEntity() {
        return entity;
    }
}