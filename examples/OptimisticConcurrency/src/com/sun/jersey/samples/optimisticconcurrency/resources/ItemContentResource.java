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

package com.sun.jersey.samples.optimisticconcurrency.resources;

import com.sun.jersey.api.ConflictException;
import com.sun.jersey.samples.optimisticconcurrency.ItemData;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ItemContentResource {
    
    @GET
    public Response get() {
        ItemData id = ItemData.ITEM;
        MediaType mediaType = null;
        byte[] content = null;
        synchronized (id) {
            mediaType = id.getMediaType();
            content = id.getContent();
        }
        
        return Response.ok(content, mediaType).build();
    }
    
    @PUT
    @Path("{version}")
    public void put(
            @PathParam("version") int version,
            @Context HttpHeaders headers,
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
