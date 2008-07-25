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
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ClientErrorTest extends AbstractResourceTester {
    
    public ClientErrorTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class WebResourceNotFoundMethodNotAllowed {
        @ProduceMime("application/foo")
        @GET
        public String doGet() {
            return "content";
        }
    }
        
    @Path("/")
    public static class WebResourceUnsupportedMediaType {
        @ConsumeMime("application/bar")
        @ProduceMime("application/foo")
        @POST
        public String doPost(String entity) {
            return "content";
        }
    }
    
    public void testNotFound() {
        initiateWebApplication(WebResourceNotFoundMethodNotAllowed.class);
        WebResource r = resource("/foo", false);

        ClientResponse response = r.accept("application/foo").get(ClientResponse.class);
        assertEquals(404, response.getStatus());
    }
    
    public void testMethodNotAllowed() {
        initiateWebApplication(WebResourceNotFoundMethodNotAllowed.class);
        WebResource r = resource("/", false);
        
        ClientResponse response = r.entity("content", "application/foo").
                accept("application/foo").post(ClientResponse.class);
        assertEquals(405, response.getStatus());
        String allow = response.getMetadata().getFirst("Allow").toString();
        assertTrue(allow.contains("GET"));
    }    
    
    public void testUnsupportedMediaType() {
        initiateWebApplication(WebResourceUnsupportedMediaType.class);
        WebResource r = resource("/", false);
        
        ClientResponse response = r.entity("content", "application/foo").
                accept("application/foo").post(ClientResponse.class);
        assertEquals(415, response.getStatus());
    }
    
    public void testNotAcceptable() {
        initiateWebApplication(WebResourceUnsupportedMediaType.class);
        WebResource r = resource("/", false);
        
        ClientResponse response = r.entity("content", "application/bar").
                accept("application/bar").post(ClientResponse.class);
        assertEquals(406, response.getStatus());
    }    
}
