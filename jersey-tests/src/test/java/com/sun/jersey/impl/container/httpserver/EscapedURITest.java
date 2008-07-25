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
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EscapedURITest extends AbstractHttpServerTester {
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
                
        WebResource r = Client.create().resource(getUri().
                userInfo("x.y").encode(false).path("x%20y").build());
        assertEquals("CONTENT", r.get(String.class));
    } 
}
