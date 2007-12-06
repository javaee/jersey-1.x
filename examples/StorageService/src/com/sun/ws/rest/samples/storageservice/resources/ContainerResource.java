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

import com.sun.ws.rest.api.NotFoundException;
import com.sun.ws.rest.samples.storageservice.Container;
import com.sun.ws.rest.samples.storageservice.Item;
import com.sun.ws.rest.samples.storageservice.MemoryStore;
import java.net.URI;
import java.util.Iterator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@ProduceMime("application/xml")
public class ContainerResource {
    @HttpContext UriInfo uriInfo;
    @HttpContext Request request;
    
    ContainerResource(UriInfo uriInfo, Request request) {
        this.uriInfo = uriInfo;
        this.request = request;
    }
    
    @GET
    public Container getContainer(
            @UriParam("container") String container,
            @QueryParam("search") String search) {
        System.out.println("GET CONTAINER " + container + ", search = " + search);

        Container c = MemoryStore.MS.getContainer(container);
        if (c == null)
            throw new NotFoundException("Container not found");
        
        
        if (search != null) {
            c = c.clone();
            Iterator<Item> i = c.getItem().iterator();
            byte[] searchBytes = search.getBytes();
            while (i.hasNext()) {
                if (!match(searchBytes, container, i.next().getName()))
                    i.remove();
            }
        }
        
        return c;
    }    

    @PUT
    public Response putContainer(@UriParam("container") String container) {
        System.out.println("PUT CONTAINER " + container);
        
        URI uri =  uriInfo.getAbsolutePath();
        Container c = new Container(container, uri.toString());
        
        Response r;
        if (!MemoryStore.MS.hasContainer(c)) {
            r = Response.created(uri).build();
        } else {
            r = Response.noContent().build();
        }
        
        MemoryStore.MS.createContainer(c);
        return r;
    }
    
    @DELETE
    public void deleteContainer(@UriParam("container") String container) {
        System.out.println("DELETE CONTAINER " + container);
        
        Container c = MemoryStore.MS.deleteContainer(container);
        if (c == null)
            throw new NotFoundException("Container not found");
    } 
    
    
    @Path(value="{item}", limited=false)
    public ItemResource getItemResource(@UriParam("container") String container, 
            @UriParam("item") String item) {
        return new ItemResource(uriInfo, request, container, item);
    }
    
    private boolean match(byte[] search, String container, String item) {
        byte[] b = MemoryStore.MS.getItemData(container, item);
        
        OUTER: for (int i = 0; i < b.length - search.length; i++) {
            int j = 0;
            for (; j < search.length; j++) {
                if (b[i + j] != search[j])
                    continue OUTER;
            }
            
            return true;
        }
        
        return false;
    }
}