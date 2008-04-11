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

import com.sun.ws.rest.api.client.Client;
import com.sun.ws.rest.api.client.ClientResponse;
import javax.ws.rs.Path;
import com.sun.ws.rest.api.client.WebResource;
import com.sun.ws.rest.api.core.DefaultResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.spi.container.ContainerListener;
import com.sun.ws.rest.spi.container.ContainerNotifier;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ReloadTest extends AbstractHttpServerTester {
    @Path("/one")
    public static class One {
        @GET
        public String get() {
            return "one";
        }               
    }
        
    @Path("/two")
    public static class Two {
        @GET
        public String get() {
            return "two";
        }               
    }
    
    private static class Reloader implements ContainerNotifier {
        List<ContainerListener> ls;
        
        public Reloader() {
            ls = new ArrayList<ContainerListener>();
        }
        
        public void addListener(ContainerListener l) {
            ls.add(l);
        }

        public void reload() {
            for (ContainerListener l : ls) {
                l.onReload();
            }
        }        
    }
    
    public ReloadTest(String testName) {
        super(testName);
    }
    
    public void testReload() {
        ResourceConfig rc = new DefaultResourceConfig(One.class);
        Reloader cr = new Reloader();
        rc.getProperties().put(ResourceConfig.PROPERTY_CONTAINER_NOTIFIER, cr);        
        startServer(rc);

        assertEquals(1, cr.ls.size());
        
        WebResource r = Client.create().resource(getUri().path("/").build());
                
        assertEquals("one", r.path("one").get(String.class));
        assertEquals(404, r.path("two").get(ClientResponse.class).getStatus());
        
        rc.getResourceClasses().add(Two.class);
        cr.reload();
        
        assertEquals("one", r.path("one").get(String.class));
        assertEquals("two", r.path("two").get(String.class));        
    }
}
