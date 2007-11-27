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

package com.sun.ws.rest.samples.optimisticconcurrency.resources;

import com.sun.ws.rest.api.ConflictException;
import com.sun.ws.rest.samples.optimisticconcurrency.ItemData;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ItemContentResource {
    
    @HttpMethod
    public Response get() {
        ItemData id = ItemData.ITEM;
        MediaType mediaType = null;
        byte[] content = null;
        synchronized (id) {
            mediaType = id.getMediaType();
            content = id.getContent();
        }
        
        return Response.Builder.representation(content, mediaType).build();
    }
    
    @HttpMethod
    @Path("{version}")
    public void put(
            @UriParam("version") int version,
            @HttpContext HttpHeaders headers,
            byte[] in) {
        ItemData id = ItemData.ITEM;
        synchronized (id) {
            int currentVersion = id.getVersion();
            if (currentVersion > version) {
                throw new ConflictException("Conflict");
            }
            id.update(headers.getMediaType(), in);
        }
        
    }    
}
