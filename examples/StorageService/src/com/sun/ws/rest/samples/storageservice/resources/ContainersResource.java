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

import com.sun.ws.rest.samples.storageservice.Containers;
import com.sun.ws.rest.samples.storageservice.MemoryStore;
import javax.ws.rs.GET;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.UriParam;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Path("/containers")
@ProduceMime("application/xml")
public class ContainersResource {
    @HttpContext UriInfo uriInfo;
    @HttpContext Request request;
    
    @Path("{container}")
    public ContainerResource getContainerResource(@UriParam("container") String container) {
        return new ContainerResource(uriInfo, request, container);
    }
    
    @GET
    public Containers getContainers() {
        System.out.println("GET CONTAINERS");
        
        return MemoryStore.MS.getContainers();
    }    
}
