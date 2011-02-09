/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContainerItemMatrixTest extends AbstractResourceTester {
    
    public ContainerItemMatrixTest(String testName) {
        super(testName);
    }

    @Path("/container/{container_id}")
    public static class ContainerResource {
        String containerId;
        
        public ContainerResource(@PathParam("container_id") String containerId) {
            this.containerId = containerId;
        }
        
        @Path("items/")
        public ItemResource getItemsResource() {
            return new ItemResource(containerId);
        }
        
        @Path("items-no-slash")
        public ItemResource getItemsNoSlashResource() {
            return new ItemNoSlashResource(containerId);
        }
        
        @Path("items-id/{id}")
        public ItemResource getItemsIdResource() {
            return new ItemIdResource(containerId);
        }
        
        @GET
        public String get() {
            return containerId;
        }
    }
    
    @Path("/items/")
    public static class ItemResource {
        String containerId;
        
        public ItemResource() {
            this.containerId = null;
        }
        
        ItemResource(String containerId) {
            this.containerId = containerId;
        }
        
        @GET
        public String get(@MatrixParam("ids") List<String> ids) {
            StringBuilder sb = new StringBuilder();
            if (containerId != null) {
                sb.append(containerId).append(":");
            }
            boolean first = true;
            for (String id : ids) {
                if (!first)
                    sb.append(";");
                first = false;
                sb.append(id);
            }
            
            return sb.toString();
        }
    }
    
    @Path("/items-no-slash")
    public static class ItemNoSlashResource extends ItemResource{
        public ItemNoSlashResource() {
            super();
            this.containerId = null;
        }
        
        ItemNoSlashResource(String containerId) {
            super(containerId);
        }
    }
    
    @Path("/items-id/{id}")
    public static class ItemIdResource extends ItemResource {
        public ItemIdResource() {
            super();
            this.containerId = null;
        }
        
        ItemIdResource(String containerId) {
            super(containerId);
        }
    }
    
    
    public void testGetItem() throws IOException {
        initiateWebApplication(ContainerResource.class, ItemResource.class);
        WebResource r = resource("/items/;ids=1;ids=2;ids=3");
        
        assertEquals("1;2;3", r.get(String.class));
    }   
    
    public void testGetItemId() throws IOException {
        initiateWebApplication(ItemIdResource.class);
        WebResource r = resource("/items-id/item;ids=1;ids=2;ids=3");
        
        assertEquals("1;2;3", r.get(String.class));
    }   
    
    public void testGetItemNoSlash() throws IOException {
        initiateWebApplication(ItemNoSlashResource.class);
        WebResource r = resource("/items-no-slash;ids=1;ids=2;ids=3");
        
        assertEquals("1;2;3", r.get(String.class));
    }   
    
    public void testGetContainer() throws IOException {
        initiateWebApplication(ContainerResource.class, ItemResource.class);
        WebResource r = resource("/container/c");
        
        assertEquals("c", r.get(String.class));
    }   
    
    public void testGetContainerItem() throws IOException {
        initiateWebApplication(ContainerResource.class, ItemResource.class);
        WebResource r = resource("/container/c/items/;ids=1;ids=2;ids=3");
        
        assertEquals("c:1;2;3", r.get(String.class));
    }   
    
    public void testGetContainerItemId() throws IOException {
        initiateWebApplication(ContainerResource.class, ItemResource.class);
        WebResource r = resource("/container/c/items-id/id;ids=1;ids=2;ids=3");
        
        assertEquals("c:1;2;3", r.get(String.class));
        
        r = resource("/container/c/items-id/item;ids=1;ids=2;ids=3");
        
        assertEquals("c:1;2;3", r.get(String.class));
    }   
    
    public void testGetContainerItemNoSlash() throws IOException {
        initiateWebApplication(ContainerResource.class, ItemResource.class);
        WebResource r = resource("/container/c/items-no-slash;ids=1;ids=2;ids=3");
        
        assertEquals("c:1;2;3", r.get(String.class));
    }   
}
