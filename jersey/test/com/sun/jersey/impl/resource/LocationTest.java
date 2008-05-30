/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.impl.resource;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import java.net.URI;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class LocationTest extends AbstractResourceTester {
    
    public LocationTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    static public class Created {
        @Path("rel-path")
        @POST
        public Response relPath() {
            return Response.created(URI.create("subpath")).entity("CONTENT").build();
        }
        
        @Path("abs-path")
        @POST
        public Response absPath() {
            return Response.created(URI.create("/subpath")).entity("CONTENT").build();
        }
        
        @Path("abs")
        @POST
        public Response abs() {
            return Response.created(URI.create("http://host:8888/subpath")).entity("CONTENT").build();
        }
    }
    
    public void testCreated() {
        initiateWebApplication(Created.class);
        WebResource r = resource("/", false);

        ClientResponse response = r.path("rel-path").post(ClientResponse.class);        
        assertEquals(201, response.getStatus());        
        URI l = UriBuilder.fromUri(BASE_URI).path("rel-path", "subpath").build();
        assertEquals(l, response.getLocation());        
        assertEquals("CONTENT", response.getEntity(String.class));
        
        response = r.path("abs-path").post(ClientResponse.class);        
        assertEquals(201, response.getStatus());        
        l = UriBuilder.fromUri(BASE_URI).path("abs-path", "subpath").build();
        assertEquals(l, response.getLocation());        
        assertEquals("CONTENT", response.getEntity(String.class));        
        
        response = r.path("abs").post(ClientResponse.class);        
        assertEquals(201, response.getStatus());        
        assertEquals(URI.create("http://host:8888/subpath"), response.getLocation());        
        assertEquals("CONTENT", response.getEntity(String.class));        
    }
    
    @Path("/")
    static public class SeeOther {
        @Path("rel-path")
        @POST
        public Response relPath() {
            return Response.seeOther(URI.create("subpath")).build();
        }
        
        @Path("abs-path")
        @POST
        public Response absPath() {
            return Response.seeOther(URI.create("/subpath")).build();
        }
        
        @Path("abs")
        @POST
        public Response abs() {
            return Response.seeOther(URI.create("http://host:8888/subpath")).build();
        }
    }
    
    public void testSeeOther() {
        initiateWebApplication(SeeOther.class);
        WebResource r = resource("/", false);

        ClientResponse response = r.path("rel-path").post(ClientResponse.class);        
        assertEquals(303, response.getStatus());        
        URI l = UriBuilder.fromUri(BASE_URI).path("subpath").build();
        assertEquals(l, response.getLocation());        
        
        response = r.path("abs-path").post(ClientResponse.class);        
        assertEquals(303, response.getStatus());        
        l = UriBuilder.fromUri(BASE_URI).path("subpath").build();
        assertEquals(l, response.getLocation());        
        
        response = r.path("abs").post(ClientResponse.class);        
        assertEquals(303, response.getStatus());        
        assertEquals(URI.create("http://host:8888/subpath"), response.getLocation());        
    }
    
    @Path("/")
    static public class TemporaryRedirect {
        @Path("rel-path")
        @POST
        public Response relPath() {
            return Response.temporaryRedirect(URI.create("subpath")).build();
        }
        
        @Path("abs-path")
        @POST
        public Response absPath() {
            return Response.temporaryRedirect(URI.create("/subpath")).build();
        }
        
        @Path("abs")
        @POST
        public Response abs() {
            return Response.temporaryRedirect(URI.create("http://host:8888/subpath")).build();
        }
    }
    
    public void testTemporaryRedirect() {
        initiateWebApplication(TemporaryRedirect.class);
        WebResource r = resource("/", false);

        ClientResponse response = r.path("rel-path").post(ClientResponse.class);        
        assertEquals(307, response.getStatus());        
        URI l = UriBuilder.fromUri(BASE_URI).path("subpath").build();
        assertEquals(l, response.getLocation());        
        
        response = r.path("abs-path").post(ClientResponse.class);        
        assertEquals(307, response.getStatus());        
        l = UriBuilder.fromUri(BASE_URI).path("subpath").build();
        assertEquals(l, response.getLocation());        
        
        response = r.path("abs").post(ClientResponse.class);        
        assertEquals(307, response.getStatus());        
        assertEquals(URI.create("http://host:8888/subpath"), response.getLocation());        
    }
}