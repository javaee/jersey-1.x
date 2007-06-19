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

import com.sun.ws.rest.samples.optimisticconcurrency.Item;
import com.sun.ws.rest.samples.optimisticconcurrency.ItemData;
import java.net.URI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.SubResources;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@UriTemplate("/item")
@SubResources({ItemContentResource.class})
public class ItemResource {
    @HttpContext UriInfo uriInfo;
    
    @HttpMethod
    @ProduceMime("application/xml")
    public Item get() {
        ItemData id = ItemData.ITEM;
        String version = null;
        MediaType mediaType = null;
        synchronized (id) {
            version = id.getVersionAsString();
            mediaType = id.getMediaType();
        }
        
        return new Item(
                getUri(uriInfo.getURI()), 
                getUpdateUri(uriInfo.getURI(), version),
                mediaType.toString());
    }    
    
    private String getUri(URI uri) {
        return uri + "/content";
    }
    
    private String getUpdateUri(URI uri, String version) {
        return uri + "/content/" + version;
    }
}
