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

import com.sun.net.httpserver.HttpHandler;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.ext.RuntimeDelegate;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ApplicationConfigTest extends AbstractHttpServerTester {
    @Path("/")
    public static class Resource {
        @GET
        public String get() {
            return "CONTENT";
        }
    }
        
    public ApplicationConfigTest(String testName) {
        super(testName);
    }
    
    public void testApplicationConfig() {
        ApplicationConfig ac = new ApplicationConfig() {
            @Override
            public Set<Class<?>> getResourceClasses() {
                Set<Class<?>> s = new HashSet<Class<?>>();
                s.add(Resource.class);
                return s;
            }            
        };
        
        HttpHandler hh = RuntimeDelegate.getInstance().
                createEndpoint(ac, HttpHandler.class);
        start(hh);
        
        WebResource r = Client.create().resource(getUri().build());
        assertEquals("CONTENT", r.get(String.class));
    }
}