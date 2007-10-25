/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.client;

import java.net.URI;
import java.text.ParseException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.ProviderFactory;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ResponseInBoundImpl implements ResponseInBound {
    private static final HeaderProvider<EntityTag> entityTagProvider = 
            ProviderFactory.getInstance().createHeaderProvider(EntityTag.class);
    
    public MediaType getContentType() {
        String ct = getMetadata().getFirst("Content-Type");
        return (ct != null) ? new MediaType(ct) : null;
    }
    
    public URI getLocation() {
        String l = getMetadata().getFirst("Location");        
        return (l != null) ? URI.create(l) : null;
    }
    
    public EntityTag getEntityTag() {
        String t = getMetadata().getFirst("ETag");
        
        try {
            return (t != null) ? entityTagProvider.fromString(t) : null;
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public String getLangauge() {
        return getMetadata().getFirst("Content-Language");
    }
}
