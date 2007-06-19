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

package com.sun.ws.rest.samples.storageservice.resources;

import com.sun.ws.rest.api.Entity;
import com.sun.ws.rest.api.NotFoundException;
import com.sun.ws.rest.samples.storageservice.Container;
import com.sun.ws.rest.samples.storageservice.Item;
import com.sun.ws.rest.samples.storageservice.MemoryStore;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PreconditionEvaluator;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ItemResource {
    UriInfo uriInfo;
    PreconditionEvaluator preconditionEvaluator;
    String container;
    String item;
    
    public ItemResource(UriInfo uriInfo, PreconditionEvaluator preconditionEvaluator,
            String container, String item) {
        this.uriInfo = uriInfo;
        this.preconditionEvaluator = preconditionEvaluator;
        this.container = container;
        this.item = item;
    }
    
    @HttpMethod
    public Response getItem() {
        System.out.println("GET ITEM " + container + " " + item);
        
        Item i = MemoryStore.MS.getItem(container, item);
        if (i == null)
            throw new NotFoundException("Item not found");
        Date lastModified = i.getLastModified().getTime();
        EntityTag et = new EntityTag(i.getDigest());
        Response r = preconditionEvaluator.evaluate(lastModified, et);
        if (r != null)
            return r;
            
        byte[] b = MemoryStore.MS.getItemData(container, item);
        return Response.Builder.representation(b, i.getMimeType()).
                lastModified(lastModified).tag(et).build();
    }    
    
    @HttpMethod
    public Response putItem(Entity<byte[]> data) {
        System.out.println("PUT ITEM " + container + " " + item);
        
        URI uri = getUri(container, item);
        MediaType mimeType = data.getMediaType();
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Item i = new Item(item, uri.toString(), mimeType.toString(), gc);
        String digest = computeDigest(data.getContent());
        i.setDigest(digest);
        
        Response r;
        if (!MemoryStore.MS.hasItem(container, item)) {
            r = Response.Builder.created(uri).build();
        } else {
            r = Response.Builder.ok().build();
        }
        
        Item ii = MemoryStore.MS.createOrUpdateItem(container, i, data.getContent());
        if (ii == null) {
            // Create the container if one has not been created
            URI containerUri = getUri(container);
            Container c = new Container(container, containerUri.toString());
            MemoryStore.MS.createContainer(c);
            i = MemoryStore.MS.createOrUpdateItem(container, i, data.getContent());
            if (i == null)
                throw new NotFoundException("Container not found");
        }
        
        return r;
    }    
    
    @HttpMethod
    public void deleteItem() {
        System.out.println("DELETE ITEM " + container + " " + item);
        
        Item i = MemoryStore.MS.deleteItem(container, item);
        if (i == null) {
            throw new NotFoundException("Item not found");
        }
    }
    
    private URI getUri(String container) {
        return uriInfo.getBaseURI().resolve("containers/" + container);
    }
    
    private URI getUri(String container, String item) {
        return uriInfo.getBaseURI().resolve("containers/" + container + "/" + item);
    }
    
    private String computeDigest(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] digest = md.digest(content);
            BigInteger bi = new BigInteger(digest);
            return bi.toString(16);
        } catch (Exception e) {
            return "";
        }
    }
}
