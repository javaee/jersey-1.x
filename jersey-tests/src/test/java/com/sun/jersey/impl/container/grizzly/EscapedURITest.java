/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.container.grizzly;

import com.sun.jersey.api.client.Client;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.grizzly.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EscapedURITest extends AbstractGrizzlyServerTester {
    @Path("x%20y")
    public static class EscapedURIResource {
        @GET
        public String get(@Context UriInfo info) {
            assertEquals(CONTEXT + "/x%20y", info.getAbsolutePath().getRawPath());
            assertEquals(CONTEXT + "/", info.getBaseUri().getRawPath());
            assertEquals("x y", info.getPath());
            assertEquals("x%20y", info.getPath(false));
            return "CONTENT";
        }
    }
        
    @Path("x y")
    public static class NonEscapedURIResource extends EscapedURIResource {}
    
    public EscapedURITest(String testName) {
        super(testName);
    }
    
    @Path("escapedPathParam")
    public static class EscapedPathParamResource {
        @GET @Path("{id}")
        public String get(@PathParam("id") String id) {
            return id;
        }
    }

    public void testEscaped() {
        startServer(EscapedURIResource.class);
                
        WebResource r = Client.create().resource(getUri().
                userInfo("x.y").path("x%20y").build());
        assertEquals("CONTENT", r.get(String.class));
    } 
    
    public void testNonEscaped() {
        startServer(NonEscapedURIResource.class);
                
        WebResource r = Client.create().resource(getUri().
                userInfo("x.y").path("x%20y").build());
        assertEquals("CONTENT", r.get(String.class));
    } 

    public void testEscapedSlashInPathParam() {
        ResourceConfig rc = new DefaultResourceConfig(EscapedPathParamResource.class);
        rc.getFeatures().put(GrizzlyServerFactory.FEATURE_ALLOW_ENCODED_SLASH, true);

        startServer(rc);

        WebResource r = Client.create().resource(getUri().
                path("escapedPathParam/he%2Flo").build());
        assertEquals("he/lo", r.get(String.class));
    }
}
