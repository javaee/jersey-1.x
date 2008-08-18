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

package com.sun.jersey.impl.container.grizzly.web;

import com.sun.jersey.api.client.Client;
import javax.ws.rs.Path;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class QueryParamTest extends AbstractGrizzlyWebContainerTester {
    @Path("/test")
    public static class QueryParamResource {
        @GET
        public String get(@QueryParam("x") String x, @QueryParam("y") String y) {
            return y;
        }
    }
        
    public QueryParamTest(String testName) {
        super(testName);
    }
    
    public void testQueryParam() {
        startServer(QueryParamResource.class);
                
        WebResource r = Client.create().resource(
                getUri().path("test").build());
        
        URI u = UriBuilder.fromPath("").
                queryParam("y", "1+%2B+2").build();
        assertEquals("1 + 2", r.uri(u).get(String.class));
        
        u = UriBuilder.fromPath("").
                queryParam("x", "1").
                queryParam("y", "1+%2B+2").build();        
        assertEquals("1 + 2", r.uri(u).get(String.class));
        
        u = UriBuilder.fromPath("").
                queryParam("x", "1").
                queryParam("y", "1+%26+2").build();
        assertEquals("1 & 2", r.uri(u).get(String.class));
        
        u = UriBuilder.fromPath("").
                queryParam("x", "1").
                queryParam("y", "1+%7C%7C+2").build();
        assertEquals("1 || 2", r.uri(u).get(String.class));
    }
}
