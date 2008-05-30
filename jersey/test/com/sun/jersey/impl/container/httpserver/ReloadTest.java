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

package com.sun.jersey.impl.container.httpserver;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
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
