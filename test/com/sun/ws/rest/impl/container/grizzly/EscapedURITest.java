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

package com.sun.ws.rest.impl.container.grizzly;

import com.sun.ws.rest.api.client.Client;
import javax.ws.rs.Path;
import com.sun.ws.rest.api.client.ResourceProxy;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EscapedURITest extends AbstractGrizzlyServerTester {
    @Path(value="x%20y", encode=false)
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
        
    public EscapedURITest(String testName) {
        super(testName);
    }
    
    public void testExpliciWebResourceReference() {
        startServer(EscapedURIResource.class);
                
        ResourceProxy r = Client.create().proxy(getUri().
                userInfo("x.y").encode(false).path("x%20y").build());
        assertEquals("CONTENT", r.get(String.class));
    } 
}
