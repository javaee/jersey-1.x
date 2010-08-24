/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.impl.container.filter;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.filter.PostReplaceFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.AbstractResourceTester;
import java.util.Arrays;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PostToPutDeleteTest extends AbstractResourceTester {
    
    @Path("/")
    public static class Resource {
        @PUT
        public String put() { return "PUT"; }
        
        @DELETE
        public String delete() { return "DELETE"; }
        
        @POST
        public String post() { return "POST"; }
    }
    
    public PostToPutDeleteTest(String testName) {
        super(testName);
    }
        
    
    public void testWithInstance() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, 
                Arrays.asList(new PostReplaceFilter()));
        initiateWebApplication(rc);
        _test();
    }
    
    public void testWithString() {
        ResourceConfig rc = new DefaultResourceConfig(Resource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, 
                PostReplaceFilter.class.getName());
        initiateWebApplication(rc);
        _test();
    }
    
    public void _test() {
        WebResource r = resource("/", false);
        
        String s = r.header("X-HTTP-Method-Override", "PUT").post(String.class);
        assertEquals("PUT", s);
        
        s = r.header("X-HTTP-Method-Override", "DELETE").post(String.class);
        assertEquals("DELETE", s);
        
        ClientResponse cr = r.header("X-HTTP-Method-Override", "PATCH").
                post(ClientResponse.class);
        assertEquals(405, cr.getStatus());
        
        s = r.post(String.class);
        assertEquals("POST", s);
    }
}