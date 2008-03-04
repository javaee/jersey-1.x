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

package com.sun.ws.rest.impl.resource;

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.Path;
import com.sun.ws.rest.api.client.WebResource;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EmptyRootResourcePath extends AbstractResourceTester {
    
    public EmptyRootResourcePath(String testName) {
        super(testName);
    }

    @Path("")
    public static class VirtualResource {
        @Context UriInfo uriInfo;
        
        public @Path("one") Object getOne() {
            return new SubResource(uriInfo.getPath());
        }
        
        public @Path("two") Object getTwo() {
            return new SubResource(uriInfo.getPath());            
        }
    }
    
    public static class SubResource {
        private String path;
        
        SubResource(String path) {
            this.path = path;
        }
        
        public @GET String get() {
            return path;
        }
    }
    
    @Path("absolute")
    public static class AbsoluteResource {
        @Context UriInfo uriInfo;
        
        public @GET String get() {
            return uriInfo.getPath();
        }
    }
    
    public void testGet() throws IOException {
        initiateWebApplication(VirtualResource.class, AbsoluteResource.class);

        WebResource r = resource("/one");
        assertEquals("one", r.get(String.class));
        
        r = resource("/two");
        assertEquals("two", r.get(String.class));
        
        r = resource("/absolute");
        assertEquals("absolute", r.get(String.class));
    }
}