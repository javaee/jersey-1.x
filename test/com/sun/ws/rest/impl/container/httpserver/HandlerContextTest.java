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

package com.sun.ws.rest.impl.container.httpserver;

import javax.ws.rs.Path;
import com.sun.ws.rest.impl.client.ResourceProxy;
import javax.ws.rs.GET;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HandlerContextTest extends AbstractHttpServerTester {
    @Path("/")
    public static class Resource {
        @GET
        public String get() {
            return "CONTENT";
        }
    }
        
    public HandlerContextTest(String testName) {
        super(testName);
    }
    
    public void setUp() {
        startServer(Resource.class);        
    }
    
    public void testRequestWithTerminatingSlash() {
        ResourceProxy r = ResourceProxy.create(getUri().path("/").build());
        assertEquals("CONTENT", r.get(String.class));
    } 
    
    public void testRequestWithoutTerminatingSlash() {
        ResourceProxy r = ResourceProxy.create(getUri().build());
        assertEquals("CONTENT", r.get(String.class));
    }
}
